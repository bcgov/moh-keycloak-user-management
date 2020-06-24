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
          <label>Roles</label>
          <v-checkbox
            hide-details="auto"
            v-for="role in clientRoles"
            v-model="selectedRoles"
            :value="role"
            :label="role.name"
            :key="role.name"
          ></v-checkbox>
        </v-col>
        <v-col class="col-4">
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
          <v-checkbox
            hide-details="auto"
            v-for="role in effectiveClientRoles"
            v-model="effectiveClientRoles"
            disabled="disabled"
            :value="role"
            :label="role.name"
            :key="role.name"
          ></v-checkbox>
        </v-col>
      </v-row>
      <div class="my-6" v-if="selectedClientId">
        <v-btn class="secondary" medium v-on:click="updateUserClientRoles()">Save User Roles</v-btn>
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
  methods: {
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
      var rolesToDelete = this.clientRoles.filter(
        value => !this.selectedRoles.includes(value)
      );
      //If in roles and selected ADD
      var rolesToAdd = this.clientRoles.filter(value =>
        this.selectedRoles.includes(value)
      );

      this.successMessage = "";
      this.errorMessage = "";

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


    