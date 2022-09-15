package pt.tecnico.rec;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.rec.grpc.ReadRequest;
import pt.tecnico.rec.grpc.ReadResponse;
import static io.grpc.Status.INVALID_ARGUMENT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class ReadIT extends BaseIT{
    @Test
    public void readOKTest() {
        ReadRequest request = ReadRequest.newBuilder().setRecordName("u/alfredo/balance").build();
        ReadResponse response = frontend.read(request);
        assertEquals(-1, response.getRecordValue());
    }

    @Test
    public void emptyReadTest() {
        ReadRequest request = ReadRequest.newBuilder().setRecordName("").build();
        assertEquals(
                INVALID_ARGUMENT.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.read(request))
                        .getStatus()
                        .getCode());
    }
}