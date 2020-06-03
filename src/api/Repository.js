import axios from 'axios';
import keycloak from '../keycloak';

/* This needs to be a function instead of just exporting axios.create as default
because the Keycloak Access Token will be periodically updated. */
function kcRequest() {
    function createAxios() {
        const baseURL = keycloak.authServerUrl + "admin/realms/" + keycloak.realm;
        let axiosInstance = axios.create({
            baseURL: baseURL,
            headers: {Authorization: 'Bearer ' + keycloak.token}
        });
        return axiosInstance;
    }

    return keycloak.updateToken(0).then(createAxios);
}

export { kcRequest }