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

        <a
          id="advancedSearchLink"
          style="float: right"
          v-on:click="advancedSearchSelected = true"
        >
          Advanced Search
        </a>

        <v-text-field
          id="user-search"
          outlined
          dense
          v-model.trim="userSearchInput"
          placeholder="Username, email, name"
          @keyup.enter="
            searchUser('&search=' + userSearchInput.replaceAll('\\', '%5C'))
          "
        />
      </v-col>
      <v-col class="col-4">
        <v-btn
          id="search-button"
          class="primary"
          medium
          @click.native="
            searchUser('&search=' + userSearchInput.replaceAll('\\', '%5C'))
          "
        >
          Search Users
        </v-btn>
        <v-btn
          id="clear-search-button-basic"
          class="BC-Gov-SecondaryButton"
          medium
          @click.native="clearSearchCriteria"
        >
          Clear Search
        </v-btn>
      </v-col>

      <v-col class="col-2">
        <v-btn
          v-if="hasCreateUserRole"
          id="create-user-button"
          class="success"
          medium
          @click.native="goToCreateUser"
        >
          Register User
        </v-btn>
      </v-col>
    </v-row>

    <!-- Advanced Search -->
    <v-row class="right-gutters" v-if="this.advancedSearchSelected">
      <v-col class="col-6">
        <h1 id="adv-search-header">Advanced User Search</h1>
        <a
          id="basic-search-link"
          style="margin-left: 10px"
          v-on:click="advancedSearchSelected = false"
        >
          Return to Basic Search
        </a>
      </v-col>
      <v-col class="col-6">
        <v-btn
          v-if="hasCreateUserRole"
          id="adv-create-user-button"
          class="success"
          medium
          @click.native="goToCreateUser"
        >
          Register User
        </v-btn>
      </v-col>

      <v-col class="col-6">
        <label for="adv-search-last-name">Last Name</label>
        <v-text-field
          id="adv-search-last-name"
          outlined
          dense
          v-model.trim="lastNameInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-first-name">First Name</label>
        <v-text-field
          id="adv-search-first-name"
          outlined
          dense
          v-model.trim="firstNameInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-username">Username</label>
        <v-text-field
          id="adv-search-username"
          outlined
          dense
          v-model.trim="usernameInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col class="col-6">
        <label for="adv-search-email">Email</label>
        <v-text-field
          id="adv-search-email"
          outlined
          dense
          v-model.trim="emailInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col class="col-6">
        <label for="org-details">Organization</label>
        <v-autocomplete
          id="org-details"
          v-model="organizationInput"
          :items="organizations"
          item-value="organizationId"
          outlined
          dense
          clearable
          placeholder="Select an Organization"
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
                            clientRoles[roleArrayPosition(col, item)]
                              .description
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
    </v-card>

    <v-row class="right-gutters" v-if="this.advancedSearchSelected">
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
          :show-select="bulkRemovalAllowed"
          v-on:click:row="selectUser"
          v-model="usersSelectedForBulkRemoval"
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
              <template v-if="bulkRemovalAllowed">
                <v-dialog
                  v-model="bulkRemoveAccessDialog"
                  persistent
                  scrollable
                  max-width="700"
                >
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn
                      id="remove-access-button"
                      class="error"
                      small
                      v-bind="attrs"
                      v-on="on"
                      :disabled="usersSelectedForBulkRemoval.length === 0"
                    >
                      Remove Access
                    </v-btn>
                  </template>

                  <v-card>
                    <template v-if="!bulkRemovalResponseIsPresent()">
                      <v-card-title class="text-h5">
                        {{
                          `Are you sure you want to remove access of ${
                            usersSelectedForBulkRemoval.length
                          } ${
                            usersSelectedForBulkRemoval.length > 1
                              ? `users`
                              : `user`
                          }?`
                        }}
                      </v-card-title>
                    </template>
                    <template v-else>
                      <v-card-title>Operation complete</v-card-title>
                    </template>

                    <v-card-text>
                      <v-list>
                        <v-subheader>
                          {{ getSelectedClientName(selectedClientId) }}
                          <v-spacer></v-spacer>
                          <v-progress-circular
                            v-if="bulkRemovalRequestInProgress"
                            color="primary"
                            indeterminate
                          ></v-progress-circular>
                        </v-subheader>
                        <v-divider></v-divider>
                        <v-list-item
                          v-for="(user, i) in usersSelectedForBulkRemoval"
                          :key="i"
                        >
                          <template>
                            <template v-if="bulkRemovalResponseIsPresent()">
                              <v-tooltip bottom>
                                <template v-slot:activator="{ on, attrs }">
                                  <v-list-item-avatar v-bind="attrs" v-on="on">
                                    <v-icon
                                      :class="
                                        getBulkRemovalListItemDetails(user.id)
                                          .iconClass
                                      "
                                      dark
                                    >
                                      {{
                                        getBulkRemovalListItemDetails(user.id)
                                          .icon
                                      }}
                                    </v-icon>
                                  </v-list-item-avatar>
                                </template>
                                <span>
                                  {{
                                    getBulkRemovalListItemDetails(user.id)
                                      .tooltipText
                                  }}
                                </span>
                              </v-tooltip>
                            </template>
                            <template v-else>
                              <v-list-item-avatar>
                                <v-icon
                                  :class="
                                    getBulkRemovalListItemDetails(user.id)
                                      .iconClass
                                  "
                                  dark
                                >
                                  {{
                                    getBulkRemovalListItemDetails(user.id).icon
                                  }}
                                </v-icon>
                              </v-list-item-avatar>
                            </template>
                          </template>

                          <v-list-item-content>
                            <v-list-item-title
                              v-text="user.username"
                            ></v-list-item-title>

                            <v-list-item-subtitle
                              v-text="user.role"
                            ></v-list-item-subtitle>
                          </v-list-item-content>
                        </v-list-item>
                      </v-list>
                    </v-card-text>
                    <v-card-actions>
                      <v-spacer></v-spacer>
                      <!-- If user did not send bulk removal request -->
                      <template
                        v-if="
                          !bulkRemovalRequestInProgress &&
                          !bulkRemovalResponseIsPresent()
                        "
                      >
                        <v-btn
                          class="primary"
                          text
                          @click="closeBulkRemovalDialog"
                        >
                          Cancel
                        </v-btn>
                        <v-btn class="error" text @click="bulkRemoveUserAccess">
                          Remove Access
                        </v-btn>
                      </template>
                      <template v-else>
                        <v-btn
                          class="primary"
                          text
                          @click="closeBulkRemovalDialog"
                        >
                          Close
                        </v-btn>
                      </template>
                    </v-card-actions>
                  </v-card>
                </v-dialog>
              </template>
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
    name: "UserSearch",
    data() {
      return {
        organizations: this.$organizations.map((item) => {
          item.value = `{"id":"${item.organizationId}","name":"${item.name}"}`;
          item.text = `${item.organizationId} - ${item.name}`;
          return item;
        }),
        clients: [""],
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
        rolesLoaded: false,
        usersSelectedForBulkRemoval: [],
        bulkRemoveAccessDialog: false,
        bulkRemovalRequestInProgress: false, //waiting for a bulk-removal response from UMS
        bulkRemovalResponse: [],
        bulkRemovalListItemDetails: {},
      };
    },
    async created() {
      await this.loadClients();
    },
    computed: {
      advancedSearchParams() {
        let params = "";
        params = this.addQueryParameter(params, "lastName", this.lastNameInput);
        params = this.addQueryParameter(
          params,
          "firstName",
          this.firstNameInput
        );
        params = this.addQueryParameter(
          params,
          "username",
          this.usernameInput.replaceAll("\\", "%5C")
        );
        params = this.addQueryParameter(params, "email", this.emailInput);
        params = this.addQueryParameter(params, "org", this.organizationInput);
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
      hasCreateUserRole: function () {
        const umsClientId = "USER-MANAGEMENT-SERVICE";
        const createUserRoleName = "create-user";

        return this.$keycloak.tokenParsed.resource_access[
          umsClientId
        ].roles.includes(createUserRoleName);
      },
      bulkRemovalAllowed() {
        return (
          this.$keycloak.tokenParsed.resource_access?.[
            "USER-MANAGEMENT-SERVICE"
          ]?.roles.includes("bulk-removal") &&
          this.advancedSearchSelected &&
          !!this.selectedClientId
        );
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
      goToCreateUser: function () {
        this.$store.commit("alert/dismissAlert");
        this.$router.push({ name: "UserCreate" });
      },
      searchUser: async function (queryParameters) {
        this.usersSelectedForBulkRemoval = [];
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
        this.closeBulkRemovalDialog();
        this.$store.commit("alert/setAlert", {
          message: message + ": " + error,
          type: "error",
        });
        window.scrollTo(0, 0);
      },
      clearSearchCriteria() {
        this.selectedRoles = [];
        this.userSearchInput = "";
        this.lastNameInput = "";
        this.firstNameInput = "";
        this.usernameInput = "";
        this.emailInput = "";
        this.organizationInput = "";
        this.selectedClientId = null;
        this.lastLogDate = "";
        this.radios = "";
      },
      async bulkRemoveUserAccess() {
        this.bulkRemovalRequestInProgress = true;
        let bulkRemovalRequest = {};
        this.usersSelectedForBulkRemoval.forEach((user) => {
          bulkRemovalRequest[user.id] = this.getRolesRepresentation(user.role);
        });
        this.bulkRemovalResponse =
          await UsersRepository.bulkDeleteUserClientRoles(
            this.selectedClientId,
            bulkRemovalRequest
          )
            .then((response) => {
              return response.data;
            })
            .catch((error) => {
              this.handleError("Bulk removal request failed", error);
            });
        this.populateBulkRemovalListItemDetails();
        this.bulkRemovalRequestInProgress = false;
      },
      closeBulkRemovalDialog() {
        this.updateUserSearchResultsAfterBulkRemoval();
        this.bulkRemovalRequestInProgress = false;
        this.bulkRemoveAccessDialog = false;
        this.bulkRemovalResponse = [];
        this.bulkRemovalListItemDetails = {};
        this.usersSelectedForBulkRemoval = [];
      },
      getSelectedClientName(id) {
        return this.clients.find((client) => client.id === id).clientId;
      },
      getRolesRepresentation(roleNames) {
        let tmp = roleNames.split(",").map((name) => name.trim());
        let roles = [];
        tmp.forEach((name) => {
          let matchingRole = this.clientRoles.find((r) => r.name === name);
          if (matchingRole) {
            //In order for keycloak API to accept the payload, we need to filter out clientId from the RoleRepresentation
            //Destructuring the object and discarding the clientId
            const { ...roleRepresentation } = matchingRole;
            delete roleRepresentation.clientId;
            roles.push(roleRepresentation);
          }
        });
        return roles;
      },
      updateUserSearchResultsAfterBulkRemoval() {
        if (this.bulkRemovalResponseIsPresent()) {
          this.searchResults = this.searchResults.filter(
            (user) =>
              !this.bulkRemovalResponse.some(
                (bulkResponseUser) => bulkResponseUser.userId === user.id
              )
          );
        }
      },
      bulkRemovalResponseIsPresent() {
        return this.bulkRemovalResponse && this.bulkRemovalResponse.length;
      },
      populateBulkRemovalListItemDetails() {
        this.bulkRemovalResponse.forEach((details) => {
          if (details.statusCode === "NO_CONTENT") {
            this.bulkRemovalListItemDetails[details.userId] = {
              icon: "mdi-check-circle",
              iconClass: "green lighten-1",
              tooltipText: "Access removed successfully",
            };
          } else {
            this.bulkRemovalListItemDetails[details.userId] = {
              icon: "mdi-alert-circle",
              iconClass: "red lighten-1",
              tooltipText: `Could not remove access: ${details.body.error}`,
            };
          }
        });
      },
      getBulkRemovalListItemDetails(userId) {
        if (
          !this.bulkRemovalListItemDetails ||
          Object.keys(this.bulkRemovalListItemDetails).length !== 0
        ) {
          return this.bulkRemovalListItemDetails[userId];
        } else {
          return {
            icon: "mdi-account",
            iconClass: "grey lighten-1",
          };
        }
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
