<!--suppress XmlInvalidId -->
<template>
  <div id="user">
    <h1>Register User</h1>
    <user-details
      ref="userDetails"
      update-or-register="Register User"
      @submit-user-updates="createUser"
    ></user-details>
  </div>
</template>

<script>
  import UsersRepository from "@/api/UsersRepository";

  import UserDetails from "@/components/UserDetails.vue";

  export default {
    name: "UserCreate",
    components: {
      UserDetails,
    },
    methods: {
      createUser: function (userDetails) {
        UsersRepository.createUser(userDetails)
          .then((response) => {
            //Keycloak returns the newly created user id in the response location
            let responseLocation = response.headers.location;
            let newUserId = responseLocation.substring(
              responseLocation.lastIndexOf("/") + 1
            );

            this.$store.commit("alert/setAlert", {
              message: "User registered successfully",
              type: "success",
            });

            //Redirect to the update user page
            this.$router.push({
              name: "UserUpdate",
              params: { userid: newUserId },
            });
          })
          .catch((error) => {
            const errorMessage = error.message.includes("409")
              ? `${error.message}. User already exists.`
              : error.message;
            this.$store.commit("alert/setAlert", {
              message: `Error creating new user: ${errorMessage}`,
              type: "error",
            });
          })
          .finally(() => {
            window.scrollTo(0, 0);
          });
      },
    },
  };
</script>
