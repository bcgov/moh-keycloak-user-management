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
        Optional<UserEntity> notFound = userRepository.findMohApplicationsUserById("non-existing-id");
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
        Specification<UserEntity> userSpec = Specification
                .where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.emailLike("test@ums.com"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().equals("umstest"))).isTrue();
    }

    @Test
    public void testFindAllByFirstName() {
        Specification<UserEntity> userSpec = Specification
                .where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.firstNameLike("ums"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().equals("umstest"))).isTrue();
    }

    @Test
    public void testFindAllByLastName() {
        Specification<UserEntity> userSpec = Specification
                .where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.lastNameLike("test"));
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().equals("umstest"))).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideUsernameForFindAllByUsername")
    public void testFindAllByUsername(String username) {
        Specification<UserEntity> userSpec = Specification
                .where(userSpecifications.fromMohApplicationsRealm())
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
        Specification<UserEntity> userSpec = Specification
                .where(userSpecifications.fromMohApplicationsRealm())
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
    public void testFindAllFromMohApplicationsRealm() {
        Specification<UserEntity> userSpec = Specification.where(userSpecifications.fromMohApplicationsRealm());
        List<UserEntity> result = userRepository.findAll(userSpec);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.stream().anyMatch(user -> user.getUsername().equals("umstest"))).isTrue();
    }
}
