package ca.bc.gov.hlth.mohums.user;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;

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

    public Specification<UserEntity> hasOrganizationWithGivenId(String organizationId) {
        return (root, query, criteriaBuilder) -> {
            Join<UserEntity, UserAttributeEntity> userAttributeJoin = root.join("attributes", JoinType.INNER);
            Predicate attributeNameIsOrgDetails = criteriaBuilder.equal(userAttributeJoin.get("name"), "org_details");
            //TODO: Consul if like is enough - there's a whole JSON object underneath, maybe {"Id":"00000010",% as regex?
            Predicate attributeHasGivenValue = criteriaBuilder.like(userAttributeJoin.get("value"), formatQueryParam(organizationId));
            return criteriaBuilder.and(attributeNameIsOrgDetails, attributeHasGivenValue);
        };
    }

    //TODO: Is this the right place for this transformation? Maybe service class
    private String formatQueryParam(String queryParam) {
        return "%" + queryParam.toLowerCase() + "%";
    }
}
