package ca.bc.gov.hlth.mohums.exceptions;

public class HttpUnauthorizedException extends RuntimeException {

    public HttpUnauthorizedException(String message) {
        super (message);
    }
}
