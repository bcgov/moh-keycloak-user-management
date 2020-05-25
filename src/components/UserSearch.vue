<template>
  <div>
    <v-alert :value="!!errorMessage" type="error" dismissible>{{ errorMessage }}</v-alert>
    <v-row no-gutters>
      <v-col class="col-6">
        <label for="user-search">
          Search
          <v-tooltip right>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on" small>mdi-help-circle</v-icon>
            </template>
            <span>Search by username, email, name, or ID</span>
          </v-tooltip>
        </label>

        <v-text-field
          id="user-search"
          outlined
          dense
          v-model="userSearchInput"
          placeholder="Username, email, name, or ID"
          @keyup.native.enter="searchUser"
        />
      </v-col>
      <v-col class="col-1">
        <v-btn id="search-button" class="secondary" medium @click.native="searchUser">Search Users</v-btn>
      </v-col>
    </v-row>
    <v-row no-gutters>
      <v-col class="col-12">
        <v-data-table
          id="users-table"
          class="base-table select-table"
          :headers="headers"
          :items="searchResults"
          :footer-props="footerProps"
          :loading="userSearchLoadingStatus"
          loading-text="Searching for users"
          v-on:click:row="selectUser"
        ></v-data-table>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import { RepositoryFactory } from "@/api/RepositoryFactory";
const UsersRepository = RepositoryFactory.get("users");

export default {
  name: "UserSearch",

  data() {
    return {
      headers: [
        { text: "Username", value: "username", class: "table-header" },
        { text: "First name", value: "firstName", class: "table-header" },
        { text: "Last name", value: "lastName", class: "table-header" },
        { text: "Email", value: "email", class: "table-header" },
        { text: "Enabled", value: "enabled", class: "table-header" },
        { text: "ID", value: "id", class: "table-header" }
      ],
      footerProps: { "items-per-page-options": [15] },
      userSearchInput: "",
      searchResults: [],
      clients: [],
      userSearchLoadingStatus: false,
      errorMessage: ""
    };
  },
  methods: {
    selectUser: function(user) {
      this.$router.push({ name: "UserInfo", params: { userid: user.id } });
    },
    searchUser: function() {
      this.userSearchLoadingStatus = true;
      this.$keycloak
        .updateToken()
        .then(response => {
          console.log(response);
          return UsersRepository.get(
            "?briefRepresentation=true&first=0&max=300&search=" +
              this.userSearchInput
          );
        })
        .then(response => {
          this.searchResults = response.data;
        })
        .catch(e => {
          console.log(e);
          this.errorMessage = "User Search Failed";
        })
        .finally(() => (this.userSearchLoadingStatus = false));
    }
  }
};
</script>

<style scoped>
#search-button {
  margin-top: 25px;
  margin-left: 20px;
}
</style>