<!--suppress XmlInvalidId -->
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
          @keyup.enter="searchUser('&search='+userSearchInput.replaceAll('\\','%5C'))"
        />
      </v-col>
      <v-col class="col-4">
          <v-btn id="search-button" class="primary" medium @click.native="searchUser('&search='+userSearchInput.replaceAll('\\','%5C'))">Search Users</v-btn>
          <v-btn id="clear-search-button-basic" class="clear-search-button-basic" medium @click.native="clearSearchCriteria">Clear Search</v-btn>
      </v-col>
         
      <v-col class="col-2">
        <v-btn v-if="hasCreateUserRole" id="create-user-button" class="success" medium @click.native="goToCreateUser">Create New User</v-btn>
      </v-col>
    </v-row>

    <!-- Advanced Search -->
    <v-row class="right-gutters" v-if="this.advancedSearchSelected">
      <v-col class="col-6">
        <h1 id="adv-search-header">Advanced User Search</h1>
        <a id="basic-search-link" style="margin-left: 10px" v-on:click="advancedSearchSelected=false">
          Return to Basic Search
        </a>
      </v-col>
      <v-col class="col-6">
        <v-btn v-if="hasCreateUserRole" id="adv-create-user-button" class="success" medium @click.native="goToCreateUser">Create New User</v-btn>
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
            @keyup.enter="searchUser(advancedSearchParams)"
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
            @keyup.enter="searchUser(advancedSearchParams)"
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
            @keyup.enter="searchUser(advancedSearchParams)"
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
            @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col class="col-6">
        <label for="org-details">
          Organization
        </label>
        <v-autocomplete
            id="org-details"
            v-model="organizationInput"
            :items="organizations"
            item-value="id"
            outlined
            dense
            clearable
            placeholder="Select an Organization"
            @keyup.enter="searchUser(advancedSearchParams)"
        ></v-autocomplete>
      </v-col>
      <v-col class="col-2" >
        <label for="last-log-date-radio">
          Last logged-in
        </label>
        <v-radio-group v-model="radios" row dense style="margin: 0px">
          <v-radio
              id="last-log-date-radio"
              label="Before"
              value="Before"
          ></v-radio>
          <v-radio
              label="After"
              value="After"
          ></v-radio>
        </v-radio-group>
      </v-col>
      <v-col class="col-4">
        <label for="last-log-date">Date</label>
          <v-menu
              :close-on-content-click="false"
              :nudge-right="40"
              transition="scale-transition"
              offset-y
              min-width="auto">
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                  v-model="lastLogDate"
                  id="last-log-date"
                  v-bind="attrs"
                  v-on="on"
                  hint="YYYY-MM-DD format"
                  prepend-inner-icon="mdi-calendar"
                  :disabled="!(radios == 'Before'||radios == 'After')"
                  outlined
                  dense
                  clearable
                  @keyup.enter="searchUser(advancedSearchParams)"
              ></v-text-field>
            </template>
            <v-date-picker
                v-model="lastLogDate"
                @input="menuDate = false"
                max="maxDateInput"
                scrollable
                elevation="10"
            ></v-date-picker>
          </v-menu>
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
              clearable
              @keyup.enter="searchUser(advancedSearchParams)"
            ></v-autocomplete>
        </v-col>
      </v-row>
      <v-skeleton-loader
          ref="roleSkeleton"
          v-show="!rolesLoaded && selectedClientId"
          type="list-item@3">
      </v-skeleton-loader>
      <div v-if="selectedClientId" v-show="rolesLoaded">
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
      <v-col class="col-6" style="margin-bottom: 30px">
        <v-btn id="adv-search-button" class="primary" medium @click.native="searchUser(advancedSearchParams)">Search Users</v-btn>
        &nbsp;
        <v-btn id="clear-search-button" class="BC-Gov-SecondaryButton" medium @click.native="clearSearchCriteria">Clear Search</v-btn>
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
        >
          <!-- https://stackoverflow.com/questions/61394522/add-hyperlink-in-v-data-table-vuetify -->
          <template #item.username="{ item }">
            <a target="_blank" :href="`#/users/${item.id}`" v-on:click="openNewTab">
              {{ item.username }}
            </a>
            <v-icon small>mdi-open-in-new</v-icon>
          </template>
          <template v-if="searchResults.length > 0" v-slot:footer>
            <v-toolbar flat>
              <v-spacer/>
              <download-csv
                  :data="searchResults"
                  :fields="['id', 'username', 'enabled', 'firstName', 'lastName', 'email', 'role', 'lastLogDate']"
              >
                <v-btn id="csv-button" class="primary" small>Download results</v-btn>
              </download-csv>
            </v-toolbar>
          </template>
        </v-data-table>
      </v-col>
    </v-row>

  </div>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";
import ClientsRepository from "@/api/ClientsRepository";
import app_config from '@/loadconfig';

const options = {dateStyle: 'short'};
const formatDate = new Intl.DateTimeFormat(undefined, options).format;
    
export default {
  name: "UserSearch",
  data() {
    return {
      organizations: app_config.organizations
          .map((item) => {
            item.value = `{"id":"${item.id}","name":"${item.name}"}`
            item.text = `${item.id} - ${item.name}`;
            return item;
          }),
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
      advancedSearchSelected: false,
      newTab: false,
      radios: "",
      lastLogDate: "",
      rolesLoaded: false
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
      params = this.addQueryParameter(params, "username", this.usernameInput.replaceAll("\\","%5C"));
      params = this.addQueryParameter(params, "email", this.emailInput);
      params = this.addQueryParameter(params, "org", this.organizationInput);
      if (this.radios == "Before" && this.lastLogDate) {
        params = this.addQueryParameter(params, "lastLogBefore", this.lastLogDate);
      } else if (this.radios == "After" && this.lastLogDate) {
        params = this.addQueryParameter(params, "lastLogAfter", this.lastLogDate);
      }
      if (this.selectedClientId){
        params = this.addQueryParameter(params, "clientId", this.selectedClientId);
        this.clients.forEach(client => {
          if (client.id==this.selectedClientId){
            params = this.addQueryParameter(params, "clientName", client.name);
          }
        });
      }
      if (this.selectedRoles){
        let roles = this.selectedRoles.map(role => role.name).join(",");
        params = this.addQueryParameter(params,"selectedRoles",roles);
      }
      
      return params;
    },
    headers(){
      let hdrs = [
        { text: "Username", value: "username", class: "table-header" },
        { text: "First name", value: "firstName", class: "table-header" },
        { text: "Last name", value: "lastName", class: "table-header" },
        { text: "Email", value: "email", class: "table-header" }
      ];
      let showLogins = (this.radios!=null && this.radios!="") && (this.lastLogDate!=null && this.lastLogDate!="");
      let showRoles = (this.selectedClientId!=null && this.selectedClientId!="");
      if (showLogins){
        hdrs.push({ text: "Last Log Date", value: "lastLogDate", class: "table-header" });
      }
      if (showRoles){
        hdrs.push({ text: "Role", value: "role", class: "table-header" });
      }
      return hdrs;
    },
    itemsInColumn() {
      return Math.ceil(this.clientRoles.length / this.numberOfClientRoleColumns);
    },
    numberOfClientRoleColumns() {
      return (this.clientRoles.length > 10) ? 2 : 1
    },
    maxResults() {
      return app_config.config.max_results ? app_config.config.max_results : 100;
    },
    maxDateInput() {
      return new Date().toISOString().substr(0, 10).toString();
    },
    hasCreateUserRole: function() {
      const umsClientId = "USER-MANAGEMENT-SERVICE";
      const createUserRoleName = "create-user";

      return !!this.$keycloak.tokenParsed.resource_access[umsClientId].roles.includes(createUserRoleName)
    }
  },
  methods: {
    openNewTab: function() {
      this.newTab = true;
    },
    selectUser: function(user) {
      if (this.newTab) {
        this.newTab = false;
        return;
      }
      this.$store.commit("alert/dismissAlert");
      this.$router.push({ name: "UserUpdate", params: { userid: user.id } });
    },
    goToCreateUser: function() {
      this.$store.commit("alert/dismissAlert");
      this.$router.push({ name: "UserCreate" });
    },
    searchUser: async function(queryParameters) {
      this.$store.commit("alert/dismissAlert");
      const maxSearch = app_config.config.max_search
                          ? app_config.config.max_search
                          : (this.maxResults * 10);
      this.userSearchLoadingStatus = true;
      try {
        let results = (await UsersRepository.get(
          `?briefRepresentation=false&first=0&max=${maxSearch}` + queryParameters
        )).data;
        for (let e of results) {
          if (e.lastLogDate) {
            e.lastLogDate = formatDate(e.lastLogDate);
          }
        }
        this.setSearchResults(results);
      }
      catch(error) {
        this.handleError("User search failed", error);
      }
      finally {
        this.userSearchLoadingStatus = false;
      }
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
      this.rolesLoaded = false;
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
        this.rolesLoaded = true;
      }
    },
    appendClientInfo: function(role) {
      role.clientId = role.containerId;
      role.clientName = this.clients
          .find(client => this.clientId === client.id).name;
      return role;
    },
    setSearchResults(results) {
      const maxRes = this.maxResults;
      if (results.length > maxRes) {
        this.searchResults = results.slice(0, maxRes);
        this.$store.commit("alert/setAlert", {
          message: "Your search returned more than the maximum number of results ("
                  + maxRes + "). Please consider refining the search criteria.",
          type: "warning"
        });
        window.scrollTo(0, 0);
      }
      else {
        this.searchResults = results;
      }
    },
    handleError(message, error) {
      this.$store.commit("alert/setAlert", {
        message: message + ": " + error,
        type: "error"
      });
      window.scrollTo(0, 0);
    },
    clearSearchCriteria(){
      this.selectedRoles = []
      this.userSearchInput = "";
      this.lastNameInput = "";
      this.firstNameInput = "";
      this.usernameInput = "";
      this.emailInput = "";
      this.organizationInput = "";
      this.selectedClientId = null;
      this.lastLogDate = "";
      this.radios = "";
    }
  }
};
</script>

<style scoped>
#search-button {
  margin-top: 25px;
  margin-left: 20px;
}
#adv-search-header {
  display: inline-block;
}
#basic-search-link {
  float: right;
  display: inline-block;
  margin-top: 10px;
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
.row.right-gutters {
  margin: 0;
}
#clear-search-button-basic{
  margin-top: 25px;
  margin-left: 20px;
}
</style>
