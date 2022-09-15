package pt.tecnico.bicloin.hub.domain.exception;

public class NoBikeLiftedException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    public NoBikeLiftedException() {
    }

    @Override
    public String getMessage() {
        return "ERROR: No bike lifted";
    }
}
