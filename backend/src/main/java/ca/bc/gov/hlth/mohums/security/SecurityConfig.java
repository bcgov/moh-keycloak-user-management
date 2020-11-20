package ca.bc.gov.hlth.mohums.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${user-management-client.roles.view-clients}")
    private String viewClientsRole;

    @Value("${user-management-client.roles.view-groups}")
    private String viewGroupsRole;

    @Value("${user-management-client.roles.view-users}")
    private String viewUsersRole;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakClientRoleConverter());

        http
                .authorizeRequests()
                .mvcMatchers("/docs/**").permitAll()
                .mvcMatchers("/clients/**").hasRole(viewClientsRole)
                .mvcMatchers("/groups/**").hasRole(viewGroupsRole)
                .mvcMatchers("/users/**").hasRole(viewUsersRole)
                .mvcMatchers("/*").denyAll()
                .and()
            .oauth2ResourceServer().jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter);

    }

}
