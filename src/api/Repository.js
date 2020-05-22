import axios from 'axios';
import { keycloak } from '../main.js';

/* this needs to be a function instead of just exporting axios.create as default
because there is a circular dependency with main.js -> App.vue -> ClientsRepository.js -> Repository.js -> main.js
so that Repository.js can get the keycloak access token */
function kcRequest() {
    const baseURL = keycloak.authServerUrl + "admin/realms/" + keycloak.realm;
    return axios.create({
        baseURL: baseURL,
        headers: { Authorization: 'Bearer ' + keycloak.token }
    });
}

export { kcRequest } 