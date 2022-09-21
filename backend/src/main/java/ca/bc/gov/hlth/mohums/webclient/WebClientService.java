package ca.bc.gov.hlth.mohums.webclient;

import ca.bc.gov.hlth.mohums.model.Group;
import ca.bc.gov.hlth.mohums.model.GroupDescriptionGenerator;
import net.minidev.json.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WebClientService {

    private final String clientsPath = "/clients";
    private final String usersPath = "/users";
    private final String groupsPath = "/groups";
    private final String identityProviderLinksPath = "/federated-identity";
    private final String userClientRoleMappingPath = "/role-mappings/clients/";

    private final WebClient kcMohAuthorizedWebClient;
    private final WebClient kcMasterAuthorizedWebClient;

    private static final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);

    @Value("${keycloak-moh.organizations-api-url}")
    private String organizationsApiBaseUrl;

    public WebClientService(WebClient kcMohAuthorizedWebClient, WebClient kcMasterAuthorizedWebClient) {
        this.kcMohAuthorizedWebClient = kcMohAuthorizedWebClient;
        this.kcMasterAuthorizedWebClient = kcMasterAuthorizedWebClient;
    }

    public ResponseEntity<List<Object>> getOrganizations() {
        WebClient orgClient = kcMohAuthorizedWebClient.mutate()
                .baseUrl(organizationsApiBaseUrl)
                .build();
        ResponseEntity<List<Object>> response =  orgClient
                .get()
                .uri(t -> t.path("/organizations").build())
                .exchange()
                .block().toEntityList(Object.class).block();
        return response;
    }

    // Clients
    public ResponseEntity<List<Object>> getClients() {
        return getList(clientsPath);

    }

    public ResponseEntity<Object> getClient(String clientId) {
        String path = clientsPath + "/" + clientId;
        return get(path, null);
    }

    public ResponseEntity<List<Object>> getClientRoles(String clientId) {
        String path = String.format("%s/%s/roles", clientsPath, clientId);
        return getList(path, null);
    }

    public ResponseEntity<List<Object>> getUsersInRole(String clientId, String roleName,
                                                       MultiValueMap<String, String> queryParams) {
        String path = String.format("%s/%s/roles/%s/users", clientsPath, clientId, roleName);
        return getList(path, queryParams);
    }

    // Groups
    public ResponseEntity<Object> getGroups() {
        ResponseEntity<Object> response = get(groupsPath, null);
        ArrayList<LinkedHashMap> groups = (ArrayList<LinkedHashMap>) response.getBody();
        List<Group> groupList = groups.stream().map(g -> getGroupById(g.get("id").toString())).collect(Collectors.toList());
        return ResponseEntity.of(Optional.of(groupList));
    }

    // Group details
    private Group getGroupById(String groupId) {
        String path = String.format("%s/%s", groupsPath, groupId);
        ResponseEntity<Object> response = get(path, null);
        return GroupDescriptionGenerator.createGroupWithDescription((LinkedHashMap) response.getBody());
    }

    // Users
    public ResponseEntity<List<Object>> getUsers(MultiValueMap<String, String> queryParams) {
        return getList(usersPath, queryParams);
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

    // Events
    public ResponseEntity<Object> getEvents(MultiValueMap<String, String> allParams) {
        return get("/events", allParams);
    }

    public ResponseEntity<Object> getAdminEvents(MultiValueMap<String, String> allParams) {
        return get("/admin-events", allParams);
    }

    public ResponseEntity<Object> removeUserIdentityProviderLink(String userId, String identityProvider, String userIdIdpRealm) {
        String path = usersPath + "/" + userId + identityProviderLinksPath + "/" + identityProvider;
        ResponseEntity<Object> deleteIDPLinkResponse = delete(path);
        if (identityProviderLinkDeletedSuccessfully(deleteIDPLinkResponse)) {
            return deleteUserFromIdpRealm(userIdIdpRealm, identityProvider);
        } else {
            return deleteIDPLinkResponse;
        }
    }

    private ResponseEntity<Object> deleteUserFromIdpRealm(String userIdIdpRealm, String identityProvider) {
        String path = "/" + identityProvider + usersPath + "/" + userIdIdpRealm;
        return kcMasterAuthorizedWebClient
                .delete()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private boolean identityProviderLinkDeletedSuccessfully(ResponseEntity<Object> deleteIdentityProviderLinkResponse) {
        return deleteIdentityProviderLinkResponse.getStatusCode().equals(HttpStatus.NO_CONTENT);
    }

    private ResponseEntity<Object> get(String path, MultiValueMap<String, String> queryParams) {
        ClientResponse o = kcMohAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange().block();
        return o.toEntity(Object.class).block();
    }

    private ResponseEntity<List<Object>> getList(String path, MultiValueMap<String, String> queryParams) {
        return kcMohAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange()
                .block().toEntityList(Object.class).block();
    }

    private ResponseEntity<List<Object>> getList(String path) {
        return kcMohAuthorizedWebClient
                .get()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntityList(Object.class).block();
    }

    private ResponseEntity<Object> post(String path, Object data) {
        return kcMohAuthorizedWebClient
                .post()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> put(String path, Object data) {
        return kcMohAuthorizedWebClient
                .put()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> put(String path) {
        return kcMohAuthorizedWebClient
                .put()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> delete(String path) {
        return kcMohAuthorizedWebClient
                .delete()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntity(Object.class).block();
    }

    private ResponseEntity<Object> delete(String path, Object data) {
        return kcMohAuthorizedWebClient
                .method(HttpMethod.DELETE)
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }


}
