<template>
  <v-card border class="subgroup">
    <v-data-table
      density="compact"
      :headers="headers"
      :items="ClientsWithEffectiveRoles"
      :loading="!rolesLoaded"
      id="user-table"
    >
      <template v-slot:top>
        <v-toolbar flat>
          <h2 style="margin-top: 20px">User Roles</h2>
          <v-spacer></v-spacer>
          <!-- adds new role -->
          <v-dialog content-class="updateRolesDialog" v-model="dialog">
            <!-- new item button -->
            <template v-slot:activator="{}">
              <v-btn
                v-if="hasRoleForManageUserRoles"
                class="bg-primary"
                size="default"
                @click="addRoles()"
              >
                Add User Role
              </v-btn>
            </template>

            <!-- pop up to add something -->
            <v-card>
              <v-card-title>
                <span class="text-h5">{{ dialogTitle }}</span>
              </v-card-title>
              <v-card-text>
                <!-- client selector -->
                <label
                  style="padding-left: 12px"
                  class="required"
                  for="select-client"
                >
                  Application
                </label>
                <v-icon style="float: right" @click="close()">mdi-close</v-icon>
                <v-row>
                  <v-col cols="7">
                    <v-autocomplete
                      id="select-client"
                      variant="outlined"
                      density="compact"
                      :items="isEdit ? [selectedClient] : clientWithoutRoles"
                      item-title="clientId"
                      return-object
                      persistent-placeholder
                      placeholder="Select an Application"
                      v-model="selectedClient"
                      @update:model-value="getUserClientRoles()"
                      :disabled="isEdit"
                      min-width="270"
                    ></v-autocomplete>
                  </v-col>
                </v-row>
                <v-skeleton-loader
                  ref="roleSkeleton"
                  v-show="selectedClient && isClientRoleLoading"
                  type="article, button"
                ></v-skeleton-loader>

                <!-- role list -->
                <div v-if="selectedClient">
                  <div
                    width="inherit"
                    id="select-user-roles"
                    v-show="clientRoles && !isClientRoleLoading"
                  >
                    <v-row class="role-list">
                      <!-- shows all possible roles -->
                      <v-col cols="8">
                        <!-- header -->
                        <v-row>
                          <label>Roles</label>
                        </v-row>
                        <!-- body -->
                        <v-row>
                          <v-col
                            v-for="col in numberOfClientRoleColumns"
                            :key="col"
                          >
                            <span v-for="item in itemsInColumn" :key="item">
                              <v-checkbox
                                density="compact"
                                color="primary"
                                :disabled="!hasRoleForManageUserRoles"
                                v-if="item * col <= clientRoles.length"
                                class="roles-checkbox"
                                hide-details="auto"
                                v-model="selectedRoles"
                                :value="
                                  clientRoles[roleArrayPosition(col, item)]
                                "
                                :key="
                                  clientRoles[roleArrayPosition(col, item)].name
                                "
                              >
                                <!-- role tooltip -->
                                <template v-slot:label>
                                  <span
                                    class="tooltip"
                                    :id="'role-' + roleArrayPosition(col, item)"
                                  >
                                    {{
                                      clientRoles[roleArrayPosition(col, item)]
                                        .name
                                    }}
                                    <span
                                      v-show="
                                        clientRoles[
                                          roleArrayPosition(col, item)
                                        ].description
                                      "
                                      class="tooltiptext"
                                    >
                                      {{
                                        clientRoles[
                                          roleArrayPosition(col, item)
                                        ].description
                                      }}
                                    </span>
                                  </span>
                                </template>
                              </v-checkbox>
                            </span>
                          </v-col>
                        </v-row>
                      </v-col>
                      <!-- effective roles -->
                      <v-col cols="4" no-gutters>
                        <v-row>
                          <label>
                            Effective Roles
                            <!-- tooltip for effective roles -->
                            <v-tooltip location="right">
                              <template v-slot:activator="{ props }">
                                <v-icon
                                  v-bind="props"
                                  size="small"
                                  class="help-icon"
                                >
                                  mdi-help-circle
                                </v-icon>
                              </template>
                              <span>
                                Effective roles represent all roles assigned to
                                a user for this client.
                                <br />
                                This may include roles provided by group
                                membership which cannot be directly removed.
                              </span>
                            </v-tooltip>
                          </label>
                        </v-row>
                        <!-- effective roles -->
                        <v-row style="flex-direction: column">
                          <v-col>
                            <v-checkbox
                              density="compact"
                              class="roles-checkbox"
                              style="word-break: break-all"
                              hide-details="auto"
                              disabled
                              readonly
                              v-for="role in effectiveClientRoles"
                              v-model="effectiveClientRoles"
                              :value="role"
                              :key="role.name"
                            >
                              <!-- effective role tooltip -->
                              <template v-slot:label>
                                <span class="tooltip">
                                  {{ role.name }}
                                  <span
                                    v-show="role.description"
                                    class="tooltiptext"
                                  >
                                    {{ role.description }}
                                  </span>
                                </span>
                              </template>
                            </v-checkbox>
                          </v-col>
                        </v-row>
                      </v-col>
                    </v-row>
                  </div>
                </div>
              </v-card-text>
              <v-card-actions v-if="selectedClient">
                <v-btn
                  v-if="hasRoleForManageUserRoles"
                  id="save-user-roles"
                  class="bg-primary"
                  size="default"
                  v-on:click="updateUserClientRoles()"
                >
                  Save User Roles
                </v-btn>
                <v-btn variant="outlined" class="text-primary" @click="close()">
                  Cancel
                </v-btn>
              </v-card-actions>
              <div v-if="showUserPayee && !isClientRoleLoading">
                <v-divider />
                <user-payee
                  @close="close"
                  @save="updateUserPayee"
                  :initialPayee="payee"
                  :payeeLoaded="payeeLoaded"
                />
              </div>
            </v-card>
          </v-dialog>
        </v-toolbar>
      </template>

      <template #item.roleArray="{ item }">
        <span style="max-width: 600px; display: flex; flex-wrap: wrap">
          <span v-for="(val, index) of item.roleArray" v-bind:key="val.name">
            {{ val }}
            <span
              v-if="index !== Object.keys(item.roleArray).length - 1"
              style="margin-right: 5px"
            >
              ,
            </span>
          </span>
        </span>
      </template>
      <template #item.lastLogin="{ item }">
        <span v-if="item.lastLogin === LAST_LOGIN_NOT_RECORDED">N/A</span>
        <span v-else>
          {{ new Date(item.lastLogin).toLocaleDateString("en-CA") }}
        </span>
      </template>

      <template #item.actions="{ item }">
        <v-icon size="small" @click="editRoles(item.clientRepresentation)"  
                :disabled = "!hasRoleToEditApplicationRoles(item.clientRepresentation.clientId)">
          mdi-pencil
        </v-icon>
      </template>
    </v-data-table>
  </v-card>
</template>

<script>
  import ClientsRepository from "@/api/ClientsRepository";
import UsersRepository from "@/api/UsersRepository";
import UserPayee from "@/components/UserPayee.vue";

  const LAST_LOGIN_NOT_RECORDED = -1;
  export default {
    name: "UserUpdateRoles",
    components: { UserPayee },
    props: ["userId"],
    data() {
      return {
        dialog: false,
        dialogTitle: "",
        headers: [
          { title: "Application", value: "clientRepresentation.clientId" },
          { title: "Role", value: "roleArray" },
          { title: "Last Log In", value: "lastLogin" },
        ],
        clientWithoutRoles: [],
        selectedClient: null,
        clientRoles: [],
        effectiveClientRoles: [],
        selectedRoles: [],
        ClientsWithEffectiveRoles: [],
        rolesLoaded: false,
        isEdit: false,
        isClientRoleLoading: false,
        LAST_LOGIN_NOT_RECORDED,
        payee: "",
        payeeLoaded: false,
      };
    },
    async created() {
      this.loadUserRoles();
      if (this.hasRoleForManageUserRoles) {
        this.headers.push({
          title: "Actions",
          value: "actions",
          sortable: false,
        });
      }
    },
    computed: {
      itemsInColumn() {
        return Math.ceil(
          this.clientRoles.length / this.numberOfClientRoleColumns
        );
      },
      numberOfClientRoleColumns() {
        return this.clientRoles.length > 10 ? 2 : 1;
      },
      hasRoleForManageUserRoles: function () {
        const umsClientId = "USER-MANAGEMENT-SERVICE";
        const manageUserRolesName = "manage-user-roles";

        return !!this.$keycloak.tokenParsed.resource_access[
          umsClientId
        ].roles.includes(manageUserRolesName);
      },
      showUserPayee() {
        return this.selectedClient?.clientId === this.$config.mspdirect_client;
      },
    },
    methods: {
      
      hasRoleToEditApplicationRoles: function (clientId) {
        const umsClientId = "USER-MANAGEMENT-SERVICE";
        const editRolePrefix = "view-client-";
        return this.$keycloak.tokenParsed.resource_access[
          umsClientId
        ].roles.includes(editRolePrefix+clientId.toLowerCase());
      },
      addRoles: function () {
        this.dialogTitle = "Add User Role";
        this.selectedClient = null;
        this.isEdit = false;
        this.dialog = true;
      },
      editRoles: function (client) {
        this.dialogTitle = "Edit User Role";
        this.selectedClient = client;
        this.isEdit = true;
        this.dialog = true;
        this.getUserClientRoles();
      },
      close: function () {
        this.dialog = false;
      },
      roleArrayPosition: function (col, item) {
        return (col - 1) * this.itemsInColumn + item - 1;
      },
      loadUserRoles: function () {
        this.rolesLoaded = false;

        let results = [];
        let lastLoginMap = [];
        let clientsNoRolesAssigned = [];

        UsersRepository.getUserLogins(this.userId).then((lastLogins) => {
          lastLoginMap = UsersRepository.mapLastLoginsClientAliases(
            lastLogins.data
          );
        });

        ClientsRepository.get().then((allClients) => {
          const allClientsWithAliases = ClientsRepository.assignClientAliases(
            allClients.data
          );
          Promise.all(
            allClientsWithAliases.map((client) => {
              return UsersRepository.getUserEffectiveClientRoles(
                this.userId,
                client.id
              ).then((clientRoles) => {
                clientRoles["clientRepresentation"] = client;
                return clientRoles;
              });
            })
          )
            .then((rolesArray) => {
              rolesArray.forEach((clientRoles) => {
                
                if (clientRoles.data.length > 0) {
                  let lastLoginStr =
                    lastLoginMap[clientRoles.clientRepresentation.clientId] ||
                    LAST_LOGIN_NOT_RECORDED;

                  clientRoles.roleArray = [];
                  clientRoles.data.forEach((role) => {
                    clientRoles.roleArray.push(role.name);
                  });

                  results.push({
                    clientRepresentation: clientRoles.clientRepresentation,
                    roleArray: clientRoles.roleArray,
                    lastLogin: lastLoginStr,
                  });
                } else {
                  if(this.hasRoleToEditApplicationRoles(clientRoles.clientRepresentation.clientId)){
                      clientsNoRolesAssigned.push(clientRoles.clientRepresentation);
                    }
                }
              });
            })
            .finally(() => {
              this.ClientsWithEffectiveRoles = results;
              this.clientWithoutRoles = clientsNoRolesAssigned;
              this.rolesLoaded = true;
            });
        });
      },
      getUserClientRoles: async function () {
        this.effectiveClientRoles = [];
        this.clientRoles = [];
        this.selectedRoles = [];
        this.isClientRoleLoading = true;

        let clientRolesResponses = await Promise.all([
          this.getUserEffectiveClientRoles(),
          this.getUserAvailableClientRoles(),
          this.getUserActiveClientRoles(),
        ]);
        // TODO roles that are in effective but not active should not be included in clientRoles
        this.clientRoles.push(...clientRolesResponses[1].data);
        this.clientRoles.push(...clientRolesResponses[2].data);
        this.selectedRoles.push(...clientRolesResponses[2].data);
        this.effectiveClientRoles.push(...clientRolesResponses[0].data);

        if (this.showUserPayee) {
          await this.loadUserPayee();
        }

        this.isClientRoleLoading = false;

        this.clientRoles.sort((a, b) => {
          return a.name.localeCompare(b.name);
        });
      },
      getUserActiveClientRoles: function () {
        return UsersRepository.getUserActiveClientRoles(
          this.userId,
          this.selectedClient.id
        ).catch((e) => {
          console.log(e);
        });
      },
      getUserAvailableClientRoles: function () {
        return UsersRepository.getUserAvailableClientRoles(
          this.userId,
          this.selectedClient.id
        ).catch((e) => {
          console.log(e);
        });
      },
      getUserEffectiveClientRoles: function () {
        return UsersRepository.getUserEffectiveClientRoles(
          this.userId,
          this.selectedClient.id
        ).catch((e) => {
          console.log(e);
        });
      },
      updateUserClientRoles: function () {
        //If in roles but not selected DELETE
        let rolesToDelete = this.clientRoles.filter(
          (value) => !this.selectedRoles.includes(value)
        );
        //If in roles and selected ADD
        let rolesToAdd = this.clientRoles.filter((value) =>
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
          ),
        ])
          .then(() => {
            this.getUserClientRoles();
            this.$store.commit("alert/setAlert", {
              message: "Roles updated successfully",
              type: "success",
            });
            //Update list of roles from UserDetails module
            this.close();
            this.loadUserRoles();
          })
          .catch((error) => {
            let errorDescription = error?.message || error;
            if (error?.response?.data) {
              errorDescription += `. ${error.response.data}`;
            }
            console.log(errorDescription)
            this.$store.commit("alert/setAlert", {
              message: "Error updating roles: " + error,
              type: "error",
            });
            this.close();
          })
          .finally(() => {
            this.$parent.getMailboxClients();
            window.scrollTo(0, 0);
          });
      },
      loadUserPayee: async function () {
        this.payeeLoaded = false;
        try {
          const response = await UsersRepository.getUserPayee(this.userId);
          this.payee = response.data.payeeNumber;
          this.payeeLoaded = true;
        } catch (err) {
          // Swallow the error and display an error in the Payee section
        }
      },
      updateUserPayee: function (payee) {
        UsersRepository.updateUserPayee(this.userId, payee)
          .then(() => {
            this.$store.commit("alert/setAlert", {
              message: "Payee updated successfully",
              type: "success",
            });
            this.close();
          })
          .catch((error) => {
            this.$store.commit("alert/setAlert", {
              message: "Error updating payee: " + error,
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

<style>
  .v-dialog > .v-overlay__content.updateRolesDialog {
    margin: 24px;
    overflow-y: auto;
    overflow-x: hidden;
    max-width: 95%;
    min-width: 450px;
    width: 900px;
    z-index: inherit;
  }
</style>
<style scoped>
  .row {
    margin: 0px;
  }
  .v-input {
    max-width: fit-content;
  }
  .popup {
    padding: 30px;
  }

  .roles-checkbox {
    margin: 0;
    padding: 0;
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
    word-break: break-all;
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
  .v-toolbar {
    background: #ffffff;
  }
  #user-table {
    border-top: none;
  }
  .role-list {
    margin: 0;
    padding-left: 15px;
  }
  .v-btn {
    text-transform: none;
    font-weight: 600;
  }
</style>
