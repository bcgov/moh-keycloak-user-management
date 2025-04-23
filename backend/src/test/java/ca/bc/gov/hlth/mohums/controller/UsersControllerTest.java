package ca.bc.gov.hlth.mohums.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class UsersControllerTest {

    private final UsersController u = new UsersController(null, null, vanityHostname, null);

    private static final String vanityHostname = "http://localhost";
    
    @Test
    void testNull() {
        Assertions.assertThrows(NullPointerException.class, () -> u.convertLocationHeader(null));
    }

    @Test
    public void testNoLocation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(null);
        HttpHeaders newHeaders = u.convertLocationHeader(httpHeaders);
        assertNotNull(newHeaders);
        assertNull(newHeaders.getLocation());
    }

    @Test
    public void testEmptyLocation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create(""));
        HttpHeaders newHeaders = u.convertLocationHeader(httpHeaders);
        assertNotNull(newHeaders);
        assertEquals("", newHeaders.getLocation().toASCIIString());
    }

    @Test
    public void testUserLocation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/moh_applications/users/d862b0ee-1e3f-423b-a200-55f2e8f103d9"));
        HttpHeaders newHeaders = u.convertLocationHeader(httpHeaders);
        assertNotNull(newHeaders);
        assertEquals(vanityHostname + "/users/d862b0ee-1e3f-423b-a200-55f2e8f103d9", newHeaders.getLocation().toASCIIString());
    }

    @Test
    public void testUserLocation_badGuid() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/moh_applications/users/NOT_A_GUID"));
        HttpHeaders newHeaders = u.convertLocationHeader(httpHeaders);
        assertNotNull(newHeaders);
        // If the Location header doesn't match what we expect, namely /users/VALID_GUID, don't touch it.
        assertEquals("https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/moh_applications/users/NOT_A_GUID", newHeaders.getLocation().toASCIIString());
    }

}