package ca.bc.gov.hlth.mohums.model;

import org.springframework.http.HttpStatus;

public class BulkRemovalResponse {
    private Object body;
    private int statusCodeValue;
    private HttpStatus statusCode;
    private String userId;

    public BulkRemovalResponse(Object body, int statusCodeValue, HttpStatus statusCode, String userId) {
        this.body = body;
        this.statusCodeValue = statusCodeValue;
        this.statusCode = statusCode;
        this.userId = userId;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public int getStatusCodeValue() {
        return statusCodeValue;
    }

    public void setStatusCodeValue(int statusCodeValue) {
        this.statusCodeValue = statusCodeValue;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
