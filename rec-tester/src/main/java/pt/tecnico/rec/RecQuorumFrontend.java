package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.rec.grpc.*;
import pt.tecnico.rec.grpc.RecordServiceGrpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class RecQuorumFrontend implements AutoCloseable {

    private final List<ManagedChannel> channels;
    private final List<RecordServiceStub> stubs;
    private final Integer quorum;

    /**
     * Creates a frontend that contacts a random replica.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     */
    public RecQuorumFrontend(String zooHost, String zooPort) throws ZKNamingException {
        ZKNaming zkNaming = new ZKNaming(zooHost, zooPort);
        this.stubs = new ArrayList<>();
        this.channels = new ArrayList<>();

        List<ZKRecord> records = new ArrayList<>(zkNaming.listRecords("/grpc/bicloin/rec"));

        this.quorum = records.size() / 2 + 1;

        for (ZKRecord recRecord : records) {
            try {
                createNewChannel(recRecord.getURI());
            } catch (IllegalArgumentException zkne) {
                throw new IllegalArgumentException("Error: " + zkne);
            }
        }
    }

    /**
     * Auxiliary method to constructors to refactor common code.
     * @param target the target to be used to create the channel
     */
    private void createNewChannel(String target) {
        try {
            ManagedChannel newChannel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
            this.channels.add(newChannel);
            this.stubs.add(RecordServiceGrpc.newStub(newChannel));
        } catch (RuntimeException sre) {
            System.out.println("ERROR : RecFrontend createNewChannel : Could not create channel\n"
                    + sre.getMessage());
        }
    }

    /* ---------- Services ---------- */

    public PingResponse ping(PingRequest request) {
        try {
            ManagedChannel tempChannel = ManagedChannelBuilder.forTarget(request.getInput()).usePlaintext().build();
            RecordServiceBlockingStub blockingStub = RecordServiceGrpc.newBlockingStub(tempChannel);
            return blockingStub.ping(request);
        } catch (IllegalArgumentException iae) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
    }

    public ReadResponse read(ReadRequest request)  {
        System.out.println("\nReceived read request");
        Instant start = Instant.now();

        ResponseCollector resCol = new ResponseCollector();

        CountDownLatch finishLatch = new CountDownLatch(this.quorum); // create a lock for each quorum

        for (RecordServiceStub stub : this.stubs) {
            String replica = stub.getChannel().authority();
            System.out.println("Contacting server " + replica + "... " + "Sending " + request.toString().replace('\n', ' '));
            stub.read(request, new RecObserver<>(resCol, finishLatch, replica, quorum));
        }

        try {
            finishLatch.await(); // freed
        } catch (InterruptedException ie) {
            System.out.println("Error: " + ie);
        }

        System.out.println("READ TOTAL TIME: " + Duration.between(start, Instant.now()));

        if(resCol.responses.isEmpty()) {
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
        return Collections.max(resCol.responses, Comparator.comparing(ReadResponse::getSeq)
                                                         .thenComparing(ReadResponse::getCid));
    }

    public WriteResponse write(WriteRequest request) {

        Instant start = Instant.now();

        ReadResponse rp = read(ReadRequest.newBuilder().setRecordName(request.getRecordName()).build());

        System.out.println("\nReceived write request");
        WriteRequest newRequest = WriteRequest.newBuilder()
                                              .setRecordName(request.getRecordName())
                                              .setRecordValue(request.getRecordValue())
                                              .setSeq(rp.getSeq() + 1)
                                              .setCid(request.getCid()).build();

        CountDownLatch finishLatch = new CountDownLatch(this.quorum); // create a lock for each quorum

        for (RecordServiceStub stub : this.stubs) {
            String replica = stub.getChannel().authority();
            System.out.println("Contacting server " + replica + "...");
            stub.write(newRequest, new RecObserver<>(null, finishLatch, replica, quorum));
        }

        try {
            finishLatch.await(); // freed
        } catch (InterruptedException ie) {
            System.out.println("Error: " + ie);
        }

        System.out.println("WRITE TOTAL TIME: " + Duration.between(start, Instant.now()));
        return null;
    }

    @Override
    public final void close() {
        this.channels.forEach(ManagedChannel::shutdown);
    }
}
