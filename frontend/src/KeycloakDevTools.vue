<template>
  <div>
    <div class="col1">
      <h2>User Account</h2>
      <v-btn size="small" v-on:click="loadProfile">Get Profile</v-btn>
      <v-btn size="small" v-on:click="updateProfile">Update profile</v-btn>
    </div>
    <div class="col1">
      <h2>Token Information</h2>
      <v-btn size="small" v-on:click="loadUserInfo">Get User Info</v-btn>
      <v-btn size="small" v-on:click="showToken">Show Token</v-btn>
      <v-btn size="small" v-on:click="showRefreshToken">Show Refresh Token</v-btn>
      <v-btn size="small" v-on:click="showIdToken">Show ID Token</v-btn>
      <v-btn size="small" v-on:click="showExpires">Show Expires</v-btn>
      <v-btn size="small" v-on:click="showDetails">Show Details</v-btn>
    </div>
    <div class="col4">
      <label style="font-weight: 600">Result</label>
      <pre
        style="
          background-color: #ddd;
          border: 1px solid #ccc;
          padding: 10px;
          word-wrap: break-word;
          white-space: pre-wrap;
          margin-bottom: 20px;
        "
        id="output"
        >{{ result }}</pre
      >
    </div>
  </div>
</template>
<script>
  export default {
    name: "KeycloakDevTools",
    data: function () {
      return {
        result: "",
      };
    },
    methods: {
      loadUserInfo: function () {
        var vm = this;
        this.$keycloak
          .loadUserInfo()
          .success(function (responseData) {
            vm.result = JSON.stringify(responseData, null, "  ");
          })
          .error(function () {
            vm.result = "Failed to load user info";
          });
      },
      loadProfile: function () {
        var vm = this;
        this.$keycloak
          .loadUserProfile()
          .success(function (profile) {
            vm.result = JSON.stringify(profile, null, "  ");
          })
          .error(function () {
            vm.result = "Failed to load user info";
          });
      },
      showToken: function () {
        this.result = this.$keycloak.tokenParsed;
      },
      showRefreshToken: function () {
        this.result = this.$keycloak.refreshTokenParsed;
      },
      showIdToken: function () {
        this.result = this.$keycloak.idTokenParsed;
      },
      showDetails: function () {
        this.result = this.$keycloak;
      },
      showExpires: function () {
        if (!this.$keycloak.tokenParsed) {
          this.result = "Not authenticated";
          return;
        }
        var o =
          "Token Expires:\t\t" +
          new Date(
            (this.$keycloak.tokenParsed.exp + this.$keycloak.timeSkew) * 1000
          ).toLocaleString() +
          "\n";
        o +=
          "Token Expires in:\t" +
          Math.round(
            this.$keycloak.tokenParsed.exp +
              this.$keycloak.timeSkew -
              new Date().getTime() / 1000
          ) +
          " seconds\n";

        if (this.$keycloak.refreshTokenParsed) {
          o +=
            "Refresh Token Expires:\t" +
            new Date(
              (this.$keycloak.refreshTokenParsed.exp +
                this.$keycloak.timeSkew) *
                1000
            ).toLocaleString() +
            "\n";
          o +=
            "Refresh Expires in:\t" +
            Math.round(
              this.$keycloak.refreshTokenParsed.exp +
                this.$keycloak.timeSkew -
                new Date().getTime() / 1000
            ) +
            " seconds";
        }

        this.result = o;
      },
      updateProfile: function () {
        var url = this.$keycloak.createAccountUrl().split("?")[0];
        var req = new XMLHttpRequest();
        req.open("POST", url, true);
        req.setRequestHeader("Accept", "application/json");
        req.setRequestHeader("Content-Type", "application/json");
        req.setRequestHeader("Authorization", "bearer " + this.$keycloak.token);

        var vm = this;
        req.onreadystatechange = function () {
          if (req.readyState === 4) {
            if (req.status === 200) {
              vm.result = "Success";
            } else {
              vm.result = "Failed";
            }
          }
        };

        req.send(
          '{"email":"myemail@foo.bar","firstName":"test","lastName":"bar"}'
        );
      },
    },
  };
</script>
