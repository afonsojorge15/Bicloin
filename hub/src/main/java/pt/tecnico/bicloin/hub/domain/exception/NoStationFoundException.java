package pt.tecnico.bicloin.hub.domain.exception;

public class NoStationFoundException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    private final String id;

    public NoStationFoundException(String id) {
        this.id = id;
    }

    @Override
    public String getMessage() {
        return "ERROR: No Station found with abrev " + getId();
    }

    public String getId() {
        return id;
    }
}
