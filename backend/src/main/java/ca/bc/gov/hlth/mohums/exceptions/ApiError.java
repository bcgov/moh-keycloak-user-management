package ca.bc.gov.hlth.mohums.exceptions;

import org.springframework.http.HttpStatus;

public class ApiError {

    private HttpStatus httpStatus;
    private String error;

    public ApiError(HttpStatus httpStatus, String error) {
        this.httpStatus = httpStatus;
        this.error = error;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getError() {
        return error;
    }
}
