package pt.tecnico.bicloin.app;

import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.*;

public class AppMain {

    public static void main(String[] args) {
        final String help =
                "--balance Format is 'balance'\n" +
                        "--top-up Format is 'top-up %long%'\n" +
                        "--info Format is 'info'\n" +
                        "--scan Format is 'scan %int%'\n" +
                        "--bike-up Format is 'bike-up %station%'\n" +
                        "--bike-down Format is 'bike-down %station%'\n" +
                        "--tag Format is 'tag %lat% %lon% %tagName%'\n" +
                        "--move Format is 'move %lat% %lon%' or 'move %tagName%'\n" +
                        "--at Format is 'at'\n" +
                        "--ping Format is 'ping'\n" +
                        "--sys_status Format is 'sys_status'\n" +
                        "--zzz Format is 'zzz %int%'\n" +
                        "--quit Format is 'quit'";

        System.out.println(AppMain.class.getSimpleName());

        // receive and print arguments
        System.out.printf("Received %d arguments%n", args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.printf("arg[%d] = %s%n", i, args[i]);
        }

        if (args.length < 6) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s zooHost zooPort user_name phone lat lon%n", AppMain.class.getName());
            return;
        }

        final String zooHost = args[0];
        final String zooPort = args[1];
        final String user_name = args[2];
        final String phone = args[3];
        double lat = Double.parseDouble(args[4]);
        double lon = Double.parseDouble(args[5]);

        HubFrontend frontend;
        App app;

        try {
            frontend = new HubFrontend(zooHost, zooPort, "1");
            app = new App(frontend);
        } catch (Exception e) {
            System.out.println("Caught exception with description: " + e.getMessage());
            return;
        }

        Map<String, List<Double>> tags = new HashMap<>();

        Scanner sin = new Scanner(System.in);
        String input;
        String[] tokens;
        boolean close = false;

        try {
            while (!close) {
                System.out.print("> ");
                System.out.flush();
                input = sin.nextLine();

                if (input.equals("") || input.charAt(0) == '#')
                    continue;

                tokens = input.split(" ");

                switch (tokens[0]) {
                    case "help":
                        System.out.println(help);
                        break;
                    case "balance":
                        app.balance(user_name);
                        break;
                    case "top-up":
                        if (tokens.length == 2) {
                            app.topup(user_name, tokens[1], phone);
                        } else {
                            System.err.println("ERROR: Usage: top-up %long%");
                        }
                        break;
                    case "tag":
                        if (tokens.length == 4) {
                            try {
                                tags.put(tokens[3], new ArrayList<>(Arrays.asList(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]))));
                                System.out.println("OK");
                            } catch (NullPointerException | NumberFormatException npe) {
                                System.err.println("ERROR: Usage: tag %lat% %lon% %tag_name%");
                            }
                        } else {
                            System.err.println("ERROR: Usage: tag %lat% %lon% %tag_name%");
                        }
                        break;
                    case "move":
                        if (tokens.length == 2) {
                            try {
                                lat = (tags.get(tokens[1]).get(0));
                                lon = (tags.get(tokens[1]).get(1));
                            } catch (NullPointerException npe) {
                                System.err.println("ERROR: tag " + tokens[1] + " does not exist!");
                                break;
                            }
                        } else if (tokens.length == 3){
                            try {
                                lat = Double.parseDouble(tokens[1]);
                                lon = Double.parseDouble(tokens[2]);
                            } catch (NumberFormatException nfe) {
                                System.err.println("ERROR: Usage: move %lat% %lon%");
                                break;
                            }
                        } else {
                            System.err.println("ERROR: Usage: move %lat% %lon%");
                            break;
                        }
                        System.out.println(user_name + " at https://www.google.com/maps/place/" + lat + "," + lon);
                        break;
                    case "at":
                        System.out.println(user_name + " at https://www.google.com/maps/place/" + lat + "," + lon);
                        break;
                    case "scan":
                        if (tokens.length == 2) {
                            app.scan(lat, lon, tokens[1]);
                        } else {
                            System.err.println("ERROR: Usage: scan %int%");
                        }
                        break;
                    case "info":
                        if (tokens.length == 2) {
                            app.info(tokens[1]);
                        } else {
                            System.err.println("ERROR: Usage: info %station%");
                        }
                        break;
                    case "bike-up":
                        if (tokens.length == 2) {
                            app.bikeup(user_name, lat, lon, tokens[1]);
                        } else {
                            System.err.println("ERROR: Usage: bike-up %station%");
                        }
                        break;
                    case "bike-down":
                        if (tokens.length == 2) {
                            app.bikedown(user_name, lat, lon, tokens[1]);
                        } else {
                            System.err.println("ERROR: Usage: bike-down %station%");
                        }
                        break;
                    case "zzz":
                        if (tokens.length == 2) {
                            app.timeout(tokens[1]);
                        } else {
                            System.err.println("ERROR: Usage: zzz %long%");
                        }
                        break;
                    case "ping":
                        app.ping();
                        break;
                    case "sys_status":
                        app.sysStatus();
                        break;
                    case "quit":
                        close = true;
                        break;
                    default:
                        System.err.println("ERROR: Command not recognized!");
                }
            }
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " + e.getStatus().getDescription());
        } finally {
            frontend.close();
            System.exit(0);
        }
    }
}
