package ca.bc.gov.hlth.mohums.model;

import java.util.List;
import java.util.Map;

public class BulkRemovalRequest {

    /***
     * Map of userId, list of roleRepresentation objects
     * In the most basic form the role representation has fields as follows (subject to change, please refer to Keycloak documentation):
     * id, name, composite(boolean), clientRole(boolean), containerId(GUID of the client that defines the role)
     */
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
