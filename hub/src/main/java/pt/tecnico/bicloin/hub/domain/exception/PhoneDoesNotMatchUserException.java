package pt.tecnico.bicloin.hub.domain.exception;

public class PhoneDoesNotMatchUserException extends Exception{

    private static final long serialVersionUID = 202104021434L;

    private final String phoneNumber;

    public PhoneDoesNotMatchUserException(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getMessage() {
        return "ERROR: Phone number " + getPhoneNumber() + " does not match User's current phone number";
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
