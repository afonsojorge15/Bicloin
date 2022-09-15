package pt.tecnico.bicloin.hub;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.bicloin.hub.domain.Hub;
import pt.tecnico.bicloin.hub.domain.exception.*;
import pt.tecnico.bicloin.hub.grpc.Hub.*;
import pt.tecnico.bicloin.hub.grpc.Hub.PingRequest;
import pt.tecnico.bicloin.hub.grpc.Hub.PingResponse;
import pt.tecnico.bicloin.hub.grpc.HubServiceGrpc;
import static pt.tecnico.bicloin.hub.HubMain.record;

import java.util.List;

import static io.grpc.Status.*;


public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {

    private final Hub hub;

    public HubServiceImpl() {
        this.hub = new Hub();
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        String input = request.getInput();

        if (input.isBlank()) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Input cannot be empty!").asRuntimeException());
            return;
        }

        String output = "OK: " + input;
        PingResponse response = PingResponse.newBuilder().setOutput(output).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
        List<String> targets = request.getTargetsList();

        if (targets.isEmpty()) {
            responseObserver.onError(INVALID_ARGUMENT
                    .withDescription("Targets cannot be empty!").asRuntimeException());
        }

        StringBuilder output = new StringBuilder();
        for (String target : targets) {
            try {
                output.append(record.ping(pt.tecnico.rec.grpc.PingRequest.newBuilder().setInput(target).build()).getOutput()).append("\n");
            } catch (StatusRuntimeException sre) {
                output.append("KO: ").append(target).append("\n");
            }
        }
        SysStatusResponse response = SysStatusResponse.newBuilder().setOutput(output.toString()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
        try {
            int res = hub.balance(request.getUserName());
            responseObserver.onNext(BalanceResponse.newBuilder().setBalance(res).build());
            responseObserver.onCompleted();
        } catch (NoUserFoundException e) {
            responseObserver.onError(NOT_FOUND.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {
        try {
            int res = hub.top_up(request.getUserName(), request.getAmount(), request.getPhoneNumber());
            responseObserver.onNext(TopUpResponse.newBuilder().setBalance(res).build());
            responseObserver.onCompleted();
        } catch (NoUserFoundException nufe) {
            responseObserver.onError(NOT_FOUND.withDescription(nufe.getMessage()).asRuntimeException());
        } catch (WrongAmountException wae) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(wae.getMessage()).asRuntimeException());
        } catch (PhoneDoesNotMatchUserException pdnmue) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(pdnmue.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
        try {
            String[] res = hub.info_station(request.getStationId());
            responseObserver.onNext(InfoStationResponse.newBuilder().setName(res[0]).setLat(Double.parseDouble(res[1]))
                    .setLon(Double.parseDouble(res[2])).setDocks(Integer.parseInt(res[3]))
                    .setAward(Integer.parseInt(res[4])).setBikeCount(Integer.parseInt(res[5]))
                    .setTotalLifts(Integer.parseInt(res[6])).setTotalDeliveries(Integer.parseInt(res[7])).build());
            responseObserver.onCompleted();
        } catch (NoStationFoundException nsfe) {
            responseObserver.onError(NOT_FOUND.withDescription(nsfe.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
        List<String> res = hub.locate_station(request.getLat(), request.getLon(), request.getLimiter());
        responseObserver.onNext(LocateStationResponse.newBuilder().addAllStations(res).build());
        responseObserver.onCompleted();
    }

    @Override
    public void bikeUp(BikeUpRequest request, StreamObserver<BikeUpResponse> responseObserver) {
        try {
            hub.bike_up(request.getUserName(), request.getLat(), request.getLon(), request.getStationId());
            responseObserver.onNext(BikeUpResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (InsufficientBalanceException ibe) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(ibe.getMessage()).asRuntimeException());
        } catch (NoUserFoundException nufe) {
            responseObserver.onError(NOT_FOUND.withDescription(nufe.getMessage()).asRuntimeException());
        } catch (NoStationFoundException nsfe) {
            responseObserver.onError(NOT_FOUND.withDescription(nsfe.getMessage()).asRuntimeException());
        } catch (NoBikesAvailableException nbae) {
            responseObserver.onError(RESOURCE_EXHAUSTED.withDescription(nbae.getMessage()).asRuntimeException());
        } catch (TooFarFromStationException tffse) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(tffse.getMessage()).asRuntimeException());
        } catch (AlreadyLiftedBikeException albe) {
            responseObserver.onError(ALREADY_EXISTS.withDescription(albe.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void bikeDown(BikeDownRequest request, StreamObserver<BikeDownResponse> responseObserver) {
        try {
            hub.bike_down(request.getUserName(), request.getLat(), request.getLon(), request.getStationId());
            responseObserver.onNext(BikeDownResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (NoStationFoundException nsfe) {
            responseObserver.onError(NOT_FOUND.withDescription(nsfe.getMessage()).asRuntimeException());
        } catch (NoUserFoundException nufe) {
            responseObserver.onError(NOT_FOUND.withDescription(nufe.getMessage()).asRuntimeException());
        } catch (TooFarFromStationException tffse) {
            responseObserver.onError(INVALID_ARGUMENT.withDescription(tffse.getMessage()).asRuntimeException());
        } catch (NoDocksAvailableException ndae) {
            responseObserver.onError(RESOURCE_EXHAUSTED.withDescription(ndae.getMessage()).asRuntimeException());
        } catch (NoBikeLiftedException nble) {
            responseObserver.onError(NOT_FOUND.withDescription(nble.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void haversineDistance(HaversineDistanceRequest request, StreamObserver<HaversineDistanceResponse> responseObserver) {
        double result = hub.haversineDistance(request.getLat1(), request.getLon1(), request.getLat2(), request.getLon2());
        responseObserver.onNext(HaversineDistanceResponse.newBuilder().setDistance(result).build());
        responseObserver.onCompleted();
    }
}