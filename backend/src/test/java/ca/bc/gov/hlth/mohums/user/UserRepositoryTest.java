package ca.bc.gov.hlth.mohums.user;

import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        UserEntity found = userRepository.findMohApplicationsUserById(umsTestUserId).get();

        assertEquals(found.getId(), umsTestUserId);
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<UserEntity> notFound = userRepository.findMohApplicationsUserById("non-existing-user-id");
        assertThat(notFound.isEmpty()).isTrue();
    }

    @Test
    public void testFindByIdFromDifferentRealmNotFound() {
        String midtierAdminIdFromMasterRealm = "0474569b-74b6-4ad4-b43c-2673dd11bdfa";
        Optional<UserEntity> notFound = userRepository.findMohApplicationsUserById(midtierAdminIdFromMasterRealm);

        assertThat(notFound.isEmpty()).isTrue();
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
                .and(userSpecifications.usernameLike("umstest"))
                .and(userSpecifications.hasOrganizationWithGivenId("00000010"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertEquals(1, result.size());
        assertEquals("umstest", result.get(0).getUsername());
    }

    @Test
    public void testFindAllByOrganizationAndEmail() {
        Specification<UserEntity> userSpec = baseUserSpecification()
                .and(userSpecifications.usernameLike("test@ums.com"))
                .and(userSpecifications.hasOrganizationWithGivenId("00000010"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertEquals(1, result.size());
        assertEquals("test@ums.com", result.get(0).getEmail());
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

    //test roles not found
    //test roles found
    //test roles empty

    private Specification<UserEntity> baseUserSpecification(){
        return Specification
                .where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.notServiceAccount());
    }
}
