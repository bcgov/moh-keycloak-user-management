import { umsRequest } from "./Repository";

const resource = "/users";
const clientRoleMappings = "role-mappings/clients";
const groups ="groups";

export default {

    get(queryParams) {
        if (queryParams) {
            return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}${queryParams}`));
        } else {
            return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
        } 
    },

    getUser(userId) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}`));
    },

    createUser(content) {
        return umsRequest().then(axiosInstance => axiosInstance.post(`${resource}`, content));
    },

    updateUser(userId, content) {
        return umsRequest().then(axiosInstance => axiosInstance.put(`${resource}/${userId}`, content));
    },

    /*Get available client-level roles that can be mapped to the user*/
    getUserAvailableClientRoles(userId, clientId) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/available`));
    },

    /* Get active client-level role mappings. Active roles include only those that have been directly added to the user and can be removed */
    getUserActiveClientRoles(userId, clientId) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}`));
    },

    /* Get effective client-level role mappings. Effective roles are all client-level roles assigned to a user including those provided by group membership or by composite roles */
    getUserEffectiveClientRoles(userId, clientId) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/composite`));
    },

    /* Get the last login date for each client for a user*/
    getUserLogins(userId){
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/last-logins`));
    },

    addUserClientRoles(userId, clientId, content) {
        return umsRequest().then(axiosInstance => axiosInstance.post(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, content));
    },

    deleteUserClientRoles(userId, clientId, content) {
        //Keycloak expects the roles that will be removed in the body of the request which Axios doesn't do by default
        const deleteContent = { data: content }
        return umsRequest().then(axiosInstance => axiosInstance.delete(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, deleteContent));
    },

    /* User Groups */
    getUserGroups(userId) {
      return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${groups}`));
    },
    addGroupToUser(userId, groupId) {
        return umsRequest().then(axiosInstance => axiosInstance.put(`${resource}/${userId}/${groups}/${groupId}`))
    },
    removeGroupFromUser(userId, groupId) {
        return umsRequest().then(axiosInstance => axiosInstance.delete(`${resource}/${userId}/${groups}/${groupId}`))
    },

}