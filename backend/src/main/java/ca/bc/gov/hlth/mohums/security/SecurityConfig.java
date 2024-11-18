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

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakClientRoleConverter());

    
        http
                .cors(Customizer.withDefaults())
                .authorizeRequests()
                .mvcMatchers(HttpMethod.GET,"/docs/**").permitAll()
                .mvcMatchers(HttpMethod.DELETE,"/bulk-removal/**").hasRole(bulkRemovalRole)
                .mvcMatchers(HttpMethod.GET,"/realms").hasRole(viewMetricsRole)
                .mvcMatchers(HttpMethod.GET, "/dashboard/**").hasRole(viewMetricsRole)
                .mvcMatchers(HttpMethod.GET,"/events/**").hasRole(viewEventsRole)
                .mvcMatchers(HttpMethod.GET,"/admin-events/**").hasRole(viewEventsRole)
                .mvcMatchers(HttpMethod.GET, "/{realm}/clients/{client-uuid}/roles/{role-name}/users").hasRole(viewClientsRole)
                .mvcMatchers(HttpMethod.GET,"/clients/**").hasRole(viewClientsRole)
                .mvcMatchers(HttpMethod.GET,"/groups/**").hasRole(viewGroupsRole)
                .mvcMatchers(HttpMethod.GET,"/organizations/**").permitAll()
                .mvcMatchers(HttpMethod.PUT,"/organizations/**").denyAll()  //disabled until further talks about editing organizations
                .mvcMatchers(HttpMethod.POST,"/organizations/**").hasRole(manageOrganizationsRole)
                .mvcMatchers(HttpMethod.GET,"/users/{userId}/groups").access(String.format("hasRole('%s') and hasRole('%s')", viewUsersRole, viewGroupsRole))
                .mvcMatchers(HttpMethod.GET,"/users/**").hasRole(viewUsersRole)
                .mvcMatchers(HttpMethod.POST, "/users/{userId}/role-mappings/**").hasRole(manageUserRolesRole)
                .mvcMatchers(HttpMethod.POST,"/users/**").hasRole(createUserRole)
                .mvcMatchers(HttpMethod.PUT,"/users/{userId}").hasRole(manageUserDetailsRole)
                .mvcMatchers(HttpMethod.PUT,"/users/{userId}/groups/{groupId}").hasAnyRole(manageOwnGroupsRole, manageAllGroupsRole)
                .mvcMatchers(HttpMethod.DELETE,"/users/{userId}/groups/{groupId}").hasAnyRole(manageOwnGroupsRole, manageAllGroupsRole)
                .mvcMatchers(HttpMethod.DELETE, "/users/{userId}/role-mappings/**").hasRole(manageUserRolesRole)
                .mvcMatchers(HttpMethod.PUT, "/users/{userId}/payee/**").hasRole(manageUserRolesRole)
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
