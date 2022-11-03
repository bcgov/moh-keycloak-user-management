package ca.bc.gov.hlth.mohums.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private final Logger webClientLogger = LoggerFactory.getLogger("KC_WEB_CLIENT");

    @Value("${keycloak-moh.admin-api-url}")
    private String keycloakMohAdminBaseUrl;

    @Value("${keycloak-master.admin-api-url}")
    private String keycloakMasterAdminBaseUrl;

    @Value("${keycloak-moh.organizations-api-url}")
    private String organizationsApiBaseUrl;

    @Value("${spring.codec.max-in-memory-size-mb}")
    int maxInMemorySize;

    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider
                = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager
                = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean("kcMohAuthorizedWebClient")
    public WebClient webClient(OAuth2AuthorizedClientManager authorizedClientManager) {

        String registrationId = "keycloak-moh";
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth = createOauthFilterFunction(authorizedClientManager, registrationId);
        return createWebClient(oauth, keycloakMohAdminBaseUrl);
    }

    @Bean("kcMasterAuthorizedWebClient")
    public WebClient webClientMaster(OAuth2AuthorizedClientManager authorizedClientManager) {

        String registrationId = "keycloak-master";
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth = createOauthFilterFunction(authorizedClientManager, registrationId);
        return createWebClient(oauth, keycloakMasterAdminBaseUrl);
    }

    @Bean("orgApiAuthorizedWebClient")
    public WebClient organizationsApiWebClient(OAuth2AuthorizedClientManager authorizedClientManager) {
        String registrationId = "keycloak-moh";
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth = createOauthFilterFunction(authorizedClientManager, registrationId);
        return createWebClient(oauth, organizationsApiBaseUrl);
    }

    private ServletOAuth2AuthorizedClientExchangeFilterFunction createOauthFilterFunction(OAuth2AuthorizedClientManager authorizedClientManager, String registrationId) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth
                = new ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId(registrationId);

        return oauth;
    }

    private WebClient createWebClient(ServletOAuth2AuthorizedClientExchangeFilterFunction oauth, String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .filter(oauth)
                .filter(logRequest())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(maxInMemorySize * 1024 * 1024))
                        .build())
                .build();
    }

    /*
     * Log request details for the downstream web service calls
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(c -> {
            webClientLogger.debug("Request: {} {}", c.method(), c.url());
            c.headers().forEach((n, v) -> webClientLogger.debug("request header {}={}", n, v));
            return Mono.just(c);
        });
    }
    
}
