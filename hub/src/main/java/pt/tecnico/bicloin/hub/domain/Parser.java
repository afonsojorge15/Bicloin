package pt.tecnico.bicloin.hub.domain;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import pt.tecnico.rec.grpc.WriteRequest;
import static pt.tecnico.bicloin.hub.HubMain.*;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class Parser {

    private final boolean initRec;

    public Parser(boolean initRec) {
        this.initRec = initRec;
    }

    public void parseFiles(String usersFilename, String stationsFilename)  {
        try (CSVReader userReader = new CSVReader(new FileReader(usersFilename));
             CSVReader stationReader = new CSVReader(new FileReader(stationsFilename))) {

            List<String[]> usersList = userReader.readAll();
            usersList.forEach(this::parseUser);

            List<String[]> stationsList = stationReader.readAll();
            stationsList.forEach(this::parseStation);

        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
    }

    private void parseUser(String[] line) {
        if (line.length != 3) {
            System.err.println("Wrong number of User attributes (3)");
        } else if (line[0].length() < 3 || line[0].length() > 20 ) {
            System.err.println("user_name must contain between 3 and 10 characters");
        } else if (line[1].length() > 30) {
            System.err.println("Name must have less than 30 characters");
        } else if (line[2].charAt(0) != '+') {
            System.err.println("Phone number must include country code");
        } else {
            users.add(new User(line[0], line[1], line[2]));
            if(this.initRec) {
                record.write(WriteRequest.newBuilder().setRecordName("u/" + line[0] + "/balance").setRecordValue(0).setCid(cid).build());
                record.write(WriteRequest.newBuilder().setRecordName("u/" + line[0] + "/hasBike").setRecordValue(0).setCid(cid).build());
            }
            return;
        }
        System.err.println("Error at: " + Arrays.toString(line));
    }

    private void parseStation(String[] line) {
        if (line.length != 7) {
            System.err.println("Wrong number of Station attributes (6)");
        } else if (line[1].length() != 4) {
            System.err.println("abrev must contain 4 characteres (4)");
        } else {
            stations.add(new Station(line[0], line[1], Double.parseDouble(line[2]), Double.parseDouble(line[3]),
                    Integer.parseInt(line[4]), Integer.parseInt(line[6])));
            if(this.initRec) {
                record.write(WriteRequest.newBuilder().setRecordName("s/" + line[1] + "/bikes").setRecordValue(Integer.parseInt(line[5])).setCid(cid).build());
                record.write(WriteRequest.newBuilder().setRecordName("s/" + line[1] + "/lifts").setRecordValue(0).setCid(cid).build());
                record.write(WriteRequest.newBuilder().setRecordName("s/" + line[1] + "/deliveries").setRecordValue(0).setCid(cid).build());
            }
            return;
        }
        System.err.println("Error at: " + Arrays.toString(line));
    }
}
