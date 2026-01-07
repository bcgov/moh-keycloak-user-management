package ca.bc.gov.hlth.mohums.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${user-management-client.roles.view-clients}")
    private String viewClientsRole;

    @Value("${user-management-client.roles.view-metrics}")
    private String viewMetricsRole;

    @Value("${user-management-client.roles.view-groups}")
    private String viewGroupsRole;

    @Value("${user-management-client.roles.view-users}")
    private String viewUsersRole;

    @Value("${user-management-client.roles.view-events}")
    private String viewEventsRole;

    @Value("${user-management-client.roles.create-user}")
    private String createUserRole;

    @Value("${user-management-client.roles.manage-user-details}")
    private String manageUserDetailsRole;

    @Value("${user-management-client.roles.manage-user-roles}")
    private String manageUserRolesRole;

    @Value("${user-management-client.roles.manage-user-groups}")
    private String manageUserGroupsRole;

    @Value("${user-management-client.roles.manage-all-groups}")
    private String manageAllGroupsRole;

    @Value("${user-management-client.roles.manage-own-groups}")
    private String manageOwnGroupsRole;

    @Value("${user-management-client.roles.manage-org}")
    private String manageOrganizationsRole;

    @Value("${user-management-client.roles.bulk-removal}")
    private String bulkRemovalRole;

    @Value("${config.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakClientRoleConverter());

        http
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/docs/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/bulk-removal/**").hasRole(bulkRemovalRole)
                        .requestMatchers(HttpMethod.GET, "/realms").hasRole(viewMetricsRole)
                        .requestMatchers(HttpMethod.GET, "/dashboard/**").hasRole(viewMetricsRole)
                        .requestMatchers(HttpMethod.GET, "/events/**").hasRole(viewEventsRole)
                        .requestMatchers(HttpMethod.GET, "/metrics/**").hasRole(viewMetricsRole)
                        .requestMatchers(HttpMethod.GET, "/admin-events/**").hasRole(viewEventsRole)
                        .requestMatchers(HttpMethod.GET, "/{realm}/clients/{client-uuid}/roles/{role-name}/users").hasRole(viewClientsRole)
                        .requestMatchers(HttpMethod.GET, "/clients/**").hasRole(viewClientsRole)
                        .requestMatchers(HttpMethod.GET, "/groups/**").hasRole(viewGroupsRole)
                        .requestMatchers(HttpMethod.GET, "/organizations/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/organizations/**").denyAll()
                        .requestMatchers(HttpMethod.POST, "/organizations/**").hasRole(manageOrganizationsRole)
                        .requestMatchers(HttpMethod.GET, "/users/{userId}/groups")
                        .access((authentication, context) -> AuthorizationManagers.allOf(
                                AuthorityAuthorizationManager.hasRole(viewUsersRole),
                                AuthorityAuthorizationManager.hasRole(viewGroupsRole)
                        ).check(authentication, context))
                        .requestMatchers(HttpMethod.GET, "/users/**").hasRole(viewUsersRole)
                        .requestMatchers(HttpMethod.POST, "/users/{userId}/role-mappings/**").hasRole(manageUserRolesRole)
                        .requestMatchers(HttpMethod.POST, "/users/**").hasRole(createUserRole)
                        .requestMatchers(HttpMethod.DELETE, "/users/{userId}/federated-identity/**")
                        .hasRole(viewUsersRole)
                        .requestMatchers(HttpMethod.PUT, "/users/{userId}").hasRole(viewUsersRole)
                        .requestMatchers(HttpMethod.PUT, "/users/{userId}/groups/{groupId}")
                        .hasAnyRole(manageOwnGroupsRole, manageAllGroupsRole)
                        .requestMatchers(HttpMethod.DELETE, "/users/{userId}/groups/{groupId}")
                        .hasAnyRole(manageOwnGroupsRole, manageAllGroupsRole)
                        .requestMatchers(HttpMethod.DELETE, "/users/{userId}/role-mappings/**")
                        .hasRole(manageUserRolesRole)
                        .requestMatchers(HttpMethod.PUT, "/users/{userId}/payee/**").hasRole(manageUserRolesRole)
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                );

        return http.build();
    }

    @Component
    public class TrailingSlashFilter extends OncePerRequestFilter {
        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain)
                throws ServletException, IOException {
            String uri = request.getRequestURI();
            if (uri.length() > 1 && uri.endsWith("/")) {
                request.getRequestDispatcher(uri.substring(0, uri.length() - 1))
                        .forward(request, response);
                return;
            }
            filterChain.doFilter(request, response);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin(allowedOrigins);
        configuration.addAllowedHeader("*");
        configuration.setExposedHeaders(Collections.singletonList("Location"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
