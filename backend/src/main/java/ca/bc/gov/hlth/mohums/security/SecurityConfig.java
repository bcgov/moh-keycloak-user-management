package ca.bc.gov.hlth.mohums.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${user-management-client.roles.view-groups}")
    private String viewGroupsRole;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {

        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakClientRoleConverter());

        http
            .authorizeExchange()
                .pathMatchers("/groups").hasRole(viewGroupsRole)
            .anyExchange().authenticated().and()
            .oauth2ResourceServer().jwt()
            .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter));

        return http.build();
    }

}
