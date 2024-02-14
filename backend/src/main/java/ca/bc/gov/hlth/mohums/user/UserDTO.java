package ca.bc.gov.hlth.mohums.user;

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
    private Map<String, List<String>> attributes;

    public UserDTO(String id, String username, String firstName, Long createdTimestamp, String lastName, String email, boolean enabled, boolean emailVerified, Map<String, List<String>> attributes) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.createdTimestamp = createdTimestamp;
        this.lastName = lastName;
        this.email = email;
        this.enabled = enabled;
        this.emailVerified = emailVerified;
        this.attributes = attributes;
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
}
