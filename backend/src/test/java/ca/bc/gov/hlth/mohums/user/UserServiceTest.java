package ca.bc.gov.hlth.mohums.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"OptionalGetWithoutIsPresent"})
@SpringBootTest
@RunWith(SpringRunner.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserSpecifications userSpecifications;

    @Test
    public void testGetUserByIdSuccessful() {
        UserEntity mockUser = getMockUser();
        Mockito.when(userRepository.findById("b728171c-7ca4-4c75-8df1-543faa5f66cc"))
                .thenReturn(Optional.of(mockUser));


        Optional<UserDTO> response = userService.getUserByID(mockUser.getId());
        assertAll("User Assertions",
                () -> assertThat(response.isPresent()).isTrue(),
                () -> assertEquals(mockUser.getId(), response.get().getId()),
                () -> assertEquals(mockUser.getCreatedTimestamp(), response.get().getCreatedTimestamp()),
                () -> assertEquals(mockUser.getEmail(), response.get().getEmail()),
                () -> assertEquals(mockUser.getUsername(), response.get().getUsername()),
                () -> assertEquals(mockUser.getFirstName(), response.get().getFirstName()),
                () -> assertEquals(mockUser.getLastName(), response.get().getLastName()),
                () -> assertEquals(mockUser.isEnabled(), response.get().isEnabled()),
                () -> assertEquals(mockUser.isEmailVerified(), response.get().isEmailVerified()),
                () -> assertEquals(mockUser.getAttributes().size(), response.get().getAttributes().size()),
                () -> assertThat(attributesMappedCorrectly(mockUser.getAttributes(), response.get().getAttributes())).isTrue()
        );
    }

    private boolean attributesMappedCorrectly(Collection<UserAttributeEntity> attributes, Map<String, List<String>> dtoAttributes) {
        attributes.forEach(attribute -> {
            assertTrue(dtoAttributes.containsKey(attribute.getName()));
            List<String> values = dtoAttributes.get(attribute.getName());
            assertTrue(values.contains(attribute.getValue()));
        });
        return true;
    }

    @Test
    public void testGetUserByIdNotFound() {
        Mockito.when(userRepository.findById("non-existing-id")).thenReturn(Optional.empty());
        Optional<UserDTO> response = userService.getUserByID("non-existing-id");

        assertThat(response.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("provideParamsForGetUsers")
    public void testUserSpecificationInvocationsInGetUsers(Optional<String> email,
                             Optional<String> firstName,
                             Optional<String> lastName,
                             Optional<String> search,
                             Optional<String> username) {
        userService.getUsers(email, firstName, lastName, search, username);
        int emailInvocations = email.isPresent() || search.isPresent() ? 1 : 0;
        int firstNameInvocations = firstName.isPresent() || search.isPresent() ? 1 : 0;
        int lastNameInvocations = lastName.isPresent() || search.isPresent() ? 1 : 0;
        int usernameInvocations = username.isPresent() || search.isPresent() ? 1 : 0;

        Mockito.verify(userSpecifications, Mockito.times(1)).notServiceAccount();
        Mockito.verify(userSpecifications, Mockito.times(1)).fromMohApplicationsRealm();
        Mockito.verify(userSpecifications, Mockito.times(emailInvocations)).emailLike(ArgumentMatchers.anyString());
        Mockito.verify(userSpecifications, Mockito.times(firstNameInvocations)).firstNameLike(ArgumentMatchers.anyString());
        Mockito.verify(userSpecifications, Mockito.times(lastNameInvocations)).lastNameLike(ArgumentMatchers.anyString());
        Mockito.verify(userSpecifications, Mockito.times(usernameInvocations)).usernameLike(ArgumentMatchers.anyString());
    }

    private static Stream<Arguments> provideParamsForGetUsers() {
        return Stream.of(
                Arguments.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
                Arguments.of(Optional.of("email"), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()),
                Arguments.of(Optional.empty(), Optional.of("firstName"), Optional.empty(), Optional.empty(), Optional.empty()),
                Arguments.of(Optional.empty(), Optional.empty(), Optional.of("lastNameName"), Optional.empty(), Optional.empty()),
                Arguments.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("search"), Optional.empty()),
                Arguments.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of("email"))
        );
    }


    //TODO: change details to UMSTEST user
    private UserEntity getMockUser(){
        UserEntity mockUser = new UserEntity();
        UserAttributeEntity mockUserAttribute = new UserAttributeEntity();

        mockUserAttribute.setUser(mockUser);
        mockUserAttribute.setId("mockAttributeId");
        mockUserAttribute.setName("mockAttributeName");
        mockUserAttribute.setValue("mockAttributeValue");

        mockUser.setId("b728171c-7ca4-4c75-8df1-543faa5f66cc");
        mockUser.setCreatedTimestamp(1692742378655L);
        mockUser.setUsername("fiflorek@idir");
        mockUser.setEnabled(true);
        mockUser.setEmail("filip.florek@cgi.com");
        mockUser.setEmailVerified(false);
        mockUser.setFirstName("Filip");
        mockUser.setLastName("Florek");
        mockUser.setRealmId("moh_applications");
        mockUser.setAttributes(List.of(mockUserAttribute));

        return mockUser;
    }
}