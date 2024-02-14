package ca.bc.gov.hlth.mohums.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<UserDTO> getUserByID(String id){
        return userRepository.findById(id).map(this::convertToDTO);
    }

    /*
    Convert UserEntity to match response structure returned by Keycloak API
    Although Attribute Value is represented as a String in the database, Keycloak API returns it as a List
     */
    private UserDTO convertToDTO(UserEntity user){

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
