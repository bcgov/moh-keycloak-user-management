package ca.bc.gov.hlth.mohums.integration;

import java.io.IOException;
import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import ca.bc.gov.hlth.mohums.model.UserPayee;
import net.minidev.json.parser.ParseException;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MspDirectApiIntegrationTests {

    private static final String PAYEE_PATH = "/payee-mapping";

    @Value("${spring.security.oauth2.client.registration.keycloak-moh.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.keycloak-moh.client-secret}")
    private String clientSecret;

    @Value("${keycloak-moh.mspdirect-api-url}")
    private String mspDirectApiBaseUrl;

    @Autowired
    private WebTestClient payeeApiWebTestClient;

    @Autowired
    private IntegrationTestsUtils integrationTestsUtils;

    private String jwt;

    /** This is mspdirect_payee_test in Dev. This technically doesn't require a real account
     *  since MSP Direct doesn't validate the id. So tests will continue to function if the user
     *  is removed.*/
    private String testClientId = "2ee882b4-53b0-408a-8ed7-fef2a15a98d0";
    
    private String nonexistentClientId = "1b6c8a7e-f1e1-4ad5-be99-fd62c45ba482";

    @BeforeAll
    public void getJWT() throws InterruptedException, ParseException, IOException {
        jwt = integrationTestsUtils.getMohApplicationsRealmKcToken(clientId, clientSecret);

        payeeApiWebTestClient = payeeApiWebTestClient
                .mutate()
                .baseUrl(mspDirectApiBaseUrl)
                .responseTimeout(Duration.ofSeconds(120))
                .build();
    }
    
    @BeforeEach
    public void resetPayee() {
    	deleteTestPayee();
        addTestPayee();
    }

    @AfterAll
    public void cleanupPayee() {
        deleteTestPayee();
    }

    @Test
    public void testGetPayee_success() {
    	Assumptions.assumeTrue(isDevEnvironment());

        UserPayee userPayee = payeeApiWebTestClient
                .get()
                .uri(String.format("%s/%s", PAYEE_PATH, testClientId))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserPayee.class)
                .returnResult()
                .getResponseBody();

        Assertions.assertThat(userPayee).isNotNull();
        Assertions.assertThat(userPayee.getPayeeNumber()).isEqualTo("ABC123");
        Assertions.assertThat(userPayee.getUserGuid()).isEqualTo(testClientId);
    }
    
    @Test
    public void testGetPayee_notFound() {
    	Assumptions.assumeTrue(isDevEnvironment());

        payeeApiWebTestClient
                .get()
                .uri(String.format("%s/%s", PAYEE_PATH, nonexistentClientId))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    public void testDeletePayee_success() {
    	Assumptions.assumeTrue(isDevEnvironment());

        payeeApiWebTestClient
                .delete()
                .uri(String.format("%s/%s", PAYEE_PATH,  testClientId))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isNoContent();       	
    }
    
    @Test
    public void testDeletePayee_notFound() {
    	Assumptions.assumeTrue(isDevEnvironment());

        payeeApiWebTestClient
                .delete()
                .uri(String.format("%s/%s", PAYEE_PATH,  nonexistentClientId))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isNotFound();
    }
    
    @Test
    public void testAddPayee_success() {
    	Assumptions.assumeTrue(isDevEnvironment());
  
    	deleteTestPayee();

    	UserPayee userPayee = payeeApiWebTestClient
                .post()
                .uri(PAYEE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserPayee(testClientId, "ABC123"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserPayee.class)
                .returnResult()
                .getResponseBody();
        
        Assertions.assertThat(userPayee).isNotNull();
        Assertions.assertThat(userPayee.getPayeeNumber()).isEqualTo("ABC123");
        Assertions.assertThat(userPayee.getUserGuid()).isEqualTo(testClientId);
    }
    
    @Test
    public void testAddPayee_conflict() {
    	Assumptions.assumeTrue(isDevEnvironment());
    	
        payeeApiWebTestClient
                .post()
                .uri(PAYEE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserPayee(testClientId, "ABC123"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.CONFLICT);
    }
    
    @Test
    public void testUpdatePayee_success() {
    	Assumptions.assumeTrue(isDevEnvironment());

    	UserPayee userPayee = payeeApiWebTestClient
                .put()
                .uri(String.format("%s/%s", PAYEE_PATH,  testClientId))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserPayee(testClientId, "DEF456"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserPayee.class)
                .returnResult()
                .getResponseBody();
    	
        Assertions.assertThat(userPayee).isNotNull();
        Assertions.assertThat(userPayee.getPayeeNumber()).isEqualTo("DEF456");
        Assertions.assertThat(userPayee.getUserGuid()).isEqualTo(testClientId);
    }
    
    @Test
    public void testUpdatePayee_notFound() {
    	Assumptions.assumeTrue(isDevEnvironment());

    	payeeApiWebTestClient
                .put()
                .uri(String.format("%s/%s", PAYEE_PATH,  nonexistentClientId))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserPayee(nonexistentClientId, "DEF456"))
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectStatus().isNotFound();
    }

    private void addTestPayee() {
    	payeeApiWebTestClient
                .post()
                .uri(PAYEE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createUserPayee(testClientId, "ABC123"))
                .header("Authorization", "Bearer " + jwt)
                .exchange();
    }

    private void deleteTestPayee() {
        payeeApiWebTestClient
                .delete()
                .uri(String.format("%s/%s", PAYEE_PATH, testClientId))
                .header("Authorization", "Bearer " + jwt)
                .exchange();
    }
    
    private UserPayee createUserPayee(String userGuid, String payeeNumber) {
    	UserPayee userPayee = new UserPayee();
    	userPayee.setPayeeNumber(payeeNumber);
    	userPayee.setUserGuid(userGuid);
    	return userPayee;
    }

    private boolean isDevEnvironment() {
        return mspDirectApiBaseUrl.contains("dev");
    }
}
