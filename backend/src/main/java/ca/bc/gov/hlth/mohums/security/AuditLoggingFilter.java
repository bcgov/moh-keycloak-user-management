/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.bc.gov.hlth.mohums.security;

import jakarta.servlet.http.HttpServletRequest;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import static ca.bc.gov.hlth.mohums.util.AuthorizedClientsParser.decodeBase64;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Custom logging filter implementation to log pertinent request info
 * to a separate audit log file
 * 
 * @author greg.perkins
 */
public class AuditLoggingFilter extends CommonsRequestLoggingFilter{
    
    /* List of HTTP Methods that we want to audit*/
    private final static Collection AUDITABLE_METHODS = Arrays.asList(new String[]{"POST","PUT","DELETE"});
    /* JSON Parser instance */
    private final JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
    
    
    /**
     * Do nothing
     * @param request
     * @param message 
     */
    @Override
    protected void afterRequest(HttpServletRequest request, String message) {}

    /**
     * Only log events that alter data
     * @param request HttpServletRequest - incoming HTTP Request
     * @return boolean
     */
    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        return AUDITABLE_METHODS.contains(request.getMethod()); 
    }

    /**
     * Log the request
     * @param request HttpServletRequest - incoming HTTP Request
     * @param message String
     */
    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        //Use a JSONObject as our message
        JSONObject messageBody = new JSONObject();
        
        //Add the Username from the auth token
        messageBody.put("username", parseUsername(request.getHeader("authorization")));
        
        //Add HTTP Method and URL from request object
        messageBody.put("method",request.getMethod());
        messageBody.put("url",request.getRequestURI());
        
        //Try to add the body of the HTTP Request
        try{
            String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            messageBody.put("body",jsonParser.parse(body));
        }catch(Exception e){
            //Can't read the body, so it will be omitted
        }
        
        //Add any request parameters
        if (!request.getParameterMap().isEmpty()){
            JSONObject params = new JSONObject();
            request.getParameterMap().keySet().forEach((key) -> {
                params.put(key,request.getParameter(key));
            });
            messageBody.put("params",params);
        }
        
        //Log to debug level
        logger.debug(messageBody.toJSONString());
    }
    
    /**
     * Returns the username from the encoded JWT token
     * @param encodedToken String
     * @return String - contents of the preferred_username field
     */
    private String parseUsername(String encodedToken) {        
        try {
            String json = decodeBase64(encodedToken.split("\\.")[1]);
            JSONObject payload = (JSONObject) jsonParser.parse(json);
            return (String)payload.get("preferred_username");            
        } catch (Exception ex) {
            return "UNAUTHENTICATED";
        }
    }
}
