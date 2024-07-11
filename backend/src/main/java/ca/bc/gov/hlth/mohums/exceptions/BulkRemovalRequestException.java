package ca.bc.gov.hlth.mohums.exceptions;

public class BulkRemovalRequestException extends RuntimeException{
    public BulkRemovalRequestException(String message){
        super(message);
    }
}
