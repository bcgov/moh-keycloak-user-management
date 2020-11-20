package ca.bc.gov.hlth.mohums.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class UsersControllerTest {

    private final UsersController u = new UsersController(null, "localhost");

    @Test
    void testNull() {
        assertNotNull(u.getHeaders(null));
    }

    @Test
    public void testNoLocation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(null);
        HttpHeaders newHeaders = u.getHeaders(httpHeaders);
        assertNotNull(newHeaders);
        assertNull(newHeaders.getLocation());
    }

    @Test
    public void testEmptyLocation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create(""));
        HttpHeaders newHeaders = u.getHeaders(httpHeaders);
        assertNotNull(newHeaders);
        assertNull(newHeaders.getLocation());
    }

    @Test
    public void testUserLocation() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/moh_applications/users/d862b0ee-1e3f-423b-a200-55f2e8f103d9"));
        HttpHeaders newHeaders = u.getHeaders(httpHeaders);
        assertNotNull(newHeaders);
        assertEquals("https://localhost/users/d862b0ee-1e3f-423b-a200-55f2e8f103d9", newHeaders.getLocation().toASCIIString());
    }

    @Test
    public void testUserLocation_badGuid() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(URI.create("https://common-logon-dev.hlth.gov.bc.ca/auth/admin/realms/moh_applications/users/NOT_A_GUID"));
        HttpHeaders newHeaders = u.getHeaders(httpHeaders);
        assertNotNull(newHeaders);
        assertNull(newHeaders.getLocation());
    }

}