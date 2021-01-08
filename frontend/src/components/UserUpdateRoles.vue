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
          item-value="id"
          placeholder="Select an Application"
          v-model="selectedClientId"
          @change="getUserClientRoles()"
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
                  <span slot="label" class="tooltip" :id="roleArrayPosition(col,item)">
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
      <div class="my-6" v-if="selectedClientId">
        <v-btn id="save-user-roles" class="secondary" medium v-on:click="updateUserClientRoles()">Save User Roles</v-btn>
      </div>
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
      selectedClientId: null,
      clientRoles: [],
      effectiveClientRoles: [],
      selectedRoles: []
    };
  },
  async created() {
    //TODO error handling
    await this.getClients();

  },
  computed: {
    itemsInColumn() {
      return Math.ceil(this.clientRoles.length / this.numberOfClientRoleColumns);
    },
    numberOfClientRoleColumns() {
      return (this.clientRoles.length > 10) ? 2 : 1
    }
  },
  methods: {
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
        this.selectedClientId
      ).catch(e => {
        console.log(e);
      });
    },

    getUserAvailableClientRoles: function() {
      return UsersRepository.getUserAvailableClientRoles(
        this.userId,
        this.selectedClientId
      ).catch(e => {
        console.log(e);
      });
    },

    getUserEffectiveClientRoles: function() {
      return UsersRepository.getUserEffectiveClientRoles(
        this.userId,
        this.selectedClientId
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
          this.selectedClientId,
          rolesToDelete
        ),
        UsersRepository.addUserClientRoles(
          this.userId,
          this.selectedClientId,
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
.roles-checkbox {
  margin: 0px 0px 12px 0px;
  padding: 8px 0px 0px 0px;
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
  left: 0%;
  margin-left: -10px;
  border-width: 5px;
  border-style: solid;
  border-color: transparent #555 transparent transparent;
}

/* Show the tooltip text when you mouse over the tooltip container */
.v-label:hover .tooltip .tooltiptext {
  visibility: visible;
  opacity: 0.9;
}
</style>


    