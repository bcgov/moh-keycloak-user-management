package ca.bc.gov.hlth.mohums.userSearch.user;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import java.util.List;

@Component
public class UserSpecifications {

    //escape lowercase
    //search by field on its own
    //search -> username, last name, first name, email (OR)
    //filter out service accounts

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

    /*
    This method doesn't use formatQueryParam method because according to Keycloak API specification,
     the default search behaviour (when using search query param) is prefix-based
     */
    public Specification<UserEntity> userParamsLike(String searchValue) {
        String searchParam = searchValue.toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> {
            Predicate usernamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), searchParam);
            Predicate firstNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), searchParam);
            Predicate lastNamePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), searchParam);
            Predicate emailPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchParam);
            return criteriaBuilder.or(usernamePredicate, firstNamePredicate, lastNamePredicate, emailPredicate);
        };
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
