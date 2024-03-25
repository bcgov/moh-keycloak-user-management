package ca.bc.gov.hlth.mohums.user;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;



    @ParameterizedTest
    @MethodSource("provideLastLoginAfterDates")
    public void testLastLoginAfterGivenDate(Long dateInMillis, boolean isResultEmpty){
        List<LastLogDate> results = eventRepository.findMohApplicationsLastLoginEventsAfterGivenDate(dateInMillis);

        assertEquals(isResultEmpty, results.isEmpty());
        assertTrue(results.stream().allMatch(lastLogDate -> lastLogDate.getLastLogin() > dateInMillis));
    }

    @ParameterizedTest
    @MethodSource("provideLastLoginAfterDates")
    public void testLastLoginWithGivenClientAfterGivenDate(Long dateInMillis, boolean isResultEmpty){
        List<LastLogDate> results = eventRepository.findMohApplicationsLastLoginEventsWithGivenClientAfterGivenDate(dateInMillis, "USER-MANAGEMENT");

        assertEquals(isResultEmpty, results.isEmpty());
        assertTrue(results.stream().allMatch(lastLogDate -> lastLogDate.getLastLogin() > dateInMillis));
    }

    private static Stream<Arguments> provideLastLoginAfterDates(){
        long tomorrow = LocalDate.now().plusDays(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long lastWeek = LocalDate.now().minusWeeks(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long lastMonth = LocalDate.now().minusMonths(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long lastYear = LocalDate.now().minusYears(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return Stream.of(
                Arguments.of(tomorrow, true),
                Arguments.of(lastWeek, false),
                Arguments.of(lastMonth, false),
                Arguments.of(lastYear, false)
        );
    }

    //what client should it be based on? UMC?
    //test lastlogin after date with client id + no results -> disabled since login is not automatic
    //test lastlogin after date with client id and roles + no results -> disabled since login is not automatic

}
