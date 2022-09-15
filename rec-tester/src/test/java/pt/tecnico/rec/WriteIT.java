package pt.tecnico.rec;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.*;
import pt.tecnico.rec.grpc.ReadRequest;
import pt.tecnico.rec.grpc.ReadResponse;
import pt.tecnico.rec.grpc.WriteRequest;
import pt.tecnico.rec.grpc.WriteResponse;

import static io.grpc.Status.INVALID_ARGUMENT;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class WriteIT extends BaseIT{

    @Test
    public void writeOKTest() {
        WriteRequest wRequest = WriteRequest.newBuilder().setRecordName("u/alice/balance").setRecordValue(100).build();
        frontend.write(wRequest);

        ReadRequest rRequest = ReadRequest.newBuilder().setRecordName("u/alice/balance").build();
        ReadResponse rResponse = frontend.read(rRequest);
        assertEquals(100, rResponse.getRecordValue());
    }

    @Test
    public void emptyWriteTest() {
        WriteRequest request = WriteRequest.newBuilder().setRecordName("").setRecordValue(100).build();
        assertEquals(
                INVALID_ARGUMENT.getCode(),
                assertThrows(
                        StatusRuntimeException.class, () -> frontend.write(request))
                        .getStatus()
                        .getCode());
    }
}