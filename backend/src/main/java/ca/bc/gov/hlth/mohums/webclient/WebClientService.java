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

    public Flux<Object> getClients() {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t.path(clientsPath).build())
                .exchange()
                .flatMapMany(r -> r.bodyToFlux(Object.class));
    }

    public Mono<Object> getClient(String clientId) {
        String path = clientsPath + "/" + clientId;
        return get(path, null);
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
    
    public Mono<Object> get(String path, MultiValueMap<String, String> queryParams) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange()
                .flatMap(r -> r.bodyToMono(Object.class));
    }

    public Mono<ClientResponse> post(String path, Object data) {
        return kcAuthorizedWebClient.post()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange();
    }
}