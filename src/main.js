import '@bcgov/bc-sans/css/BCSans.css';

import Vue from 'vue'
import App from './App.vue'
import Keycloak from 'keycloak-js';
import vuetify from './plugins/vuetify';
import router from './router'

Vue.config.productionTip = false

var keycloak = Keycloak();
Vue.prototype.$keycloak = keycloak; //maybe be able to remove this and use the export

var initOptions = {
  responseMode: 'fragment',
  flow: 'standard',
  onLoad: 'login-required'
};

keycloak.init(initOptions).success((auth) => {

  if (!auth) {
    window.location.reload();
  } else {
    console.log("Authenticated");
  }

  new Vue({
    vuetify,
    router,
    render: h => h(App)
  }).$mount('#app');

  setInterval(() => {
    keycloak.updateToken(70).success((refreshed) => {
      if (refreshed) {
        console.log('Token refreshed');
        localStorage.token = keycloak.token;
        localStorage.refreshToken = keycloak.refreshToken;
      } else {
        console.log('Token not refreshed, valid for '
          + Math.round(keycloak.tokenParsed.exp + keycloak.timeSkew - new Date().getTime() / 1000) + ' seconds');
      }
    }).error(() => {
      console.log('Failed to refresh token');
    });

  }, 60000)

}).error(() => {
  console.log("Authenticated Failed");
});

export { keycloak }

