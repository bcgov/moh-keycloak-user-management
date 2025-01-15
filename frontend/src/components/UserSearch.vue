<!--suppress XmlInvalidId -->
<template>
  <div>
    <!-- Basic Search -->
    <v-row no-gutters v-if="!this.advancedSearchSelected">
      <v-col cols="6">
        <label for="user-search">
          Search
          <v-tooltip location="right">
            <template v-slot:activator="{ props }">
              <v-icon v-bind="props" size="x-small" class="help-icon">
                mdi-help-circle
              </v-icon>
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
          variant="outlined"
          density="compact"
          v-model.trim="userSearchInput"
          placeholder="Username, email, name"
          @keyup.enter="
            searchUser('&search=' + encodeURIComponent(userSearchInput))
          "
        />
      </v-col>
      <v-col cols="4">
        <v-btn
          id="search-button"
          class="bg-primary"
          size="default"
          @click="searchUser('&search=' + encodeURIComponent(userSearchInput))"
        >
          Search Users
        </v-btn>
        <v-btn
          id="clear-search-button-basic"
          class="BC-Gov-SecondaryButton"
          size="default"
          @click="clearSearchCriteria"
        >
          Clear Search
        </v-btn>
      </v-col>

      <v-col cols="2">
        <v-btn
          v-if="hasCreateUserRole"
          id="create-user-button"
          class="bg-success"
          size="default"
          @click="goToCreateUser"
        >
          Register User
        </v-btn>
      </v-col>
    </v-row>

    <!-- Advanced Search -->
    <v-row class="right-gutters" v-if="this.advancedSearchSelected">
      <v-col cols="6" class="col">
        <h1 id="adv-search-header">Advanced User Search</h1>
        <a
          id="basic-search-link"
          style="margin-left: 10px"
          v-on:click="advancedSearchSelected = false"
        >
          Return to Basic Search
        </a>
      </v-col>
      <v-col cols="6" class="col">
        <v-btn
          v-if="hasCreateUserRole"
          id="adv-create-user-button"
          class="bg-success"
          size="default"
          @click="goToCreateUser"
        >
          Register User
        </v-btn>
      </v-col>

      <v-col cols="6" class="col">
        <label for="adv-search-last-name">Last Name</label>
        <v-text-field
          id="adv-search-last-name"
          variant="outlined"
          density="compact"
          v-model.trim="lastNameInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col cols="6" class="col">
        <label for="adv-search-first-name">First Name</label>
        <v-text-field
          id="adv-search-first-name"
          variant="outlined"
          density="compact"
          v-model.trim="firstNameInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col cols="6" class="col">
        <label for="adv-search-username">Username</label>
        <v-text-field
          id="adv-search-username"
          variant="outlined"
          density="compact"
          v-model.trim="usernameInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col cols="6" class="col">
        <label for="adv-search-email">Email</label>
        <v-text-field
          id="adv-search-email"
          variant="outlined"
          density="compact"
          v-model.trim="emailInput"
          @keyup.enter="searchUser(advancedSearchParams)"
        />
      </v-col>
      <v-col cols="6" class="col">
        <label for="org-details">Organization</label>
        <v-autocomplete
          id="org-details"
          v-model="organizationInput"
          :items="organizations"
          item-value="organizationId"
          variant="outlined"
          density="compact"
          clearable
          placeholder="Select an Organization"
          @keyup.enter="searchUser(advancedSearchParams)"
        ></v-autocomplete>
      </v-col>
      <v-col cols="2" class="last-log-radio-group">
        <label for="last-log-date-radio">Last logged-in</label>
        <v-radio-group v-model="radios" density="compact" style="margin: 0px">
          <v-radio
            color="primary"
            id="last-log-date-radio"
            label="Before"
            value="Before"
          ></v-radio>
          <v-radio color="primary" label="After" value="After"></v-radio>
        </v-radio-group>
      </v-col>
      <v-col cols="4">
        <label for="last-log-date">Date</label>
        <v-date-input
          :disabled="!(radios == 'Before' || radios == 'After')"
          color="primary"
          density="compact"
          id="last-log-date"
          variant="outlined"
          v-model="lastLogDate"
          :max="maxDateInput"
          :min="minDateInput"
          min-width="auto"
          elevation="10"
        ></v-date-input>
      </v-col>
    </v-row>

    <v-card border class="subgroup" v-if="this.advancedSearchSelected">
      <h2>User Roles</h2>

      <v-row no-gutters>
        <v-col cols="5">
          <label for="select-client">Application</label>
          <v-autocomplete
            id="select-client"
            variant="outlined"
            density="compact"
            :items="clients"
            item-title="clientId"
            item-value="id"
            placeholder="Select an Application"
            v-model="selectedClientId"
            @update:model-value="loadUserClientRoles()"
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
          <v-col cols="12">
            <v-row no-gutters>
              <v-col cols="12">
                <label>Roles</label>
              </v-col>
              <v-col
                cols="6"
                v-for="col in numberOfClientRoleColumns"
                :key="col"
              >
                <span v-for="item in itemsInColumn" :key="item">
                  <v-checkbox
                    density="compact"
                    v-if="item * col <= clientRoles.length"
                    class="roles-checkbox"
                    color="primary"
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
                        location="right"
                        max-width="300px"
                      >
                        <template v-slot:activator="{ props }">
                          <span v-bind="props">
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
      <v-col cols="6" style="margin-bottom: 30px">
        <v-btn
          id="adv-search-button"
          class="bg-primary"
          size="default"
          @click="searchUser(advancedSearchParams)"
        >
          Search Users
        </v-btn>
        &nbsp;
        <v-btn
          id="clear-search-button"
          class="BC-Gov-SecondaryButton"
          size="default"
          @click="clearSearchCriteria"
        >
          Clear Search
        </v-btn>
      </v-col>
    </v-row>

    <v-row no-gutters>
      <v-col cols="12">
        <v-data-table
          density="compact"
          id="users-table"
          class="base-table select-table"
          :headers="headers"
          :items="searchResults"
          items-per-page="15"
          :loading="userSearchLoadingStatus"
          loading-text="Searching for users"
          :show-select="bulkRemovalAllowed"
          v-on:click:row="selectUser"
          v-model="usersSelectedForBulkRemoval"
          :item-value="(item) => item"
          :hide-default-footer="true"
        >
          <template v-slot:header.data-table-select>
            <template v-if="searchResults.length > 0">
              <v-checkbox
                :indeterminate="someUsersSelected"
                :model-value="allUsersSelected"
                @click="toggleAllUsers"
              ></v-checkbox>
            </template>
          </template>
          <!-- https://stackoverflow.com/questions/61394522/add-hyperlink-in-v-data-table-vuetify -->
          <!-- :footer-props="footerProps" -->
          <template #item.username="{ item }">
            <a
              target="_blank"
              :href="`#/users/${item.id}`"
              v-on:click="openNewTab"
            >
              {{ item.username }}
            </a>
            <v-icon size="small">mdi-open-in-new</v-icon>
          </template>
          <template v-if="searchResults.length > 0" v-slot:bottom>
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
                <v-btn id="csv-button" class="bg-primary" size="default">
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
                  <template v-slot:activator="{ props }">
                    <v-btn
                      id="remove-access-button"
                      class="bg-error"
                      size="default"
                      v-bind="props"
                      :disabled="
                        usersSelectedForBulkRemoval.length === 0 ||
                        !usersSelectedForBulkRemoval.every(
                          (user) => 'role' in user
                        )
                      "
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
                        <v-list-subheader class="list-subheader">
                          {{ getSelectedClientName(selectedClientId) }}
                          <v-spacer></v-spacer>
                          <v-progress-circular
                            v-if="bulkRemovalRequestInProgress"
                            color="primary"
                            indeterminate
                          ></v-progress-circular>
                        </v-list-subheader>
                        <v-divider></v-divider>
                        <v-list-item
                          v-for="(user, i) in usersSelectedForBulkRemoval"
                          :key="i"
                        >
                          <template v-slot:prepend>
                            <template v-if="bulkRemovalResponseIsPresent()">
                              <v-tooltip
                                :text="
                                  getBulkRemovalListItemDetails(user.id)
                                    .tooltipText
                                "
                                location="bottom"
                              >
                                <template v-slot:activator="{ props }">
                                  <v-icon
                                    size="large"
                                    v-bind="props"
                                    :color="
                                      getBulkRemovalListItemDetails(user.id)
                                        .iconColor
                                    "
                                    :icon="
                                      getBulkRemovalListItemDetails(user.id)
                                        .icon
                                    "
                                  ></v-icon>
                                </template>
                              </v-tooltip>
                            </template>
                            <template v-else>
                              <v-icon
                                size="large"
                                :color="
                                  getBulkRemovalListItemDetails(user.id)
                                    .iconColor
                                "
                                :icon="
                                  getBulkRemovalListItemDetails(user.id).icon
                                "
                              ></v-icon>
                            </template>
                          </template>

                          <v-list-item-title>
                            {{ user.username }}
                          </v-list-item-title>

                          <v-list-item-subtitle>
                            {{ user.role }}
                          </v-list-item-subtitle>
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
                          class="bg-primary"
                          variant="text"
                          @click="closeBulkRemovalDialog"
                        >
                          Cancel
                        </v-btn>
                        <v-btn
                          class="bg-error"
                          variant="text"
                          @click="bulkRemoveUserAccess"
                        >
                          Remove Access
                        </v-btn>
                      </template>
                      <template v-else>
                        <v-btn
                          class="bg-primary"
                          variant="text"
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
            <v-data-table-footer
              items-per-page="15"
              items-per-page-text=""
            ></v-data-table-footer>
          </template>
        </v-data-table>
      </v-col>
    </v-row>
  </div>
</template>

<script>
  import ClientsRepository from "@/api/ClientsRepository";
  import UsersRepository from "@/api/UsersRepository";
  import { VDateInput } from "vuetify/labs/VDateInput";

  const options = { dateStyle: "short" };
  const formatDate = new Intl.DateTimeFormat("en-CA", options).format;

  export default {
    components: {
      VDateInput,
    },
    name: "UserSearch",
    data() {
      return {
        organizations: this.$organizations.map((item) => {
          item.value = `{"id":"${item.organizationId}","name":"${item.name}"}`;
          item.title = `${item.organizationId} - ${item.name}`;
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
        lastLogDate: null,
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
        params = this.addQueryParameter(params, "username", this.usernameInput);
        params = this.addQueryParameter(params, "email", this.emailInput);
        params = this.addQueryParameter(params, "org", this.organizationInput);
        if (this.radios == "Before" && this.lastLogDate) {
          params = this.addQueryParameter(
            params,
            "lastLogBefore",
            formatDate(this.lastLogDate)
          );
        } else if (this.radios == "After" && this.lastLogDate) {
          params = this.addQueryParameter(
            params,
            "lastLogAfter",
            formatDate(this.lastLogDate)
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
          { title: "Username", value: "username", class: "table-header" },
          { title: "First name", value: "firstName", class: "table-header" },
          { title: "Last name", value: "lastName", class: "table-header" },
          { title: "Email", value: "email", class: "table-header" },
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
            title: "Last Log Date",
            value: "lastLogDate",
            class: "table-header",
          });
        }
        if (showRoles) {
          hdrs.push({ title: "Role", value: "role", class: "table-header" });
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
      someUsersSelected() {
        return (
          this.usersSelectedForBulkRemoval.length > 0 &&
          this.usersSelectedForBulkRemoval.length < this.searchResults.length
        );
      },
      allUsersSelected() {
        return (
          this.searchResults.length > 0 &&
          this.usersSelectedForBulkRemoval.length === this.searchResults.length
        );
      },
    },
    methods: {
      openNewTab: function () {
        this.newTab = true;
      },
      selectUser: function (click, row) {
        const user = row.item;
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
            //error.message is the basic message generated by axios. error.response.data are details retuned by UMS
            let errorDescription = error?.message || error;
            if (error?.response?.data) {
              errorDescription += `. ${error.response.data}`;
            }
            this.handleError("User search failed", errorDescription);
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
          parameters += "&" + parameter + "=" + encodeURIComponent(value);
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
              iconColor: "green-darken-2",
              tooltipText: "Access removed successfully",
            };
          } else {
            this.bulkRemovalListItemDetails[details.userId] = {
              icon: "mdi-alert-circle",
              iconColor: "red-darken-2",
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
            iconColor: "gray-darken-2",
          };
        }
      },
      toggleAllUsers() {
        if (this.allUsersSelected) {
          this.usersSelectedForBulkRemoval = [];
        } else {
          this.usersSelectedForBulkRemoval = [...this.searchResults];
        }
      },
    },
  };
</script>

<style scoped>
  .BC-Gov-SecondaryButton {
    background: none;
    border: 2px solid #003366;
    color: #003366;
  }
  .BC-Gov-SecondaryButton:hover {
    opacity: 0.8;
    background-color: #003366;
    color: #ffffff;
  }
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
  .v-toolbar {
    background: #ffffff;
  }
  .v-btn {
    text-transform: none;
    font-weight: 600;
  }
  .v-row {
    margin: 0;
  }
  .last-log-radio-group {
    margin-right: -12px;
  }
  .list-subheader {
    font-size: large;
    font-weight: 600;
    margin-bottom: 10px;
  }
</style>
