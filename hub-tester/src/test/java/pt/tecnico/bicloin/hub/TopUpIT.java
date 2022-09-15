package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import static io.grpc.Status.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TopUpIT {
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
    public void topUpOKTest() {
        BalanceRequest balance_request = BalanceRequest.newBuilder().setUserName("alice").build();
        BalanceResponse balance_response = frontend.balance(balance_request);

        TopUpRequest request = TopUpRequest.newBuilder().setUserName("alice").setAmount(15).setPhoneNumber("+35191102030").build();
        TopUpResponse response = frontend.topUp(request);
        assertEquals(balance_response.getBalance() + 150, response.getBalance());
    }
    @Test
    public void NoUserFoundKOTest() {
        TopUpRequest request = TopUpRequest.newBuilder().setUserName("leonardo").setAmount(15)
                .setPhoneNumber("+35191102030").build();
        assertEquals(
                NOT_FOUND.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.topUp(request))
                        .getStatus()
                        .getCode());
    }

    @Test
    public void WrongAmountKOTest() {
        TopUpRequest request = TopUpRequest.newBuilder().setUserName("alice").setAmount(-5)
                .setPhoneNumber("+35191102030").build();
        assertEquals(
                INVALID_ARGUMENT.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.topUp(request))
                        .getStatus()
                        .getCode());
    }

    @Test
    public void noNumberMatchKOTest() {
        TopUpRequest request = TopUpRequest.newBuilder().setUserName("alice").setAmount(15)
                .setPhoneNumber("+35192102030").build();
        assertEquals(
                INVALID_ARGUMENT.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.topUp(request))
                        .getStatus()
                        .getCode());
    }
}
