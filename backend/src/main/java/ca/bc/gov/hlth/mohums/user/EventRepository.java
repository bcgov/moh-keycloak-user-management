package ca.bc.gov.hlth.mohums.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {

    @Query("SELECT ee.userId AS userId, MAX(ee.time) AS lastLogin FROM EventEntity ee" +
            " WHERE ee.realmId = 'moh_applications' AND ee.type = 'LOGIN' AND ee.time > ?1" +
            " GROUP BY ee.userId")
    List<LastLogDate> findMohApplicationsLastLoginEventsAfterGivenDate(long milliseconds);

    @Query("SELECT ee.userId AS userId, MAX(ee.time) AS lastLogin FROM EventEntity ee" +
            " WHERE ee.realmId = 'moh_applications' AND ee.type = 'LOGIN' AND ee.time > ?1 AND ee.clientId = ?2" +
            " GROUP BY ee.userId")
    List<LastLogDate> findMohApplicationsLastLoginEventsWithGivenClientAfterGivenDate(long milliseconds, String clientId);

//    List<LastLogDate> findMohApplicationsLastLoginEventsBeforeGivenDate(long milliseconds);
//
//    List<LastLogDate> findMohApplicationsLastLoginEventsWithGivenRoleBeforeGivenDate(long milliseconds);


}
