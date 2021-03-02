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
    </v-row>
    <v-card outlined class="subgroup" v-if="this.advancedSearchSelected">
      <h2>User Roles</h2>

      <v-row no-gutters>
        <v-col class="col-5 col">
          <label for="select-client">Application</label>
          <v-autocomplete
              id="select-client"
              outlined
              dense
              :items="clients"
              item-text="clientId"
              item-value="id"
              placeholder="Select an Application"
              v-model="selectedClientId"
              @change="loadUserClientRoles()"
            ></v-autocomplete>
        </v-col>
      </v-row>

      <div v-if="selectedClientId">
        <v-row no-gutters>
          <v-col class="col-4">
            <v-row no-gutters>
              <v-col class="col-12">
                <label>Roles</label>
              </v-col>
              <v-col class="col-6" v-for="col in numberOfClientRoleColumns" :key="col">
                <span v-for="item in itemsInColumn" :key="item">
                  <v-checkbox
                      v-if="item*col <= clientRoles.length"
                      class="roles-checkbox"
                      hide-details="auto"
                      v-model="selectedRoles"
                      :value="clientRoles[roleArrayPosition(col, item)]"
                      :key="clientRoles[roleArrayPosition(col, item)].name"
                      >
                    <span slot="label" class="tooltip" :id="'role-' + roleArrayPosition(col,item)">
                      {{clientRoles[roleArrayPosition(col, item)].name}}
                      <span v-show="clientRoles[roleArrayPosition(col, item)].description" class="tooltiptext"> {{ clientRoles[roleArrayPosition(col, item)].description }} </span>
                    </span>
                  </v-checkbox>
                </span>
              </v-col>
            </v-row>
          </v-col>
        </v-row>
      </div>
    </v-card>
    <v-row class="right-gutters" v-if="this.advancedSearchSelected">
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
      clients: [ "" ],
      selectedClientId: null,
      clientRoles: [],
      selectedRoles: [],
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
  async created() {
    await this.loadClients();
  },
  computed: {
    advancedSearchParams() {
      let params = '';
      params = this.addQueryParameter(params, "lastName", this.lastNameInput);
      params = this.addQueryParameter(params, "firstName", this.firstNameInput);
      params = this.addQueryParameter(params, "username", this.usernameInput);
      params = this.addQueryParameter(params, "email", this.emailInput);
      params = this.addQueryParameter(params, "org", this.organizationInput);
      return params;
    },
    itemsInColumn() {
      return Math.ceil(this.clientRoles.length / this.numberOfClientRoleColumns);
    },
    numberOfClientRoleColumns() {
      return (this.clientRoles.length > 10) ? 2 : 1
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
      let isSearchByRole = this.advancedSearchSelected
        && this.selectedRoles.length > 0;
      
      UsersRepository.get(
        "?briefRepresentation=false&first=0&max=300" + queryParameters
      )
        .then(response => {
          let results = response.data;
          if (isSearchByRole) {
            this.filterUsersByRole(results)
              .then(filteredResults => this.searchResults = filteredResults)
              .finally(() => (this.userSearchLoadingStatus = false));
          }
          else {
            this.searchResults = results;
          }
        })
        .catch(error => {
          this.handleError("User search failed", error);
        })
        .finally(() => {
          if (!isSearchByRole) {
            this.userSearchLoadingStatus = false;
          }
        });
    },
    addQueryParameter: function(parameters, parameter, value) {
      if (value) {
        parameters += "&" + parameter + "=" + value;
      }
      return parameters;
    },
    roleArrayPosition: function(col, item) {
      return (col - 1) * (this.itemsInColumn) + item - 1;
    },
    loadClients: function() {
      return ClientsRepository.get()
        .then(response => {
          this.clients.push(...response.data);
        })
        .catch(error => {
          this.handleError("Client search failed", error);
        });
    },
    loadUserClientRoles: async function() {
      this.clientRoles = [];
      this.selectedRoles = [];
      if (this.selectedClientId) {
        let loadedRoles = await ClientsRepository.getRoles(this.selectedClientId)
          .then(response => response.data
            .map(role => this.appendClientInfo(role))
            .sort((a, b) => a.name.localeCompare(b.name))
          )
          .catch(error => {
            this.handleError("Client Role search failed", error);
          });
        this.clientRoles.push(...loadedRoles);
      }
    },
    appendClientInfo: function(role) {
      role.clientId = role.containerId;
      role.clientName = this.clients
          .find(client => this.clientId === client.id).name;
      return role;
    },
    filterUsersByRole: function(searchResults) {
      let roleRequests = this.selectedRoles.map(
        clientRole => ClientsRepository
          .getUsersInRole(clientRole.clientId, clientRole.name)
          .catch(error => {
            this.handleError(`Search failed for role ${clientRole.name}`, error);
          })
      );
      return Promise.all(roleRequests)
        .then(responses => responses.flatMap(
          response => response.data.map(user => user.id)
        ))
        .then(userIds => searchResults.filter(
          user => userIds.includes(user.id)
        ));
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
