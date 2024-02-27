package ca.bc.gov.hlth.mohums.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserSpecifications userSpecifications;

    @Autowired
    public UserService(UserRepository userRepository, UserSpecifications userSpecifications) {
        this.userRepository = userRepository;
        this.userSpecifications = userSpecifications;
    }

    public Optional<UserDTO> getUserByID(String id) {
        return userRepository.findById(id).map(this::convertToDTO);
    }

    public List<UserDTO> getUsers(Optional<String> email,
                                  Optional<String> firstName,
                                  Optional<String> lastName,
                                  Optional<String> search,
                                  Optional<String> username,
                                  Optional<String> organizationId) {

        Specification<UserEntity> userSpec = Specification.where(userSpecifications.fromMohApplicationsRealm())
                .and(userSpecifications.notServiceAccount());

        if (search.isPresent()) {
            String searchValue = search.get();
            userSpec = userSpec.and(userSpecifications.firstNameLike(searchValue))
                    .or(userSpecifications.lastNameLike(searchValue))
                    .or(userSpecifications.usernameLike(searchValue))
                    .or(userSpecifications.emailLike(searchValue));
        } else {
            if (firstName.isPresent()) {
                userSpec.and(userSpecifications.firstNameLike(firstName.get()));
            }
            if (lastName.isPresent()) {
                userSpec.and(userSpecifications.lastNameLike(lastName.get()));
            }
            if (username.isPresent()) {
                userSpec.and(userSpecifications.usernameLike(username.get()));
            }
            if (email.isPresent()) {
                userSpec.and(userSpecifications.emailLike(email.get()));
            }
        }

        //TODO: should the block below be nested - there's possibility of manually calling API with search + organization, but not through UMC - does anyone else uses it?
        if(organizationId.isPresent()) {
            userSpec.and(userSpecifications.hasOrganizationWithGivenId(organizationId.get()));
        }

        //if client id + selected roles present
        //fetch roles from RoleEntity based on client id, names, moh_applications realm
        //map of role id -> role name (role name will always be unique because it returns from one client only)
        //fetch users based on list of role ids
        //attach role names to the response object

        return userRepository.findAll(userSpec).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /*
    Convert UserEntity to match response structure returned by Keycloak API
    Although Attribute Value is represented as a String in the database, Keycloak API returns it as a List
    TODO: lastLogDate should be optional param included in some responses
     */
    private UserDTO convertToDTO(UserEntity user) {

        Map<String, List<String>> attributeMap = new HashMap<>();
        for (UserAttributeEntity attribute : user.getAttributes()) {
            String attributeName = attribute.getName();
            String attributeValue = attribute.getValue();
            List<String> values = attributeMap.computeIfAbsent(attributeName, k -> new ArrayList<>());
            values.add(attributeValue);
        }
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getCreatedTimestamp(),
                user.getLastName(),
                user.getEmail(),
                user.isEnabled(),
                user.isEmailVerified(),
                attributeMap
        );
    }
}
