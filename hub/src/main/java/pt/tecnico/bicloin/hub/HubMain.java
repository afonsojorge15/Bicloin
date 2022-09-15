package pt.tecnico.bicloin.hub;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.bicloin.hub.domain.Parser;
import pt.tecnico.bicloin.hub.domain.Station;
import pt.tecnico.bicloin.hub.domain.User;
import pt.tecnico.rec.RecQuorumFrontend;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import sun.misc.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class HubMain {

	public static List<User> users = new ArrayList<>();
	public static List<Station> stations = new ArrayList<>();
	public static RecQuorumFrontend record;
	public static Integer cid;

	public static void main(String[] args) throws ZKNamingException {
		System.out.println(HubMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 7) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort host port instance users.csv stations.csv [initRec]%n", HubMain.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String host = args[2];
		final String port = args[3];
		final String instance = args[4];
		cid = Integer.parseInt(instance);
		final String path = "/grpc/bicloin/hub/" + instance;
		final String userCSV = args[5];
		final String stationCSV = args[6];
		boolean initRec = args.length == 8 && args[7].equals("initRec");

		try {
			record = new RecQuorumFrontend(zooHost, zooPort);
		} catch (IllegalArgumentException iae) {
			System.out.println("Caught exception with description: " + iae.getMessage());
			return;
		}

		Parser p = new Parser(initRec);
		p.parseFiles(userCSV, stationCSV);

		ZKNaming zkNaming = null;
		try {
			zkNaming = new ZKNaming(zooHost, zooPort);
			// publish
			zkNaming.rebind(path, host, port);

			final BindableService impl = new HubServiceImpl();

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(Integer.parseInt(port)).addService(impl).build();

			// Start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Server started");

			// Create new thread where we wait for the user input.
			new Thread(() -> {
				System.out.println("<Press enter to shutdown>");
				new Scanner(System.in).nextLine();

				server.shutdown();
			}).start();

			// Catch SIGINT signal
			Signal.handle(new Signal("INT"), signal -> server.shutdown());

			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();

		} catch (Exception e) {
			System.out.println("Internal Server Error: " + e.getMessage());
		} finally {
			try {
				if (zkNaming != null) {
					System.out.println("Server unbinding...");
					zkNaming.unbind(path, host, port);
				}
			} catch (ZKNamingException zkne) {
				System.out.println("ERROR : Unbind zknaming HubMain");
			}
			System.out.println("Server closed");
			System.exit(0);
		}
	}
}
