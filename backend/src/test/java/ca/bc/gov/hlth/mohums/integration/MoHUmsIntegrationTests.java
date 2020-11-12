package ca.bc.gov.hlth.mohums.integration;

import ca.bc.gov.hlth.mohums.MoHUmsApplication;
import ca.bc.gov.hlth.mohums.controller.GroupController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
public class MoHUmsIntegrationTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void openApiNoJwtOk() throws Exception {
        webTestClient
                .get()
                .uri("/docs/api-docs")
                .exchange()
                .expectStatus().isOk(); //HTTP 200
    }

    @Test
    public void groupsNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/groups")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void usersNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/users")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }

    @Test
    public void clientsNoJwtUnauthorized() throws Exception {
        webTestClient
                .get()
                .uri("/clients")
                .exchange()
                .expectStatus().isUnauthorized(); //HTTP 401
    }
}
