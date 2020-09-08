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
              <span>Username should be the exact user id
                  followed directly by @idir, @bceid, or @phsa
                  in alignment with the digital id type
                  (e.g. userjane@idir)</span>
            </v-tooltip>
            <v-text-field
              dense
              :disabled="!!userId"
              outlined
              id="user-name"
              v-model="username"
              required
              :rules="[v => !!v || 'Username is required']"
            />

            <label for="first-name" class="required">First Name</label>
            <v-text-field
              dense
              outlined
              id="first-name"
              v-model="firstName"
              required
              :rules="[v => !!v || 'First Name is required']"
            />

            <label for="last-name" class="required">Last Name</label>
            <v-text-field
              dense
              outlined
              id="last-name"
              v-model="lastName"
              required
              :rules="[v => !!v || 'Last Name is required']"
            />

            <label for="email" class="required">Email Address</label>
            <v-text-field
              dense
              outlined
              id="email"
              v-model="email"
              required
              :rules="emailRules"
              type="email"
            />
            <label for="phone">Telephone Number</label>
            <v-text-field dense outlined id="phone" v-model="phone" />

            <label for="org-details">Organization</label>
            <v-autocomplete
                id="org-details"
                v-model="org_details"
                :items="$options.organizations"
                dense
                outlined
            ></v-autocomplete>

            <label for="notes">Notes</label>
            <v-textarea outlined dense id="notes" v-model="access_team_notes" maxlength="255" />

            <label for="enabled-radio">Lockout Status</label>
            <v-radio-group id="enabled-radio" v-model="computedLockout" row height="1">
              <v-radio v-for="n in LOCK_STATES" :key="n" :label="n" :value="n"></v-radio>
            </v-radio-group>

            <label
              for="lockout-reason"
              v-bind:class="[enabled ? 'disabled' : 'required']"
            >Lockout Reason</label>
            <v-text-field
              dense
              outlined
              id="lockout-reason"
              v-model="lockout_reason"
              :disabled="enabled"
              :required="!enabled"
              :rules="[v => enabled ? true : ((!!v && typeof v === 'string') || (Array.isArray(v) && !!v[0])) || 'Lockout Reason is required' ]"
            />
          </v-form>
        </v-col>
        <v-col class="col-4" style="margin-left: 30px; padding-left: 20px; border-left: 1px solid #efefef">
          <label for="linked-idps">Linked Identity Types</label>
          <ul id="linked-idps" style="margin-top: 5px; list-style: square">
            <li v-for="identity in user.federatedIdentities" :key="identity.id">
              {{ formatIdentityProvider(identity.identityProvider) }} [{{ identity.userName }}]
            </li>
          </ul>
        </v-col>
      </v-row>
      <slot></slot>
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
      ENABLED: "Enabled",
      LOCKED: "Locked",
      REVOKED: "Revoked"
    };
  },
  async created() {
    //TODO error handling
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
        })
        .catch(e => {
          console.log(e);
        });
    },
    // the IDP name in keycloak doesn't always match what's known by users
    // format to match standard naming or capitalization
    formatIdentityProvider: function(idp) {
      let formattedIdp = idp;
      switch(idp) {
        case 'phsa':
          formattedIdp = 'PHSA';
          break;
        case 'moh_idp':
          formattedIdp = 'MoH LDAP';
          break;
        case 'idir':
          formattedIdp = 'IDIR';
          break;
        case 'bceid':
          formattedIdp = 'BCeID';
          break;
        case 'bcsc':
          formattedIdp = 'BCSC';
          break;
      }
      return formattedIdp;
    }
  },
  computed: {
    LOCK_STATES() {
      return [this.ENABLED, this.REVOKED, this.LOCKED];
    },
    computedLockout: {
      get: function() {
        if (this.enabled) {
          return this.ENABLED;
        } else {
          if (this.revoked) {
            return this.REVOKED;
          } else {
            return this.LOCKED;
          }
        }
      },
      set: function(newValue) {
        if (newValue === this.ENABLED) {
          this.enabled = true;
          this.revoked = false;
          this.lockout_reason = "";
        } else if (newValue === this.REVOKED) {
          this.enabled = false;
          this.revoked = true;
        } else if (newValue === this.LOCKED) {
          this.enabled = false;
          this.revoked = false;
        } else {
          throw Error(`Unrecognized lock state '${newValue}'`);
        }
      }
    },
    user: {
      get() {
        return this.$store.state.user;
      },
      set(user) {
        this.$store.commit("user/setUser", user);
      }
    },
    username: {
      get() {
        return this.$store.state.user.username;
      },
      set(username) {
        this.$store.commit("user/setUsername", username);
      }
    },
    firstName: {
      get() {
        return this.$store.state.user.firstName;
      },
      set(firstName) {
        this.$store.commit("user/setFirstName", firstName);
      }
    },
    lastName: {
      get() {
        return this.$store.state.user.lastName;
      },
      set(lastName) {
        this.$store.commit("user/setLastName", lastName);
      }
    },
    email: {
      get() {
        return this.$store.state.user.email;
      },
      set(email) {
        this.$store.commit("user/setEmail", email);
      }
    },
    enabled: {
      get() {
        return this.$store.state.user.enabled;
      },
      set(enabled) {
        this.$store.commit("user/setEnabled", enabled);
      }
    },
    phone: {
      get() {
        return this.$store.state.user.attributes.phone;
      },
      set(phone) {
        this.$store.commit("user/setPhone", phone);
      }
    },
    org_details: {
      get() {
        //Keycloak returns attributes as arrays which doesn't work with the autocomplete
        let org = this.$store.state.user.attributes.org_details;
        if (Array.isArray(org)) {
          return org[0];
        } else {
          return org;
        }
      },
      set(org_details) {
        this.$store.commit("user/setOrgDetails", org_details);
      }
    },
    lockout_reason: {
      get() {
        return this.$store.state.user.attributes.lockout_reason;
      },
      set(lockout_reason) {
        this.$store.commit("user/setLockoutReason", lockout_reason);
      }
    },
    revoked: {
      get() {
        return this.$store.state.user.attributes.revoked;
      },
      set(revoked) {
        this.$store.commit("user/setRevoked", revoked);
      }
    },
    access_team_notes: {
      get() {
        return this.$store.state.user.attributes.access_team_notes;
      },
      set(access_team_notes) {
        this.$store.commit("user/setAccessTeamNotes", access_team_notes);
      }
    },
    federatedIdentities: {
      get() {
        return this.$store.state.user.attributes.federatedIdentities;
      }
    }
  }
};
</script>

<style scoped>
#user-name-tooltip-icon {
  margin-left: 10px;
}
</style>