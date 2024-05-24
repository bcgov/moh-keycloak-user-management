package ca.bc.gov.hlth.mohums.userSearch;

import ca.bc.gov.hlth.mohums.userSearch.user.UserEntity;
import ca.bc.gov.hlth.mohums.userSearch.user.UserRepository;
import ca.bc.gov.hlth.mohums.userSearch.user.UserSpecifications;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"OptionalGetWithoutIsPresent"})
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {


    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSpecifications userSpecifications;

    @Test
    public void testFindByIdSuccessful() {
        String umsTestUserId = "c35d48ea-3df9-4758-a27b-94e4cab1ba44";
        UserEntity found = userRepository.findMohApplicationsUsersByIdList(List.of(umsTestUserId)).get(0);

        assertEquals(found.getId(), umsTestUserId);
    }

    @Test
    public void testFindByIdNotFound() {
        List<UserEntity> notFound = userRepository.findMohApplicationsUsersByIdList(List.of("non-existing-user-id"));
        assertTrue(notFound.isEmpty());
    }

    @Test
    public void testFindByIdFromDifferentRealmNotFound() {
        String midtierAdminIdFromMasterRealm = "0474569b-74b6-4ad4-b43c-2673dd11bdfa";
        List<UserEntity> notFound = userRepository.findMohApplicationsUsersByIdList(List.of(midtierAdminIdFromMasterRealm));

        assertTrue(notFound.isEmpty());
    }

    @Test
    public void testFindAllByEmail() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.emailLike("test@ums.com"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().equals("umstest"))).isTrue();
    }

    @Test
    public void testFindAllByFirstName() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.firstNameLike("ums"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().equals("umstest"))).isTrue();
    }

    @Test
    public void testFindAllByLastName() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.lastNameLike("test"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().equals("umstest"))).isTrue();
    }

    @Test
    public void testFindAllByOrganization() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.hasOrganizationWithGivenId("00000010"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
    }

    @Test
    public void testFindAllByOrganizationAndUsername() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.usernameLike("ums-organization"))
                .and(userSpecifications.hasOrganizationWithGivenId("00000010"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertEquals(1, result.size());
        assertEquals("ums-organization", result.get(0).getUsername());
    }

    @Test
    public void testFindAllByOrganizationAndEmail() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.emailLike("organization@ums.com"))
                .and(userSpecifications.hasOrganizationWithGivenId("00000010"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertEquals(1, result.size());
        assertEquals("organization@ums.com", result.get(0).getEmail());
    }

    @Test
    public void testFindAllByOrganizationNoResults() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.hasOrganizationWithGivenId("non-existing-org-id"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideUsernameForFindAllByUsername")
    public void testFindAllByUsername(String username) {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.usernameLike(username));

        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
    }

    private static Stream<String> provideUsernameForFindAllByUsername() {
        return Stream.of("umstest", "Umstest", "UMSTEST", "UMStest");
    }

    @Test
    public void testFindAllBySearchParam() {
        String searchValue = "test";
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.firstNameLike(searchValue))
                .or(userSpecifications.lastNameLike(searchValue))
                .or(userSpecifications.usernameLike(searchValue))
                .or(userSpecifications.emailLike(searchValue));

        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().contains(searchValue))).isTrue();
        assertThat(result.stream().anyMatch(user -> user.getEmail() != null && user.getEmail().contains(searchValue))).isTrue();
        assertThat(result.stream().anyMatch(user -> user.getFirstName() != null && user.getFirstName().contains(searchValue))).isTrue();
        assertThat(result.stream().anyMatch(user -> user.getLastName() != null && user.getLastName().contains(searchValue))).isTrue();
    }

    @Test
    public void testFindAllNotServiceAccountsFromMohApplicationsRealm() {
        Specification<UserEntity> userSpec = baseUserSpecification();
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().noneMatch(user -> user.getUsername().contains("service-account"))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideRoleForFindAllByRole")
    public void testFindAllByRole(List<String> roles, boolean isResultEmpty) {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.rolesLike(roles));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertEquals(isResultEmpty, result.isEmpty());
    }

    private static Stream<Arguments> provideRoleForFindAllByRole() {
        String roleIdAssignedToUmsUser = "f9d917e2-7544-4f3d-b866-ad22a16ae5aa";
        String anotherRoleIdAssignedToUmsUser = "61d15faa-7a70-4a1e-be5e-2037d7e27e86";
        String roleIdNotAssignedToAnyone = "e5625153-1cd0-48f7-b305-78339520740a";

        return Stream.of(
                Arguments.of(List.of(roleIdAssignedToUmsUser, roleIdNotAssignedToAnyone), false),
                Arguments.of(List.of(roleIdAssignedToUmsUser, anotherRoleIdAssignedToUmsUser), false),
                Arguments.of(List.of(roleIdAssignedToUmsUser), false),
                Arguments.of(List.of(roleIdNotAssignedToAnyone), true),
                Arguments.of(List.of(), true)
        );
    }

    private Specification<UserEntity> baseUserSpecification() {
        return Specification
                .where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.notServiceAccount());
    }
}
