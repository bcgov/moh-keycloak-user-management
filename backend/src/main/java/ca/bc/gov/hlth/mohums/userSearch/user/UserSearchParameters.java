package ca.bc.gov.hlth.mohums.userSearch.user;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class UserSearchParameters {

    private final Optional<Boolean> briefRepresentation;
    private final Optional<String> email;
    private final Optional<String> firstName;
    private final Optional<String> lastName;
    private final Optional<String> search;
    private final Optional<String> username;
    private final Optional<String> organizationId;
    private final Optional<String> clientId;
    private final Optional<String[]> selectedRoles;
    private final Optional<String> lastLogAfter;
    private final Optional<String> lastLogBefore;

    public UserSearchParameters(Optional<Boolean> briefRepresentation,
                                Optional<String> email, Optional<String> firstName,
                                Optional<String> lastName, Optional<String> search,
                                Optional<String> username, Optional<String> organizationId,
                                Optional<String> clientId, Optional<String[]> selectedRoles,
                                Optional<String> lastLogAfter, Optional<String> lastLogBefore) {
        this.briefRepresentation = briefRepresentation;
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

    public Optional<Boolean> getBriefRepresentation() {
        return briefRepresentation;
    }

    public List<String> validateParameters() {
        List<String> errors = new ArrayList<>();

        validateOrganizationId(errors);
        validateDateFormat(lastLogAfter, "Last logged-in after date", errors);
        validateDateFormat(lastLogBefore, "Last logged-in before date", errors);

        return errors;
    }

    private void validateOrganizationId(List<String> errors) {
        organizationId.ifPresent(id -> {
            if (!id.matches("\\d+")) {
                errors.add("Invalid query parameter. Organization id must contain only numbers.");
            }
        });
    }

    private void validateDateFormat(Optional<String> date, String fieldName, List<String> errors) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        date.ifPresent(d -> {
            try {
                LocalDate.parse(d, formatter);
            } catch (DateTimeParseException e) {
                errors.add("Invalid query parameter. \"" + fieldName + "\" must be in yyyy-mm-dd format.");
            }
        });
    }
}
