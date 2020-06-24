<template>
  <v-app>
    <the-header></the-header>
    <the-nav-bar></the-nav-bar>
    <main>
      <section class="content">
        <alert/>
        <router-view></router-view>
      </section>
      <v-checkbox v-model="showKeycloakTools" :label="`Keycloak Dev Tools`"></v-checkbox>
      <KeycloakDevTools v-show="showKeycloakTools" />
    </main>
    <the-footer></the-footer>
  </v-app>
</template>

<script>
import TheHeader from "./components/template/TheHeader.vue";
import TheNavBar from "./components/template/TheNavBar.vue";
import TheFooter from "./components/template/TheFooter.vue";
import Alert from "./components/Alert.vue";

import KeycloakDevTools from "./KeycloakDevTools";

export default {
  name: "App",
  components: {
    Alert,
    TheHeader,
    TheNavBar,
    TheFooter,
    KeycloakDevTools
  },
  data() {
    return {
      showKeycloakTools: false
    };
  }
};
</script>

<style src=./assets/css/main.css></style>
<style src=./assets/css/reset.css></style>
<style src=./assets/css/typography.css></style>