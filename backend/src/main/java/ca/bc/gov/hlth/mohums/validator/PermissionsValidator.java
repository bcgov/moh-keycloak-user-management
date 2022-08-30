package ca.bc.gov.hlth.mohums.validator;

import ca.bc.gov.hlth.mohums.util.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class PermissionsValidator {

    @Value("${user-management-client.roles.manage-all-groups}")
    private String manageAllGroupsRole;
    public boolean validateGroupManagementPermission(Jwt requesterToken, String groupId){
        if(!canManageAllGroups(requesterToken)){
            return JwtTokenUtils.getUserGroups(requesterToken).contains(groupId);
        }
        return true;
    }

    private boolean canManageAllGroups(Jwt requesterToken){
        return JwtTokenUtils.containsRole(requesterToken, manageAllGroupsRole);
    }

}
