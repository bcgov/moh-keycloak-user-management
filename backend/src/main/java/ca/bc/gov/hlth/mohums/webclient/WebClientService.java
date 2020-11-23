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
    private final String userClientRoleMappingPath = "/roles-mappings/clients/";

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

    public Flux<Object> getAssignedUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId;
        return getFlux(path, null);
    }

    public Flux<Object> getAvailableUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/available";
        return getFlux(path, null);
    }

    public Flux<Object> getEffectiveUserClientRoleMappings(String userId, String clientId) {
        String path = usersPath + "/" + userId + userClientRoleMappingPath + clientId + "/composite";
        return getFlux(path, null);
    }

    private Flux<Object> getFlux(String path, MultiValueMap<String, String> queryParams) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange()
                .flatMapMany(r -> r.bodyToFlux(Object.class));
    }

    //TODO this should be a getMono and private
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