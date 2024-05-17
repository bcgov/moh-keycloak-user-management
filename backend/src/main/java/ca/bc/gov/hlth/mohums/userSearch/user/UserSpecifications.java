package ca.bc.gov.hlth.mohums.userSearch.user;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserSpecifications {

    private static final char ESCAPE_BACKSLASH = '\\';

    public Specification<UserEntity> notServiceAccount() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.notLike(root.get("username"), "service-account-%");
    }

    public Specification<UserEntity> fromMohApplicationsRealm() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("realmId"), "moh_applications");
    }

    public Specification<UserEntity> firstNameLike(String firstName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), formatQueryParam(firstName));
    }

    public Specification<UserEntity> lastNameLike(String lastName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), formatQueryParam(lastName));
    }

    public Specification<UserEntity> usernameLike(String username) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), formatQueryParam(username));
    }

    public Specification<UserEntity> emailLike(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), formatQueryParam(email));
    }

    /**
   To keep the basic search functionality intact, most of the logic is copied from this class:
     https://github.com/keycloak/keycloak/blob/24.0.4/model/jpa/src/main/java/org/keycloak/models/jpa/JpaUserProvider.java
   The method splits the input value and compares each part of it with first name, last name, email and username.
     @param searchValue A String contained in username, first or last name, or email. Default search behavior is prefix-based (e.g., foo or foo*). Use foo for infix search and &quot;foo&quot; for exact search.
     */
    public Specification<UserEntity> userParamsLike(String searchValue) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> searchPredicates = new ArrayList<>();
            for (String value : searchValue.trim().split("\\s+")) {
                searchPredicates.addAll(getSearchPredicateArray(value, root, criteriaBuilder));
            }
            return criteriaBuilder.or(searchPredicates.toArray(new Predicate[0]));
        };
    }

    private List<Predicate> getSearchPredicateArray(String value, Root<UserEntity> root, CriteriaBuilder criteriaBuilder){
        value = value.toLowerCase();

        List<Predicate> orPredicates = new ArrayList<>();

        if (value.length() >= 2 && value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            // exact search
            value = value.substring(1, value.length() - 1);

            orPredicates.add(criteriaBuilder.equal(root.get("username"), value));
            orPredicates.add(criteriaBuilder.equal(root.get("email"), value));
            orPredicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("firstName")), value));
            orPredicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("lastName")), value));
        } else {
            value = value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            value = value.replace("*", "%");
            if (value.isEmpty() || value.charAt(value.length() - 1) != '%') value += "%";

            orPredicates.add(criteriaBuilder.like(root.get("username"), value, ESCAPE_BACKSLASH));
            orPredicates.add(criteriaBuilder.like(root.get("email"), value, ESCAPE_BACKSLASH));
            orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), value, ESCAPE_BACKSLASH));
            orPredicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), value, ESCAPE_BACKSLASH));
        }
        return orPredicates;
    }



    public Specification<UserEntity> rolesLike(List<String> roles) {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Join<UserEntity, UserRoleMappingEntity> userRoleJoin = root.join("roles", JoinType.INNER);
            Predicate userHasSomeOfTheGivenRoles = userRoleJoin.get("roleId").in(roles);
            return criteriaBuilder.and(userHasSomeOfTheGivenRoles);
        };
    }

    public Specification<UserEntity> hasOrganizationWithGivenId(String organizationId) {
        return (root, query, criteriaBuilder) -> {
            Join<UserEntity, UserAttributeEntity> userAttributeJoin = root.join("attributes", JoinType.INNER);
            Predicate attributeNameIsOrgDetails = criteriaBuilder.equal(userAttributeJoin.get("name"), "org_details");
            Predicate attributeValueHasIdField = criteriaBuilder.like(userAttributeJoin.get("value"), formatQueryParam("\"id\":"));
            Predicate attributeValueHasGivenOrgId = criteriaBuilder.like(userAttributeJoin.get("value"), formatQueryParam(organizationId));
            return criteriaBuilder.and(attributeNameIsOrgDetails, attributeValueHasIdField, attributeValueHasGivenOrgId);
        };
    }

    private String formatQueryParam(String queryParam) {
        return "%" + queryParam.toLowerCase() + "%";
    }
}
