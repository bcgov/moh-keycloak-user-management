package ca.bc.gov.hlth.mohums.util;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
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
