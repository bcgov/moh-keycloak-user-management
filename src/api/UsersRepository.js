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

    getUserAvailableClientRoles(userId, clientId) {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/available`));
    },

    getUserEffectiveClientRoles(userId, clientId) {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/composite`));
    },

    addUserClientRoles(userId, clientId, content) {
        return kcRequest().then(axiosInstance => axiosInstance.post(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, content));
    },

    deleteUserClientRoles(userId, clientId, content) {
        //Keycloak expects the roles that will be removed in the body of the request which Axios doesn't do my default
        const deleteContent = { data: content }
        return kcRequest().then(axiosInstance => axiosInstance.delete(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, deleteContent));
    }
}
