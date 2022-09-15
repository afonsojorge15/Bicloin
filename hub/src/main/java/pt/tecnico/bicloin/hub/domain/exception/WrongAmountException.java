package pt.tecnico.bicloin.hub.domain.exception;

public class WrongAmountException extends Exception {

    private static final long serialVersionUID = 202104021434L;

    public WrongAmountException() {
    }

    @Override
    public String getMessage() {
        return "ERROR: Deposit amount must be between 1 and 20 euros";
    }
}
