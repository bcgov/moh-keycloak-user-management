package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {

    private final String groupsPath = "/groups";

    private final WebClient kcAuthorizedWebClient;

    public WebClientService(WebClient kcAuthorizedWebClient) {
        this.kcAuthorizedWebClient = kcAuthorizedWebClient;
    }

    public Mono<Object> getGroups() {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t.path(groupsPath).build())
                .exchange()
                .flatMap(r -> r.bodyToMono(Object.class));
    }
}
