package ca.bc.gov.hlth.mohums.controller;


import ca.bc.gov.hlth.mohums.webclient.OrganizationsApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrganizationsController {

    private final OrganizationsApiService webClientService;
    
    public OrganizationsController(OrganizationsApiService webClientService) {
        this.webClientService = webClientService;
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<Object>> getOrganizations() {
        return webClientService.getOrganizations();
    }


}
