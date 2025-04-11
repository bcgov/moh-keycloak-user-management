package ca.bc.gov.hlth.mohums.userSearch.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
    @Query("SELECT u FROM UserEntity u WHERE u.realmId = 'moh_applications' AND u.username NOT LIKE 'service-account-%' AND u.id IN :userIdList")
    List<UserEntity> findMohApplicationsUsersByIdList(@Param("userIdList") List<String> userIdList);

    @Query(value="SELECT r.NAME AS realmName, fi.user_id AS userId FROM KEYCLOAK.FEDERATED_IDENTITY fi JOIN KEYCLOAK.REALM r ON r.ID = fi.REALM_ID WHERE fi.FEDERATED_USER_ID = ?1", nativeQuery = true)
    List<ApplicationRealmUser> findFederatedApplicationRealmUsers(String federatedUserId);
}
