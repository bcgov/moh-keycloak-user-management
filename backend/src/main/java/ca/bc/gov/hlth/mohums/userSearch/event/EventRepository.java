package ca.bc.gov.hlth.mohums.userSearch.event;

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

    @Query(value = "SELECT ee.USER_ID AS userId, MAX(ee.EVENT_TIME) AS lastLogin FROM KEYCLOAK.EVENT_ENTITY ee" +
            " WHERE ee.REALM_ID = 'moh_applications' AND ee.type = 'LOGIN' AND ee.EVENT_TIME > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000" +
            " GROUP BY ee.user_id HAVING MAX(ee.EVENT_TIME) < ?1",
            nativeQuery = true)
    List<LastLogDate> findMohApplicationsLastLoginEventsBeforeGivenDate(long milliseconds);

    @Query(value = "SELECT ee.USER_ID AS userId, MAX(ee.EVENT_TIME) AS lastLogin FROM KEYCLOAK.EVENT_ENTITY ee" +
            " WHERE ee.REALM_ID = 'moh_applications' AND ee.type = 'LOGIN' " +
            "AND ee.EVENT_TIME > (SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000 AND ee.CLIENT_ID = ?2" +
            " GROUP BY ee.user_id HAVING MAX(ee.EVENT_TIME) < ?1",
            nativeQuery = true)
    List<LastLogDate> findMohApplicationsLastLoginEventsWithGivenClientBeforeGivenDate(long milliseconds, String clientId);

    @Query(value = "SELECT ue.id FROM KEYCLOAK.USER_ENTITY ue  LEFT JOIN KEYCLOAK.EVENT_ENTITY ee ON ee.user_id = ue.id " +
            "WHERE ee.user_id IS NULL AND ue.created_timestamp < ((SYSDATE-365-TO_DATE('1970-01-01','YYYY-MM-DD'))*24*60*60*1000) AND ue.REALM_ID = 'moh_applications'",
            nativeQuery = true)
    List<String> findMohApplicationUsersThatExistForOverAYearWithoutLoginEvents();


}
