package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ExternalApiCallerConfig {

    @Bean("orgApiCaller")
    public ExternalApiCaller organizationApiCaller(@Qualifier("orgApiAuthorizedWebClient") WebClient organizationsApiWebClient){
        return new ExternalApiCaller(organizationsApiWebClient);
    }

    @Bean("keycloakMohApiCaller")
    public ExternalApiCaller keycloakMohApiCaller(@Qualifier("kcMohAuthorizedWebClient") WebClient keycloakMohWebClient){
        return new ExternalApiCaller(keycloakMohWebClient);
    }

    @Bean("keycloakMasterApiCaller")
    public ExternalApiCaller keycloakMasterApiCaller(@Qualifier("kcMasterAuthorizedWebClient") WebClient keycloakMasterWebClient){
        return new ExternalApiCaller(keycloakMasterWebClient);
    }
    
    @Bean("payeeApiCaller")
    public ExternalApiCaller payeeApiCaller(@Qualifier("payeeApiAuthorizedWebClient") WebClient payeeApiAuthorizedWebClient){
        return new ExternalApiCaller(payeeApiAuthorizedWebClient);
    }
}
