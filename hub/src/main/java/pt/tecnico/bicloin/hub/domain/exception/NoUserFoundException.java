package pt.tecnico.bicloin.hub.domain.exception;

public class NoUserFoundException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    private final String userName;

    public NoUserFoundException(String user_name) {
        this.userName = user_name;
    }

    @Override
    public String getMessage() {
        return "ERROR: No User found with user_name " + getUserName();
    }

    public String getUserName() {
        return userName;
    }
}
