<!--suppress XmlInvalidId -->
<template>
  <div id="user">
    <v-card outlined class="subgroup">
      <h2>User Details</h2>
      <v-row no-gutters>
        <v-col class="col-7">
          <v-form ref="form">
            <label for="user-name" :disabled="!!userId" class="required">Username</label>
            <v-tooltip right v-if="!userId">
              <template v-slot:activator="{ on }">
                <v-icon id="user-name-tooltip-icon" v-on="on" small>mdi-help-circle</v-icon>
              </template>
              <span>Username should include the corresponding prefix or suffix in alignment with the id type.
                <ul>
                  <li>IDIR: username<strong>@idir</strong></li>
                  <li>Business BCeID: username<strong>@business_bceid</strong></li>
                  <li>Fraser Health: <strong>sfhr\</strong>username</li>
                  <li>Interior Health: <strong>iha\</strong>username</li>
                  <li>Northern Health: <strong>nirhb\</strong>username</li>
                  <li>Provincial Health: <strong>phsabc\</strong>username</li>
                  <li>Vancouver Coastal Health: <strong>vch\</strong>username or <strong>vrhb\</strong>username</li>
                  <li>Vancouver Island Health: <strong>viha\</strong>username</li>
                </ul>
              </span>
            </v-tooltip>
            <v-text-field
              dense
              :disabled="!!userId"
              outlined
              id="user-name"
              v-model="user.username"
              required
              :rules="[v => !!v || 'Username is required']"
            />

            <label for="first-name" class="required">First Name</label>
            <v-text-field
              dense
              outlined
              id="first-name"
              v-model="user.firstName"
              required
              :rules="[v => !!v || 'First Name is required']"
            />

            <label for="last-name" class="required">Last Name</label>
            <v-text-field
              dense
              outlined
              id="last-name"
              v-model="user.lastName"
              required
              :rules="[v => !!v || 'Last Name is required']"
            />

            <label for="email" class="required">Email Address</label>
            <v-text-field
              dense
              outlined
              id="email"
              v-model="user.email"
              required
              :rules="emailRules"
              type="email"
            />
            <label for="phone">Telephone Number</label>
            <v-text-field dense outlined id="phone" v-model="user.attributes.phone" />

            <label for="org-details">Organization</label>
            <v-autocomplete
                id="org-details"
                v-model="user.attributes.org_details"
                :items="$options.organizations"
                dense
                outlined
            ></v-autocomplete>

            <label for="notes">Notes</label>
            <v-textarea outlined dense id="notes" v-model="user.attributes.access_team_notes" maxlength="255" />

          </v-form>
        </v-col>
        <v-col class="col-4" style="margin-left: 30px; padding-left: 20px; border-left: 1px solid #efefef">
          <label for="linked-idps">Linked Identity Types</label>
          <ul id="linked-idps" style="margin-top: 5px; list-style: square">
            <li v-for="identity in user.federatedIdentities" :key="identity.id">
              {{ identity.identityProvider | formatIdentityProvider }} [{{ identity.userName }}]
            </li>
          </ul>
        </v-col>
      </v-row>
      <v-btn id="submit-button" class="primary" medium @click="updateUser">Update User</v-btn>
    </v-card>
  </div>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";
import organizations from "@/assets/organizations"

export default {
  name: "UserDetails",
  props: ['userId'],
  organizations: organizations.map((item) => {
    item.value = JSON.stringify(item);
    item.text = `${item.id} - ${item.name}`;
    return item;
  }),
  data() {
    return {
      emailRules: [
        v => !!v || "Email is required",
        v => /^\S+@\S+$/.test(v) || "Email is not valid"
      ],
      user: {
        username: '',
        firstName: '',
        lastName: '',
        email: '',
        enabled: '',
        attributes: {
          phone: '',
          org_details: '',
          access_team_notes: ''
        },
        federatedIdentities: ''
      }
    };
  },
  async created() {
    // TODO error handling
    this.$store.commit("user/resetState");
    if (this.userId) {
      await this.getUser();
    }
  },
  methods: {
    getUser: function() {
      return UsersRepository.getUser(this.userId)
        .then(response => {
          this.user = response.data;
          // Keycloak returns attributes as arrays which doesn't work with the autocomplete for org details
          this.user.attributes.org_details = formatOrganization(this.user.attributes.org_details);
          this.$store.commit("user/setUser", this.user);
        })
        .catch(e => {
          console.log(e);
        });
    },
    updateUser: function() {
      this.$emit('submit-user-updates', this.user)
    }
  },
  filters: {
    // The IDP alias in keycloak doesn't always match what's known by users
    // Formatted to match standard naming conventions
    formatIdentityProvider: function(idp) {
      let formattedIdentityProviders = {
        'phsa': 'Health Authority',
        'moh_idp': 'MoH LDAP',
        'idir': 'IDIR',
        'bceid': 'BCeID',
        'bcsc': 'BCSC'
      }
      return formattedIdentityProviders[idp] || idp;
    }
  }
};
function formatOrganization(organization) {
  if (Array.isArray(organization)) {
    return organization[0];
  } else {
    return organization;
  }
}
</script>

<style scoped>
#user-name-tooltip-icon {
  margin-left: 10px;
}
</style>
