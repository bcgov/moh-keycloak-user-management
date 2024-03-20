package ca.bc.gov.hlth.mohums.user;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
                () -> assertEquals("role1, role2", response.get().getRole()),
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

    //TODO: change details to UMSTEST user
    private UserEntity getMockUser() {
        UserEntity mockUser = new UserEntity();
        UserAttributeEntity mockUserAttribute = new UserAttributeEntity();
        UserRoleMappingEntity userRoleMappingEntityOne = new UserRoleMappingEntity();
        UserRoleMappingEntity userRoleMappingEntityTwo = new UserRoleMappingEntity();


        mockUserAttribute.setUser(mockUser);
        mockUserAttribute.setId("mockAttributeId");
        mockUserAttribute.setName("mockAttributeName");
        mockUserAttribute.setValue("mockAttributeValue");

        userRoleMappingEntityOne.setUser(mockUser);
        userRoleMappingEntityOne.setRoleId("role1");

        userRoleMappingEntityTwo.setUser(mockUser);
        userRoleMappingEntityTwo.setRoleId("role2");

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
        mockUser.setRoles(List.of(userRoleMappingEntityOne, userRoleMappingEntityTwo));

        return mockUser;
    }
}