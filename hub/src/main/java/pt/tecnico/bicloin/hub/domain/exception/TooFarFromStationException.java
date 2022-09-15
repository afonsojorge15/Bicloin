package pt.tecnico.bicloin.hub.domain.exception;

public class TooFarFromStationException extends Exception {

    private static final long serialVersionUID = 202104021434L;

    public TooFarFromStationException() {
    }

    @Override
    public String getMessage() {
        return "ERROR: Too far from station (min 200m)";
    }
}
