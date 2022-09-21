package ca.bc.gov.hlth.mohums.util;

import net.minidev.json.JSONArray;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.*;
import java.util.stream.Collectors;

public class JwtTokenUtils {

    private static final String GROUPS_CLAIM = "groups";
    private static final String ROLES_CLAIM = "roles";
    private static final List<String> RESOURCES = List.of("USER-MANAGEMENT-SERVICE", "ORGANIZATIONS-API");
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";

    public static List<String> getUserGroups(Jwt jwt) {
        return jwt.containsClaim(GROUPS_CLAIM) ? (List<String>) jwt.getClaims().get(GROUPS_CLAIM) : List.of();
    }

    public static boolean containsRole(Jwt jwt, String role) {
        return getUserRoles(jwt).contains(role);
    }
    /* The roles in the access token look like
       "resource_access": {
         "user-management-service": {
           "roles": [
              "view-groups"
           ]
         },
         "organizations-api": {
           "roles": [
              "view-organizations"
           ]
         }
       }*/
    public static List<String> getUserRoles(Jwt jwt){
        if(jwt.containsClaim(RESOURCE_ACCESS_CLAIM)){
            final Map<String, Object> resourceAccess = (Map<String, Object>) jwt.getClaims().get(RESOURCE_ACCESS_CLAIM);
            List rolesWithinResources = RESOURCES.stream()
                    .map(resourceAccess::get)
                    .map(Map.class::cast)
                    .filter(Objects::nonNull)
                    .map(roles -> roles.get(ROLES_CLAIM))
                    .collect(Collectors.toList());

            List<String> flattenedRolesList = new ArrayList<>();
            rolesWithinResources.forEach(l -> flattenedRolesList.addAll((Collection) l));
            return flattenedRolesList;
        }
        return List.of();
    }
}
