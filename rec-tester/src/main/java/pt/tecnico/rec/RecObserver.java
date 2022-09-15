package pt.tecnico.rec;

import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.ReadResponse;

import java.util.concurrent.CountDownLatch;

public class RecObserver<R> implements StreamObserver<R> {

    final ResponseCollector resCollector;
    final CountDownLatch finishLatch;
    final String replica;
    final int quorum;

    public RecObserver(ResponseCollector resCollector, CountDownLatch fLatch, String replica, int quorum) {
        this.resCollector = resCollector;
        this.finishLatch = fLatch;
        this.replica = replica;
        this.quorum = quorum;
    }

    @Override
    public void onNext(R r) {
        System.out.println("Received " + r.toString().replace('\n', ' ')+ "from server " + this.replica);
        if(this.resCollector != null  && this.resCollector.responses.size() < this.quorum) {
            this.resCollector.addResponse((ReadResponse) r);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        System.out.println("Received error: " + throwable);
        finishLatch.countDown();
    }

    @Override
    public void onCompleted() {
        System.out.println("Request completed from server " + this.replica);
        finishLatch.countDown();
    }
}
