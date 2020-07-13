import Keycloak from "keycloak-js";

let keycloak = Keycloak();

let initOptions = {
    responseMode: 'fragment',
    flow: 'standard',
    onLoad: 'login-required',
    pkceMethod: 'S256'
};

keycloak.init(initOptions);

export default keycloak;