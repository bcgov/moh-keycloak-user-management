package ca.bc.gov.hlth.mohums.model;

import org.springframework.http.HttpStatus;

public class BulkRemovalResponse {
    /**
     * Object body is an actual response from Keycloak API. If the request is successful the body will be empty.
     * In case of failure an object wth the following structure will be passed: {body: error: {....}}
     */
    private Object body;
    private int statusCodeValue;
    private HttpStatus statusCode;
    /**
     * userId is added to a response, so that it can be associated with a particular user
     */
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
