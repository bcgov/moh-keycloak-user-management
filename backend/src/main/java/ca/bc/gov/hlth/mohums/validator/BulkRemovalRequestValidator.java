package ca.bc.gov.hlth.mohums.validator;

import ca.bc.gov.hlth.mohums.model.BulkRemovalRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BulkRemovalRequestValidator {

    public Optional<String> validateBulkRemovalRequest(BulkRemovalRequest bulkRemovalRequest) {
        if (bulkRemovalRequest.getUserRolesForRemoval() == null) {
            return Optional.of("UserRolesForRemoval cannot be null");
        }
        if (bulkRemovalRequest.getUserRolesForRemoval().isEmpty()) {
            return Optional.of("UserRolesForRemoval cannot be empty");
        }
        for (Map.Entry<String, List<Object>> entry : bulkRemovalRequest.getUserRolesForRemoval().entrySet()) {
            if (entry.getKey() == null || entry.getKey().isEmpty()) {
                return Optional.of("User ID cannot be null or empty");
            }
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                return Optional.of("List of roles to remove cannot be null or empty");
            }
        }
        return Optional.empty();
    }
}
