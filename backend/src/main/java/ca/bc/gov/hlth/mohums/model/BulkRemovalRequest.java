package ca.bc.gov.hlth.mohums.model;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

public class BulkRemovalRequest {

    @Valid
    private Map<@NotBlank(message = "my message") String, @NotEmpty(message="my message 2") List<Object>> userRolesForRemoval;

    public BulkRemovalRequest(Map<String, List<Object>> userRolesForRemoval) {
        this.userRolesForRemoval = userRolesForRemoval;
    }

    public BulkRemovalRequest() {
    }

    public Map<String, List<Object>> getUserRolesForRemoval() {
        return userRolesForRemoval;
    }

    public void setUserRolesForRemoval(Map<String, List<Object>> userRolesForRemoval) {
        this.userRolesForRemoval = userRolesForRemoval;
    }
}
