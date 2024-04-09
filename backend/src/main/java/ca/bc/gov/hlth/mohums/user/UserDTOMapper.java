package ca.bc.gov.hlth.mohums.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDTOMapper {


    /*
        Convert UserEntity to match response structure returned by Keycloak API
        Although Attribute Value is represented as a String in the database, Keycloak API returns it as a List
        TODO: lastLogDate should be optional param included in some responses + roles (String with "," as delimiter)
    */
    public static UserDTO convertToDTO(UserEntity user, Map<String, String> roleIdNameMap, boolean briefRepresentation, Object lastLog) {
        Map<String, List<String>> attributeMap = new HashMap<>();
        String roleString = null;

        if (!briefRepresentation) {
            for (UserAttributeEntity attribute : user.getAttributes()) {
                String attributeName = attribute.getName();
                String attributeValue = attribute.getValue();
                List<String> values = attributeMap.computeIfAbsent(attributeName, k -> new ArrayList<>());
                values.add(attributeValue);
            }
        }
        if (!roleIdNameMap.isEmpty()) {
            roleString = user.getRoles().stream().filter(userRoleMappingEntity -> roleIdNameMap.containsKey(userRoleMappingEntity.getRoleId()))
                    .map(userRoleMappingEntity -> roleIdNameMap.get(userRoleMappingEntity.getRoleId()))
                    .collect(Collectors.joining(", "));
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
                attributeMap,
                roleString,
                lastLog);
    }
}
