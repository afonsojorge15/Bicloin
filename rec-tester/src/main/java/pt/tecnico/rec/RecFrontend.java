package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;


public class RecFrontend implements AutoCloseable {

    private final ManagedChannel channel;
    private final RecordServiceGrpc.RecordServiceBlockingStub stub;


    /**
     * Creates a frontend that contacts the specified path.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     * @param path Names server path
     */
    public RecFrontend(String zooHost, String zooPort, String path) throws ZKNamingException {
        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);

        // lookup
        ZKRecord record = zkNaming.lookup(path);
        String target = record.getURI();
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        // Create a blocking stub.
        stub = RecordServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Creates a frontend that contacts the only replica.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     */
    public RecFrontend(String zooHost, String zooPort) throws ZKNamingException {
        // Channel is the abstraction to connect to a service endpoint.
        // Let us use plaintext communication because we do not have certificates.
        ZKNaming zkNaming = new ZKNaming(zooHost,zooPort);

        // lookup
        ZKRecord record = zkNaming.lookup("/grpc/bicloin/rec/1");
        String target = record.getURI();
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        // Create a blocking stub.
        stub = RecordServiceGrpc.newBlockingStub(channel);
    }

    public PingResponse ping(PingRequest request) {
        return stub.ping(request);
    }

    public ReadResponse read(ReadRequest request) { return stub.read(request); }

    public WriteResponse write(WriteRequest request) { return stub.write(request); }

    @Override
    public final void close() {
        channel.shutdown();
    }
}