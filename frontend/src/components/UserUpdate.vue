<!--suppress XmlInvalidId -->
<template>
  <div>

    <v-skeleton-loader
        :loading="loading"
        v-show="!user.username"
        ref="skeleton"
        :boilerplate="boilerplate"
        tile="true"
        type="article, button"
    >
    </v-skeleton-loader>
    <div id="user-info" v-show="user.username">
      <h1>Update - {{ user.username }}</h1>
      <user-details :userId="this.$route.params.userid" ref="userDetails">
        <v-btn id="submit-button" class="secondary" medium v-on:click.prevent="updateUser">Update User</v-btn>
      </user-details>
      <user-update-roles :userId="this.$route.params.userid"></user-update-roles>
      <user-update-groups :userId="this.$route.params.userid"></user-update-groups>
    </div>
  </div>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";

import UserDetails from "@/components/UserDetails.vue";
import UserUpdateRoles from "@/components/UserUpdateRoles.vue";
import UserUpdateGroups from "@/components/UserUpdateGroups";

export default {
  name: "UserInfo",
  components: {
    UserUpdateGroups,
    UserDetails,
    UserUpdateRoles
  },
  data() {
    return {
      loading: true,
      boilerplate: false,
      type: 'list-item-avatar-three-line',
    }
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
        })
        .catch(error => {
          this.$store.commit("alert/setAlert", {
            message: "Error updating user: " + error,
            type: "error"
          });
        })
        .finally(() => {
          window.scrollTo(0, 0);
        });
    }
  },
  computed: {
    user() {
      return this.$store.state.user;
    }
  }
};
</script>