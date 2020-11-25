package ca.bc.gov.hlth.mohums.controller;

import ca.bc.gov.hlth.mohums.exceptions.HttpUnauthorizedException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler({HttpUnauthorizedException.class})
    @ResponseBody
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    ResponseEntity<Object> unauthorizedAccess(Exception e) {

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("WWW-Authenticate",
                "Bearer error=\"insufficient_scope\", " +
                        "error_description=\"The request requires higher privileges than provided by the access token.\", " +
                        "error_uri=\"https://tools.ietf.org/html/rfc6750#section-3.1");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).headers(responseHeaders).body("");
    }
}
