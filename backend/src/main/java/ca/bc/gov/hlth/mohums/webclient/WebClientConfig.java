package ca.bc.gov.hlth.mohums.webclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class WebClientConfig {

    private final Logger webClientLogger = LoggerFactory.getLogger("KC_WEB_CLIENT");

    @Value("${keycloak.admin-api-url}")
    private String keycloakAdminBaseUrl;

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            final ReactiveClientRegistrationRepository clientRegistrationRepository,
            final ServerOAuth2AuthorizedClientRepository authorizedClientRepository) {

        ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider
                = ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        DefaultReactiveOAuth2AuthorizedClientManager authorizedClientManager
                = new DefaultReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository,
                        authorizedClientRepository);

        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    @Bean("kcAuthorizedWebClient")
    public WebClient webClient(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {

        String registrationId = "keycloak";

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth
                = new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth.setDefaultClientRegistrationId(registrationId);

        return WebClient.builder()
                .baseUrl(keycloakAdminBaseUrl)
                .filter(oauth)
                .filter(logRequest())
                .build();
    }

    /*
     * Log request details for the downstream web service calls
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(c -> {
            webClientLogger.info("Request: {} {}", c.method(), c.url());
            c.headers().forEach((n, v) -> webClientLogger.info("request header {}={}", n, v));
            return Mono.just(c);
        });
    }

}
