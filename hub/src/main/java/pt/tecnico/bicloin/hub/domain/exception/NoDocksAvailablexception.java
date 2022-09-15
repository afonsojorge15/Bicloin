package pt.tecnico.bicloin.hub.domain.exception;

public class NoDocksAvailablexception extends Exception{

    private static final long serialVersionUID = 202104021434L;

    public NoDocksAvailablexception() {
    }

    @Override
    public String getMessage() {
        return "ERROR: No docks available";
    }
}
