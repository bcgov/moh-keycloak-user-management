import { kcRequest, umsRequest } from "./Repository";

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
      return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${userId}/${groups}`));
    },
    addGroupToUser(userId, groupId) {
        return kcRequest().then(axiosInstance => axiosInstance.put(`${resource}/${userId}/${groups}/${groupId}`))
    },
    removeGroupFromUser(userId, groupId) {
        return kcRequest().then(axiosInstance => axiosInstance.delete(`${resource}/${userId}/${groups}/${groupId}`))
    },

    /**
     * Adds usernames to the events array using the userId to lookup the username.
     *
     * The events array should contain objects with a userId property.
     * The username will be added to the corresponding object. It caches
     * the userId:username in sessionStorage.
     *
     * @param events object array, objects should contain userId property.
     * @returns {Promise<void>}
     */
    async addUsernamesToEvents(events) {
        for (let event of events) {
            const storageKey = 'username-' + event.userId;
            if (!sessionStorage[storageKey]) {
                try {
                    const userData = await this.getUser(event.userId);
                    sessionStorage[storageKey] = userData.data.username;
                } catch (ex) {
                    console.log(ex);
                    sessionStorage[storageKey] = 'no user found';
                }
            }
            event.username = sessionStorage[storageKey];
        }
    },

    /**
     * Adds the administrator's full name to the events array using the userId to lookup the full name.
     *
     * The events array should contain objects with a authDetails.userId property.
     * The full name will be added to the corresponding object. It caches
     * the authDetails:fullName in sessionStorage.
     *
     * @param events object array, objects should contain authDetails.userId property.
     * @returns {Promise<void>}
     */
    async addAdminNamesToEvents(events) {
        for (let event of events) {
            const storageKey = 'name-' + event.authDetails.userId;
            if (!sessionStorage[storageKey]) {
                try {
                    const userData = await this.getUser(event.authDetails.userId);
                    sessionStorage[storageKey] = `${userData.data.firstName} ${userData.data.lastName}`;
                } catch (ex) {
                    console.log(ex);
                    sessionStorage[storageKey] = 'no user found';
                }
            }
            event.authDetails.fullName = sessionStorage[storageKey];
        }
    }
}