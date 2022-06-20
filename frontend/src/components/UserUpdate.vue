<!--suppress XmlInvalidId -->
<template>
  <div>
    <v-skeleton-loader
      ref="skeleton"
      v-show="!user.username"
      type="article, button, article"
    >
    </v-skeleton-loader
    >
    <div id="user-info" v-show="user.username">
      <h1>Update - {{ user.username }}</h1>
      <user-details
        :userId="this.$route.params.userid"
        update-or-create="Update"
        @submit-user-updates="updateUser"
        ref="userDetails"
      ></user-details>
      <user-update-roles
        :userId="this.$route.params.userid"
      ></user-update-roles>
      <user-mailbox-authorizations
        :userId="this.$route.params.userid"
      ></user-mailbox-authorizations>
      <user-update-groups
        :userId="this.$route.params.userid"
      ></user-update-groups>
    </div>
  </div>
</template>

<script>
// import UsersRepository from "@/api/UsersRepository";

import UserDetails from "@/components/UserDetails.vue";
import UserUpdateRoles from "@/components/UserUpdateRoles.vue";
import UserUpdateGroups from "@/components/UserUpdateGroups";
import UserMailboxAuthorizations from "./UserMailboxAuthorizations.vue";
import UsersRepository from "@/api/UsersRepository";

export default {
  name: "UserInfo",
  components: {
    UserUpdateGroups,
    UserDetails,
    UserUpdateRoles,
    UserMailboxAuthorizations,
  },
  methods: {
    updateUser: function(userDetails) {
      // Update the user in Keycloak
      this.$store.commit("user/setUserDetails", userDetails);
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
    },

  },
  computed: {
    user() {
      return this.$store.state.user;
    },
  },
};
</script>
<style>
.v-skeleton-loader__button {
  margin-left: 16px;
}
</style>