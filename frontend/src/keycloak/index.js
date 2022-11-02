import Keycloak from "keycloak-js";

// Keycloak uses "public\keycloak.json" by default if not otherwise specified.
// https://www.keycloak.org/docs/latest/securing_apps/index.html#_javascript_adapter
let keycloak = new Keycloak();

let initOptions = {
    responseMode: 'fragment',
    flow: 'standard',
    onLoad: 'login-required',
    pkceMethod: 'S256'
};


let kcCreateLoginUrl = keycloak.createLoginUrl;
keycloak.createLoginUrl = (options) => {
    var loginUrl = kcCreateLoginUrl(options);
    if (options && options.idpsToShow) {
        loginUrl += '&idps_to_show=' + encodeURIComponent(options.idpsToShow);
    }
    return loginUrl;
};

// For some reason idpHint cannot be specified in the Keycloak constructor or init options.
// https://stackoverflow.com/a/56338011/201891
let kcLogin = keycloak.login;
keycloak.login = (options) => {
    options.idpsToShow = 'idir,phsa';
    return kcLogin(options);
};

keycloak.init(initOptions);

export default keycloak;