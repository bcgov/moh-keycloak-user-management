package ca.bc.gov.hlth.mohums.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;

@Component
public class GroupMapper {

    private final GroupService groupService;

    @Autowired
    public GroupMapper(GroupService groupService) {
        this.groupService = groupService;
    }

    public Group mapToGroup(LinkedHashMap<String, Object> body) {
        LinkedHashMap clientRoles = (LinkedHashMap) body.get("clientRoles");
        Object umsRolesNode = clientRoles.get("USER-MANAGEMENT-SERVICE");
        List<String> umsRoles = umsRolesNode == null ? List.of() : (List<String>) umsRolesNode;
        String groupName = body.get("name").toString();
        return Group.builder()
                .id(body.get("id").toString())
                .name(groupName)
                .path(body.get("path").toString())
                .umsRoles(umsRoles)
                .description(umsRoles.isEmpty() ? "No roles within UMS" : groupService.getDescription(umsRoles, groupName))
                .build();
    }
}
