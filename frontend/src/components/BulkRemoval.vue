<template>
  <div>
    <v-row class="right-gutters">
      <v-col class="col-6">
        <h1 id="adv-search-header">Bulk User Permission Removal</h1>
      </v-col>
    </v-row>
    <v-row>
      <v-col class="col-6">
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
      <v-col class="col-2">
        <label for="last-log-date-radio">Last logged-in</label>
        <v-radio-group v-model="radios" row dense style="margin: 0px">
          <v-radio
            id="last-log-date-radio"
            label="Before"
            value="Before"
          ></v-radio>
          <v-radio label="After" value="After"></v-radio>
        </v-radio-group>
      </v-col>
      <v-col class="col-4">
        <label for="last-log-date">Date</label>
        <v-menu
          :close-on-content-click="false"
          :nudge-right="40"
          transition="scale-transition"
          offset-y
          min-width="auto"
        >
          <template v-slot:activator="{ on, attrs }">
            <v-text-field
              v-model.trim="lastLogDate"
              id="last-log-date"
              v-bind="attrs"
              v-on="on"
              hint="YYYY-MM-DD format"
              prepend-inner-icon="mdi-calendar"
              :disabled="!(radios == 'Before' || radios == 'After')"
              outlined
              dense
              clearable
              @keyup.enter="searchUser(advancedSearchParams)"
            ></v-text-field>
          </template>
          <v-date-picker
            v-model="lastLogDate"
            @input="menuDate = false"
            :max="maxDateInput"
            :min="minDateInput"
            scrollable
            elevation="10"
          ></v-date-picker>
        </v-menu>
      </v-col>
    </v-row>
    <v-skeleton-loader
      ref="roleSkeleton"
      v-show="!rolesLoaded && selectedClientId"
      type="list-item@3"
    ></v-skeleton-loader>
    <div v-if="selectedClientId" v-show="rolesLoaded">
      <v-row no-gutters>
        <v-col class="col-12">
          <v-row no-gutters>
            <v-col class="col-12">
              <label>Roles</label>
            </v-col>
            <v-col
              class="col-6"
              v-for="col in numberOfClientRoleColumns"
              :key="col"
            >
              <span v-for="item in itemsInColumn" :key="item">
                <v-checkbox
                  v-if="item * col <= clientRoles.length"
                  class="roles-checkbox"
                  hide-details="auto"
                  v-model="selectedRoles"
                  :value="clientRoles[roleArrayPosition(col, item)]"
                  :key="clientRoles[roleArrayPosition(col, item)].name"
                >
                  <template v-slot:label>
                    <v-tooltip
                      v-if="
                        clientRoles[roleArrayPosition(col, item)].description
                      "
                      right
                      max-width="300px"
                    >
                      <template v-slot:activator="{ on }">
                        <span v-on="on">
                          {{ clientRoles[roleArrayPosition(col, item)].name }}
                        </span>
                      </template>
                      <span class="white-space-fix">
                        {{
                          clientRoles[roleArrayPosition(col, item)].description
                        }}
                      </span>
                    </v-tooltip>
                    <span v-else>
                      {{ clientRoles[roleArrayPosition(col, item)].name }}
                    </span>
                  </template>
                </v-checkbox>
              </span>
            </v-col>
          </v-row>
        </v-col>
      </v-row>
    </div>

    <v-row class="right-gutters" style="margin-top: 30px">
      <v-col class="col-6" style="margin-bottom: 30px">
        <v-btn
          id="adv-search-button"
          class="primary"
          medium
          @click.native="searchUser(advancedSearchParams)"
        >
          Search Users
        </v-btn>
        &nbsp;
        <v-btn
          id="clear-search-button"
          class="BC-Gov-SecondaryButton"
          medium
          @click.native="clearSearchCriteria"
        >
          Clear Search
        </v-btn>
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
          show-select
          v-model="selectedUsers"
        >
          <!-- https://stackoverflow.com/questions/61394522/add-hyperlink-in-v-data-table-vuetify -->
          <template #item.username="{ item }">
            <a
              target="_blank"
              :href="`#/users/${item.id}`"
              v-on:click="openNewTab"
            >
              {{ item.username }}
            </a>
            <v-icon small>mdi-open-in-new</v-icon>
          </template>
          <template v-if="searchResults.length > 0" v-slot:footer>
            <v-toolbar flat>
              <v-spacer />
              <download-csv
                :data="searchResults"
                :fields="[
                  'id',
                  'username',
                  'enabled',
                  'firstName',
                  'lastName',
                  'email',
                  'role',
                  'lastLogDate',
                ]"
              >
                <v-btn id="csv-button" class="primary" small>
                  Download results
                </v-btn>
              </download-csv>
              &nbsp; &nbsp;
              <v-btn id="remove-button" class="error" small>
                Remove Users Access
              </v-btn>
            </v-toolbar>
          </template>
        </v-data-table>
      </v-col>
    </v-row>
  </div>
</template>

<script>
  import ClientsRepository from "@/api/ClientsRepository";
  import UsersRepository from "@/api/UsersRepository";

  const options = { dateStyle: "short" };
  const formatDate = new Intl.DateTimeFormat("en-CA", options).format;

  export default {
    name: "BulkRemoval",
    data() {
      return {
        clients: [""],
        selectedClientId: null,
        clientRoles: [],
        selectedRoles: [],
        footerProps: { "items-per-page-options": [15] },
        searchResults: [],
        userSearchLoadingStatus: false,
        newTab: false,
        radios: "",
        lastLogDate: "",
        rolesLoaded: false,
        selectedUsers: [],
      };
    },
    async created() {
      await this.loadClients();
    },
    computed: {
      advancedSearchParams() {
        let params = "";
        if (this.radios == "Before" && this.lastLogDate) {
          params = this.addQueryParameter(
            params,
            "lastLogBefore",
            this.lastLogDate
          );
        } else if (this.radios == "After" && this.lastLogDate) {
          params = this.addQueryParameter(
            params,
            "lastLogAfter",
            this.lastLogDate
          );
        }
        if (this.selectedClientId) {
          params = this.addQueryParameter(
            params,
            "clientId",
            this.selectedClientId
          );
        }
        if (this.selectedRoles) {
          let roles = this.selectedRoles.map((role) => role.name).join(",");
          params = this.addQueryParameter(params, "selectedRoles", roles);
        }

        return params;
      },
      headers() {
        let hdrs = [
          { text: "Username", value: "username", class: "table-header" },
          { text: "First name", value: "firstName", class: "table-header" },
          { text: "Last name", value: "lastName", class: "table-header" },
          { text: "Email", value: "email", class: "table-header" },
        ];
        let showLogins =
          this.radios != null &&
          this.radios != "" &&
          this.lastLogDate != null &&
          this.lastLogDate != "";
        let showRoles =
          this.selectedClientId != null && this.selectedClientId != "";
        if (showLogins) {
          hdrs.push({
            text: "Last Log Date",
            value: "lastLogDate",
            class: "table-header",
          });
        }
        if (showRoles) {
          hdrs.push({ text: "Role", value: "role", class: "table-header" });
        }
        return hdrs;
      },
      itemsInColumn() {
        return Math.ceil(
          this.clientRoles.length / this.numberOfClientRoleColumns
        );
      },
      numberOfClientRoleColumns() {
        return this.clientRoles.length > 10 ? 2 : 1;
      },
      maxDateInput() {
        return formatDate(new Date());
      },
      minDateInput() {
        var date = new Date();
        date.setFullYear(date.getFullYear() - 1);
        return formatDate(date);
      },
    },
    methods: {
      openNewTab: function () {
        this.newTab = true;
      },
      selectUser: function (user) {
        if (this.newTab) {
          this.newTab = false;
          return;
        }
        this.$store.commit("alert/dismissAlert");
        this.$router.push({ name: "UserUpdate", params: { userid: user.id } });
      },
      searchUser: async function (queryParameters) {
        if (this.noQueryParameters(queryParameters)) {
          this.$store.commit("alert/setAlert", {
            message: "The Search Criteria cannot be blank.",
            type: "warning",
          });
          window.scrollTo(0, 0);
          return;
        } else {
          this.$store.commit("alert/dismissAlert");
          this.userSearchLoadingStatus = true;
          try {
            let results = (
              await UsersRepository.get(
                `?briefRepresentation=true` + queryParameters
              )
            ).data;
            for (let e of results) {
              if (e.lastLogDate && e.lastLogDate !== "Over a year ago") {
                e.lastLogDate = formatDate(e.lastLogDate);
              }
            }
            this.setSearchResults(results);
          } catch (error) {
            this.handleError("User search failed", error);
          } finally {
            this.userSearchLoadingStatus = false;
          }
        }
      },
      noQueryParameters: function (queryParameters) {
        //&search= corresponds to basic search with no parameters
        return (
          queryParameters === null ||
          queryParameters.trim().length === 0 ||
          queryParameters === "&search="
        );
      },
      addQueryParameter: function (parameters, parameter, value) {
        if (value) {
          parameters += "&" + parameter + "=" + value;
        }
        return parameters;
      },
      roleArrayPosition: function (col, item) {
        return (col - 1) * this.itemsInColumn + item - 1;
      },
      loadClients: function () {
        return ClientsRepository.get()
          .then((response) => {
            const clientsWithAliases = ClientsRepository.assignClientAliases(
              response.data
            );
            this.clients.push(...clientsWithAliases);
          })
          .catch((error) => {
            this.handleError("Client search failed", error);
          });
      },
      loadUserClientRoles: async function () {
        this.rolesLoaded = false;
        this.clientRoles = [];
        this.selectedRoles = [];
        if (this.selectedClientId) {
          let loadedRoles = await ClientsRepository.getRoles(
            this.selectedClientId
          )
            .then((response) =>
              response.data
                .map((role) => this.appendClientInfo(role))
                .sort((a, b) => a.name.localeCompare(b.name))
            )
            .catch((error) => {
              this.handleError("Client Role search failed", error);
            });
          this.clientRoles.push(...loadedRoles);
          this.rolesLoaded = true;
        }
      },
      appendClientInfo: function (role) {
        role.clientId = role.containerId;
        role.clientName = this.clients.find(
          (client) => this.clientId === client.id
        ).name;
        return role;
      },
      setSearchResults(results) {
        this.searchResults = results.sort((a, b) =>
          a.username.localeCompare(b.username)
        );
      },
      handleError(message, error) {
        this.$store.commit("alert/setAlert", {
          message: message + ": " + error,
          type: "error",
        });
        window.scrollTo(0, 0);
      },
      clearSearchCriteria() {
        this.selectedRoles = [];
        this.selectedClientId = null;
        this.lastLogDate = "";
        this.radios = "";
      },
      removeUserAccess() {
        console.log(`Would remove ${this.selectedUsers.length} users access`);
        console.log(this.searchUsers);
      },
    },
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
  #clear-search-button-basic {
    margin-top: 25px;
    margin-left: 20px;
  }
</style>
