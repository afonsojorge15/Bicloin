package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static io.grpc.Status.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BikeUpIT {

    private HubFrontend frontend;

    @BeforeEach
    public void setUp() throws ZKNamingException {
        frontend = new HubFrontend("localhost", "2181", "1");
    }

    @AfterEach
    public void tearDown() {
        frontend.close();
        frontend = null;
    }

    @Test
    public void BikeUpOKTest() {
        String user_name = "bruno";
        String station_id = "stao";

        TopUpRequest topUp_request = TopUpRequest.newBuilder().setUserName(user_name).setAmount(15).setPhoneNumber("+35193334444").build();
        frontend.topUp(topUp_request);

        BalanceRequest balance_request = BalanceRequest.newBuilder().setUserName(user_name).build();
        BalanceResponse balance_response = frontend.balance(balance_request);

        InfoStationRequest infoStation_request = InfoStationRequest.newBuilder().setStationId(station_id).build();
        InfoStationResponse infoStation_response = frontend.infoStation(infoStation_request);

        BikeUpRequest request = BikeUpRequest.newBuilder().setUserName(user_name).setLat(38.6867).setLon(-9.3117)
                .setStationId(station_id).build();
        BikeUpResponse response = frontend.bikeUp(request);

        BalanceRequest new_balance_request = BalanceRequest.newBuilder().setUserName(user_name).build();
        BalanceResponse new_balance_response = frontend.balance(new_balance_request);

        InfoStationRequest new_infoStation_request = InfoStationRequest.newBuilder().setStationId(station_id).build();
        InfoStationResponse new_infoStation_response = frontend.infoStation(new_infoStation_request);

        // checks if the parameters needed changed

        assertEquals("", response.toString());
        assertEquals(balance_response.getBalance(), new_balance_response.getBalance() + 10);
        assertEquals(infoStation_response.getBikeCount(), new_infoStation_response.getBikeCount() + 1);
        assertEquals(infoStation_response.getTotalLifts() + 1, new_infoStation_response.getTotalLifts());
    }

    @Test
    public void tooFarFromStationKOTest() {
        BikeUpRequest request = BikeUpRequest.newBuilder().setUserName("carlos").setLat(38.7376).setLon(-9.1545)
                .setStationId("istt").build();
        assertEquals(INVALID_ARGUMENT.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.bikeUp(request))
                        .getStatus()
                        .getCode());

    }

    /**
     * noBikesAvailableKOTest uses station where the number of
     * total docks equals the current number of bikes
     */

    @Test
    public void noBikesAvailableKOTest() {
        BikeUpRequest request = BikeUpRequest.newBuilder().setUserName("diana").setLat(38.7097).setLon(-9.1336)
                .setStationId("cate").build();
        assertEquals(RESOURCE_EXHAUSTED.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.bikeUp(request))
                        .getStatus()
                        .getCode());

    }

    @Test
    public void alreadyLiftedBikeKOTest() {
        TopUpRequest topUp_request = TopUpRequest.newBuilder().setUserName("eva").setAmount(15).setPhoneNumber("+155509080706").build();
        frontend.topUp(topUp_request);
        BikeUpRequest request1 = BikeUpRequest.newBuilder().setUserName("eva").setLat(38.6867).setLon(-9.3117)
                .setStationId("stao").build();
        frontend.bikeUp(request1);
        BikeUpRequest request2 = BikeUpRequest.newBuilder().setUserName("eva").setLat(38.6867).setLon(-9.3117)
                .setStationId("stao").build();
        assertEquals(
                ALREADY_EXISTS.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.bikeUp(request2))
                        .getStatus()
                        .getCode());
    }
}