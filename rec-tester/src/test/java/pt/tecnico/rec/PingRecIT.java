package pt.tecnico.rec;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import pt.tecnico.rec.grpc.PingRequest;
import pt.tecnico.rec.grpc.PingResponse;
import static io.grpc.Status.INVALID_ARGUMENT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class PingRecIT extends BaseIT{

    @Test
    public void pingOKTest() {
        PingRequest request = PingRequest.newBuilder().setInput("localhost:8091").build();
        PingResponse response = frontend.ping(request);
        assertEquals("OK: localhost:8091", response.getOutput());
    }

    @Test
    public void emptyPingTest() {
        PingRequest request = PingRequest.newBuilder().setInput("").build();
        assertEquals(
                INVALID_ARGUMENT.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.ping(request))
                        .getStatus()
                        .getCode());
    }
}
