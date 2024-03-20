package ca.bc.gov.hlth.mohums.user;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

public class UserDTO {

    private String id;
    private String username;
    private String firstName;
    private Long createdTimestamp;
    private String lastName;
    private String email;
    private boolean enabled;
    private boolean emailVerified;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, List<String>> attributes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String role;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String lastLogDate;

    public UserDTO(String id, String username, String firstName, Long createdTimestamp, String lastName, String email, boolean enabled, boolean emailVerified, Map<String, List<String>> attributes, String role, String lastLogDate) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.createdTimestamp = createdTimestamp;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.attributes = attributes;
        this.role = role;
        this.lastLogDate = lastLogDate;
    }
    public UserDTO() {
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public String getRole() {
        return role;
    }

    public String getLastLogDate() {
        return lastLogDate;
    }

}
