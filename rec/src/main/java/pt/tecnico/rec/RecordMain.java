package pt.tecnico.rec;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import sun.misc.Signal;

import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

public class RecordMain {

	public static SortedMap<String, Integer[]> records = new TreeMap<>();

	public static void main(String[] args) {
		System.out.println(RecordMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort host port instance%n", RecordMain.class.getName());
			return;
		}

		final BindableService impl = new RecServiceImpl();

		final String zooHost = args[0];
		final String zooPort = args[1];
		final String host = args[2];
		final String port = args[3];
		final String instance = args[4];
		final String path = "/grpc/bicloin/rec/" + instance;

		ZKNaming zkNaming = null;
		try {
			zkNaming = new ZKNaming(zooHost, zooPort);

			// publish
			zkNaming.rebind(path, host, port);

			// Create a new server to listen on port
			Server server = ServerBuilder.forPort(Integer.parseInt(port)).addService(impl).build();

			// Start the server
			server.start();

			// Server threads are running in the background.
			System.out.println("Replica " + instance + " started");

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
			System.out.println("Server closing...");
			System.exit(0);
		}
	}
}
