package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.webclient.WebClientService;
import net.minidev.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
// import org.springframework.http.converter.xml.SourceHttpMessageConverter;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
// import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
// import org.springframework.util.CollectionUtils;
// import org.springframework.util.LinkedMultiValueMap;
// import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;



@RestController
public class OrganizationsController {
    private Map<String,JSONObject> organizations;
    private Map<String, String> orgMap;
    // private final WebClientService webClientService;
    // private final String vanityHostname;

    public OrganizationsController(WebClientService webClientService, @Value("${config.vanity-hostname}") String vanityHostname) {
        organizations = new HashMap<>();
        orgMap = new HashMap<>();
        
        orgMap.put( "Ministry of Health","00000010");
        organizations.put("00000010", new JSONObject(Map.of("id", "00000010", "name", "Ministry of Health")));
        
        orgMap.put("Government of Yukon - Dept of Health", "00000025");
        organizations.put("00000025", new JSONObject(Map.of("id", "00000025", "name",  "Government of Yukon - Dept of Health")));
        
        orgMap.put("Providence Health Care","00000032");
        organizations.put("00000032", new JSONObject(Map.of("id", "00000032", "name",  "Providence Health Care")));
        
        orgMap.put("TimberWest Forest Company","00001480");
        organizations.put("00001480", new JSONObject(Map.of("id", "00001480", "name",  "TimberWest Forest Company")));
        
        orgMap.put("Northern Health Authority", "00001762");
        organizations.put("00001762", new JSONObject(Map.of("id", "00001762", "name",  "Northern Health Authority")));
        
        orgMap.put("Interior Health Authority", "00001763");
        organizations.put("00001763", new JSONObject(Map.of("id", "00001763", "name",  "Interior Health Authority")));
        
        orgMap.put("Vancouver Island Health Authority", "00001764");
        organizations.put("00001764", new JSONObject(Map.of("id", "00001764", "name",  "Vancouver Island Health Authority")));
        
        orgMap.put("Vancouver Coastal Health Authority", "00001765");
        organizations.put("00001765", new JSONObject(Map.of("id", "00001765", "name",  "Vancouver Coastal Health Authority")));
        
        orgMap.put("Fraser Health Authority", "00001766");
        organizations.put("00001766", new JSONObject(Map.of("id", "00001766", "name",  "Fraser Health Authority")));
        
        orgMap.put("Provincial Health Services Authority", "00001767");
        organizations.put("00001767", new JSONObject(Map.of("id", "00001767", "name",  "Provincial Health Services Authority")));
       
        orgMap.put("Provider Registry Project", "00002158");
        organizations.put("00002158", new JSONObject(Map.of("id", "00002158",  "name", "Provider Registry Project")));
        
        orgMap.put("ICBC - Head Office", "00005502");
        organizations.put("00005502", new JSONObject(Map.of("id", "00005502",  "name", "ICBC - Head Office")));
        
        orgMap.put("Belkorp Industries Inc - Parent", "00006501");
        organizations.put("00006501", new JSONObject(Map.of("id", "00006501",  "name", "Belkorp Industries Inc - Parent")));
        
        orgMap.put("First Nations Health Authority - Employees", "00015824");
        organizations.put("00015824", new JSONObject(Map.of("id", "00015824",  "name", "First Nations Health Authority - Employees")));
    }

    @GetMapping("/organizations")
    public Collection<JSONObject> getOrganizations(
        @RequestParam Optional<String> id,
        @RequestParam Optional<String> name
    ) {
        System.out.println("EABOUAFOAF");

        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        id.ifPresent(idValue -> queryParams.add("id", idValue.toString()));
        name.ifPresent(nameValue -> queryParams.add("name", nameValue.toString()));
     
    return organizations.values();
    }



    @PostMapping("/organizations")
    public void createOrganization(@RequestBody JSONObject body) {
        String id = (String) body.get("id");

        if(organizations.containsKey(id)){
            System.out.println("ERROR HANDLING HERE.");
        }else{
            organizations.put (id, body);
        }
        organizations.entrySet().forEach((e) -> System.out.println(e.toString()));
    }

}
