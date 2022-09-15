package pt.tecnico.bicloin.hub.domain;

import java.util.*;
import java.util.stream.Collectors;

import pt.tecnico.bicloin.hub.domain.exception.*;
import pt.tecnico.rec.grpc.ReadRequest;
import pt.tecnico.rec.grpc.WriteRequest;

import static pt.tecnico.bicloin.hub.HubMain.*;

/**
 * Facade class.
 * Contains the service operations.
 */
public class Hub {

    public Hub() {}


    /* ---------- Auxiliary Functions ---------- */

    /**
     * Finds an User by giving its user_name.
     */
    private User getUser(String user_name) throws NoUserFoundException {
        return users.stream().filter(a -> a.getUsername().equals(user_name))
                .findFirst()
                .orElseThrow(() -> new NoUserFoundException(user_name));
    }

    /**
     * Finds a Station by giving its id.
     */
    private Station getStation(String id) throws NoStationFoundException {
        return stations.stream().filter(a -> a.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoStationFoundException(id));
    }

    /**
     * Converts number to radians.
     */
    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

    /**
     * Applies haversine formula to calculate distance between two given coordinates.
     * @return Distance in meters
     */
    public synchronized double haversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth

        double latDist = toRad(lat2 - lat1);
        double lonDist = toRad(lon2 - lon1);
        double a = Math.sin(latDist / 2) * Math.sin(latDist / 2) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDist / 2) * Math.sin(lonDist / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000;
    }

    public int readFromRecord(String key) {
        return record.read(ReadRequest.newBuilder().setRecordName(key).build()).getRecordValue();
    }

    public void writeToRecord(String key, int value) {
        record.write(WriteRequest.newBuilder().setRecordName(key).setRecordValue(value).setCid(cid).build());
    }

    /* ---------- Service Functions ---------- */

    /**
     * Gives the user current Bicloins.
     *
     * @param user_name user_name of the User whose balance is being returned
     * @return User bicloins
     *
     * @throws NoUserFoundException when the passed user_name does not exist
     */
    public synchronized int balance(String user_name) throws NoUserFoundException {
        User u = getUser(user_name);
        return readFromRecord("u/" + u.getUsername() + "/balance");
    }

    /**
     * Charges user balance and gives its current Bicloins.
     *
     * @param user_name user_name of the User whose balance is being charged
     * @param amount amount of money in euros
     * @param phone_number User's phone number
     * @return User bicloins
     *
     * @throws NoUserFoundException when the passed user_name does not exist
     * @throws WrongAmountException when the passed amount is invalid.
     * @throws PhoneDoesNotMatchUserException when the passed phone_number does not belong to user_name.
     */
    public synchronized int top_up(String user_name, int amount, String phone_number)
            throws NoUserFoundException, WrongAmountException, PhoneDoesNotMatchUserException {
        User u = getUser(user_name);

        int current_balance = readFromRecord("u/" + user_name + "/balance");

        if (phone_number.equals(u.getPhoneNumber())) {
            if (1 <= amount && amount <= 20) {
                writeToRecord("u/" + user_name + "/balance", current_balance + amount * 10);
                return current_balance + amount * 10;
            } else {
                throw new WrongAmountException();
            }
        } else {
            throw new PhoneDoesNotMatchUserException(phone_number);
        }
    }

    /**
     * Gives the station information and its stats.
     *
     * @param id id of the Station whose info is being returned
     * @return List with the information
     *
     * @throws NoStationFoundException when the passed station id does not exist
     */
    public synchronized String[] info_station(String id) throws NoStationFoundException {
        Station s = getStation(id);

        int bikes = readFromRecord("s/" + id + "/bikes");
        int lifts = readFromRecord("s/" + id + "/lifts");
        int deliveries = readFromRecord("s/" + id + "/deliveries");

        return new String[]
                {
                    s.getName(), String.valueOf(s.getLatitude()), String.valueOf(s.getLongitude()),
                        String.valueOf(s.getTotalDocks()), String.valueOf(s.getTotalPrizes()),
                        String.valueOf(bikes),String.valueOf(lifts), String.valueOf(deliveries)
                };
    }

    /**
     * List User's nearest stations.
     *
     * @param user_lat User's current latitude
     * @param user_lon User's current longitude
     * @param limiter limits number of the stations to be returned
     * @return List with the Station's id
     */
    public synchronized List<String> locate_station(double user_lat, double user_lon, int limiter) {
        TreeMap<Double, String> distMap = new TreeMap<>();
        stations.forEach(s -> distMap.put(haversineDistance(user_lat, user_lon, s.getLatitude(), s.getLongitude()), s.getId()));
        List<String> stationByDist = new ArrayList<>(distMap.values());
        return stationByDist.stream().limit(limiter).collect(Collectors.toList());
    }

    /**
     * Lifts a bike of a specific Station.
     *
     * @param user_name User's user_name
     * @param user_lat User's current latitude
     * @param user_lon User's current longitude
     * @param id Station's id
     *
     * @throws NoUserFoundException when the passed user_name does not exist
     * @throws NoStationFoundException when the passed station id does not exist
     * @throws InsufficientBalanceException when User does not have sufficient money
     * @throws NoBikesAvailableException when there are no bikes in station
     * @throws TooFarFromStationException when the User is more than 200m far from the station
     * @throws AlreadyLiftedBikeException when the User already lifted a bike
     */
    public synchronized void bike_up(String user_name, double user_lat, double user_lon, String id)
            throws InsufficientBalanceException, NoUserFoundException, NoStationFoundException,
                    NoBikesAvailableException, TooFarFromStationException, AlreadyLiftedBikeException {
        Station s = getStation(id);
        User u = getUser(user_name);

        int current_balance = readFromRecord("u/" + u.getUsername() + "/balance");
        int hasBike = readFromRecord("u/" + u.getUsername() + "/hasBike");
        int bikes = readFromRecord("s/" + id + "/bikes");
        int lifts = readFromRecord("s/" + id + "/lifts");

        if (haversineDistance(user_lat, user_lon, s.getLatitude(), s.getLongitude()) < 200) {
            if (bikes > 0) {
                if (current_balance >= 10) {
                    if(hasBike == 0) {
                        writeToRecord("u/" + u.getUsername() + "/balance", current_balance - 10);
                        writeToRecord("u/" + u.getUsername() + "/hasBike", 1);
                        writeToRecord("s/" + id + "/bikes", bikes - 1);
                        writeToRecord("s/" + id + "/lifts", lifts + 1);
                    } else {
                        throw new AlreadyLiftedBikeException();
                    }
                } else {
                    throw new InsufficientBalanceException();
                }
            } else {
                throw new NoBikesAvailableException();
            }
        } else {
            throw new TooFarFromStationException();
        }
    }

    /**
     * Delivers a bike to a specific Station.
     *
     * @param user_name User's user_name
     * @param user_lat User's current latitude
     * @param user_lon User's current longitude
     * @param id Station's id
     *
     * @throws NoUserFoundException when the passed user_name does not exist
     * @throws NoStationFoundException when the passed station id does not exist
     * @throws NoDocksAvailableException when there are no docks in station
     * @throws TooFarFromStationException when the User is more than 200m far from the station
     * @throws NoBikeLiftedException when the User did not lift a bike yet
     */
    public synchronized void bike_down(String user_name, double user_lat, double user_lon, String id)
            throws NoStationFoundException, NoUserFoundException, TooFarFromStationException,
                    NoDocksAvailableException, NoBikeLiftedException {
        Station s = getStation(id);
        User u = getUser(user_name);

        int current_balance = readFromRecord("u/" + u.getUsername() + "/balance");
        int hasBike = readFromRecord("u/" + u.getUsername() + "/hasBike");
        int bikes = readFromRecord("s/" + id + "/bikes");
        int deliveries = readFromRecord("s/" + id + "/deliveries");

        if (haversineDistance(user_lat, user_lon, s.getLatitude(), s.getLongitude()) < 200) {
            if (s.getTotalDocks() != bikes) {
                if(hasBike != 0) {
                    writeToRecord("u/" + u.getUsername() + "/balance", current_balance + s.getTotalPrizes());
                    writeToRecord("u/" + u.getUsername() + "/hasBike", 0);
                    writeToRecord("s/" + id + "/bikes", bikes + 1);
                    writeToRecord("s/" + id + "/deliveries", deliveries + 1);
                } else {
                    throw new NoBikeLiftedException();
                }
            } else {
                throw new NoDocksAvailableException();
            }
        } else {
            throw new TooFarFromStationException();
        }
    }
}
