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
    private final String groupsPath = "/groups";
    private final String usersPath = "/users";

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

    public Mono<Object> getGroups() {
        return kcAuthorizedWebClient
                .get()
                .uri(t -> t.path(groupsPath).build())
                .exchange()
                .flatMap(r -> r.bodyToMono(Object.class));
    }

    public Mono<Object> getUsers(Optional<Boolean> briefRepresentation,
                                 Optional<String> email,
                                 Optional<Integer> first,
                                 Optional<String> firstName,
                                 Optional<String> lastName,
                                 Optional<Integer> max,
                                 Optional<String> search,
                                 Optional<String> username) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        briefRepresentation.ifPresent(briefRepresentationValue -> queryParams.add("briefRepresentation", briefRepresentationValue.toString()));
        email.ifPresent(emailValue -> queryParams.add("email", emailValue));
        first.ifPresent(firstValue -> queryParams.add("first", firstValue.toString()));
        firstName.ifPresent(firstNameValue -> queryParams.add("firstName", firstNameValue));
        lastName.ifPresent(lastNameValue -> queryParams.add("lastName", lastNameValue));
        max.ifPresent(maxValue -> queryParams.add("max", maxValue.toString()));
        search.ifPresent(searchValue -> queryParams.add("search", searchValue));
        username.ifPresent(usernameValue -> queryParams.add("username", usernameValue));

        return kcAuthorizedWebClient
                .get()
                .uri(t -> t
                        .path(usersPath)
                        .queryParams(queryParams)
                        .build())
                .exchange()
                .flatMap(r -> r.bodyToMono(Object.class));
    }
}
