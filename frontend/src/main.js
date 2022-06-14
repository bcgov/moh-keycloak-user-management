import '@bcgov/bc-sans/css/BCSans.css';

import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify';
import JsonCSV from 'vue-json-csv'
import router from './router'
import keycloak from './keycloak';
import store from './store'

Vue.config.productionTip = false
Vue.prototype.$keycloak = keycloak;
Vue.component('downloadCsv', JsonCSV)

keycloak.onAuthSuccess = async function () {
    try {
        const configResp = await fetch(process.env.BASE_URL + "config.json");
        Vue.prototype.$config = await configResp.json();
        const organizationsResp = await fetch(process.env.BASE_URL + "organizations.json");
        Vue.prototype.$organizations = await organizationsResp.json();  
    } catch (err) {
        console.error(err);
    } finally {
        new Vue({
            vuetify,
            router,
            store,
            render: h => h(App)
        }).$mount('#app');
    }
}
