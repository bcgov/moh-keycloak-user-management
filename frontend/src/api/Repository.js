import axios from 'axios';
import keycloak from '../keycloak';
import app_config from '../loadconfig';

/* This needs to be a function instead of just exporting axios.create as default
because the Keycloak Access Token will be periodically updated. */
function kcRequest() {
    function createAxios() {
        const baseURL = keycloak.authServerUrl + "admin/realms/" + keycloak.realm;
        return axios.create({
            baseURL: baseURL,
            headers: {Authorization: 'Bearer ' + keycloak.token}
        });
    }
    if(keycloak.isTokenExpired(0)) {
        return keycloak.logout({redirectUri: app_config.config.siteminder_logout});
    }
        return keycloak.updateToken(0).then(createAxios);
}

function umsRequest() {
    function createAxios() {
        const baseURL = app_config.config.service_url;
        return axios.create({
            baseURL: baseURL,
            headers: {Authorization: 'Bearer ' + keycloak.token}
        });
    }
    if(keycloak.isTokenExpired(0)) {
        return keycloak.logout({redirectUri: app_config.config.siteminder_logout});
    }
        return keycloak.updateToken(0).then(createAxios);
}

function sfdsRequest() {
    function createAxios() {
        const baseURL = app_config.config.sfds_url;
        return axios.create({
            baseURL: baseURL
        });
    }
    if(keycloak.isTokenExpired(0)) {
        return keycloak.logout({redirectUri: app_config.config.siteminder_logout});
    }
        return keycloak.updateToken(0).then(createAxios);
}

export {kcRequest, umsRequest, sfdsRequest}