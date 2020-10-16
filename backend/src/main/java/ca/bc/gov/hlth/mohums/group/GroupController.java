package ca.bc.gov.hlth.mohums.group;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GroupController {

    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/groups")
    public Group groups() {
        counter.incrementAndGet();
        return new Group(counter.toString(), "Test Group", "/Test Group");
    }
}
