<!--suppress XmlInvalidId -->
<template>
  <div id="user-info" v-show="user.username">
    <h1>Update - {{ user.username }}</h1>
    <user-details :userId="this.$route.params.userid" ref="userDetails">
      <v-btn id="save-button" class="secondary" medium v-on:click.prevent="updateUser">Update User</v-btn>
    </user-details>
    <user-update-roles :userId="this.$route.params.userid"></user-update-roles>
  </div>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";

import UserDetails from "@/components/UserDetails.vue";
import UserUpdateRoles from "@/components/UserUpdateRoles.vue"

export default {
  name: "UserInfo",
  components: {
    UserDetails,
    UserUpdateRoles
  },
  methods: {
    updateUser: function() {
      //Validate the User Details
      if (!this.$refs.userDetails.$refs.form.validate()) {
        this.$store.commit("alert/setAlert", {
          message: "Please correct errors before submitting",
          type: "error"
        });
        window.scrollTo(0, 0);
        return;
      }
      //Update the user in Keycloak
      UsersRepository.updateUser(this.$route.params.userid, this.user)
        .then(() => {
          this.$store.commit("alert/setAlert", {
            message: "User updated successfully",
            type: "success"
          });
          window.scrollTo(0, 0);
        })
        .catch(error => {
          this.$store.commit("alert/setAlert", {
            message: "Error updating user: " + error,
            type: "error"
          });
        });
    },
  },
  computed: {
    user() {
      return this.$store.state.user;
    }
  }
};
</script>