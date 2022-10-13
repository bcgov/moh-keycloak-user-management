package ca.bc.gov.hlth.mohums.controller;


import ca.bc.gov.hlth.mohums.webclient.OrganizationsApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrganizationsController {
    
    private final OrganizationsApiService organizationsApiService;

    public OrganizationsController(OrganizationsApiService organizationsApiService) {
        this.organizationsApiService = organizationsApiService;
    }

    @GetMapping("/organizations")
    public ResponseEntity<List<Object>> getOrganizations() {
        return organizationsApiService.getOrganizations();
    }

    @GetMapping("/organizations/{organizationId}")
    public ResponseEntity<Object> getOrganizationById(@PathVariable String organizationId) {
        return organizationsApiService.getOrganization(organizationId);
    }

    @PostMapping("/organizations")
    public ResponseEntity<Object> addOrganization(@RequestBody Object newOrganization) {
        return organizationsApiService.addOrganization(newOrganization);
    }

    @PutMapping("/organizations/{organizationId}")
    public ResponseEntity<Object> updateOrganization(@PathVariable String organizationId, @RequestBody Object organizationToUpdate) {
        return organizationsApiService.updateOrganization(organizationId, organizationToUpdate);
    }


}
