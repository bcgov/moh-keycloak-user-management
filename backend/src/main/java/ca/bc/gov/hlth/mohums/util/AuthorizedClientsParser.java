package ca.bc.gov.hlth.mohums.util;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AuthorizedClientsParser {

    private final Logger logger = LoggerFactory.getLogger(AuthorizedClientsParser.class);

    public List<String> parse(String token) {
        JSONArray roles = parseRoles(token);
        return parseClients(roles);
    }

    private JSONArray parseRoles(String token) {

        JSONParser jsonParser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
        JSONArray roles = new JSONArray();
        String[] parts = token.split("\\.");

        try {
            JSONObject payload = (JSONObject) jsonParser.parse(decodeBase64(parts[1]));
            JSONObject resourceAccess = (JSONObject) payload.get("resource_access");
            JSONObject userManagementService = (JSONObject) resourceAccess.get("USER-MANAGEMENT-SERVICE");
            roles = (JSONArray) userManagementService.get("roles");
        } catch (ParseException ex) {
            logger.error(ex.getMessage());
            throw new RuntimeException(ex);
        }

        return roles;
    }

    private List<String> parseClients(JSONArray roles) {

        List<String> clients = new ArrayList<>();

        roles.forEach(role -> {
            String[] parts = ((String) role).split("view-client-");
            if (parts.length == 2) {
                clients.add(parts[1].toLowerCase());
            }
        });

        return clients;
    }

    public static String decodeBase64(String stringToDecode) {

        byte[] bytesToDecode = stringToDecode.getBytes(StandardCharsets.UTF_8);
        byte[] decodedBytes = Base64.getDecoder().decode(bytesToDecode);

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }
}
