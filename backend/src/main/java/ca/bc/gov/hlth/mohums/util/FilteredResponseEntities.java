package ca.bc.gov.hlth.mohums.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

/**
 * 
 */
public class FilteredResponseEntities {
    
    private static final HttpStatus OK_STATUS_CODE = HttpStatus.OK;

    private final HttpStatus sourceStatusCode;
    private final List<Object> entities;
    
    public static FilteredResponseEntities of(final ResponseEntity<List<Object>> responseEntities) {
        final HttpStatus statusCode = responseEntities.getStatusCode();
        final FilteredResponseEntities objectResponseEntities;

        if (statusCode == OK_STATUS_CODE) {
            objectResponseEntities = new FilteredResponseEntities(responseEntities.getBody());
        }
        else {
            objectResponseEntities = new FilteredResponseEntities(statusCode);
        }
        
        return objectResponseEntities;
    }
    
    FilteredResponseEntities(final HttpStatus statusCode) {
        this.sourceStatusCode = statusCode;
        this.entities = Collections.emptyList();
    }
    
    FilteredResponseEntities(final List<Object> allUsers) {
        this.sourceStatusCode = OK_STATUS_CODE;
        
        if (CollectionUtils.isEmpty(allUsers)) {
            this.entities = Collections.emptyList();
        }
        else {
            this.entities = new ArrayList<>(allUsers);
        }
    }

    public FilteredResponseEntities filter(final Predicate<Object> filter) {
        this.entities.removeIf(Predicate.isEqual(null).or(filter.negate()));
        
        return this;
    }

    public ResponseEntity<Object> toResponseEntity() {
        final ResponseEntity<Object> responseEntity;
        
        if (sourceStatusCode == OK_STATUS_CODE) {
            if (CollectionUtils.isEmpty(entities)) {
                // REVIEW - Is the frontend expecting a 404 HTTP status when no results are found?
                responseEntity = ResponseEntity.notFound().build();
            }
            else {
                responseEntity = ResponseEntity.ok(entities);
            }
        }
        else {
            responseEntity = ResponseEntity.status(sourceStatusCode).build();
        }
        
        return responseEntity;
    }

}
