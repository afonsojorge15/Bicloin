package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocateStationIT {

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
    public void LocateStationOKTest() {
        LocateStationRequest request = LocateStationRequest.newBuilder().setLat(38.7097).setLon(-9.1336).setLimiter(3).build();
        LocateStationResponse response = frontend.locateStation(request);
        List<String> list_response = response.getStationsList();
        assertEquals("cate", list_response.get(0));
        assertEquals("prcm", list_response.get(1));
        assertEquals("cais", list_response.get(2));
    }
}
