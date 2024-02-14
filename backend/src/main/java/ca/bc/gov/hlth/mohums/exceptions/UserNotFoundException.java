package ca.bc.gov.hlth.mohums.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super (message);
    }
}
