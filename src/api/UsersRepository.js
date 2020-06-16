import { kcRequest } from "./Repository";

const resource = "/users";
const clientRoleMappings = "role-mappings/clients";

export default {

    get(queryParams) {
        if (queryParams) {
            return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}${queryParams}`));
        } else {
            return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
        } 
    },

    getUser(userId) {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}`));
    },

    updateUser(userId, content) {
        return kcRequest().then(axiosInstance => axiosInstance.put(`${resource}/${userId}`, content));
    },

    /*Get available client-level roles that can be mapped to the user*/
    getUserAvailableClientRoles(userId, clientId) {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/available`));
    },

    /* Get active client-level role mappings. Active roles include only those that have been directly added to the user and can be removed */
    getUserActiveClientRoles(userId, clientId) {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}`));
    },

    /* Get effective client-level role mappings. Effective roles are all client-level roles assigned to a user including those provided by group membership or by composite roles */
    getUserEffectiveClientRoles(userId, clientId) {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/composite`));
    },

    addUserClientRoles(userId, clientId, content) {
        return kcRequest().then(axiosInstance => axiosInstance.post(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, content));
    },

    deleteUserClientRoles(userId, clientId, content) {
        //Keycloak expects the roles that will be removed in the body of the request which Axios doesn't do by default
        const deleteContent = { data: content }
        return kcRequest().then(axiosInstance => axiosInstance.delete(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, deleteContent));
    }
}
