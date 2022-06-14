<template>
  <v-card outlined class="subgroup">
    <v-data-table
      :headers="headers"
      :items="AllEffectiveClientRoles"
      :loading="rolesLoaded"
    >
      <template v-slot:top>
        <v-toolbar flat>
          <h2 style="margin-top: 20px">User Roles</h2>
          <v-spacer></v-spacer>
          <!-- adds new role -->
          <v-dialog v-model="dialog" max-width="800px">
            <!-- new item button -->
            <template v-slot:activator="{ on }">
              <v-btn v-if="hasRoleForManageUserRoles" color="primary" v-on="on">
                Add Role
              </v-btn>
            </template>

            <!-- pop up to add something -->
            <v-card class="popup">
              <!-- pop up header -->
              <v-row class="header">
                <label for="select-client">Application</label>
                <v-icon @click="dialog = false">mdi-close</v-icon>
              </v-row>
              <!-- pop up content: -->
              <!-- client selector -->
              <v-row>
                <v-col class="col-7">
                  <v-autocomplete
                    id="select-client"
                    outlined
                    dense
                    :items="clients"
                    item-text="clientId"
                    return-object
                    placeholder="Select an Application"
                    v-model="selectedClient"
                    @change="getUserClientRoles()"
                  ></v-autocomplete>
                </v-col>
              </v-row>

              <!-- role list -->
              <div v-if="selectedClient">
                <div
                  width="inherit"
                  id="select-user-roles"
                  v-show="clientRoles && clientRoles.length > 0"
                >
                  <v-row>
                    <!-- shows all possible roles -->
                    <v-col class="col-8 fit-content">
                      <!-- header -->
                      <v-row>
                        <label>Roles</label>
                      </v-row>
                      <!-- body -->
                      <v-row>
                        <v-col
                          class="fit-content"
                          v-for="col in numberOfClientRoleColumns"
                          :key="col"
                        >
                          <span v-for="item in itemsInColumn" :key="item">
                            <v-checkbox
                              :disabled="!hasRoleForManageUserRoles"
                              v-if="item * col <= clientRoles.length"
                              class="roles-checkbox"
                              hide-details="auto"
                              v-model="selectedRoles"
                              :value="clientRoles[roleArrayPosition(col, item)]"
                              :key="
                                clientRoles[roleArrayPosition(col, item)].name
                              "
                            >
                              <!-- role tooltip -->
                              <span
                                slot="label"
                                class="tooltip"
                                :id="'role-' + roleArrayPosition(col, item)"
                              >
                                {{
                                  clientRoles[roleArrayPosition(col, item)].name
                                }}
                                <span
                                  v-show="
                                    clientRoles[roleArrayPosition(col, item)]
                                      .description
                                  "
                                  class="tooltiptext"
                                >
                                  {{
                                    clientRoles[roleArrayPosition(col, item)]
                                      .description
                                  }}
                                </span>
                              </span>
                            </v-checkbox>
                          </span>
                        </v-col>
                      </v-row>
                    </v-col>
                    <!-- effective roles -->
                    <v-col class="col-4 fit-content" no-gutters>
                      <v-row>
                        <label>
                          Effective Roles
                          <!-- tooltip for effective roles -->
                          <v-tooltip right>
                            <template v-slot:activator="{ on }">
                              <v-icon v-on="on" small>mdi-help-circle</v-icon>
                            </template>
                            <span>
                              Effective roles represent all roles assigned to a
                              user for this client.
                              <br />This may include roles provided by group
                              membership which cannot be directly removed.
                            </span>
                          </v-tooltip>
                        </label>
                      </v-row>
                      <!-- effective roles -->
                      <v-row style="flex-direction: column" class="fit-content">
                      <v-col>
                        <v-checkbox
                          class="roles-checkbox"
                          hide-details="auto"
                          disabled
                          readonly
                          v-for="role in effectiveClientRoles"
                          v-model="effectiveClientRoles"
                          :value="role"
                          :key="role.name"
                        >
                          <!-- effective role tooltip -->
                          <span slot="label" class="tooltip">
                            {{ role.name }}
                            <span v-show="role.description" class="tooltiptext">
                              {{ role.description }}
                            </span>
                          </span>
                        </v-checkbox>
                        </v-col>
                      </v-row>
                    </v-col>
                  </v-row>

                  <!-- button to save new role assignments -->
                  <div v-if="selectedClient">
                    <v-btn
                      v-if="hasRoleForManageUserRoles"
                      id="save-user-roles"
                      class="primary"
                      medium
                      v-on:click="updateUserClientRoles()"
                      >Save User Roles</v-btn
                    >
                  </div>
                </div>
              </div>
            </v-card>
          </v-dialog>
        </v-toolbar>
      </template>

      <!-- template for each role row. delete button -->
      <template #item.actions="{ item }">
        <v-icon :disabled=!isSelectedRole(item) small @click="deleteItem(item)"> mdi-delete </v-icon>
      </template>
    </v-data-table>
  </v-card>
</template>

<script>
import ClientsRepository from "@/api/ClientsRepository";
import UsersRepository from "@/api/UsersRepository";

export default {
  name: "UserUpdateRoles",
  props: ['userId'],
  data() {
    return {
      dialog: false,
      headers: [
        { text: "Application", value: "clientRepresentation.name" },
        { text: "Role", value: "roleRepresentation.name" },
        { text: "Last Log In", value: "lastLogin" },
        { text: "Actions", value: "actions", sortable: false },
      ],
      clients: [],
      selectedClient: null,
      clientRoles: [],
      effectiveClientRoles: [],
      selectedRoles: [],
      AllEffectiveClientRoles: [],
      AllSelectedClientRoles: [],
      allRoles: [],
      rolesLoaded: true,
    };
  },
  async created() {
    //TODO error handling
    await this.getClients();
    await this.loadUserRoles();
  },
  computed: {
    itemsInColumn() {
      return Math.ceil(this.clientRoles.length / this.numberOfClientRoleColumns);
    },
    numberOfClientRoleColumns() {
      return (this.clientRoles.length > 10) ? 2 : 1
    },
    hasRoleForManageUserRoles: function() {
      const umsClientId = "USER-MANAGEMENT-SERVICE";
      const manageUserRolesName = "manage-user-roles";

      return !!this.$keycloak.tokenParsed.resource_access[umsClientId].roles.includes(manageUserRolesName)
    }
  },
  methods: {
    isSelectedRole: function(role) {
      return this.AllSelectedClientRoles.includes(role.roleRepresentation.id);
    },
    close() {
      this.dialog = false;
    },
    loadUserRoles: async function () {
      let results = [];
      let resultsActive = [];
      this.rolesLoaded = true;
      let lastLoginMap = [];
      UsersRepository.getUserLogins(this.userId).then((lastLogins) => {
        lastLoginMap = lastLogins.data;
      });

      ClientsRepository.get().then((allClients) => {
        Promise.all(
          allClients.data.map((client) => {
            return UsersRepository.getUserEffectiveClientRoles(
              this.userId,
              client.id
            ).then((clientRoles) => {
              clientRoles["clientRepresentation"] = client;
              return clientRoles;
            });
          })
        )
          .then(function (rolesArray) {
            rolesArray.forEach((clientRoles) => {
              if (clientRoles.data.length > 0) {
                //todo: since this is a string, it can't be sorted via date?
                let lastLoginStr = "N/A";
                if (lastLoginMap[clientRoles.clientName]) {
                  lastLoginStr = new Date(
                    lastLoginMap[clientRoles.clientName]
                  ).toLocaleDateString("en-CA");
                }
                clientRoles.data.forEach((role) => {
                  results.push({
                    clientRepresentation: clientRoles.clientRepresentation,
                    roleRepresentation: role,
                    lastLogin: lastLoginStr,
                  });
                });
              }
            });
            })
            .then((this.AllEffectiveClientRoles = results));

          Promise.all(
            allClients.data.map((client) => {
              return UsersRepository.getUserActiveClientRoles(
                this.userId,
                client.id
              );
            })
          )
            .then(function (rolesArray) {
              rolesArray.forEach((clientRoles) => {
                  clientRoles.data.forEach((role) => {
                    resultsActive.push(role.id);
                  });
              });
            })
            .then((this.AllSelectedClientRoles = resultsActive));
        })
        .then((this.rolesLoaded = false));
    },
    getClients: function() {
      return ClientsRepository.get()
        .then(response => {
          this.clients = response.data;
        })
        .catch(e => {
          console.log(e);
        });
    },
    getUserClientRoles: async function() {
      this.effectiveClientRoles = [];
      this.clientRoles = [];
      this.selectedRoles = [];

      let clientRolesResponses = await Promise.all([
        this.getUserEffectiveClientRoles(),
        this.getUserAvailableClientRoles(),
        this.getUserActiveClientRoles()
      ]);
      // TODO roles that are in effective but not active should not be included in clientRoles
      this.clientRoles.push(...clientRolesResponses[1].data);
      this.clientRoles.push(...clientRolesResponses[2].data);
      this.selectedRoles.push(...clientRolesResponses[2].data);
      this.effectiveClientRoles.push(...clientRolesResponses[0].data);

      this.clientRoles.sort(function(a, b) {
        return a.name.localeCompare(b.name);
      });
    },

    getUserActiveClientRoles: function() {
      return UsersRepository.getUserActiveClientRoles(
        this.userId,
        this.selectedClient.id
      ).catch(e => {
        console.log(e);
      });
    },

    getUserAvailableClientRoles: function() {
      return UsersRepository.getUserAvailableClientRoles(
        this.userId,
        this.selectedClient.id
      ).catch(e => {
        console.log(e);
      });
    },

    getUserEffectiveClientRoles: function() {
      return UsersRepository.getUserEffectiveClientRoles(
        this.userId,
        this.selectedClient.id
      ).catch(e => {
        console.log(e);
      });
    },
    deleteItem: function (item) {
      let rolesToDelete = this.clientRoles.filter(
        (value) => !this.selectedRoles.includes(value)
      );
      rolesToDelete.push(item.roleRepresentation);
      Promise.all([
        UsersRepository.deleteUserClientRoles(
          this.userId,
          item.clientRepresentation.id,
          rolesToDelete
        ),
      ])
        .then(() => {
          this.getUserClientRoles();
          this.$store.commit("alert/setAlert", {
            message: "Role deleted successfully",
            type: "success",
          });
          this.loadUserRoles();
          this.$root.$refs.UserMailboxAuthorizations.getAllowedClients();
        })
        .catch((error) => {
          this.$store.commit("alert/setAlert", {
            message: "Error updating roles: " + error,
            type: "error",
          });
          window.scrollTo(0, 0);
        });
    },
    updateUserClientRoles: function () {
      //If in roles but not selected DELETE
      let rolesToDelete = this.clientRoles.filter(
        value => !this.selectedRoles.includes(value)
      );
      //If in roles and selected ADD
      let rolesToAdd = this.clientRoles.filter(value =>
        this.selectedRoles.includes(value)
      );

      Promise.all([
        UsersRepository.deleteUserClientRoles(
          this.userId,
          this.selectedClient.id,
          rolesToDelete
        ),
        UsersRepository.addUserClientRoles(
          this.userId,
          this.selectedClient.id,
          rolesToAdd
        )
      ])
        .then(() => {
          this.getUserClientRoles();
          this.$store.commit("alert/setAlert", {
            message: "Roles updated successfully",
            type: "success"
          });
          window.scrollTo(0, 0);
          //Update list of roles from UserDetails module
          this.close();
          this.loadUserRoles();
          this.$root.$refs.UserMailboxAuthorizations.getAllowedClients();
        })
        .catch((error) => {
          this.$store.commit("alert/setAlert", {
            message: "Error updating roles: " + error,
            type: "error"
          });
          window.scrollTo(0, 0);
        });
    }
  }
};
</script>

<style scoped>
.row {
  margin: 0px;
}
.fit-content,
.v-input {
  max-width: fit-content;
}
.popup {
  padding: 30px;
}

.popup .header {
  justify-content: space-between;
  padding: 12px;
}
.roles-checkbox {
  margin: 0 0 12px 0;
  padding: 8px 0 0 0;
}
/* Tooltip text */
.tooltip .tooltiptext {
  visibility: hidden;
  background-color: #555;
  color: #fff;
  text-align: center;
  padding: 5px 5px;
  border-radius: 6px;
  font-weight: 400;
  font-size: 14px;

  /* Position the tooltip text */
  position: absolute;
  z-index: 1;
  top: -25%;
  margin-left: 8px;
  width: 200px;

  /* Fade in tooltip */
  opacity: 0;
  transition: opacity 0.3s;
}

/* Tooltip arrow */
.tooltip .tooltiptext::after {
  content: "";
  position: absolute;
  top: 10px;
  left: 0;
  margin-left: -10px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent #555 transparent transparent;
}

/* Show the tooltip text when you mouse over the tooltip container */
.v-label:hover .tooltip .tooltiptext {
  visibility: visible;
  opacity: 1;
}
</style>


    
