package ca.bc.gov.hlth.mohums.userSearch.role;

import ca.bc.gov.hlth.mohums.userSearch.role.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {

    @Query("SELECT r FROM RoleEntity r WHERE r.realmId = 'moh_applications' AND r.clientId = ?1 AND r.name IN ?2")
    List<RoleEntity> findMohApplicationsRolesByClientAndNames (String clientId, List<String> roleNames);

    @Query("SELECT r FROM RoleEntity r WHERE r.realmId = 'moh_applications' AND r.clientId = ?1")
    List<RoleEntity> findMohApplicationsRolesByClient (String clientId);
}
