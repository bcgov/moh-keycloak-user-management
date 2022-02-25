package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import net.minidev.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;


@RestController
public class OrganizationsController {
    private Map<String, JSONObject> organizations;

    public OrganizationsController(WebClientService webClientService, @Value("${config.vanity-hostname}") String vanityHostname) {
        organizations = new HashMap<>();

        organizations.put("00000010", new JSONObject(Map.of("id", "00000010", "name", "Ministry of Health")));
        organizations.put("00000025", new JSONObject(Map.of("id", "00000025", "name", "Government of Yukon - Dept of Health")));
        organizations.put("00000032", new JSONObject(Map.of("id", "00000032", "name", "Providence Health Care")));
        organizations.put("00001480", new JSONObject(Map.of("id", "00001480", "name", "TimberWest Forest Company")));
        organizations.put("00001762", new JSONObject(Map.of("id", "00001762", "name", "Northern Health Authority")));
        organizations.put("00001763", new JSONObject(Map.of("id", "00001763", "name", "Interior Health Authority")));
        organizations.put("00001764", new JSONObject(Map.of("id", "00001764", "name", "Vancouver Island Health Authority")));
        organizations.put("00001765", new JSONObject(Map.of("id", "00001765", "name", "Vancouver Coastal Health Authority")));
        organizations.put("00001766", new JSONObject(Map.of("id", "00001766", "name", "Fraser Health Authority")));
        organizations.put("00001767", new JSONObject(Map.of("id", "00001767", "name", "Provincial Health Services Authority")));
        organizations.put("00002158", new JSONObject(Map.of("id", "00002158", "name", "Provider Registry Project")));
        organizations.put("00005502", new JSONObject(Map.of("id", "00005502", "name", "ICBC - Head Office")));
        organizations.put("00006501", new JSONObject(Map.of("id", "00006501", "name", "Belkorp Industries Inc - Parent")));
        organizations.put("00015824", new JSONObject(Map.of("id", "00015824", "name", "First Nations Health Authority - Employees")));
    }

    @GetMapping("/organizations")
    public Collection<JSONObject> getOrganizations() {
        return organizations.values();
    }


    @GetMapping("/organizations/{organizationId}")
    public JSONObject getOrganization(@PathVariable String organizationId) {
        return organizations.get(organizationId);
    }


    @PostMapping("/organizations")
    public void createOrganization(@RequestBody JSONObject body) {
        String id = (String) body.get("id");

        // todo: error handling
        if (organizations.containsKey(id)) {
            System.out.println("ERROR HANDLING HERE: an org with this name already exists");
        } else {
            organizations.put(id, body);
        }
    }

}
