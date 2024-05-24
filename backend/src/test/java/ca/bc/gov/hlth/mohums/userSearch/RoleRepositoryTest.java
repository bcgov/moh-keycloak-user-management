package ca.bc.gov.hlth.mohums.userSearch;

import ca.bc.gov.hlth.mohums.userSearch.role.RoleEntity;
import ca.bc.gov.hlth.mohums.userSearch.role.RoleRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    private final String umsIntegrationTestsClientId = "24447cb4-f3b1-455b-89d9-26c081025fb9";

    @Test
    public void testFindByClientIdNotFound() {
        List<RoleEntity> notFound = roleRepository.findMohApplicationsRolesByClient("non-existing-client-id");
        assertTrue(notFound.isEmpty());
    }

    @Test
    public void testFindByClientIdFromDifferentRealmNotFound() {
        String masterRealmClient = "0bdb6f27-403c-4510-82b6-c10355426737";
        List<RoleEntity> notFound = roleRepository.findMohApplicationsRolesByClient(masterRealmClient);
        assertTrue(notFound.isEmpty());
    }

    @Test
    public void testFindByClientIdSuccessful() {
        List<RoleEntity> roles = roleRepository.findMohApplicationsRolesByClient(umsIntegrationTestsClientId);
        assertTrue(roles.size() > 0);
        assertTrue(roles.stream().allMatch(r -> r.getClientId().equals(umsIntegrationTestsClientId) && r.getRealmId().equals("moh_applications")));
    }

    @Test
    public void testFindByClientIdAndNamesNotFound() {
        List<String> nonExistingRoles = List.of("role-a", "role-b");
        List<RoleEntity> notFound = roleRepository.findMohApplicationsRolesByClientAndNames(umsIntegrationTestsClientId, nonExistingRoles);
        assertTrue(notFound.isEmpty());
    }

    @Test
    public void testFindByClientIdAndEmptyNamesNotFound() {
        List<String> emptyRoles = List.of();
        List<RoleEntity> notFound = roleRepository.findMohApplicationsRolesByClientAndNames(umsIntegrationTestsClientId, emptyRoles);
        assertTrue(notFound.isEmpty());
    }

    @Test
    public void testFindByClientIdAndOneNameSuccessful() {
        List<String> roleName = List.of("TEST_ROLE");
        List<RoleEntity> rolesResponse = roleRepository.findMohApplicationsRolesByClientAndNames(umsIntegrationTestsClientId, roleName);
        assertEquals(1, rolesResponse.size());
        assertTrue(rolesResponse.stream().allMatch(r -> r.getClientId().equals(umsIntegrationTestsClientId) && r.getRealmId().equals("moh_applications") && r.getName().equals(roleName.get(0))));
    }

    @Test
    public void testFindByClientIdAndMultipleNameSuccessful() {
        List<String> roleNames = List.of("TEST_ROLE", "getUsersInRole_TEST_ROLE");
        List<RoleEntity> rolesResponse = roleRepository.findMohApplicationsRolesByClientAndNames(umsIntegrationTestsClientId, roleNames);
        assertEquals(roleNames.size(), rolesResponse.size());
        assertTrue(rolesResponse.stream().allMatch(r -> r.getClientId().equals(umsIntegrationTestsClientId) && r.getRealmId().equals("moh_applications") && roleNames.contains(r.getName())));
    }
}
