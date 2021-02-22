import { umsRequest } from "./Repository";

const resource = "/clients";

export default {
    get() {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
    },
    
    getRoles(clientId){
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${clientId}/roles`));
    },
    
    getUsersInRole(clientId, roleName) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${clientId}/roles/${roleName}/users`))
    },

    /**
     * Adds client names to the events array using the clientId to lookup the client name.
     *
     * The events array should contain objects with a clientId property.
     * The client name will be added to the corresponding object. It caches
     * the clientId:clientName in sessionStorage.
     *
     * @param events object array, objects should contain clientId property.
     * @returns {Promise<void>}
     */
    async addClientNamesToEvents(events) {
        const allClients = await this.get();
        // Note clientResponse.data.clientId is not the numeric ID, it is actually a name.
        // There is also a longer display name property ("name").
        allClients.data.map((client) => sessionStorage[client.id] = client.clientId);
        for (let event of events) {
            if (event.clientId && sessionStorage[event.clientId]) {
                event.clientName = sessionStorage[event.clientId];
            } else {
                event.clientName = 'no client found';
            }
        }
    }

}