package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class WebClientService {

    private final String clientsPath = "/clients";
    private final String usersPath = "/users";
    private final String userClientRoleMappingPath = "/role-mappings/clients/";

    private final WebClient kcAuthorizedWebClient;

    public WebClientService(WebClient kcAuthorizedWebClient) {
        this.kcAuthorizedWebClient = kcAuthorizedWebClient;
    }

    // Clients
    public Flux<Object> getClients() {
        return getFlux(clientsPath);

    }

    public ResponseEntity<Object> getClient(String clientId) {
        String path = clientsPath + "/" + clientId;
        return get(path, null);
    }

    // Groups
    public ResponseEntity<Object> getGroups() {
        String path = "/groups";
        return get(path, null);
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

    private ResponseEntity<Object> get(String path, MultiValueMap<String, String> queryParams) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange().block().toEntity(Object.class).block();
    }

    private Flux<Object> getFlux(String path) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t.path(path).build())
                .exchange()
                .flatMapMany(r -> r.bodyToFlux(Object.class));
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
}
