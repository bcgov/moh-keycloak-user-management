package ca.bc.gov.hlth.mohums.user;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

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

    //TODO: Is this the right place for this transformation? Maybe service class
    private String formatQueryParam(String queryParam) {
        return "%" + queryParam.toLowerCase() + "%";
    }
}
