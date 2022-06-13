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

keycloak.onAuthSuccess = function () {
    fetch(process.env.BASE_URL + "config.json")
        .then(response => {
            return response.json();
        })
        .then((config) => {
            Vue.prototype.$config = config;
            
            fetch(process.env.BASE_URL + "organizations.json")
                .then(response => {
                    return response.json();
                })
                .then((organizations) => {
                    Vue.prototype.$organizations = organizations;
                    
                    new Vue({
                        vuetify,
                        router,
                        store,
                        render: h => h(App)
                    }).$mount('#app');
                });
        });
    }
