package pt.tecnico.bicloin.hub.domain.exception;

public class AlreadyLiftedBikeException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    public AlreadyLiftedBikeException() {
    }

    @Override
    public String getMessage() {
        return "ERROR: Cannot lift 2 bikes at the same time";
    }
}
