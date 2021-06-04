<template>
  <v-card outlined class="subgroup">
    <h2>User Roles</h2>

    <v-row no-gutters>
      <v-col class="col-7">
        <label for="select-client">Application</label>
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

    <div v-if="selectedClient">
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
        <v-col class="col-4" no-gutters>
          <v-row no-gutters>
            <v-col class="col-12">
              <label>
                Effective Roles
                <v-tooltip right>
                  <template v-slot:activator="{ on }">
                    <v-icon v-on="on" small>mdi-help-circle</v-icon>
                  </template>
                  <span>
                    Effective roles represent all roles assigned to a user for this client.
                    <br />This may include roles provided by group membership which cannot be directly removed.
                  </span>
                </v-tooltip>
              </label>
            </v-col>
            <v-col class="col-6">
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
                <span slot="label" class="tooltip">
                  {{role.name}}
                  <span v-show="role.description" class="tooltiptext"> {{ role.description }} </span>
                </span>
              </v-checkbox>
            </v-col>
          </v-row>
        </v-col>
      </v-row>
      <div class="my-6" v-if="selectedClient">
        <v-btn id="save-user-roles" class="primary" medium v-on:click="updateUserClientRoles()">Save User Roles</v-btn>
      </div>
      <span v-if="selectedClient.clientId.includes('SFDS')">
        <v-divider class="sub-permissions"></v-divider>

        <v-data-table
            :headers="sfdsTableHeaders"
            :items="sfds_authorizations">
          <template v-slot:top>
            <v-toolbar
                flat
            >
              <h2 class="sfds-authorizations-header">SFDS Authorizations</h2>
              <v-spacer></v-spacer>
              <v-dialog v-model="editDialog" max-width="840px">
                <template v-slot:activator="{ on, attrs }">
                  <v-btn color="primary" darkclass="mb-2" v-bind="attrs" v-on="on">
                    New Authorization
                  </v-btn>
                </template>
                <v-card>
                  <v-card-title>
                    <span class="headline">{{ dialogTitle }}</span>
                  </v-card-title>
                  <v-card-text>
                    <v-container>
                      <v-row class="right-gutters">
                        <v-col class="col-4">
                          <label>Mailbox</label>
                          <v-autocomplete
                              id="sfds-mailboxes"
                              v-model="currentSfdsAuthorization.mailbox"
                              outlined
                              dense
                              :items="sfdsMailboxes"
                          ></v-autocomplete>
                        </v-col>
                        <v-col class="col-4">
                          <label>Use</label>
                          <v-autocomplete
                              id="sfds-uses"
                              v-model="currentSfdsAuthorization.uses"
                              outlined
                              dense
                              multiple
                              chips
                              :items="sfdsUses"
                          ></v-autocomplete>
                        </v-col>
                        <v-col class="col-4">
                          <label>Permission</label>
                          <v-select
                              id="sfds-permissions"
                              v-model="currentSfdsAuthorization.permission"
                              outlined
                              dense
                              :items="sfdsPermissions"
                          ></v-select>
                        </v-col>
                      </v-row>
                    </v-container>
                  </v-card-text>
                  <v-card-actions>
                    <v-btn class="primary" @click="saveSfdsAuthorization">
                      Save
                    </v-btn>
                    <v-btn outlined class="primary--text" @click="close">
                      Cancel
                    </v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
              <v-dialog v-model="deleteDialog" max-width="650px" >
                <v-card >
                  <v-card-title class="headline">Are you sure you want to delete this authorization?</v-card-title>
                  <v-card-text class="black--text">
                    Use: {{ currentSfdsAuthorization.uses }} <br/>
                    Mailbox: {{ currentSfdsAuthorization.mailbox }} <br/>
                    Permission: {{ currentSfdsAuthorization.permission }}
                  </v-card-text>
                  <v-card-actions>
                    <v-btn class="red white--text" @click="deleteItemConfirm">Delete</v-btn>
                    <v-btn outlined class="primary--text" @click="closeDelete">Cancel</v-btn>
                  </v-card-actions>
                </v-card>
              </v-dialog>
            </v-toolbar>
          </template>
          <template v-slot:item.actions="{ item }">
            <v-icon
                small
                class="mr-2"
                @click="editItem(item)">
              mdi-pencil
            </v-icon>
            <v-icon
                small
                @click="deleteItem(item)">
              mdi-delete
            </v-icon>
          </template>
        </v-data-table>
      </span>
    </div>

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
      clients: [],
      selectedClient: null,
      clientRoles: [],
      effectiveClientRoles: [],
      selectedRoles: [],
      sfdsTableHeaders: [
        { text: 'Mailbox', value: 'mailbox' },
        { text: 'Uses', value: 'uses' },
        { text: 'Permission', value: 'permission' },
        { text: 'Actions', value: 'actions', sortable: false }
      ],
      sfds_authorizations: [],
      sfdsMailboxes: ['HSCIS', 'BCMA', 'PHC', 'HOOPC'],
      sfdsUses: ['bcma', 'hscis', 'phc', 'wda', 'hoopc', 'grp'],
      sfdsPermissions: ['get', 'send', 'get-send', 'get-delete', 'get-send-delete'],
      currentSfdsAuthorization: {},
      editDialog: false,
      deleteDialog: false,
      editedIndex: -1,
      defaultAuthorization: {
        uses: [],
        mailbox: '',
        permission: ''
      }
    };
  },
  async created() {
    //TODO error handling
    await this.getClients();
  },
  watch: {
    editDialog (val) {
      val || this.close()
    },
    deleteDialog (val) {
      val || this.closeDelete()
    },
  },
  computed: {
    dialogTitle () {
      return this.editedIndex === -1 ? 'New Authorization' : 'Edit Authorization'
    },
    itemsInColumn() {
      return Math.ceil(this.clientRoles.length / this.numberOfClientRoleColumns);
    },
    numberOfClientRoleColumns() {
      return (this.clientRoles.length > 10) ? 2 : 1
    }
  },
  methods: {
    editItem (item) {
      this.editedIndex = this.sfds_authorizations.indexOf(item)
      this.currentSfdsAuthorization = Object.assign({}, item)
      this.editDialog = true
    },
    deleteItem: function (item) {
      this.editedIndex = this.sfds_authorizations.indexOf(item)
      this.currentSfdsAuthorization = Object.assign({}, item)
      this.deleteDialog = true
    },
    deleteItemConfirm: function () {
      this.sfds_authorizations.splice(this.editedIndex, 1)
      this.closeDelete()
    },
    saveSfdsAuthorization: function() {
      if (this.editedIndex > -1) {
        Object.assign(this.sfds_authorizations[this.editedIndex], this.currentSfdsAuthorization)
      } else {
        this.sfds_authorizations.push(this.currentSfdsAuthorization)
      }
      UsersRepository.updateUser(this.$route.params.userid, {'attributes': { 'sfds_auth_1' : JSON.stringify(this.sfds_authorizations) } })
      this.close()
    },
    close () {
      this.editDialog = false
      this.$nextTick(() => {
        this.currentSfdsAuthorization = Object.assign({}, this.defaultAuthorization)
        this.editedIndex = -1
      })
    },
    closeDelete: function () {
      this.deleteDialog = false
      this.$nextTick(() => {
        this.currentSfdsAuthorization = Object.assign({}, this.defaultAuthorization)
        this.editedIndex = -1
      })
    },
    roleArrayPosition: function(col, item) {
      return (col - 1) * (this.itemsInColumn) + item - 1;
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

    updateUserClientRoles: function() {
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

<style>
.sub-permissions {
  margin: 20px 0px 20px 0px;
}
.sfds-authorizations-header {
  margin: 22px 0px 22px 0px;
}
.right-gutters .col {
  padding: 10px 12px 10px 0px;
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


    