<template>
  <v-app>
    <the-header></the-header>
    <the-nav-bar></the-nav-bar>
    <main>
      <section class="content">
        <alert/>
        <router-view></router-view>
      </section>
      <v-checkbox v-model="showKeycloakTools" :label="`Keycloak Dev Tools`" v-if="vueCliMode === 'development'"></v-checkbox>
      <KeycloakDevTools v-if="showKeycloakTools" />
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
      vueCliMode: process.env.NODE_ENV,
      showKeycloakTools: false
    };
  },
  mounted() {
    const requiredRoles = ["view-events", "manage-users", "view-users", "query-clients", "query-groups", "query-users"];
    let actualRoles = [];
    if (this.$keycloak.tokenParsed.resource_access) {
      actualRoles = this.$keycloak.tokenParsed.resource_access['realm-management'].roles;
    }
    const missingRoles = getMissingRoles(
        requiredRoles,
        actualRoles
    );
    if (missingRoles.length > 0) {
      this.$store.commit("alert/setAlert", {
        message: `You are missing roles required to use this application.
          Missing roles: ${missingRoles.join(', ')}.`,
        type: "error"
      });
    }
  }
};

function getMissingRoles(requiredRoles, actualRoles) {
  const missingRoles = [];
  requiredRoles.forEach(requiredRole => {
    if (!actualRoles.includes(requiredRole)) {
      missingRoles.push(requiredRole);
    }
  });
  return missingRoles;
}
</script>

<style src=./assets/css/main.css></style>
<style src=./assets/css/reset.css></style>
<style src=./assets/css/typography.css></style>