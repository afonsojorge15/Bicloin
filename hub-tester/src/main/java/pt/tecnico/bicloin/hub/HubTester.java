package pt.tecnico.bicloin.hub;


import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class HubTester {
	
	public static void main(String[] args) throws ZKNamingException {
		System.out.println(HubTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s path zooHost zooPort%n", HubTester.class.getName());
			return;
		}

		final String instance = args[0];
		final String zooHost = args[1];
		final String zooPort = args[2];

		HubFrontend frontend = new HubFrontend(zooHost, zooPort, instance);

		try {
			PingRequest request = PingRequest.newBuilder().setInput("friend").build();
			PingResponse response = frontend.ping(request);
			System.out.println(response.getOutput());
		} catch (StatusRuntimeException sre) {
			System.err.println("ERROR: " + sre.getMessage());
		}

		frontend.close();

	}
	
}
