package ca.bc.gov.hlth.mohums.userSearch;

import ca.bc.gov.hlth.mohums.userSearch.event.EventRepository;
import ca.bc.gov.hlth.mohums.userSearch.event.LastLogDate;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
        assertTrue(results.stream().allMatch(lastLogDate -> (Long)lastLogDate.getLastLogin() > dateInMillis));
    }

    @ParameterizedTest
    @MethodSource("provideLastLoginAfterDates")
    public void testLastLoginWithGivenClientAfterGivenDate(Long dateInMillis, boolean isResultEmpty){
        List<LastLogDate> results = eventRepository.findMohApplicationsLastLoginEventsWithGivenClientAfterGivenDate(dateInMillis, "USER-MANAGEMENT");

        assertEquals(isResultEmpty, results.isEmpty());
        assertTrue(results.stream().allMatch(lastLogDate -> (Long)lastLogDate.getLastLogin() > dateInMillis));
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


    @ParameterizedTest
    @MethodSource("provideLastLoginBeforeDates")
    public void testLastLoginBeforeGivenDate(Long dateInMillis, boolean isResultEmpty){
        List<LastLogDate> results = eventRepository.findMohApplicationsLastLoginEventsBeforeGivenDate(dateInMillis);

        assertEquals(isResultEmpty, results.isEmpty());
        //Java casts Object inside LastLogDate to BigDecimal and not Long, so this conversion is required
        assertTrue(results.stream().allMatch(lastLogDate -> BigDecimal.valueOf(dateInMillis).compareTo((BigDecimal) lastLogDate.getLastLogin()) > 0));
    }

    private static Stream<Arguments> provideLastLoginBeforeDates(){
        long tomorrow = LocalDate.now().plusDays(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long lastWeek = LocalDate.now().minusWeeks(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long lastMonth = LocalDate.now().minusMonths(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long lastYear = LocalDate.now().minusYears(1L).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        return Stream.of(
                Arguments.of(tomorrow, false),
                Arguments.of(lastWeek, false),
                Arguments.of(lastMonth, false),
                Arguments.of(lastYear, true) //login events are stored for a year so result set should be empty
        );
    }

    @Test
    public void testGetUsersThatExistForOverAYearWithoutLoginEvents(){
        List<LastLogDate> results = eventRepository.findMohApplicationUsersThatExistForOverAYearWithoutLoginEvents();

        assertFalse(results.isEmpty());
    }

}
