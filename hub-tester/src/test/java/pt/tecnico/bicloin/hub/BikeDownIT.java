package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static io.grpc.Status.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BikeDownIT {

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
    public void bikeDownOKTest() {
        String user_name = "alice";
        String station_id = "stao";

        TopUpRequest topUp_request = TopUpRequest.newBuilder().setUserName(user_name).setAmount(15)
                .setPhoneNumber("+35191102030").build();
        TopUpResponse topUp_response = frontend.topUp(topUp_request);

        BikeUpRequest bikeUp_request = BikeUpRequest.newBuilder().setStationId("ocea").setUserName(user_name)
                .setLat(38.7633).setLon(-9.0950).build();
        BikeUpResponse bikeUp_response = frontend.bikeUp(bikeUp_request);

        BalanceRequest balance_request = BalanceRequest.newBuilder().setUserName(user_name).build();
        BalanceResponse balance_response = frontend.balance(balance_request);

        InfoStationRequest infoStation_request = InfoStationRequest.newBuilder().setStationId(station_id).build();
        InfoStationResponse infoStation_response = frontend.infoStation(infoStation_request);

        BikeDownRequest request = BikeDownRequest.newBuilder().setUserName(user_name).setLat(38.6867).setLon(-9.3117)
                .setStationId(station_id).build();
        BikeDownResponse response = frontend.bikeDown(request);

        BalanceRequest new_balance_request = BalanceRequest.newBuilder().setUserName(user_name).build();
        BalanceResponse new_balance_response = frontend.balance(new_balance_request);

        InfoStationRequest new_infoStation_request = InfoStationRequest.newBuilder().setStationId(station_id).build();
        InfoStationResponse new_infoStation_response = frontend.infoStation(new_infoStation_request);

        // checks if the parameters needed changed

        assertEquals("", response.toString());
        assertEquals(balance_response.getBalance() + infoStation_response.getAward(), new_balance_response
                .getBalance());
        assertEquals(infoStation_response.getBikeCount() + 1, new_infoStation_response.getBikeCount());
        assertEquals(infoStation_response.getTotalDeliveries() + 1, new_infoStation_response.getTotalDeliveries());

    }

    @Test
    public void tooFarFromStationKOTest() {
        TopUpRequest topUp_request = TopUpRequest.newBuilder().setUserName("carlos").setAmount(15).setPhoneNumber("+34203040").build();
        TopUpResponse topUp_response = frontend.topUp(topUp_request);

        BikeUpRequest bikeUp_request = BikeUpRequest.newBuilder().setStationId("stao").setUserName("carlos")
                .setLat(38.6867).setLon(-9.3124).build();
        BikeUpResponse bikeUp_response = frontend.bikeUp(bikeUp_request);

        BikeDownRequest request = BikeDownRequest.newBuilder().setUserName("carlos").setLat(38.7376).setLon(-9.1545)
                .setStationId("istt").build();
        assertEquals(INVALID_ARGUMENT.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.bikeDown(request))
                        .getStatus()
                        .getCode());
    }

    /**
     * noDocksAvailbleKOTest uses station where the number of
     * total docks equals the current number of bikes
     */
    @Test
    public void noDocksAvailbleKOTest() {
        BikeDownRequest request = BikeDownRequest.newBuilder().setUserName("alice").setLat(38.7376).setLon(-9.1545)
                .setStationId("gulb").build();
        assertEquals(
                RESOURCE_EXHAUSTED.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.bikeDown(request))
                        .getStatus()
                        .getCode());
    }



}