<!--suppress XmlInvalidId -->
<template>
  <div id="user-info" v-if="dataReady">
    <v-alert v-model="successState" type="success" dismissible>{{ successMessage }}</v-alert>
    <v-alert v-model="errorState" type="error" dismissible>{{ errorMessage }}</v-alert>

    <h1>Update - {{ user.username }}</h1>
    <v-card outlined class="subgroup">
      <h2>User Details</h2>
      <v-row no-gutters>
        <v-col class="col-7">
          <v-form ref="form">
            <label for="user-name" class="disabled required">User Name</label>
            <v-text-field dense outlined disabled id="user-name" v-model="user.username"
                          required :rules="[v => !!v || 'Username is required']"/>

            <label for="first-name" class="required">First Name</label>
            <v-text-field dense outlined id="first-name" v-model="user.firstName"
                          required :rules="[v => !!v || 'First Name is required']"/>

            <label for="last-name" class="required">Last Name</label>
            <v-text-field dense outlined id="last-name" v-model="user.lastName"
                          required :rules="[v => !!v || 'Last Name is required']"/>

            <label for="email" class="required">Email Address</label>
            <v-text-field dense outlined id="email" v-model="user.email"
                          required :rules="emailRules" type="email"/>

            <label for="phone">Telephone Number</label>
            <v-text-field dense outlined id="phone" v-model="user.attributes.phone"/>

            <label for="org-details">Organization Name and Address</label>
            <v-textarea outlined dense id="org-details" v-model="user.attributes.orgdetails"/>

            <v-btn id="save-button" class="secondary" medium v-on:click.prevent="updateUser">Save</v-btn>
          </v-form>
        </v-col>
      </v-row>
    </v-card>

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
            v-on:change="getUserClientRoles()"
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
                v-bind:key="role.name"
              ></v-checkbox>
            </v-col>
            <v-col class="col-4">
              <label>Effective Roles</label>
              <v-checkbox
                hide-details="auto"
                v-for="role in effectiveClientRoles"
                v-model="effectiveClientRoles"
                disabled="disabled"
                :value="role"
                :label="role.name"
                v-bind:key="role.name"
              ></v-checkbox>
            </v-col>
          </v-row>
          <div class="my-6" v-if="selectedClientId">
            <v-btn class="secondary" medium v-on:click="updateUserClientRoles()">Save User Role</v-btn>
          </div>
        </div>    
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
      dataReady: false,
      successMessage: "",
      successState: false,
      errorMessage: "",
      errorState: false,
      user: { 'attributes': {}},
      clients: [],
      selectedClientId: null,
      clientRoles: [],
      effectiveClientRoles: [],
      selectedRoles: [],
      emailRules: [
        v => !!v || 'Email is required',
        v => /^\S+@\S+$/.test(v) || 'Email is not valid'
      ]
    };
  },
  async created() {
    await Promise.all([this.getClients(), this.getUser()]);
    this.dataReady = true;
  },

  methods: {
    updateUser: function () {
      if (!this.$refs.form.validate()) {
        this.errorState = true;
        this.successState = false;
        this.errorMessage = "Please correct errors before submitting.";
        window.scrollTo(0, 0);
        return;
      }
      UsersRepository.updateUser(this.$route.params.userid, this.user)
              .then(() => {
                this.successState = true;
                this.errorState = false;
                this.successMessage = 'User updated';
                window.scrollTo(0, 0);
              })
              .catch(reason => {
                this.errorMessage = reason;
              });
    },
    getUser: function() {
      return UsersRepository.getUser(this.$route.params.userid)
        .then(response => {
          this.user = response.data;
          if (this.user.attributes === undefined) {
            this.user.attributes = {};
          }        
        })
        .catch(e => {
          console.log(e);
        });
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

      let clientRolesResponses = await Promise.all([this.getUserEffectiveClientRoles(), this.getUserAvailableClientRoles(), this.getUserActiveClientRoles() ]);
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
        this.user.id,
        this.selectedClientId
      ).catch(e => {
        console.log(e);
      });
    },

    getUserAvailableClientRoles: function() {
      return UsersRepository.getUserAvailableClientRoles(
        this.user.id,
        this.selectedClientId
      ).catch(e => {
        console.log(e);
      });
    },

    getUserEffectiveClientRoles: function() {
      return UsersRepository.getUserEffectiveClientRoles(
        this.user.id,
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
          this.user.id,
          this.selectedClientId,
          rolesToDelete
        ),
        UsersRepository.addUserClientRoles(
          this.user.id,
          this.selectedClientId,
          rolesToAdd
        )
      ]).then(() => {
        this.getUserClientRoles();
        this.successState = true;
        this.errorState = false;
        this.successMessage =
            this.successMessage + "Roles Updated Successfully ";
        window.scrollTo(0, 0);    
      }).catch(error => {
          this.errorMessage = this.errorMessage + "Error Updating Roles";
          console.log(error);
      });
    }
  }
};
</script>