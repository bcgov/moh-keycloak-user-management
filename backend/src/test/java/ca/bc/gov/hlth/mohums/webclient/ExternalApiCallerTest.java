package ca.bc.gov.hlth.mohums.webclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.BasicJsonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalApiCallerTest {
    public static MockWebServer mockWebServer;
    private static MultiValueMap<String, String> queryParamsMap;
    private ExternalApiCaller externalApiCaller;
    private final BasicJsonTester basicJsonTester = new BasicJsonTester(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();




    @BeforeAll
    static void setUp() throws IOException {
        createAndStartMockWebServer();
        createQueryParamsMap();
    }

    private static void createQueryParamsMap() {
        queryParamsMap = new LinkedMultiValueMap<String, String>();
        queryParamsMap.add("key", "value");
        queryParamsMap.add("otherKey", "otherValue");
    }

    private static void createAndStartMockWebServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @BeforeEach
    void setupMockWebServer() throws IOException {
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        externalApiCaller = new ExternalApiCaller(WebClient.create(baseUrl));
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }


    @Test
    void get() throws InterruptedException, JsonProcessingException {
        String jsonObject = "{\"response\": \"abc\"}";

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody(jsonObject)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        );
        ResponseEntity<Object> response =  externalApiCaller.get("/user", queryParamsMap);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(queryParamsMap.keySet().containsAll(recordedRequest.getRequestUrl().queryParameterNames())).isTrue();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(objectResponseIsCorrect(response, jsonObject));
    }

    @Test
    void getUnsuccessful() throws InterruptedException, JsonProcessingException {
        String jsonObject = "{\"error_code\": \"403\", \"error_message\": \"Access forbidden\"}";

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(403)
                        .setBody(jsonObject)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        );
        ResponseEntity<Object> response =  externalApiCaller.get("/user", queryParamsMap);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertThat(queryParamsMap.keySet().containsAll(recordedRequest.getRequestUrl().queryParameterNames())).isTrue();
        assertEquals("GET", recordedRequest.getMethod());
        assertEquals(response.getStatusCode(), HttpStatus.FORBIDDEN);
        assertTrue(objectResponseIsCorrect(response, jsonObject));
    }

    @Test
    void post() throws InterruptedException, JsonProcessingException {
        String jsonObject = "{\"response\": \"abc\"}";

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody(jsonObject)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        );
        ResponseEntity<Object> response =  externalApiCaller.post("/user", jsonObject);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(objectResponseIsCorrect(response, jsonObject));
    }

    @Test
    void put() throws InterruptedException, JsonProcessingException {
        String jsonObject = "{\"response\": \"abc\"}";

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody(jsonObject)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        );
        ResponseEntity<Object> response =  externalApiCaller.put("/user");
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("PUT", recordedRequest.getMethod());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(objectResponseIsCorrect(response, jsonObject));
    }

    @Test
    void putWithBody() throws InterruptedException, JsonProcessingException {
        String jsonObject = "{\"response\": \"abc\"}";

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody(jsonObject)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        );
        ResponseEntity<Object> response =  externalApiCaller.put("/user", jsonObject);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        JsonContent<Object> bodySentWithRequest = basicJsonTester.from(recordedRequest.getBody().readUtf8());


        assertEquals("PUT", recordedRequest.getMethod());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertThat(bodySentWithRequest).extractingJsonPathStringValue("$.response").isEqualTo("abc");
        assertTrue(objectResponseIsCorrect(response, jsonObject));
    }

    @Test
    void delete() throws InterruptedException, JsonProcessingException {
        String jsonObject = "{\"response\": \"abc\"}";

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody(jsonObject)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        );
        ResponseEntity<Object> response =  externalApiCaller.delete("/user");
        RecordedRequest recordedRequest = mockWebServer.takeRequest();

        assertEquals("DELETE", recordedRequest.getMethod());
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(objectResponseIsCorrect(response, jsonObject));
    }

    @Test
    void deleteWithBody() throws InterruptedException, JsonProcessingException {
        String jsonObject = "{\"response\": \"abc\"}";

        mockWebServer.enqueue(
                new MockResponse().setResponseCode(200)
                        .setBody(jsonObject)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)

        );
        ResponseEntity<Object> response =  externalApiCaller.delete("/user", jsonObject);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        JsonContent<Object> bodySentWithRequest = basicJsonTester.from(recordedRequest.getBody().readUtf8());

        assertEquals("DELETE", recordedRequest.getMethod());
        assertThat(bodySentWithRequest).extractingJsonPathStringValue("$.response").isEqualTo("abc");
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertTrue(objectResponseIsCorrect(response, jsonObject));
    }

    private boolean objectResponseIsCorrect(ResponseEntity<Object> apiResponse, String mockedJsonResponse) throws JsonProcessingException {
        LinkedHashMap responseBody = (LinkedHashMap) apiResponse.getBody();
        Map<String, String> mockedResponseAsMap = convertRawJsonToMap(mockedJsonResponse);
        return responseBody.equals(mockedResponseAsMap);
    }

    private Map<String, String> convertRawJsonToMap(String rawJson) throws JsonProcessingException {
        return mapper.readValue(rawJson, Map.class);
    }
}