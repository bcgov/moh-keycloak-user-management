package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono<Object> getClient(String clientId) {
        String path = clientsPath + "/" + clientId;
        return get(path, null);
    }

    // Groups
    public Mono<Object> getGroups() {
        String path = "/groups";
        return get(path, null);
    }

    // Users
    public Mono<Object> getUsers(MultiValueMap<String, String> queryParams) {
        return get(usersPath, queryParams);
    }

    public Mono<Object> getUser(String userId) {
        String path = usersPath + "/" + userId;
        return get(path, null);
    }

    public Mono<ClientResponse> createUser(Object data) {
        return post(usersPath, data);
    }

    public Mono<ClientResponse> updateUser(String userId, Object data) {
        String path = usersPath + "/" + userId;
        return put(path, data);
    }

    public Mono<Object> getAssignedUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return get(path, null);
    }

    public Mono<Object> getAvailableUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/available";
        return get(path, null);
    }

    public Mono<Object> getEffectiveUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/composite";
        return get(path, null);
    }

    // Private Webclient methods
    private Mono<Object> get(String path, MultiValueMap<String, String> queryParams) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange()
                .flatMap(r -> r.bodyToMono(Object.class));
    }

    private Flux<Object> getFlux(String path) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t.path(path).build())
                .exchange()
                .flatMapMany(r -> r.bodyToFlux(Object.class));
    }

    private Mono<ClientResponse> post(String path, Object data) {
        return kcAuthorizedWebClient
                .post()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange();
    }

    private Mono<ClientResponse> put(String path, Object data) {
        return kcAuthorizedWebClient
                .put()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange();
    }
}
