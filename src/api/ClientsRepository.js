import { kcRequest } from "./Repository";

const resource = "/clients";

export default {
    get() {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
    },

    getClient(clientId) {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${clientId}`));
    },
    
    getRoles(clientId){
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${clientId}/roles`));
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
        for (let event of events) {
            if (event.clientId && !sessionStorage[event.clientId]) {
                try {
                    const clientResponse = await this.getClient(event.clientId);
                    // Note clientResponse.data.clientId is not the numeric ID, it is actually a name.
                    // There is also a longer display name property ("name").
                    sessionStorage[event.clientId] = clientResponse.data.clientId;
                } catch (ex) {
                    console.log(ex);
                    sessionStorage[event.clientId] = 'no client found';
                }
            }
            event.clientName = sessionStorage[event.clientId];
        }
    }

}