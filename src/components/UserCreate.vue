<!--suppress XmlInvalidId -->
<template>
  <div id="user">
    <v-alert v-model="successState" type="success" dismissible>{{ successMessage }}</v-alert>
    <v-alert v-model="errorState" type="error" dismissible>{{ errorMessage }}</v-alert>
    <h1>Create New User</h1>
    <user-details ref="userDetails"></user-details>
    <v-btn class="secondary" medium @click="createUser()">Create User</v-btn>
  </div>
</template>

<script>
import UserDetails from "./UserDetails.vue";
import { RepositoryFactory } from "./../api/RepositoryFactory";
const UsersRepository = RepositoryFactory.get("users");

export default {
  name: "UserCreate",
  components: {
    UserDetails
  },
  data() {
    return {
      successMessage: "",
      successState: false,
      errorMessage: "",
      errorState: false
    };
  },
  methods: {
    createUser: function() {
      if (!this.$refs.userDetails.$refs.form.validate()) {
        this.errorState = true;
        this.successState = false;
        this.errorMessage = "Please correct errors before submitting";
        window.scrollTo(0, 0);
        return;
      }
      UsersRepository.createUser(this.user)
              .then(() => {
                this.successState = true;
                this.errorState = false;
                this.successMessage = 'User created';
                window.scrollTo(0, 0);
              })
              .catch(reason => {
                this.errorMessage = reason;
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