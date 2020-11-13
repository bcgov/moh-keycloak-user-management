package ca.bc.gov.hlth.mohums.webclient;

import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class WebClientService {

    private final String clientsPath = "/clients";

    private final WebClient kcAuthorizedWebClient;

    public WebClientService(WebClient kcAuthorizedWebClient) {
        this.kcAuthorizedWebClient = kcAuthorizedWebClient;
    }

    public Mono<Object> getClients(List<String> authorizedClients) {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t.path(clientsPath).build())
                .exchange()
                .flatMap(r -> r
                    .bodyToFlux(ClientRepresentation.class)
                    .filter(c -> authorizedClients.contains(((ClientRepresentation) c).getClientId().toLowerCase()))
                    .collectList()
                );
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
}
