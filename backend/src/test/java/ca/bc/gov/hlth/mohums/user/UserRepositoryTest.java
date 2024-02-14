package ca.bc.gov.hlth.mohums.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"OptionalGetWithoutIsPresent"})
@RunWith(SpringRunner.class)
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindByIdSuccessful() {
        UserEntity found = userRepository.findById("b728171c-7ca4-4c75-8df1-543faa5f66cc").get();

        assertEquals(found.getId(), "b728171c-7ca4-4c75-8df1-543faa5f66cc");
    }

    @Test
    public void testFindByIdNotFound() {
        Optional<UserEntity> notFound = userRepository.findById("b728171c-7ca4-4c75-8df1-543faa5f66cc");
        assertThat(notFound.isEmpty()).isTrue();
    }
}
