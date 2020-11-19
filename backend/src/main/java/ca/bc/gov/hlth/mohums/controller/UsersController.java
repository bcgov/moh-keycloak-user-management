package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
public class UsersController {

    private final String usersPath = "/users";

    private final WebClientService webClientService;

    public UsersController(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/users")
    public Mono<Object> users(
            @RequestParam Optional<Boolean> briefRepresentation,
            @RequestParam Optional<String> email,
            @RequestParam Optional<Integer> first,
            @RequestParam Optional<String> firstName,
            @RequestParam Optional<String> lastName,
            @RequestParam Optional<Integer> max,
            @RequestParam Optional<String> search,
            @RequestParam Optional<String> username
    ) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        briefRepresentation.ifPresent(briefRepresentationValue -> queryParams.add("briefRepresentation", briefRepresentationValue.toString()));
        email.ifPresent(emailValue -> queryParams.add("email", emailValue));
        first.ifPresent(firstValue -> queryParams.add("first", firstValue.toString()));
        firstName.ifPresent(firstNameValue -> queryParams.add("firstName", firstNameValue));
        lastName.ifPresent(lastNameValue -> queryParams.add("lastName", lastNameValue));
        max.ifPresent(maxValue -> queryParams.add("max", maxValue.toString()));
        search.ifPresent(searchValue -> queryParams.add("search", searchValue));
        username.ifPresent(usernameValue -> queryParams.add("username", usernameValue));

        return webClientService.get(usersPath, queryParams);
    }

    @GetMapping("/users/{id}")
    public Mono<Object> users(@PathVariable String id) {
        String path = usersPath + "/" + id;
        return webClientService.get(path, null);
    }

    @PostMapping(value = "/users")
    public ResponseEntity<Object> createUser(@RequestBody Object body) {
        ClientResponse post = webClientService.post("https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/moh_applications/users", body).block();
        Object responseBody = post.bodyToMono(Object.class).block();
        HttpHeaders httpHeaders = post.headers().asHttpHeaders();
        HttpStatus status = post.statusCode();
        return new ResponseEntity<>(responseBody, httpHeaders, status);
    }

}
