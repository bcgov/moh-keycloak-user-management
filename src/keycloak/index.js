import Keycloak from "keycloak-js";

let keycloak = Keycloak();

let initOptions = {
    responseMode: 'fragment',
    flow: 'standard',
    onLoad: 'login-required'
};

keycloak.init(initOptions).then((auth) => {

    if (!auth) {
        throw "Some error";
    }

    setInterval(() => {
        keycloak.updateToken(70).then((refreshed) => {
            if (refreshed) {
                console.log('Token refreshed');
            } else {
                console.log('Token not refreshed, valid for '
                    + Math.round(keycloak.tokenParsed.exp + keycloak.timeSkew - new Date().getTime() / 1000) + ' seconds');
            }
        }).error(() => {
            console.log('Failed to refresh token');
        });

    }, 60000)

}).catch(() => {
    console.log("Authenticated Failed");
});

export default keycloak;