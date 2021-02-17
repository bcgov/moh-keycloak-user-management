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
          <v-btn id="search-button" class="secondary" medium @click.native="searchUser('&search='+userSearchInput)">Search Users</v-btn>
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
            outlined
            dense
        ></v-autocomplete>
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-role">
          Role
        </label>
        <v-autocomplete
            id="adv-search-role"
            v-model="roleInput"
            :items="clientRoles"
            item-value="id"
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
import ClientsRepository from "@/api/ClientsRepository";
import organizations from "@/assets/organizations"

// REVIEW - Is this the best approach to define custom objects using Vue?
//   Should the custom object be defined in a more Vue-compliant way?
class ClientRole {

  constructor(role, clients) {
    this.id = role.id;
    this.name = role.name;
    this.description = role.description;
    this.clientId = role.containerId;
    this.clientName = clients.find(client => this.clientId === client.id).name;
  }

  toString() {
    let s = `${this.clientName} : ${this.name}`;
    if (this.description) {
      s += ` - ${this.description}`;
    }
    return s;
  }

}

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
      clients: [],
      clientRoles: [ "" ],
      footerProps: { "items-per-page-options": [15] },
      userSearchInput: "",
      lastNameInput: "",
      firstNameInput: "",
      usernameInput: "",
      emailInput: "",
      organizationInput: "",
      roleInput: "",
      searchResults: [],
      userSearchLoadingStatus: false,
      advancedSearchSelected: false
    };
  },
  async created() {
    // TODO error handling
    // REVIEW - Should this be pushed to the "$store" once it is loaded?
    await this.loadClientsAndRoles();
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
      let roleId = this.roleInput.trim();

      UsersRepository.get(
        "?briefRepresentation=false&first=0&max=300" + queryParameters
      )
        .then(response => {
          let results = response.data;
          if (this.advancedSearchSelected && roleId !== "") {
            this.findUserIdsInRole(roleId)
              .then(roleSearchResults =>
                this.searchResults = results.filter(
                  user => roleSearchResults.includes(user.id)
                )
              );
          }
          else {
            this.searchResults = results;
          }
        })
        .catch(error => {
          this.handleError("User search failed", error);
        })
        .finally(() => (this.userSearchLoadingStatus = false));
    },
    addQueryParameter: function(parameters, parameter, value) {
      if (value) {
        parameters += "&" + parameter + "=" + value;
      }
      return parameters;
    },
    loadClientsAndRoles: function() {
      return ClientsRepository.get()
        .then(response => {
          this.clients = response.data;
          let rolesRequests = this.clients.map(
            client => ClientsRepository.getRoles(client.id)
          );
          Promise.all(rolesRequests)
            .then(responses => {
              let clientRole = responses
                .flatMap(response => response.data)
                .map(role => new ClientRole(role, this.clients))
                .sort((a, b) => a.toString().localeCompare(b.toString()));
              this.clientRoles.push(...clientRole);
            });
        })
        .catch(error => {
          this.handleError("Client search failed", error);
        });
    },
    findUserIdsInRole: function(roleId) {
      let clientRole = this.clientRoles.find(
        clientRole => clientRole.id === roleId
      );
      return ClientsRepository.getUsersInRole(clientRole.clientId, clientRole.name)
        .then(response => {
          return response.data.map(user => user.id);
        })
        .catch(error => {
          this.handleError("Role search failed", error);
        })
    },
    handleError(message, error) {
      this.$store.commit("alert/setAlert", {
        message: message + ": " + error,
        type: "error"
      });
      window.scrollTo(0, 0);
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
