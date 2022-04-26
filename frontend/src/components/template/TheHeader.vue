<template>
  <header>
    <div id="header">
      <section class="container">
        <section class="identity">
          <img src="../../assets/images/logo.png" width="154" class="logo" alt="BC Government Logo" />
          <div class="sitename">{{ title }}</div>
        </section>
        <section class="options user-select-off">
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
      }
    },
    methods: {
    logout: function () {
      if(confirm("Please confirm you want to sign out. " +
          "\nThis will also end all other active Keycloak or SiteMinder sessions you have open.")) {
        this.$keycloak.logout({redirectUri: this.$config.siteminder_logout});
      }
    }
  }
}
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
    color: #FFFFFF;
}
header .container .options {
    float: right;
    height: 60px;
    padding-top: 10px;
}
header .container .options .sign-out {
    display: inline-block;
    height: 40px;
    padding: 0 20px;
    border: 2px solid #FFFFFF;
    border-radius: 4px;
    text-decoration: none;
    font-size: 1rem;
    line-height: 2.25rem;
    font-weight: bold;
    color: #FFFFFF;
    cursor: pointer;
}
header .container .options .sign-out:focus {
    box-shadow: 0 0 3px #FFFFFF;
}
</style>