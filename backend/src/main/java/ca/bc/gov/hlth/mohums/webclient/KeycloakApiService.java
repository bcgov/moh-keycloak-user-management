package ca.bc.gov.hlth.mohums.webclient;

import ca.bc.gov.hlth.mohums.model.Group;
import ca.bc.gov.hlth.mohums.model.GroupDescriptionGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final String userClientRoleMappingPath = "/role-mappings/clients/";

    private final ExternalApiCaller externalApiCaller;

    public KeycloakApiService(@Qualifier("keycloakApiCaller") ExternalApiCaller externalApiCaller) {
        this.externalApiCaller = externalApiCaller;
    }

    // Clients
    public ResponseEntity<List<Object>> getClients() {
        return externalApiCaller.getList(clientsPath);

    }

    public ResponseEntity<Object> getClient(String clientId) {
        String path = clientsPath + "/" + clientId;
        return externalApiCaller.get(path, null);
    }

    public ResponseEntity<List<Object>> getClientRoles(String clientId) {
        String path = String.format("%s/%s/roles", clientsPath, clientId);
        return externalApiCaller.getList(path, null);
    }

    public ResponseEntity<List<Object>> getUsersInRole(String clientId, String roleName,
                                                       MultiValueMap<String, String> queryParams) {
        String path = String.format("%s/%s/roles/%s/users", clientsPath, clientId, roleName);
        return externalApiCaller.getList(path, queryParams);
    }

    // Groups
    public ResponseEntity<Object> getGroups() {
        ResponseEntity<Object> response = externalApiCaller.get(groupsPath, null);
        ArrayList<LinkedHashMap> groups = (ArrayList<LinkedHashMap>) response.getBody();
        List<Group> groupList = groups.stream().map(g -> getGroupById(g.get("id").toString())).collect(Collectors.toList());
        return ResponseEntity.of(Optional.of(groupList));
    }

    // Group details
    private Group getGroupById(String groupId) {
        String path = String.format("%s/%s", groupsPath, groupId);
        ResponseEntity<Object> response = externalApiCaller.get(path, null);
        return GroupDescriptionGenerator.createGroupWithDescription((LinkedHashMap) response.getBody());
    }

    // Users
    public ResponseEntity<List<Object>> getUsers(MultiValueMap<String, String> queryParams) {
        return externalApiCaller.getList(usersPath, queryParams);
    }

    public ResponseEntity<Object> getUser(String userId) {
        String path = usersPath + "/" + userId;
        return externalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> createUser(Object data) {
        return externalApiCaller.post(usersPath, data);
    }

    public ResponseEntity<Object> updateUser(String userId, Object data) {
        String path = usersPath + "/" + userId;
        return externalApiCaller.put(path, data);
    }

    public ResponseEntity<Object> getAssignedUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return externalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> getAvailableUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/available";
        return externalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> getEffectiveUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/composite";
        return externalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> addUserClientRole(String userId, String clientId, Object data) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return externalApiCaller.post(path, data);
    }

    public ResponseEntity<Object> deleteUserClientRole(String userId, String clientId, Object data) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return externalApiCaller.delete(path, data);
    }

    public ResponseEntity<Object> getUserGroups(String userId) {
        String path = usersPath + "/" + userId + groupsPath;
        return externalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> addUserGroups(String userId, String groupId) {
        String path = usersPath + "/" + userId + groupsPath + "/" + groupId;
        return externalApiCaller.put(path);
    }

    public ResponseEntity<Object> removeUserGroups(String userId, String groupId) {
        String path = usersPath + "/" + userId + groupsPath + "/" + groupId;
        return externalApiCaller.delete(path);
    }

    // Events
    public ResponseEntity<Object> getEvents(MultiValueMap<String, String> allParams) {
        return externalApiCaller.get("/events", allParams);
    }

    public ResponseEntity<Object> getAdminEvents(MultiValueMap<String, String> allParams) {
        return externalApiCaller.get("/admin-events", allParams);
    }
}
