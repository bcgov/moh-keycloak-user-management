package ca.bc.gov.hlth.mohums.util;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

public class JwtTokenUtils {

    private static final String GROUPS_CLAIM = "groups";
    private static final String ROLES_CLAIM = "roles";
    private static final String API_CLIENT_NAME = "USER-MANAGEMENT-SERVICE";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";

    public static List<String> getUserGroups(Jwt jwt) {
        return jwt.containsClaim(GROUPS_CLAIM) ? (List<String>) jwt.getClaims().get(GROUPS_CLAIM) : List.of();
    }

    public static boolean containsRole(Jwt jwt, String role) {
        return getUserRoles(jwt).contains(role);
    }

    public static List<String> getUserRoles(Jwt jwt) {
        if (jwt.containsClaim(RESOURCE_ACCESS_CLAIM)) {
            final Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get(RESOURCE_ACCESS_CLAIM);
            final Map<String, Object> resource = (Map<String, Object>) resourceAccess.get(API_CLIENT_NAME);
            if (resource != null)
                return (List<String>) resource.get(ROLES_CLAIM);
        }
        return List.of();
    }
}
