package pt.tecnico.bicloin.app;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.Hub.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

public class App {

    private final HubFrontend frontend;

    public App(HubFrontend frontend) {
        this.frontend = frontend;
    }

    void balance(String user_name) {
        try {
            BalanceRequest r = BalanceRequest.newBuilder().setUserName(user_name).build();
            System.out.println(user_name + " " + frontend.balance(r).getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            printError(e);
        }
    }

    void topup(String user_name, String money, String phone) {
        try {
            TopUpRequest t = TopUpRequest.newBuilder().setUserName(user_name)
                                                      .setAmount(Integer.parseInt(money))
                                                      .setPhoneNumber(phone).build();
            System.out.println(user_name + " " + frontend.topUp(t).getBalance() + " BIC");
        } catch (StatusRuntimeException e) {
            printError(e);
        } catch (NumberFormatException nfe) {
            System.err.println("ERROR: Usage: top-up %int%");
        }
    }

    void info(String station_id) {
        try {
            List<String> is = info_aux(station_id);
            System.out.println(is.get(0) + ", lat " + is.get(1) + ", " + is.get(2) + " long, "
                + is.get(3) + " docks, " + is.get(4) + " BIC prize, " + is.get(5) + " bikes, "
                    + is.get(6) + " lifts, " + is.get(7)
                    + " deliveries, https://www.google.com/maps/place/" + is.get(1) + "," + is.get(2));

        } catch (StatusRuntimeException e) {
            printError(e);
        }
    }

    void bikeup(String user_name, Double lat, Double lon, String station_id) {
        try {
            BikeUpRequest b = BikeUpRequest.newBuilder().setUserName(user_name)
                                                        .setLat(lat)
                                                        .setLon(lon)
                                                        .setStationId(station_id).build();
            frontend.bikeUp(b);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            printError(e);
        }
    }

    void bikedown(String user_name, Double lat, Double lon, String station_id) {
        try {
            BikeDownRequest b = BikeDownRequest.newBuilder().setUserName(user_name)
                                                            .setLat(lat).setLon(lon)
                                                            .setStationId(station_id).build();
            frontend.bikeDown(b);
            System.out.println("OK");
        } catch (StatusRuntimeException e) {
            printError(e);
        }
    }

    void scan(Double lat, Double lon, String limiter) {
        try {
            if (Integer.parseInt(limiter) < 0) {
                System.err.println("ERROR: Limiter must be positive");
                return;
            }
            LocateStationRequest l = LocateStationRequest.newBuilder().setLat(lat)
                                                                      .setLon(lon)
                                                                      .setLimiter(Integer.parseInt(limiter)).build();
            List<String> ls = frontend.locateStation(l).getStationsList();

            List<List<String>> is = new ArrayList<>();

            ls.forEach(x -> is.add(info_aux(x).subList(1, 6)));

            for (int i = 0; i < ls.size(); i++) {
                List<String> aux = is.get(i);

                double dist = getHaversineDistance(lat, lon, Double.parseDouble(aux.get(0)), Double.parseDouble(aux.get(1)));

                System.out.println(ls.get(i) + ", lat " + aux.get(0) + ", " + aux.get(1) + " long, " + aux.get(2) +
                        " docks, " + aux.get(3) + " BIC prize, " + aux.get(4) + " bikes, at " + (int)dist + " meters.");
            }

        } catch (StatusRuntimeException e) {
            printError(e);
        } catch (NumberFormatException nfe) {
            System.err.println("ERROR: Usage: scan %int%");
        }
    }

    void timeout(String time) {
        try {
            long t = Long.parseLong(time);
            System.out.println("sleeping...");
            sleep(t);
            System.out.println("awaken!");
        } catch (InterruptedException ie) {
            System.err.println("ERROR: sleep : " + ie.getMessage());
        } catch (IllegalArgumentException nfe) {
            System.err.println("ERROR: Usage: zzz %long%");
        }
    }

    void ping() {
        try {
            PingRequest pr = PingRequest.newBuilder().setInput(frontend.getChannel().authority()).build();

            String response = frontend.ping(pr).getOutput();
            System.out.println(response);
        } catch (StatusRuntimeException e) {
            System.out.println("Warn: Server not responding!");
        }
    }

    void sysStatus() {
        try {
            SysStatusRequest pr = SysStatusRequest.newBuilder().build();
            String response = frontend.sysStatus(pr).getOutput();
            System.out.print(response);
        } catch (StatusRuntimeException e) {
            System.out.println("Warn: Server not responding!");
        }
    }

    // Aux

    List<String> info_aux(String station_id) {
        InfoStationRequest i = InfoStationRequest.newBuilder().setStationId(station_id).build();
        InfoStationResponse a = frontend.infoStation(i);
        return Arrays.asList(a.getName(), String.valueOf(a.getLat()), String.valueOf(a.getLon()),
                String.valueOf(a.getDocks()), String.valueOf(a.getAward()),
                String.valueOf(a.getBikeCount()), String.valueOf(a.getTotalLifts()),
                String.valueOf(a.getTotalDeliveries()));
    }

    double getHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        HaversineDistanceRequest hdr = HaversineDistanceRequest.newBuilder().setLat1(lat1)
                                                                            .setLon1(lon1)
                                                                            .setLat2(lat2)
                                                                            .setLon2(lon2).build();
        return frontend.haversineDistance(hdr).getDistance();
    }

    private void printError(StatusRuntimeException e) {
        if (e.getStatus().getDescription() != null && e.getStatus().getDescription().equals("io exception")) {
            System.out.println("Warn: Server not responding!");
        } else {
            System.out.println(e.getStatus().getDescription());
        }
    }
}
