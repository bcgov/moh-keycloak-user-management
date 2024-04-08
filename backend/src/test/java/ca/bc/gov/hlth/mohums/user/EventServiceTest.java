package ca.bc.gov.hlth.mohums.user;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class EventServiceTest {

    @Autowired
    private EventService eventService;

    @MockBean
    private EventRepository eventRepository;


    private final String date = "2024-04-02";
    private final long dateInMilliseconds = 1712037600000L;

    @Test
    public void testGetLastLoginEventsAfterGivenDate() {
        List<LastLogDate> lastLogDateList = getMockLastLogDateList();
        Map<String, String> expected = Map.of(lastLogDateList.get(0).getUserId(), lastLogDateList.get(0).getLastLogin().toString(),
                lastLogDateList.get(1).getUserId(), lastLogDateList.get(1).getLastLogin().toString());

        Mockito.when(eventRepository.findMohApplicationsLastLoginEventsAfterGivenDate(dateInMilliseconds)).thenReturn(getMockLastLogDateList());
        Map<String, String> results = eventService.getLastLoginEventsAfterGivenDate(date);

        assertEquals(expected, results);
    }

    @Test
    public void testGetLastLoginEventsBeforeGivenDate() {
        List<LastLogDate> lastLogDateList = getMockLastLogDateList();
        List<String> usersWithoutLogins = List.of("id-3", "id-4");
        Map<String, String> expected = Map.of(lastLogDateList.get(0).getUserId(), lastLogDateList.get(0).getLastLogin().toString(),
                lastLogDateList.get(1).getUserId(), lastLogDateList.get(1).getLastLogin().toString(),
                usersWithoutLogins.get(0), "Over a year ago",
                usersWithoutLogins.get(1), "Over a year ago");

        Mockito.when(eventRepository.findMohApplicationsLastLoginEventsBeforeGivenDate(dateInMilliseconds)).thenReturn(getMockLastLogDateList());
        Mockito.when(eventRepository.findMohApplicationUsersThatExistForOverAYearWithoutLoginEvents()).thenReturn(usersWithoutLogins);
        Map<String, String> results = eventService.getLastLoginEventsBeforeGivenDate(date);

        assertEquals(expected, results);
    }


    private List<LastLogDate> getMockLastLogDateList() {
        LastLogDate lastLogDateOne = new LastLogDate() {
            @Override
            public String getUserId() {
                return "id-1";
            }

            @Override
            public Long getLastLogin() {
                return 1712349913139L;
            }
        };

        LastLogDate lastLogDateTow = new LastLogDate() {
            @Override
            public String getUserId() {
                return "id-2";
            }

            @Override
            public Long getLastLogin() {
                return 1712349913149L;
            }
        };

        return List.of(lastLogDateOne, lastLogDateTow);
    }


}
