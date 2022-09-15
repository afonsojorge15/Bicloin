package pt.tecnico.bicloin.hub;

import org.junit.jupiter.api.*;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.SysStatusResponse;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SysStatusIT {

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

    /**
     * Test passes if sys_status output contains at least 1 "OK" string
     */
    @Test
    public void SysStatusOKTest() {
        SysStatusRequest request = SysStatusRequest.newBuilder().build();
        SysStatusResponse response = frontend.sysStatus(request);
        String r = response.getOutput();

        assertTrue(r.contains("OK"));
    }

}