import axios from 'axios';
import keycloak from '../keycloak';

/* This needs to be a function instead of just exporting axios.create as default
because the Keycloak Access Token will be periodically updated. */
function kcRequest() {
    const baseURL = keycloak.authServerUrl + "admin/realms/" + keycloak.realm;
    return axios.create({
        baseURL: baseURL,
        headers: { Authorization: 'Bearer ' + keycloak.token }
    });
}

export { kcRequest }