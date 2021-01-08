package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class WebClientService {

    private final String clientsPath = "/clients";
    private final String usersPath = "/users";
    private final String groupsPath = "/groups";
    private final String userClientRoleMappingPath = "/role-mappings/clients/";

    private final WebClient kcAuthorizedWebClient;

    public WebClientService(WebClient kcAuthorizedWebClient) {
        this.kcAuthorizedWebClient = kcAuthorizedWebClient;
    }

    // Clients
    public ResponseEntity<List<Object>> getClients() {
        return getList(clientsPath);

    }

    public ResponseEntity<Object> getClient(String clientId) {
        String path = clientsPath + "/" + clientId;
        return get(path, null);
    }

    // Groups
    public ResponseEntity<Object> getGroups() {
        return get(groupsPath, null);
    }

    // Users
    public ResponseEntity<Object> getUsers(MultiValueMap<String, String> queryParams) {
        return get(usersPath, queryParams);
    }

    public ResponseEntity<Object> getUser(String userId) {
        String path = usersPath + "/" + userId;
        return get(path, null);
    }

    public ResponseEntity<Object> createUser(Object data) {
        return post(usersPath, data);
    }

    public ResponseEntity<Object> updateUser(String userId, Object data) {
        String path = usersPath + "/" + userId;
        return put(path, data);
    }

    public ResponseEntity<Object> getAssignedUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return get(path, null);
    }

    public ResponseEntity<Object> getAvailableUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/available";
        return get(path, null);
    }

    public ResponseEntity<Object> getEffectiveUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/composite";
        return get(path, null);
    }

    public ResponseEntity<Object> addUserClientRole(String userId, String clientId, Object data) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return post(path, data);
    }

    public ResponseEntity<Object> deleteUserClientRole(String userId, String clientId, Object data) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return delete(path, data);
    }

    public ResponseEntity<Object> getUserGroups(String userId) {
        String path = usersPath + "/" + userId + groupsPath;
        return get(path, null);
    }

    public ResponseEntity<Object> addUserGroups(String userId, String groupId) {
        String path = usersPath + "/" + userId + groupsPath + "/" + groupId;
        return put(path);
    }

    public ResponseEntity<Object> removeUserGroups(String userId, String groupId) {
        String path = usersPath + "/" + userId + groupsPath + "/" + groupId;
        return delete(path);
    }

    private ResponseEntity<Object> get(String path, MultiValueMap<String, String> queryParams) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange().block().toEntity(Object.class).block();
    }

    private ResponseEntity<List<Object>> getList(String path) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntityList(Object.class).block();
    }

    private ResponseEntity<Object> post(String path, Object data) {
        return kcAuthorizedWebClient
                .post()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> put(String path, Object data) {
        return kcAuthorizedWebClient
                .put()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> put(String path) {
        return kcAuthorizedWebClient
                .put()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> delete(String path) {
        return kcAuthorizedWebClient
                .delete()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> delete(String path, Object data) {
        return kcAuthorizedWebClient
                .method(HttpMethod.DELETE)
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }
}
