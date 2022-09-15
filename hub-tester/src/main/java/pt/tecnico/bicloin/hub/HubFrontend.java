package pt.tecnico.bicloin.hub;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class HubFrontend implements Closeable {

    private final ManagedChannel channel;
    private final HubServiceGrpc.HubServiceBlockingStub stub;
    private final ZKNaming zkNaming;

    /**
     * Creates a frontend that contacts the only replica.
     *
     * @param zooHost Names server host
     * @param zooPort Names server port
     */
    public HubFrontend(String zooHost, String zooPort, String instance) throws ZKNamingException {
        this.zkNaming = new ZKNaming(zooHost, zooPort);

        ZKRecord hubRecord = zkNaming.lookup("/grpc/bicloin/hub/" + instance);
        this.channel = ManagedChannelBuilder.forTarget(hubRecord.getURI()).usePlaintext().build();
        this.stub = HubServiceGrpc.newBlockingStub(this.channel);
    }

    public ManagedChannel getChannel() {
        return this.channel;
    }

    /* ---------- Services ---------- */

    public PingResponse ping(PingRequest request) {
        return stub.ping(PingRequest.newBuilder().setInput(request.getInput()).build());
    }

    public SysStatusResponse sysStatus(SysStatusRequest request) {
        try {
            List<ZKRecord> records = new ArrayList<>(this.zkNaming.listRecords("/grpc/bicloin/rec"));
            List<String> targets = records.stream().map(ZKRecord::getURI).collect(Collectors.toList());

            PingResponse pr = ping(PingRequest.newBuilder().setInput(this.channel.authority()).build());
            SysStatusResponse ssp = stub.sysStatus(SysStatusRequest.newBuilder().addAllTargets(targets).build());

            String output =  pr.getOutput() + "\n" + ssp.getOutput();

            return SysStatusResponse.newBuilder().setOutput(output).build();

        } catch (ZKNamingException zke) {
            System.out.println("ERROR : HubFrontend listRecords : listRecords failed\n" + zke.getMessage());
            return null;
        }
    }

    public BalanceResponse balance(BalanceRequest request) {
        return stub.balance(request);
    }

    public TopUpResponse topUp(TopUpRequest request) {
        return stub.topUp(request);
    }

    public InfoStationResponse infoStation(InfoStationRequest request) {
        return stub.infoStation(request);
    }

    public LocateStationResponse locateStation(LocateStationRequest request) {
        return stub.locateStation(request);
    }

    public BikeUpResponse bikeUp(BikeUpRequest request) {
        return stub.bikeUp(request);
    }

    public BikeDownResponse bikeDown(BikeDownRequest request) {
        return stub.bikeDown(request);
    }

    public HaversineDistanceResponse haversineDistance(HaversineDistanceRequest request) {
        return stub.haversineDistance(request);
    }

    @Override
    public final void close() {
        channel.shutdown();
    }
}
