package ca.bc.gov.hlth.mohums.webclient;

import ca.bc.gov.hlth.mohums.model.*;
import ca.bc.gov.hlth.mohums.userSearch.user.ApplicationRealmUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KeycloakApiService {

    private static final String CLIENTS_PATH = "/clients";
    private static final String USERS_PATH = "/users";
    private static final String GROUPS_PATH = "/groups";
    private static final String IDENTITY_PROVIDER_LINKS_PATH = "/federated-identity";
    private static final String USER_CLIENT_ROLE_MAPPING_PATH = "/role-mappings/clients/";
    private static final String SERVICE_ACCOUNT_PREFIX = "service-account-";
    private static final String USERNAME_ATTRIBUTE = "username";
    private static final String SERVICE_ACCOUNT_FORBIDDEN_MESSAGE = "Service account cannot be accessed through UMC";
    private final ExternalApiCaller keycloakMohExternalApiCaller;
    private final ExternalApiCaller keycloakMasterExternalApiCaller;

    public KeycloakApiService(@Qualifier("keycloakMohApiCaller") ExternalApiCaller keycloakMohExternalApiCaller, @Qualifier("keycloakMasterApiCaller") ExternalApiCaller keycloakMasterExternalApiCaller) {
        this.keycloakMohExternalApiCaller = keycloakMohExternalApiCaller;
        this.keycloakMasterExternalApiCaller = keycloakMasterExternalApiCaller;
    }

    // Clients
    @SuppressWarnings("unchecked")
    public ResponseEntity<List<Object>> getClients() {
        ResponseEntity<List<Object>> clientsResponse = keycloakMohExternalApiCaller.getList(CLIENTS_PATH);
        if(clientsResponse.getStatusCode().is2xxSuccessful() && clientsResponse.hasBody()){
            List<Object> clientsWithoutSecret = clientsResponse.getBody().stream()
                    .map(client -> {
                        LinkedHashMap<String, Object> clientMap = (LinkedHashMap<String, Object>) client;
                        clientMap.remove("secret");
                        return clientMap;
                    })
                    .collect(Collectors.toList());
            clientsResponse = new ResponseEntity<>(clientsWithoutSecret, clientsResponse.getHeaders(), clientsResponse.getStatusCode());
        }
        return clientsResponse;

    }
    public ResponseEntity<Object> getClient(String clientId) {
        String path = CLIENTS_PATH + "/" + clientId;
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<List<Object>> getClientRoles(String clientId) {
        String path = String.format("%s/%s/roles", CLIENTS_PATH, clientId);
        return keycloakMohExternalApiCaller.getList(path, null);
    }

    public ResponseEntity<List<Object>> getUsersInRole(String clientId, String roleName,
                                                       MultiValueMap<String, String> queryParams) {
        String path = String.format("%s/%s/roles/%s/users", CLIENTS_PATH, clientId, roleName);
        return keycloakMohExternalApiCaller.getList(path, queryParams);
    }

    public ResponseEntity<List<Object>> getUsersInRoleInGivenRealm(String realmName, String clientId, String roleName,
                                                                   MultiValueMap<String, String> queryParams) {
        String path = String.format("/%s%s/%s/roles/%s/users", realmName, CLIENTS_PATH, clientId, roleName);
        return keycloakMasterExternalApiCaller.getList(path, queryParams);
    }

    // Groups
    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> getGroups() {
        ResponseEntity<Object> response = keycloakMohExternalApiCaller.get(GROUPS_PATH, null);
        ArrayList<LinkedHashMap<String, Object>> groups = (ArrayList<LinkedHashMap<String, Object>>) response.getBody();
        List<Group> groupList = groups.stream().map(g -> getGroupById(g.get("id").toString())).collect(Collectors.toList());
        return ResponseEntity.of(Optional.of(groupList));
    }

    // Group details
    @SuppressWarnings("unchecked")
    private Group getGroupById(String groupId) {
        String path = String.format("%s/%s", GROUPS_PATH, groupId);
        ResponseEntity<Object> response = keycloakMohExternalApiCaller.get(path, null);
        return GroupDescriptionGenerator.createGroupWithDescription((LinkedHashMap<String, Object>) response.getBody());
    }

    public ResponseEntity<Object> getGroupMembers(String groupId) {
        String path = String.format("%s/%s/members", GROUPS_PATH, groupId);
        return keycloakMohExternalApiCaller.get(path, null);
    }

    // Users
    public ResponseEntity<List<Object>> getUsers(MultiValueMap<String, String> queryParams) {
        ResponseEntity<List<Object>> searchResults = keycloakMohExternalApiCaller.getList(USERS_PATH, queryParams);
        ResponseEntity<List<Object>> response;

        if (searchResults.getStatusCode().is2xxSuccessful()) {
            List<Object> users = filterOutServiceAccounts(searchResults.getBody());

            response = ResponseEntity
                    .status(searchResults.getStatusCode())
                    .body(users);
        } else {
            response = searchResults;
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    private List<Object> filterOutServiceAccounts(List<Object> searchResults) {
        return searchResults.stream()
                .map(user -> (LinkedHashMap<String, Object>) user)
                .filter(user -> !isServiceAccount(user))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> getUser(String userId) {
        String path = USERS_PATH + "/" + userId;
        ResponseEntity<Object> userResponse = keycloakMohExternalApiCaller.get(path, null);

        if (userResponse.getStatusCode().is2xxSuccessful()) {
            LinkedHashMap<String, Object> user = (LinkedHashMap<String, Object>) userResponse.getBody();
            if (isServiceAccount(user)) {
                userResponse = ResponseEntity.status(HttpStatus.FORBIDDEN).body(SERVICE_ACCOUNT_FORBIDDEN_MESSAGE);
            }
        }

        return userResponse;
    }

    private boolean isServiceAccount(LinkedHashMap<String, Object> userAttributes) {
        return userAttributes.get(USERNAME_ATTRIBUTE).toString().startsWith(SERVICE_ACCOUNT_PREFIX);
    }

    public ResponseEntity<Object> createUser(Object data) {
        return keycloakMohExternalApiCaller.post(USERS_PATH, data);
    }

    public ResponseEntity<Object> updateUser(String userId, Object data) {
        String path = USERS_PATH + "/" + userId;
        return keycloakMohExternalApiCaller.put(path, data);
    }

    public ResponseEntity<Object> getAssignedUserClientRoleMappings(String userId, String clientId) {
        String path = USERS_PATH + "/" + userId + USER_CLIENT_ROLE_MAPPING_PATH + clientId;
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> getAvailableUserClientRoleMappings(String userId, String clientId) {
        String path = USERS_PATH + "/" + userId + USER_CLIENT_ROLE_MAPPING_PATH + clientId + "/available";
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> getEffectiveUserClientRoleMappings(String userId, String clientId) {
        String path = USERS_PATH + "/" + userId + USER_CLIENT_ROLE_MAPPING_PATH + clientId + "/composite";
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> addUserClientRole(String userId, String clientId, Object data) {
        String path = USERS_PATH + "/" + userId + USER_CLIENT_ROLE_MAPPING_PATH + clientId;
        return keycloakMohExternalApiCaller.post(path, data);
    }

    public ResponseEntity<Object> deleteUserClientRole(String userId, String clientId, Object data) {
        String path = USERS_PATH + "/" + userId + USER_CLIENT_ROLE_MAPPING_PATH + clientId;
        return keycloakMohExternalApiCaller.delete(path, data);
    }

    public ResponseEntity<Object> getUserGroups(String userId) {
        String path = USERS_PATH + "/" + userId + GROUPS_PATH;
        return keycloakMohExternalApiCaller.get(path, null);
    }

    public ResponseEntity<Object> addUserGroups(String userId, String groupId) {
        String path = USERS_PATH + "/" + userId + GROUPS_PATH + "/" + groupId;
        return keycloakMohExternalApiCaller.put(path);
    }

    public ResponseEntity<Object> removeUserGroups(String userId, String groupId) {
        String path = USERS_PATH + "/" + userId + GROUPS_PATH + "/" + groupId;
        return keycloakMohExternalApiCaller.delete(path);
    }

    // Events
    public ResponseEntity<Object> getEvents(MultiValueMap<String, String> allParams) {
        return keycloakMohExternalApiCaller.get("/events", allParams);
    }

    public ResponseEntity<Object> getAdminEvents(MultiValueMap<String, String> allParams) {
        return keycloakMohExternalApiCaller.get("/admin-events", allParams);
    }

    @SuppressWarnings("unchecked")
    public ResponseEntity<Object> removeUserIdentityProviderLink(List<ApplicationRealmUser> applicationRealmUsers, String identityProvider, String userIdIdpRealm) {

        ArrayList<ResponseEntity<Object>> deleteIDPLinkResponses = new ArrayList<>();

        for (ApplicationRealmUser applicationRealmUser : applicationRealmUsers) {
            String userId = applicationRealmUser.getUserId();
            String applicationRealm = applicationRealmUser.getRealmName();
            if (identityProvider.startsWith("bcsc")) {
                LinkedHashMap<String, Object> user = (LinkedHashMap<String, Object>) getUser(userId).getBody();
                ArrayList<LinkedHashMap<String, String>> federatedIdentities = (ArrayList<LinkedHashMap<String, String>>) user.get("federatedIdentities");
                federatedIdentities.forEach(fi -> {
                    String idpAlias = fi.get("identityProvider");
                    if (idpAlias.startsWith("bcsc")) {
                        ResponseEntity<Object> response = deleteUserIdentityProviderLink(applicationRealm, userId, idpAlias);
                        deleteIDPLinkResponses.add(response);
                    }
                });
            } else {
                ResponseEntity<Object> response = deleteUserIdentityProviderLink(applicationRealm, userId, identityProvider);
                deleteIDPLinkResponses.add(response);
            }
        }



        if (identityProviderLinkDeletedSuccessfully(deleteIDPLinkResponses)) {
            // Some BCSC users can have an IDP alias that does not match the "bcsc" IDP realm (e.g. "bcsc_mspdirect") 
            if (identityProvider.startsWith("bcsc_")) {
                return deleteUserFromIdpRealm(userIdIdpRealm, "bcsc");
            } else {
                return deleteUserFromIdpRealm(userIdIdpRealm, identityProvider);
            }
        } else {
            return deleteIDPLinkResponses.stream()
                    .filter(response -> response.getStatusCode().is4xxClientError() || response.getStatusCode().is5xxServerError())
                    .findFirst().orElse(ResponseEntity.of(Optional.of(HttpStatus.INTERNAL_SERVER_ERROR)));
        }
    }

    private ResponseEntity<Object> deleteUserFromIdpRealm(String userIdIdpRealm, String identityProvider) {
        String path = "/" + identityProvider + USERS_PATH + "/" + userIdIdpRealm;
        return keycloakMasterExternalApiCaller.delete(path);
    }

    private ResponseEntity<Object> deleteUserIdentityProviderLink(String realm, String userId, String identityProviderAlias) {
        String path =  "/" + realm + USERS_PATH + "/" + userId + IDENTITY_PROVIDER_LINKS_PATH + "/" + identityProviderAlias;
        return keycloakMasterExternalApiCaller.delete(path);
    }

    private boolean identityProviderLinkDeletedSuccessfully(List<ResponseEntity<Object>> deleteIdentityProviderLinkResponses) {
        return deleteIdentityProviderLinkResponses.stream().allMatch(deleteIdentityProviderLinkResponse -> deleteIdentityProviderLinkResponse.getStatusCode().equals(HttpStatus.NO_CONTENT));
    }

    public List<BulkRemovalResponse> bulkRemoveUserClientRoles(String clientGuid, BulkRemovalRequest bulkRemovalRequest) {
        List<BulkRemovalResponse> responseList = new ArrayList<>();
        bulkRemovalRequest.getUserRolesForRemoval().forEach((userId, rolesToDelete) -> {
                    ResponseEntity<Object> keycloakApiResponse = deleteUserClientRole(userId, clientGuid, rolesToDelete);
                    responseList.add(new BulkRemovalResponse(keycloakApiResponse.getBody(), keycloakApiResponse.getStatusCodeValue(), keycloakApiResponse.getStatusCode(), userId));
                }
        );
        return responseList;
    }
}
