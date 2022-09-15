package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.*;

import static pt.tecnico.rec.RecordMain.records;
import static io.grpc.Status.INVALID_ARGUMENT;

public class RecServiceImpl extends RecordServiceGrpc.RecordServiceImplBase {

    public static final int DEFAULT_VALUE = -1;

    /** Rec implementation */
    @Override
    public synchronized void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        String input = request.getInput();

        if (input == null || input.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT
                    .withDescription("Input cannot be empty!").asRuntimeException());
        }

        String output = "OK: " + input;
        PingResponse response = PingResponse.newBuilder().setOutput(output).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public synchronized void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
        String register_name = request.getRecordName();
        int val, seq, cid;

        if (register_name == null || register_name.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT
                    .withDescription("Name of record cannot be empty!").asRuntimeException());
        }
        if (!records.containsKey(register_name)) {
            records.put(register_name, new Integer[]{DEFAULT_VALUE, DEFAULT_VALUE, DEFAULT_VALUE});
            val = DEFAULT_VALUE;
            seq = DEFAULT_VALUE;
            cid = DEFAULT_VALUE;
        } else {
            val = records.get(register_name)[0];
            seq = records.get(register_name)[1];
            cid = records.get(register_name)[2];
        }
        responseObserver.onNext(ReadResponse.newBuilder().setRecordValue(val).setSeq(seq).setCid(cid).build());
        responseObserver.onCompleted();
    }

    public synchronized void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
        String record_name = request.getRecordName();
        int record_value = request.getRecordValue();
        int seq = request.getSeq();
        int cid = request.getCid();
        if (record_name == null || record_name.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT
                    .withDescription("Name of record cannot be empty!").asRuntimeException());
        } else if (records.get(record_name) == null) {
            responseObserver.onNext(WriteResponse.newBuilder().setAck(false).build());
            responseObserver.onCompleted();
        } else {
            if (seq > records.get(record_name)[1]) {
                records.put(record_name, new Integer[]{record_value, seq, cid});
            } else if (seq == records.get(record_name)[1] && cid > records.get(record_name)[2]) {
                records.put(record_name, new Integer[]{record_value, seq, cid});
            }
            responseObserver.onNext(WriteResponse.newBuilder().setAck(true).build());
            responseObserver.onCompleted();
        }
    }

}