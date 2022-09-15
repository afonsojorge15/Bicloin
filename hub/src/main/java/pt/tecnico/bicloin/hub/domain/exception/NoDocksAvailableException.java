package pt.tecnico.bicloin.hub.domain.exception;

public class NoDocksAvailableException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    public NoDocksAvailableException() {
    }

    @Override
    public String getMessage() {
        return "ERROR: No docks available";
    }
}
