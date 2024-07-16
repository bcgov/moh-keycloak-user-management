package ca.bc.gov.hlth.mohums.model;

import java.util.List;
import java.util.Map;

public class BulkRemovalRequest {

    private Map<String, List<Object>> userRolesForRemoval;

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
