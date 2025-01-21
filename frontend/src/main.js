import "@bcgov/bc-sans/css/BCSans.css";

import { createApp } from "vue";
import JsonCSV from "vue-json-csv";
import OrganizationsRepository from "./api/OrganizationsRepository";
import App from "./App.vue";
import keycloak from "./keycloak";
import vuetify from "./plugins/vuetify";
import store from "./store";

const app = createApp(App);

app.config.globalProperties.$keycloak = keycloak;
app.config.globalProperties.$UserCountCache = {};
app.component("downloadCsv", JsonCSV);

keycloak.onAuthSuccess = async function () {
  try {
    const configResp = await fetch(process.env.BASE_URL + "config.json");
    app.config.globalProperties.$config = await configResp.json();
    const organizationsResp = await OrganizationsRepository.get();
    app.config.globalProperties.$organizations = organizationsResp?.data;
  } catch (err) {
    console.error(err);
  } finally {
    //Importing Vue Router dynamically to resolve the following issue:
    //The module init for vue-router is capturing a value from window.location and using it later for routing before the caller executes app.use(router), removing the caller's ability to control the flow.
    //This leads to Keycloak response fragment (to be precise the state, session_state and code parameters) being appended to the application URL
    //This breaks the navigation inside that application and leaks authorization information.
    //More detail can be found at:
    //https://github.com/keycloak/keycloak/issues/14742
    const router = await import("./router");
    app.use(vuetify).use(router.default).use(store).mount("#app");
  }
};

export default app;
