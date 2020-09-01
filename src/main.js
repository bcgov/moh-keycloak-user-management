import '@bcgov/bc-sans/css/BCSans.css';

import Vue from 'vue'
import App from './App.vue'
import vuetify from './plugins/vuetify';
import router from './router'
import keycloak from './keycloak';
import store from './store'

Vue.config.devtools = true
Vue.config.productionTip = false
Vue.prototype.$keycloak = keycloak;

keycloak.onAuthSuccess = function () {
    fetch(process.env.BASE_URL + "config.json")
        .then((response) => {
            return response.json();
        })
        .then((config) => {
                Vue.prototype.$config = config
                new Vue({
                    vuetify,
                    router,
                    store,
                    render: h => h(App)
                }).$mount('#app');
        })
}
