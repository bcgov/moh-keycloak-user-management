package ca.bc.gov.hlth.mohums.user;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserSearchParameters {

    private Optional<String> email;
    private Optional<String> firstName;
    private Optional<String> lastName;
    private Optional<String> search;
    private Optional<String> username;
    private Optional<String> organizationId;
    private Optional<String> clientId;
    private Optional<String[]> selectedRoles;
    private Optional<String> lastLogAfter;
    private Optional<String> lastLogBefore;

    public UserSearchParameters(Optional<String> email, Optional<String> firstName,
                                Optional<String> lastName, Optional<String> search,
                                Optional<String> username, Optional<String> organizationId,
                                Optional<String> clientId, Optional<String[]> selectedRoles,
                                Optional<String> lastLogAfter, Optional<String> lastLogBefore) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.search = search;
        this.username = username;
        this.organizationId = organizationId;
        this.clientId = clientId;
        this.selectedRoles = selectedRoles;
        this.lastLogAfter = lastLogAfter;
        this.lastLogBefore = lastLogBefore;
    }

    public boolean isSearchByLastLogOnly() {
        return (lastLogAfter.isPresent() || lastLogBefore.isPresent())
                && email.isEmpty() && firstName.isEmpty()
                && lastName.isEmpty() && search.isEmpty()
                && username.isEmpty() && organizationId.isEmpty()
                && clientId.isEmpty() && selectedRoles.isEmpty();
    }

    public boolean isBasicSearch() {
        return search.isPresent() && email.isEmpty()
                && firstName.isEmpty() && lastName.isEmpty()
                && username.isEmpty() && organizationId.isEmpty()
                && clientId.isEmpty() && selectedRoles.isEmpty()
                && lastLogAfter.isEmpty() && lastLogBefore.isEmpty();

    }

    public Optional<String> getEmail() {
        return email;
    }

    public Optional<String> getFirstName() {
        return firstName;
    }

    public Optional<String> getLastName() {
        return lastName;
    }

    public Optional<String> getSearch() {
        return search;
    }

    public Optional<String> getUsername() {
        return username;
    }

    public Optional<String> getOrganizationId() {
        return organizationId;
    }

    public Optional<String> getClientId() {
        return clientId;
    }

    public Optional<String[]> getSelectedRoles() {
        return selectedRoles;
    }

    public Optional<String> getLastLogAfter() {
        return lastLogAfter;
    }

    public Optional<String> getLastLogBefore() {
        return lastLogBefore;
    }
}
