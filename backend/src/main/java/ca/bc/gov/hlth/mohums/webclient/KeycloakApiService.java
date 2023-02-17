package ca.bc.gov.hlth.mohums.webclient;

import ca.bc.gov.hlth.mohums.model.Group;
import ca.bc.gov.hlth.mohums.model.GroupDescriptionGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KeycloakApiService {

    private final String clientsPath = "/clients";
    private final String usersPath = "/users";
    private final String groupsPath = "/groups";
    private final String identityProviderLinksPath = "/federated-identity";
    private final String userClientRoleMappingPath = "/role-mappings/clients/";

    private final ExternalApiCaller keycloakMohExternalApiCaller;
    private final ExternalApiCaller keycloakMasterExternalApiCaller;

    public KeycloakApiService(@Qualifier("keycloakMohApiCaller") ExternalApiCaller keycloakMohExternalApiCaller, @Qualifier("keycloakMasterApiCaller") ExternalApiCaller keycloakMasterExternalApiCaller) {
        this.keycloakMohExternalApiCaller = keycloakMohExternalApiCaller;
        this.keycloakMasterExternalApiCaller = keycloakMasterExternalApiCaller;
    }

    // Clients
    public ResponseEntity<List<Object>> getClients() {
        return keycloakMohExternalApiCaller.getList(clientsPath);

    }

    public ResponseEntity<Object> getClient(String clientId) {
        String path = clientsPath + "/" + clientId;
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<List<Object>> getClientRoles(String clientId) {
        String path = String.format("%s/%s/roles", clientsPath, clientId);
        return keycloakMohExternalApiCaller.getList(path, null);
    }

    public ResponseEntity<List<Object>> getUsersInRole(String clientId, String roleName,
                                                       MultiValueMap<String, String> queryParams) {
        String path = String.format("%s/%s/roles/%s/users", clientsPath, clientId, roleName);
        return keycloakMohExternalApiCaller.getList(path, queryParams);
    }

    // Groups
    public ResponseEntity<Object> getGroups() {
        ResponseEntity<Object> response = keycloakMohExternalApiCaller.get(groupsPath, null);
        ArrayList<LinkedHashMap> groups = (ArrayList<LinkedHashMap>) response.getBody();
        List<Group> groupList = groups.stream().map(g -> getGroupById(g.get("id").toString())).collect(Collectors.toList());
        return ResponseEntity.of(Optional.of(groupList));
    }

    // Group details
    private Group getGroupById(String groupId) {
        String path = String.format("%s/%s", groupsPath, groupId);
        ResponseEntity<Object> response = keycloakMohExternalApiCaller.get(path, null);
        return GroupDescriptionGenerator.createGroupWithDescription((LinkedHashMap) response.getBody());
    }

    // Users
    public ResponseEntity<List<Object>> getUsers(MultiValueMap<String, String> queryParams) {
        return keycloakMohExternalApiCaller.getList(usersPath, queryParams);
    }

    public ResponseEntity<Object> getUser(String userId) {
        String path = usersPath + "/" + userId;
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> createUser(Object data) {
        return keycloakMohExternalApiCaller.post(usersPath, data);
    }

    public ResponseEntity<Object> updateUser(String userId, Object data) {
        String path = usersPath + "/" + userId;
        return keycloakMohExternalApiCaller.put(path, data);
    }

    public ResponseEntity<Object> getAssignedUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> getAvailableUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/available";
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> getEffectiveUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/composite";
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> addUserClientRole(String userId, String clientId, Object data) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return keycloakMohExternalApiCaller.post(path, data);
    }

    public ResponseEntity<Object> deleteUserClientRole(String userId, String clientId, Object data) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return keycloakMohExternalApiCaller.delete(path, data);
    }

    public ResponseEntity<Object> getUserGroups(String userId) {
        String path = usersPath + "/" + userId + groupsPath;
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> addUserGroups(String userId, String groupId) {
        String path = usersPath + "/" + userId + groupsPath + "/" + groupId;
        return keycloakMohExternalApiCaller.put(path);
    }

    public ResponseEntity<Object> removeUserGroups(String userId, String groupId) {
        String path = usersPath + "/" + userId + groupsPath + "/" + groupId;
        return keycloakMohExternalApiCaller.delete(path);
    }

    // Events
    public ResponseEntity<Object> getEvents(MultiValueMap<String, String> allParams) {
        return keycloakMohExternalApiCaller.get("/events", allParams);
    }

    public ResponseEntity<Object> getAdminEvents(MultiValueMap<String, String> allParams) {
        return keycloakMohExternalApiCaller.get("/admin-events", allParams);
    }

    public ResponseEntity<Object> removeUserIdentityProviderLink(String userId, String identityProvider, String userIdIdpRealm) {
        String path = usersPath + "/" + userId + identityProviderLinksPath + "/" + identityProvider;
        ResponseEntity<Object> deleteIDPLinkResponse = keycloakMohExternalApiCaller.delete(path);
        if (identityProviderLinkDeletedSuccessfully(deleteIDPLinkResponse)) {
            return deleteUserFromIdpRealm(userIdIdpRealm, identityProvider);
        } else {
            return deleteIDPLinkResponse;
        }
    }

    private ResponseEntity<Object> deleteUserFromIdpRealm(String userIdIdpRealm, String identityProvider) {
        String path = "/" + identityProvider + usersPath + "/" + userIdIdpRealm;
        return keycloakMasterExternalApiCaller.delete(path);
    }

    private boolean identityProviderLinkDeletedSuccessfully(ResponseEntity<Object> deleteIdentityProviderLinkResponse) {
        return deleteIdentityProviderLinkResponse.getStatusCode().equals(HttpStatus.NO_CONTENT);
    }
}
