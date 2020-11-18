package ca.bc.gov.hlth.mohums.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${user-management-client.roles.view-clients}")
    private String viewClientsRole;

    @Value("${user-management-client.roles.view-groups}")
    private String viewGroupsRole;

    @Value("${user-management-client.roles.view-users}")
    private String viewUsersRole;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {

        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakClientRoleConverter());

        http
            .securityMatcher(ServerWebExchangeMatchers.pathMatchers("/**"))
            .authorizeExchange()
                .pathMatchers("/docs/**").permitAll()
                .pathMatchers("/clients/**").hasRole(viewClientsRole)
                .pathMatchers("/groups/**").hasRole(viewGroupsRole)
                .pathMatchers("/users/**").hasRole(viewUsersRole)
                .pathMatchers("/*").denyAll()
            .anyExchange().authenticated().and()
            .oauth2ResourceServer().jwt()
            .jwtAuthenticationConverter(new ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter));

        return http.build();
    }

}
