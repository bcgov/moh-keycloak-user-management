import '@bcgov/bc-sans/css/BCSans.css';

import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify';
import JsonCSV from 'vue-json-csv'
import router from './router'
import keycloak from './keycloak';
import app_config from "@/loadconfig";
import store from './store'

Vue.config.productionTip = false
Vue.prototype.$keycloak = keycloak;
Vue.component('downloadCsv', JsonCSV)

keycloak.onAuthSuccess = async function () {
    Vue.prototype.$config = await app_config.config;
    new Vue({
        vuetify,
        router,
        store,
        render: h => h(App)
    }).$mount('#app');
}
