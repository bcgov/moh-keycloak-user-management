package ca.bc.gov.hlth.mohums.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"OptionalGetWithoutIsPresent"})
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRepositoryTest {


    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByIdSuccessful() {
        String testCafeUserID = "3195a1bf-4bea-47c4-955d-cf52d4e2fc15";
        UserEntity found = userRepository.findMohApplicationsUserById(testCafeUserID).get();

        assertEquals(found.getId(), testCafeUserID);
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
}
