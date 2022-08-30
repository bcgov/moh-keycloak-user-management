package ca.bc.gov.hlth.mohums.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class JwtTokenUtilsTest {
    private final String GROUPS_CLAIM = "groups";
    private static final String ROLES_CLAIM = "roles";
    private static final String API_CLIENT_NAME = "USER-MANAGEMENT-SERVICE";
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";

    Jwt jwtMock = Mockito.mock(Jwt.class);

    @Test
    void getUserGroupsValid() {
        Map<String, Object> claims = Map.of("groups", List.of("group1"), "email", "email@gmail.com");
        when(jwtMock.containsClaim(GROUPS_CLAIM)).thenReturn(true);
        when(jwtMock.getClaims()).thenReturn(claims);
        List<String> userGroups = JwtTokenUtils.getUserGroups(jwtMock);
        assertEquals( List.of("group1"), userGroups);
    }

    @Test
    void getUserGroupsInvalidNoGroupsClaims() {
        when(jwtMock.containsClaim(GROUPS_CLAIM)).thenReturn(false);
        List<String> userGroups = JwtTokenUtils.getUserGroups(jwtMock);
        assertEquals(List.of(), userGroups);
    }

    @Test
    void getUserGroupsInvalidEmptyGroupsClaims() {
        Map<String, Object> claims = Map.of("groups", List.of());
        when(jwtMock.containsClaim(GROUPS_CLAIM)).thenReturn(true);
        when(jwtMock.getClaims()).thenReturn(claims);
        List<String> userGroups = JwtTokenUtils.getUserGroups(jwtMock);
        assertEquals( List.of(), userGroups);
    }

    @Test
    void getUserRolesValid() {
        Map<String, Object> claims = Map.of("resource_access", Map.of(API_CLIENT_NAME, Map.of(ROLES_CLAIM, List.of("role1", "role2"))));
        when(jwtMock.containsClaim(RESOURCE_ACCESS_CLAIM)).thenReturn(true);
        when(jwtMock.getClaims()).thenReturn(claims);
        List<String> userRoles = JwtTokenUtils.getUserRoles(jwtMock);
        assertEquals(List.of("role1", "role2"), userRoles);
    }

    @Test
    void getUserRolesInvalidNoResourceAccessClaim() {
        when(jwtMock.containsClaim(RESOURCE_ACCESS_CLAIM)).thenReturn(false);
        List<String> userRoles = JwtTokenUtils.getUserRoles(jwtMock);
        assertEquals(List.of(), userRoles);
    }

    @Test
    void getUserRolesInvalidEmptyRoles() {
        Map<String, Object> claims = Map.of("resource_access", Map.of(API_CLIENT_NAME, Map.of(ROLES_CLAIM, List.of())));
        when(jwtMock.containsClaim(RESOURCE_ACCESS_CLAIM)).thenReturn(true);
        when(jwtMock.getClaims()).thenReturn(claims);
        List<String> userRoles = JwtTokenUtils.getUserRoles(jwtMock);
        assertEquals(List.of(), userRoles);
    }


}