package ca.bc.gov.hlth.mohums.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

/**
 * Request Wrapper to use a cached InputStream so it can be read multiple times
 *
 * @author greg.perkins
 */
public class InputStreamCachingHttpServletRequest extends HttpServletRequestWrapper {

    /* Cached message body */
    private byte[] body = null;

    /**
     * Default constructor: read the incoming request's InputStream into our cache
     * @param request HttpServletRequest
     */
    public InputStreamCachingHttpServletRequest(HttpServletRequest request) {
        super(request);
        try {
            body = StreamUtils.copyToByteArray(request.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(InputStreamCachingHttpServletRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a custom InputStream that reads from our cached body
     * @return ServletInputStream
     * @throws IOException 
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {

            @Override
            public int read() throws IOException {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener arg0) {
            }
        };
    }

}
