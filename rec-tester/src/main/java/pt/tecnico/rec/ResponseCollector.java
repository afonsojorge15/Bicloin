package pt.tecnico.rec;

import pt.tecnico.rec.grpc.ReadResponse;

import java.util.ArrayList;
import java.util.List;

public class ResponseCollector {

    List<ReadResponse> responses = new ArrayList<>();

    public ResponseCollector() {
    }

    public void addResponse(ReadResponse res) {
        responses.add(res);
    }
}