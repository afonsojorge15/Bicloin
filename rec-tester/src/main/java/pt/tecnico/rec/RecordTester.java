package pt.tecnico.rec;


import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.PingRequest;
import pt.tecnico.rec.grpc.PingResponse;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;


public class RecordTester {
	
	public static void main(String[] args) throws ZKNamingException {
		System.out.println(RecordTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 1) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zooHost zooPort%n", RecordTester.class.getName());
			return;
		}

		final String zooHost = args[0];
		final String zooPort = args[1];

		RecQuorumFrontend frontend = new RecQuorumFrontend(zooHost, zooPort);

		try {
			PingRequest request = PingRequest.newBuilder().setInput("localhost:8091").build();
			System.out.println(frontend.ping(request).getOutput());
			request = PingRequest.newBuilder().setInput("localhost:8092").build();
			System.out.println(frontend.ping(request).getOutput());
			request = PingRequest.newBuilder().setInput("localhost:8093").build();
			System.out.println(frontend.ping(request).getOutput());
		} catch (StatusRuntimeException  sre) {
			System.err.println("ERROR: " + sre.getMessage());
		}

		System.exit(0);
	}
	
}
