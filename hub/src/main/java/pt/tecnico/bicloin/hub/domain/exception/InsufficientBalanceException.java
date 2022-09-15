package pt.tecnico.bicloin.hub.domain.exception;

public class InsufficientBalanceException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    public InsufficientBalanceException() {
    }

    @Override
    public String getMessage() {
        return "ERROR: Insufficient Bicloins";
    }
}
