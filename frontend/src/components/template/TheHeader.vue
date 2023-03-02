<template>
  <header>
    <div id="header">
      <section class="container">
        <section class="identity">
          <img
            src="../../assets/images/logo.png"
            width="154"
            class="logo"
            alt="BC Government Logo"
          />
          <div class="sitename">{{ title }}</div>
        </section>
        <section class="options user-select-off">
          <p>{{ this.$keycloak.tokenParsed.preferred_username }}</p>
          <a id="logoutLink" class="sign-out" v-on:click="logout">Sign Out</a>
        </section>
      </section>
    </div>
  </header>
</template>

<script>
export default {
  name: "TheHeader",
  data() {
    return {
      title: this.$config.app_title,
    };
  },
  methods: {
    logout: function() {
      if (
        confirm(
          "Please confirm you want to sign out. " +
            "\nThis will also end all other active Keycloak or SiteMinder sessions you have open."
        )
      ) {
        /**
         * https://www.keycloak.org/docs/18.0/upgrading/#openid-connect-logout
         * For keycloak vs18 and above, the parameter redirect_uri is no longer supported so to support full sso
         * first we set the SiteMinder logout endpoint with a
         * redirect back to the Keycloak logout endpoint and
         * then redirect back to the application from the Keycloak logout endpoint
         * with parameter post_logout_redirect_uri & id_token_hint
         * https://stackoverflow.developer.gov.bc.ca/questions/83/84#84
         */

        const siteminderUri = this.$config.siteminder_logout;
        const redirectUri =
          this.$config.keycloak_logout +
          "?post_logout_redirect_uri=" +
          this.$config.redirect_uri +
          "&id_token_hint=" +
          this.$keycloak.idToken;

        window.location.href =
          siteminderUri + "?retnow=1&returl=" + encodeURIComponent(redirectUri);
      }
    },
  },
};
</script>

<style scoped>
header {
  height: 80px;
  background-color: #003366;
  border-bottom: 2px solid #fcba19;
}
header .container {
  max-width: 1320px;
  min-width: 1100px;
  height: 80px;
  padding: 10px 60px;
  margin: 0 auto;
}
header .container .identity {
  float: left;
  height: 60px;
}
header .container .identity .logo {
  display: inline-block;
  margin: 10px 30px 0 0;
}
header .container .identity .sitename {
  display: inline-block;
  vertical-align: top;
  height: 60px;
  margin: 0;
  font-size: 1.375rem;
  line-height: 59px;
  font-weight: bold;
  color: #ffffff;
}
header .container .options {
  float: right;
  height: 60px;
  display: flex;
  align-items: center;
}
header .container .options .sign-out {
  display: inline-block;
  height: 40px;
  padding: 0 20px;
  border: 2px solid #ffffff;
  border-radius: 4px;
  text-decoration: none;
  font-size: 1rem;
  line-height: 2.25rem;
  font-weight: bold;
  color: #ffffff;
  cursor: pointer;
}
header .container .options .sign-out:focus {
  box-shadow: 0 0 3px #ffffff;
}

header .container .options p {
  color: #ffffff;
  margin: 0 10px;
}
</style>
