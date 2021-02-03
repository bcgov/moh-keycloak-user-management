<template>
  <div>
    <!-- Basic Search -->
    <v-row no-gutters v-if="!this.advancedSearchSelected">
      <v-col class="col-6">
        <label for="user-search">
          Search
          <v-tooltip right>
            <template v-slot:activator="{ on }">
              <v-icon v-on="on" small>mdi-help-circle</v-icon>
            </template>
            <span>Search by username, email, or name</span>
          </v-tooltip>
        </label>

        <a id="advancedSearchLink" style="float:right" v-on:click="advancedSearchSelected=true">
          Advanced Search
        </a>

        <v-text-field
          id="user-search"
          outlined
          dense
          v-model="userSearchInput"
          placeholder="Username, email, name, or ID"
          @keyup.native.enter="searchUser"
        />
      </v-col>
      <v-col class="col-4">
        <v-btn id="search-button" class="secondary" medium @click.native="searchUser(userSearchInput)">Search Users</v-btn>
      </v-col>
      <v-col class="col-2">
        <v-btn id="create-user-button" class="success" medium @click.native="goToCreateUser">Create New User</v-btn>
      </v-col>
    </v-row>

    <!-- Advanced Search -->
    <v-row class="right-gutters" v-if="this.advancedSearchSelected">
      <v-col class="col-6">
        <h1>Advanced User Search</h1>
      </v-col>
      <v-col class="col-6">
        <v-btn id="adv-create-user-button" class="success" medium @click.native="goToCreateUser">Create New User</v-btn>
      </v-col>

      <v-col class="col-6">
        <label for="adv-search-last-name">
          Last Name
        </label>
        <v-text-field
            id="adv-search-last-name"
            outlined
            dense
            v-model="lastNameInput"
        />
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-first-name">
          First Name
        </label>
        <v-text-field
            id="adv-search-first-name"
            outlined
            dense
            v-model="firstNameInput"
        />
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-username">
          Username
        </label>
        <v-text-field
            id="adv-search-username"
            outlined
            dense
            v-model="usernameInput"
        />
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-email">
          Email
        </label>
        <v-text-field
            id="adv-search-email"
            outlined
            dense
            v-model="emailInput"
        />
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-org">
          Organization
        </label>
        <v-autocomplete
            id="org-details"
            v-model="organizationInput"
            :items="organizations"
            item-value="id"
            dense
            outlined
        ></v-autocomplete>
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-role">
          Role
        </label>
        <v-text-field
            id="adv-search-role"
            outlined
            dense
        />
      </v-col>
      <v-col class="col-4" style="margin-bottom: 30px">
        <v-btn id="adv-search-button" class="secondary" medium @click.native="searchUser(advancedSearchParams)">Search Users</v-btn>
        <a id="basicSearchLink" style="margin-left: 10px" v-on:click="advancedSearchSelected=false">
         Return to Basic Search
        </a>
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
import UsersRepository from "@/api/UsersRepository";
import organizations from "@/assets/organizations"

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
        { text: "Keycloak User ID", value: "id", class: "table-header" }
      ],
      organizations: organizations,
      footerProps: { "items-per-page-options": [15] },
      userSearchInput: "",
      lastNameInput: "",
      firstNameInput: "",
      usernameInput: "",
      emailInput: "",
      organizationInput: "",
      searchResults: [],
      userSearchLoadingStatus: false,
      advancedSearchSelected: false
    };
  },
  computed: {
    advancedSearchParams() {
      let params = '';
      params = this.addQueryParameter(params, "lastName", this.lastNameInput)
      params = this.addQueryParameter(params, "firstName", this.firstNameInput)
      params = this.addQueryParameter(params, "username", this.usernameInput)
      params = this.addQueryParameter(params, "email", this.emailInput)
      params = this.addQueryParameter(params, "org", this.organizationInput)
      return params;
    }
  },
  methods: {
    selectUser: function(user) {
      this.$store.commit("alert/dismissAlert");
      this.$router.push({ name: "UserUpdate", params: { userid: user.id } });
    },
    goToCreateUser: function() {
      this.$store.commit("alert/dismissAlert");
      this.$router.push({ name: "UserCreate" });
    },
    searchUser: function(queryParameters) {
      this.userSearchLoadingStatus = true;

      UsersRepository.get(
        "?briefRepresentation=false&first=0&max=300&search=" + queryParameters
      )
        .then(response => {
          this.searchResults = response.data;
        })
        .catch(error => {
          this.$store.commit("alert/setAlert", {
            message: "User search failed: " + error,
            type: "error"
          });
          window.scrollTo(0, 0);
        })
        .finally(() => (this.userSearchLoadingStatus = false));
    },
    addQueryParameter: function(parameters, parameter, value) {
      if (value) {
        parameters += "&" + parameter + "=" + value;
      }
      return parameters;
    }
  }
};
</script>

<style scoped>
#search-button {
  margin-top: 25px;
  margin-left: 20px;
}
#create-user-button {
  float: right;
  margin-top: 25px;
}
#adv-create-user-button {
  float: right;
}
.right-gutters .col {
  padding: 0 24px 0 0;
}
</style>