package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

public class ExternalApiCaller {

    private final WebClient webClient;

    public ExternalApiCaller(WebClient webClient) {
        this.webClient = webClient;
    }

    public ResponseEntity<Object> get(String path, MultiValueMap<String, String> queryParams) {
        return webClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange().block().toEntity(Object.class).block();
    }

    public ResponseEntity<List<Object>> getList(String path, MultiValueMap<String, String> queryParams) {
        return webClient
                .get()
                .uri(t -> t
                        .path(path)
                        .queryParams(queryParams)
                        .build())
                .exchange()
                .block().toEntityList(Object.class).block();
    }

    public ResponseEntity<List<Object>> getList(String path) {
        ClientResponse c = webClient
                .get()
                .uri(t -> t.path(path).build())
                .exchange()
                .block();
        return c.toEntityList(Object.class).block();
    }

    public ResponseEntity<Object> post(String path, Object data) {
        return webClient
                .post()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }

    public ResponseEntity<Object> put(String path, Object data) {
        return webClient
                .put()
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }

    public ResponseEntity<Object> put(String path) {
        return webClient
                .put()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntity(Object.class).block();
    }

    public ResponseEntity<Object> delete(String path) {
        return webClient
                .delete()
                .uri(t -> t.path(path).build())
                .exchange()
                .block().toEntity(Object.class).block();
    }

    public ResponseEntity<Object> delete(String path, Object data) {
        return webClient
                .method(HttpMethod.DELETE)
                .uri(t -> t.path(path).build())
                .bodyValue(data)
                .exchange()
                .block().toEntity(Object.class).block();
    }
}
