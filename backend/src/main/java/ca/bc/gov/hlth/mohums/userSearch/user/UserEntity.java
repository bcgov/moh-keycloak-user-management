package ca.bc.gov.hlth.mohums.userSearch.user;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import jakarta.persistence.*;
import java.util.Collection;
import java.util.LinkedList;


/**
 * Entity class based on Keycloak source code: https://github.com/keycloak/keycloak/blob/main/model/jpa/src/main/java/org/keycloak/models/jpa/entities/UserEntity.java
 */

@Entity
@Table(name = "USER_ENTITY", schema = "KEYCLOAK")
public class UserEntity {

    @Id
    @Column(name = "ID", length = 36)
    protected String id;

    @Column(name = "USERNAME")
    protected String username;
    @Column(name = "FIRST_NAME")
    protected String firstName;
    @Column(name = "CREATED_TIMESTAMP")
    protected Long createdTimestamp;
    @Column(name = "LAST_NAME")
    protected String lastName;
    @Column(name = "EMAIL")
    protected String email;
    @Column(name = "ENABLED")
    protected boolean enabled;
    @Column(name = "EMAIL_VERIFIED")
    protected boolean emailVerified;
    @Column(name = "REALM_ID")
    protected String realmId;

    @OneToMany(mappedBy = "user")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 20)
    protected Collection<UserAttributeEntity> attributes = new LinkedList<>();

    @OneToMany(mappedBy = "user")
    @Fetch(FetchMode.SELECT)
    @BatchSize(size = 20)
    protected Collection<UserRoleMappingEntity> roles = new LinkedList<>();

    @Transient
    protected String lastLogDate;

    public String getLastLogDate() {
        return lastLogDate;
    }

    public void setLastLogDate(String lastLogDate) {
        this.lastLogDate = lastLogDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<UserRoleMappingEntity> getRoles() {
        return roles;
    }

    public void setRoles(Collection<UserRoleMappingEntity> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public Collection<UserAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(Collection<UserAttributeEntity> attributes) {
        this.attributes = attributes;
    }
}
