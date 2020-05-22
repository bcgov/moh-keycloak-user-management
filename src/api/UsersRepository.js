import { kcRequest } from "./Repository";

const resource = "/users";
const clientRoleMappings = "role-mappings/clients";

export default {

    get(queryParams) {
        if (queryParams) {
            return kcRequest().get(`${resource}${queryParams}`)
        } else {
            return kcRequest().get(`${resource}`);
        } 
    },

    getUser(userId) {
        return kcRequest().get(`${resource}/${userId}`)
    },

    updateUser(userId, content) {
        return kcRequest.put(`${resource}/${userId}`, content)
    },

    getUserAvailableClientRoles(userId, clientId) {
        return kcRequest().get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/available`)
    },

    getUserEffectiveClientRoles(userId, clientId) {
        return kcRequest().get(`${resource}/${userId}/${clientRoleMappings}/${clientId}/composite`)
    },

    addUserClientRoles(userId, clientId, content) {
        return kcRequest().post(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, content)
    },

    deleteUserClientRoles(userId, clientId, content) {
        //Keycloak expects the roles that will be removed in the body of the request which Axios doesn't do my default
        const deleteContent = { data: content }
        return kcRequest().delete(`${resource}/${userId}/${clientRoleMappings}/${clientId}/`, deleteContent)
    }
}
