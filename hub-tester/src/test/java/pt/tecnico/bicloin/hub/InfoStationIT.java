package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static io.grpc.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InfoStationIT {

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
    public void InfoStationOKTest() {
        InfoStationRequest request = InfoStationRequest.newBuilder().setStationId("gulb").build();
        InfoStationResponse response = frontend.infoStation(request);
        assertEquals("Gulbenkian", response.getName());
        assertEquals(38.7376, response.getLat());
        assertEquals(-9.1545, response.getLon());
        assertEquals(30, response.getDocks());
        assertEquals(30, response.getBikeCount());
        assertEquals(2, response.getAward());
        assertEquals(0, response.getTotalLifts());
        assertEquals(0, response.getTotalDeliveries());
    }

    @Test
    public void noStationFoundKOTest() {
        InfoStationRequest request = InfoStationRequest.newBuilder().setStationId("asdf").build();
        assertEquals(
                NOT_FOUND.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.infoStation(request))
                        .getStatus()
                        .getCode());
    }
}