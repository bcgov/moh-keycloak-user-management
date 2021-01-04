package ca.bc.gov.hlth.mohums.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${user-management-client.roles.view-clients}")
    private String viewClientsRole;

    @Value("${user-management-client.roles.view-groups}")
    private String viewGroupsRole;

    @Value("${user-management-client.roles.view-users}")
    private String viewUsersRole;

    @Value("${user-management-client.roles.manage-users}")
    private String manageUsersRole;

    @Value("${user-management-client.roles.manage-user-groups}")
    private String manageUserGroupsRole;

    @Value("${config.allowed-origins}")
    private String allowedOrigins;

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakClientRoleConverter());

        http
                .cors(Customizer.withDefaults())
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET,"/docs/**").permitAll()
                .mvcMatchers(HttpMethod.GET,"/clients/**").hasRole(viewClientsRole)
                .mvcMatchers(HttpMethod.GET,"/groups/**").hasRole(viewGroupsRole)
                .mvcMatchers(HttpMethod.GET,"/users/{userId}/groups").access(String.format("hasRole('%s') and hasRole('%s')", viewGroupsRole, viewUsersRole))
                .mvcMatchers(HttpMethod.GET,"/users/**").hasRole(viewUsersRole)
                .mvcMatchers(HttpMethod.POST,"/users/**").hasRole(manageUsersRole)
                .mvcMatchers(HttpMethod.PUT,"/users/{userId}/groups/{groupId}").access(String.format("hasRole('%s') and hasRole('%s')", manageUsersRole, manageUserGroupsRole))
                .mvcMatchers(HttpMethod.PUT,"/users/**").hasRole(manageUsersRole)
                .mvcMatchers(HttpMethod.DELETE, "/users/**").hasRole(manageUsersRole)
                .mvcMatchers("/*").denyAll()
                .and()
            .oauth2ResourceServer().jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter);

    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(allowedOrigins);
        configuration.addAllowedHeader("*");
        configuration.setExposedHeaders(Collections.singletonList("Location"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "DELETE", "PUT"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
