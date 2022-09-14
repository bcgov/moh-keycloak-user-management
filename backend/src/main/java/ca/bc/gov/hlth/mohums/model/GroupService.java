package ca.bc.gov.hlth.mohums.model;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final String DESCRIPTION = "As a member of %s group, an administrator is able to: %s";


    /**
     * four main roles groups:
     * 1. user management - managing user group, details, roles, creating users
     * 2. client management
     * 3. general view roles - view metrics, users, groups
     * 4. miscellaneous - don't fit to any naming-schema of roles, for example create-user
     */
    public String getDescription(List<String> umsRoles, String groupName) {
        StringBuilder groupRolesDescription = new StringBuilder();
        List<String> descriptions = List.of(getMiscellaneousRolesDescription(umsRoles),
                getUserManagementRolesDescription(umsRoles),
                getClientManagementRolesDescription(umsRoles),
                getGeneralViewRolesDescription(umsRoles));

        descriptions.stream().filter(d -> !d.isEmpty()).forEach(groupRolesDescription::append);
        return String.format(DESCRIPTION, groupName, groupRolesDescription);
    }

    // roles with view-client prefix are for client management
    private String getGeneralViewRolesDescription(List<String> umsRoles) {
        List<String> generalViewRoles = umsRoles.stream()
                .filter(role -> role.contains("view-") && !role.contains("view-client")).collect(Collectors.toList());
        String prefix = "View ";

        return generalViewRoles.isEmpty() ? "" : createRolesDescription(generalViewRoles, prefix);
    }

    private String getClientManagementRolesDescription(List<String> umsRoles) {
        List<String> clientManagementRoles = umsRoles.stream().filter(role -> role.contains("view-client-")).collect(Collectors.toList());
        String prefix = "Manage ";

        return clientManagementRoles.isEmpty() ? "" : createRolesDescription(clientManagementRoles, prefix);
    }

    private String getUserManagementRolesDescription(List<String> umsRoles) {
        List<String> userManagementRoles = umsRoles.stream().filter(role -> role.contains("manage-")).collect(Collectors.toList());
        String prefix = "Manage user ";

        return userManagementRoles.isEmpty() ? "" : createRolesDescription(userManagementRoles, prefix);
    }

    private String getMiscellaneousRolesDescription(List<String> umsRoles) {
        List<String> miscellaneousRoles = umsRoles.stream()
                .filter(role -> !role.contains("manage-") && !role.contains("view-"))
                .collect(Collectors.toList());

        return miscellaneousRoles.isEmpty() ? "" : miscellaneousRoles.stream().map(role -> role.replace("-", " ")).collect(Collectors.joining(", ")) + ". ";
    }

    private String createRolesDescription(List<String> userManagementRoles, String prefix) {
        List<String> roleKeyWords = userManagementRoles.stream().map(role -> {
                    String[] splitString = role.split("-", 3);
                    return splitString[splitString.length - 1];
                })
                .distinct()
                .collect(Collectors.toList());

        String rolesString = String.join(", ", roleKeyWords);
        return prefix + rolesString + ". ";
    }
}
