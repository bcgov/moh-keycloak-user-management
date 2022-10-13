package ca.bc.gov.hlth.mohums.webclient;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationsApiService {

    private final String organizationsPath = "/organizations";
    private final ExternalApiCaller externalApiCaller;

    public OrganizationsApiService(@Qualifier("orgApiCaller") ExternalApiCaller externalApiCaller) {
        this.externalApiCaller = externalApiCaller;
    }

    public ResponseEntity<List<Object>> getOrganizations() {
        return externalApiCaller.getList(organizationsPath);
    }

    public ResponseEntity<Object> getOrganization(String organizationId) {
        return externalApiCaller.get(organizationsPath + "/" + organizationId, null);
    }

    public ResponseEntity<Object> addOrganization(Object newOrganization) {
        return externalApiCaller.post(organizationsPath, newOrganization);
    }

    public ResponseEntity<Object> updateOrganization(String organizationId, Object organizationToUpdate) {
        //make sure both org ids are the same
        return externalApiCaller.put(organizationsPath + "/" + organizationId, organizationToUpdate);
    }
}
