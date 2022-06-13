import axios from 'axios';
import keycloak from '../keycloak';
import Vue from 'vue'

/* This needs to be a function instead of just exporting axios.create as default
because the Keycloak Access Token will be periodically updated. */
function kcRequest() {
    function createAxios() {
        const baseURL = keycloak.authServerUrl + "admin/realms/" + keycloak.realm;
        return axios.create({
            baseURL: baseURL,
            headers: { Authorization: 'Bearer ' + keycloak.token }
        });
    }
    if (keycloak.isTokenExpired(0)) {
        return keycloak.logout();
    }
    return keycloak.updateToken(0).then(createAxios);
}

function umsRequest() {
    function createAxios() {
        const baseURL = Vue.prototype.$config.service_url;
        return axios.create({
            baseURL: baseURL,
            headers: { Authorization: 'Bearer ' + keycloak.token }
        });
    }
    if (keycloak.isTokenExpired(0)) {
        return keycloak.logout();

    }
    return keycloak.updateToken(0).then(createAxios);
}

function sfdsRequest() {
    function createAxios() {
        const baseURL = Vue.prototype.$config.sfds_url;
        return axios.create({
            baseURL: baseURL
        });
    }
    if (keycloak.isTokenExpired(0)) {
        return keycloak.logout();
    }
    return keycloak.updateToken(0).then(createAxios);
}

export { kcRequest, umsRequest, sfdsRequest }