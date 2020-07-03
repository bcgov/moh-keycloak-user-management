<!--suppress XmlInvalidId -->
<template>
  <div id="user">
    <h1>Create New User</h1>
    <user-details ref="userDetails">
      <v-btn id="submit-button" class="secondary" medium @click="createUser()">Create User</v-btn>
    </user-details>
  </div>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";

import UserDetails from "@/components/UserDetails.vue";

export default {
  name: "UserCreate",
  components: {
    UserDetails
  },
  methods: {
    createUser: function() {
      if (!this.$refs.userDetails.$refs.form.validate()) {
        this.$store.commit("alert/setAlert", {
          message: "Please correct errors before submitting",
          type: "error"
        });
        window.scrollTo(0, 0);
        return;
      }
      UsersRepository.createUser(this.user)
        .then(response => {
          //Keycloak returns the newly created user id in the response location
          let responseLocation = response.headers.location;
          let newUserId = responseLocation.substring(
            responseLocation.lastIndexOf("/") + 1
          );

          this.$store.commit("alert/setAlert", {
            message: "User created successfully",
            type: "success"
          });

          //Redirect to the update user page
          this.$router.push({
            name: "UserUpdate",
            params: { userid: newUserId }
          });
        })
        .catch(error => {
          this.$store.commit("alert/setAlert", {
            message: "Error creating new user: " + error,
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