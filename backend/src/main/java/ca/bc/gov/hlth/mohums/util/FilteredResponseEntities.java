package ca.bc.gov.hlth.mohums.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

/**
 * 
 */
public class FilteredResponseEntities {
    
    private static final HttpStatus OK_STATUS_CODE = HttpStatus.OK;

    private final HttpStatus sourceStatusCode;
    private final Collection<Object> entities;
    
    public static FilteredResponseEntities of(final ResponseEntity<?> responseEntity) {
        final HttpStatus statusCode = responseEntity.getStatusCode();
        final FilteredResponseEntities objectResponseEntities;

        if (statusCode.isError()) {
            objectResponseEntities = new FilteredResponseEntities(statusCode);
        }
        else {
            objectResponseEntities = new FilteredResponseEntities(responseEntity.getBody());
        }
        
        return objectResponseEntities;
    }
    
    FilteredResponseEntities(final HttpStatus statusCode) {
        this.sourceStatusCode = statusCode;
        this.entities = Collections.emptyList();
    }
    
    FilteredResponseEntities(final Object responseBody) {
        this.sourceStatusCode = OK_STATUS_CODE;
        this.entities = extractEntities(responseBody);
    }
    
    private static Collection<Object> extractEntities(final Object responseBody) {
        final Collection<Object> entities;
        
        if (Objects.isNull(responseBody)) {
            entities = Collections.emptyList();
        }
        else if (responseBody instanceof Collection) {
            final Collection<?> collection = (Collection) responseBody;
            entities = collection.stream().filter(Objects::nonNull).collect(Collectors.toList());
        }
        else {
            entities = Collections.singleton(responseBody);
        }
        
        return entities;
    }

    public FilteredResponseEntities filter(final Predicate<Object> filter) {
        this.entities.removeIf(filter.negate());
        
        return this;
    }

    public ResponseEntity<Object> toResponseEntity() {
        final ResponseEntity<Object> responseEntity;
        
        if (sourceStatusCode == OK_STATUS_CODE) {
            if (CollectionUtils.isEmpty(entities)) {
                // REVIEW - Is the frontend expecting a 404 (NOT_FOUND) or a 204 (NO_CONTENT)
                // HTTP status code when no results are found?
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
