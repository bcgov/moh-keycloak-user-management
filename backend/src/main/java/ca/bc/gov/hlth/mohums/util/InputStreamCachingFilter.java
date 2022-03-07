/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.hlth.mohums.util;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filter to setup a cached request InputStream so we can audit the request body
 * @author greg.perkins
 */
@Component
@Order(1)
public class InputStreamCachingFilter implements Filter{

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException {
        fc.doFilter(new InputStreamCachingHttpServletRequest((HttpServletRequest)request), response);
    }
    
}
