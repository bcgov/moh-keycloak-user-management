<template>
  <div id="user-info">
    <v-alert :value="!!successMessage" type="success" dismissible>{{ successMessage }}</v-alert>
    <v-alert :value="!!errorMessage" type="error" dismissible>{{ errorMessage }}</v-alert>

    <h1>Update - {{ user.username }}</h1>
    <v-row no-gutters>
      <v-col class="col-7">
        <label for="user-name" class="disabled required">User Name</label>
        <v-text-field dense outlined disabled id="user-name" v-model="user.username"
        required :rules="[v => !!v || 'Username is required']"/>

        <label for="first-name" class="required">First Name</label>
        <v-text-field dense outlined id="first-name" v-model="user.firstName" 
        required :rules="[v => !!v || 'First Name is required']"/>

        <label for="last-name" class="required">Last Name</label>
        <v-text-field dense outlined id="last-name" v-model="user.lastName"
        required :rules="[v => !!v || 'Last Name is required']"/>
      </v-col>
    </v-row>

    <v-card outlined class="subgroup">
      <h2>Permissions</h2>

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
            v-on:change="getUserClientRoles();"
          ></v-autocomplete>
        </v-col>
        <v-col class="col-7">
          <label v-show="selectedClientId">Roles</label>
          <div class="checkbox-group" v-if="selectedClientId">
            <v-checkbox
              hide-details="auto"
              v-for="role in effectiveClientRoles"
              v-model="selectedRoles"
              :value="role"
              :label="role.name"
              v-bind:key="role.name"
            ></v-checkbox>
            <v-checkbox
              hide-details="auto"
              v-for="role in availableClientRoles"
              v-model="selectedRoles"
              :value="role"
              :label="role.name"
              v-bind:key="role.name"
            ></v-checkbox>
          </div>
          <div class="my-6" v-if="selectedClientId">
            <v-btn class="secondary" medium v-on:click="updateUserClientRoles()">Save User Role</v-btn>
          </div>
        </v-col>
      </v-row>
    </v-card>
  </div>
</template>

<script>
import { RepositoryFactory } from "./../api/RepositoryFactory";
const ClientsRepository = RepositoryFactory.get("clients");
const UsersRepository = RepositoryFactory.get("users");

export default {
  name: "UserInfo",
  data() {
    return {
      successMessage: "",
      errorMessage: "",
      user: '',
      clients: [],
      selectedClientId: null,
      effectiveClientRoles: [],
      availableClientRoles: [],
      selectedRoles: []
    };
  },
  created() {
    this.getClients();
    this.getUser();
  },
  methods: {
    getUser: function() {
      UsersRepository.getUser(this.$route.params.userid)
        .then(response => {
          this.user = response.data;
        })
        .catch(e => {
          console.log(e);
        });
    },
    getClients: function() {
      ClientsRepository.get()
        .then(response => {
          this.clients = response.data;
        })
        .catch(e => {
          console.log(e);
        });
    },
    getUserClientRoles: function() {
      this.effectiveClientRoles = [];
      this.availableClientRoles = [];
      this.selectedRoles = [];
      this.getUserEffectiveClientRoles();
      this.getUserAvailableClientRoles();
    },

    getUserAvailableClientRoles: function() {
      UsersRepository.getUserAvailableClientRoles(
        this.user.id,
        this.selectedClientId
      )
        .then(response => {
          this.availableClientRoles.push(...response.data);
        })
        .catch(e => {
          console.log(e);
        });
    },

    getUserEffectiveClientRoles: function() {
      UsersRepository.getUserEffectiveClientRoles(
        this.user.id,
        this.selectedClientId
      )
        .then(response => {
          this.effectiveClientRoles.push(...response.data);
          this.selectedRoles.push(...response.data);
        })
        .catch(e => {
          console.log(e);
        });
    },

    updateUserClientRoles: function() {
      //If in effective but not selected DELETE
      var rolesToDelete = this.effectiveClientRoles.filter(
        value => !this.selectedRoles.includes(value)
      );

      //If in available and selected ADD
      var rolesToAdd = this.availableClientRoles.filter(value =>
        this.selectedRoles.includes(value)
      );

      this.successMessage = "";
      this.errorMessage = "";

      //TODO refactor this
      UsersRepository.deleteUserClientRoles(
        this.user.id,
        this.selectedClientId,
        rolesToDelete
      )
        .then(response => {
          console.log(response);

          return UsersRepository.addUserClientRoles(
            this.user.id,
            this.selectedClientId,
            rolesToAdd
          );
        })
        .then(response => {
          console.log(response);

          this.successMessage =
            this.successMessage + "Roles Updated Successfully ";

          this.getUserClientRoles();
        })
        .catch(error => {
          this.errorMessage = this.errorMessage + "Error Updating Roles";
          console.log(error);
        });
    }
  }
};
</script>