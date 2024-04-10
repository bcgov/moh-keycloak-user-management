package ca.bc.gov.hlth.mohums.userSearch.user;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity class based on Keycloak source code: https://github.com/keycloak/keycloak/blob/main/model/jpa/src/main/java/org/keycloak/models/jpa/entities/UserRoleMappingEntity.java
 */

@Table(name="USER_ROLE_MAPPING", schema = "KEYCLOAK")
@Entity
@IdClass(UserRoleMappingEntity.Key.class)
public class UserRoleMappingEntity {
    @Id
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="USER_ID")
    private UserEntity user;

    @Id
    @Column(name = "ROLE_ID")
    private String roleId;

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    /**
     * This class represents composite key
     */
    public static class Key implements Serializable {

        protected UserEntity user;

        protected String roleId;

        public Key() {
        }

        public Key(UserEntity user, String roleId) {
            this.user = user;
            this.roleId = roleId;
        }

        public UserEntity getUser() {
            return user;
        }

        public String getRoleId() {
            return roleId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!roleId.equals(key.roleId)) return false;
            if (!user.equals(key.user)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = user.hashCode();
            result = 31 * result + roleId.hashCode();
            return result;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof UserRoleMappingEntity)) return false;

        UserRoleMappingEntity key = (UserRoleMappingEntity) o;

        if (!roleId.equals(key.roleId)) return false;
        if (!user.equals(key.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = user.hashCode();
        result = 31 * result + roleId.hashCode();
        return result;
    }
}
