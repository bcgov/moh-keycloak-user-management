import '@bcgov/bc-sans/css/BCSans.css';

import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify';
import router from './router'
import keycloak from './keycloak';
import store from './store'

Vue.config.productionTip = false

Vue.prototype.$keycloak = keycloak; //maybe be able to remove this and use the export

Vue.config.devtools = true

keycloak.onAuthSuccess = function () {
  new Vue({
    vuetify,
    router,
    store,
    render: h => h(App)
  }).$mount('#app');
}
