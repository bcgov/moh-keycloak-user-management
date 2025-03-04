import Keycloak from "keycloak-js";

//https://www.keycloak.org/securing-apps/javascript-adapter#_constructor
let keycloak = new Keycloak("/keycloak.json");

let initOptions = {
  responseMode: "fragment",
  flow: "standard",
  onLoad: "login-required",
  pkceMethod: "S256",
  checkLoginIframe: false,
};

// For some reason idpHint cannot be specified in the Keycloak constructor or init options.
// https://stackoverflow.com/a/56338011/201891
let kcLogin = keycloak.login;
keycloak.login = (options) => {
  if (process.env.NODE_ENV !== "development") {
    options.idpHint = "idir_aad";
  }
  return kcLogin(options);
};

keycloak.init(initOptions);

export default keycloak;
