package pt.tecnico.bicloin.hub.domain.exception;

public class NoBikesAvailableException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    public NoBikesAvailableException() {
    }

    @Override
    public String getMessage() {
        return "ERROR: No bikes available";
    }
}
