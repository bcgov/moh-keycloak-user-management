<template>
  <v-card outlined class="subgroup">
    <v-data-table
      :headers="headers"
      :items="ClientsWithEffectiveRoles"
      :loading="!rolesLoaded"
    >
      <template v-slot:top>
        <v-toolbar flat>
          <h2 style="margin-top: 20px">User Roles</h2>
          <v-spacer></v-spacer>
          <!-- adds new role -->
          <v-dialog content-class="updateRolesDialog" v-model="dialog">
            <!-- new item button -->
            <template v-slot:activator="{}">
              <v-btn v-if="hasRoleForManageUserRoles" color="primary" @click="addRoles()">
                Add Roles
              </v-btn>
            </template>

            <!-- pop up to add something -->
            <v-card class="popup">
              <!-- client selector -->
              <label style="padding-left: 12px;" for="select-client">Application</label>
              <v-icon style="float: right" @click="close()">mdi-close</v-icon>
              <v-row>
                <v-col class="col-7">
                  <v-autocomplete
                    id="select-client"
                    outlined
                    dense
                    :items="isEdit ? [selectedClient] : clientWithoutRoles"
                    item-text="clientId"
                    return-object
                    placeholder="Select an Application"
                    v-model="selectedClient"
                    @change="getUserClientRoles()"
                    :disabled="isEdit"
                  ></v-autocomplete>
                </v-col>
              </v-row>
              <v-skeleton-loader
                ref="roleSkeleton"
                v-show="
                  selectedClient && isClientRoleLoading
                "
                type="article, button"
              ></v-skeleton-loader>

              <!-- role list -->
              <div v-if="selectedClient">
                <div
                  width="inherit"
                  id="select-user-roles"
                  v-show="clientRoles && !isClientRoleLoading"
                >
                  <v-row>
                    <!-- shows all possible roles -->
                    <v-col class="col-8">
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
                    <v-col class="col-4" no-gutters>
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
                      <v-row style="flex-direction: column">
                        <v-col>
                          <v-checkbox
                            class="roles-checkbox"
                            style="word-break:break-all"
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
                              <span
                                v-show="role.description"
                                class="tooltiptext"
                              >
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

      <template #item.roleArray="{ item }">
        <span style="max-width:600px;display: flex;flex-wrap: wrap">
          <span v-for="(val, index) of item.roleArray" v-bind:key="val.name">
            {{val}}<span v-if="index !== Object.keys(item.roleArray).length - 1" style="margin-right:5px">,
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
        <v-icon small @click="editRoles(item.clientRepresentation)">
          mdi-pencil
        </v-icon>
      </template>
    </v-data-table>
  </v-card>
</template>

<script>
import ClientsRepository from "@/api/ClientsRepository";
import UsersRepository from "@/api/UsersRepository";

const LAST_LOGIN_NOT_RECORDED = -1;
export default {
  name: "UserUpdateRoles",
  props: ["userId"],
  data() {
    return {
      dialog: false,
      headers: [
        { text: "Application", value: "clientRepresentation.clientId" },
        { text: "Role", value: "roleArray" },
        { text: "Last Log In", value: "lastLogin"},
      ],
      clientWithoutRoles: [],
      selectedClient: null,
      clientRoles: [],
      effectiveClientRoles: [],
      selectedRoles: [],
      ClientsWithEffectiveRoles: [],
      rolesLoaded: false,
      isEdit: false,
      isClientRoleLoading:false,
      LAST_LOGIN_NOT_RECORDED
    };
  },
  async created() {
   this.loadUserRoles();
   if(this.hasRoleForManageUserRoles){
    this.headers.push({ text: "Actions", value: "actions", sortable: false})
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
  },
  methods: {
    addRoles: function() {
      this.selectedClient = null;
      this.isEdit = false;
      this.dialog = true;
    },
    editRoles: function (client) {
      this.selectedClient = client;
      this.isEdit = true;
      this.dialog = true;
      this.getUserClientRoles();
    },
    close: function() {
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
        lastLoginMap = lastLogins.data;
      });

      ClientsRepository.get()
        .then((allClients) => {
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
            .then((rolesArray) => {
              rolesArray.forEach((clientRoles) => {
                if (clientRoles.data.length > 0) {
                  let lastLoginStr = LAST_LOGIN_NOT_RECORDED;
                  if (lastLoginMap[clientRoles.clientRepresentation.name]) {
                  lastLoginStr =
                    lastLoginMap[clientRoles.clientRepresentation.name];
                }
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
                  clientsNoRolesAssigned.push(clientRoles.clientRepresentation);
                }
              });
            })
            .finally(() =>{
              this.ClientsWithEffectiveRoles = results
              this.clientWithoutRoles = clientsNoRolesAssigned
              this.rolesLoaded = true
              });
        })
        
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
          this.$root.$refs.UserMailboxAuthorizations.getMailboxClients();
        })
        .catch((error) => {
          this.$store.commit("alert/setAlert", {
            message: "Error updating roles: " + error,
            type: "error",
          });
        })
        .finally(() => {
          window.scrollTo(0, 0);})    
    },
  },
};
</script>

<style>
.updateRolesDialog {
    margin: 24px;
    /* overflow-y: auto;
    overflow-x: hidden; */
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
</style>


    
