package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static io.grpc.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BalanceIT {

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
    public void balanceOKTest() {
        BalanceRequest request = BalanceRequest.newBuilder().setUserName("diana").build();
        BalanceResponse response = frontend.balance(request);
        assertEquals(0, response.getBalance());
    }

    @Test
    public void noUserFoundKOTest() {
        BalanceRequest request = BalanceRequest.newBuilder().setUserName("leonardo").build();
        assertEquals(
                NOT_FOUND.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.balance(request))
                        .getStatus()
                        .getCode());
    }
}
