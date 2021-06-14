<template>
  <v-data-table
      id="sfds-authorizations-table"
      :headers="sfdsTableHeaders"
      :items="sfdsAuthorizations">
    <template v-slot:top>
      <v-toolbar
          flat
      >
        <h2 class="sfds-authorizations-header">SFDS Authorizations</h2>
        <v-spacer></v-spacer>
        <v-dialog v-model="editDialog" max-width="840px">
          <template v-slot:activator="{ on, attrs }">
            <v-btn id="new-sfds-auth-btn" color="primary" darkclass="mb-2" v-bind="attrs" v-on="on">
              New Authorization
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="headline">{{ dialogTitle }}</span>
            </v-card-title>
            <v-card-text>
              <v-alert id="sfds-auth-alert" v-model="alertStatus" :type="alertType" dismissible>{{ alertMessage }}</v-alert>
              <v-container>
                <v-form ref="sfdsForm">
                  <v-row class="right-gutters">
                    <v-col class="col-4">
                      <label class="required" for="sfds-mailboxes" >Mailbox</label>
                      <v-autocomplete
                          id="sfds-mailboxes"
                          v-model="currentSfdsAuthorization.mailbox"
                          :items="sfdsMailboxes"
                          required
                          :rules="mailboxRules"
                          outlined
                          dense
                      ></v-autocomplete>
                    </v-col>
                    <v-col class="col-4">
                      <label class="required" for="sfds-uses" id="sfds-uses-label" >Use</label>
                      <v-autocomplete
                          id="sfds-uses"
                          class="sfds-uses"
                          v-model="currentSfdsAuthorization.uses"
                          :items="sfdsUses"
                          required
                          :rules="[v => !!v || 'At least one use is required',
                                  (v) =>  v.length>0 || 'At least one use is required']"
                          outlined
                          dense
                          multiple
                          chips
                      ></v-autocomplete>
                    </v-col>
                    <v-col class="col-4">
                      <label class="required" for="sfds-permissions" >Permission</label>
                      <v-select
                          id="sfds-permissions"
                          v-model="currentSfdsAuthorization.permission"
                          :items="sfdsPermissions"
                          required
                          :rules="[v => !!v || 'A permission is required']"
                          outlined
                          dense
                      ></v-select>
                    </v-col>
                  </v-row>
                </v-form>
              </v-container>
            </v-card-text>
            <v-card-actions>
              <v-btn id="save-sfds-auth-btn" class="primary" @click="saveSfdsAuthorization">
                Save
              </v-btn>
              <v-btn outlined class="primary--text" @click="closeDialog">
                Cancel
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-dialog v-model="deleteDialog" max-width="650px" >
          <v-card >
            <v-card-title class="headline">Are you sure you want to delete this authorization?</v-card-title>
            <v-card-text class="black--text">
              <v-alert id="sfds-delete-auth-alert" v-model="alertStatus" :type="alertType" dismissible>{{ alertMessage }}</v-alert>
              <strong>Mailbox:</strong> {{ currentSfdsAuthorization.mailbox }} <br/>
              <strong>Use:</strong> {{ currentSfdsAuthorization.uses }} <br/>
              <strong>Permission:</strong> {{ currentSfdsAuthorization.permission }}
            </v-card-text>
            <v-card-actions>
              <v-btn id="confirm-delete-sfds-btn" class="red white--text" @click="deleteItemConfirm">Delete</v-btn>
              <v-btn outlined class="primary--text" @click="closeDialog">Cancel</v-btn>
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
          :id="item.mailbox+'-delete-btn'"
          small
          @click="deleteItem(item)">
        mdi-delete
      </v-icon>
    </template>
  </v-data-table>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";

export default {
  name: "UserUpdateSfdsAuth",
  data() {
    return {
      sfdsTableHeaders: [
        { text: 'Mailbox', value: 'mailbox' },
        { text: 'Uses', value: 'uses' },
        { text: 'Permission', value: 'permission' },
        { text: 'Actions', value: 'actions', sortable: false }
      ],
      sfdsAuthorizations: [],
      sfdsAuthorizationsToSet: [],
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
      },
      alertStatus: false,
      alertType: "error",
      alertMessage: ''
    };
  },
  async created() {
    let sfdsAuthString = "";

    if (this.$store.state.user.attributes.sfds_auth_1) {
      sfdsAuthString = sfdsAuthString.concat(this.$store.state.user.attributes.sfds_auth_1[0]);
    }
    if (this.$store.state.user.attributes.sfds_auth_2) {
      sfdsAuthString = sfdsAuthString.concat(this.$store.state.user.attributes.sfds_auth_2[0]);
    }
    if (this.$store.state.user.attributes.sfds_auth_3) {
      sfdsAuthString = sfdsAuthString.concat(this.$store.state.user.attributes.sfds_auth_3[0]);
    }
    if (this.$store.state.user.attributes.sfds_auth_4) {
      sfdsAuthString = sfdsAuthString.concat(this.$store.state.user.attributes.sfds_auth_4[0]);
    }

    if (sfdsAuthString !== "") {
      this.sfdsAuthorizations = JSON.parse(sfdsAuthString);
    } else {
      this.sfdsAuthorizations = [];
    }
  },
  watch: {
    editDialog (val) {
      val || this.closeDialog()
    },
    deleteDialog (val) {
      val || this.closeDialog()
    }
  },
  computed: {
    dialogTitle () {
      return this.editedIndex === -1 ? 'New Authorization' : 'Edit Authorization'
    },
    mailboxRules() {
      const rules = []
      const rule1 = v => !!v || 'A mailbox is required'
      rules.push(rule1)

      if (this.sfdsAuthorizations.length>0 && this.editedIndex === -1) {
        const rule2 = v => this.sfdsAuthorizations.some( sfdsAuth => sfdsAuth['mailbox'] !== v) || 'Mailbox must be unique'
        rules.push(rule2)
      }
      return rules
    }
  },
  methods: {
    editItem: function (item) {
      this.editedIndex = this.sfdsAuthorizations.indexOf(item)
      this.currentSfdsAuthorization = Object.assign({}, item)
      this.editDialog = true
    },
    deleteItem: function (item) {
      this.editedIndex = this.sfdsAuthorizations.indexOf(item)
      this.currentSfdsAuthorization = Object.assign({}, item)
      this.deleteDialog = true
    },
    deleteItemConfirm: function () {
      this.alertStatus = false
      this.sfdsAuthorizationsToSet = JSON.parse(JSON.stringify(this.sfdsAuthorizations));
      this.sfdsAuthorizationsToSet.splice(this.editedIndex, 1)
      this.commitAndUpdateUserWithSfdsAuthUpdates()
    },
    saveSfdsAuthorization: function() {
      this.alertStatus = false
      if (!this.$refs.sfdsForm.validate()) {
        return;
      }
      // JSON stringify and parse for deep clone of an array with simple objects
      this.sfdsAuthorizationsToSet = JSON.parse(JSON.stringify(this.sfdsAuthorizations));
      if (this.editedIndex > -1) {
        Object.assign(this.sfdsAuthorizationsToSet[this.editedIndex], this.currentSfdsAuthorization);
      } else {
        this.sfdsAuthorizationsToSet.push(this.currentSfdsAuthorization);
      }
      this.commitAndUpdateUserWithSfdsAuthUpdates();
    },
    closeDialog: function () {
      if (this.deleteDialog === true) {
        this.deleteDialog = false
      } else {
        this.$refs.sfdsForm.resetValidation()
        this.editDialog = false
      }
      this.alertStatus = false
      this.$nextTick(() => {
        this.currentSfdsAuthorization = Object.assign({}, this.defaultAuthorization)
        this.editedIndex = -1
      })
    },
    commitAndUpdateUserWithSfdsAuthUpdates: function () {
      this.setSfdsAuthStoreState(this.sfdsAuthorizationsToSet)
      UsersRepository.updateUser(this.$route.params.userid, this.$store.state.user)
          .then(() => {
            // JSON stringify and parse for deep clone of an array with simple objects
            this.sfdsAuthorizations = JSON.parse(JSON.stringify(this.sfdsAuthorizationsToSet));
            this.closeDialog()
          })
          .catch(error => {
            this.alertStatus = true
            this.alertMessage = "Error updating user sfds authorizations: " + error
            this.setSfdsAuthStoreState(this.sfdsAuthorizations)
          });
    },
    setSfdsAuthStoreState: function(sfdsAuthz) {
      let sfdsAuthString = JSON.stringify(sfdsAuthz);
      // Keycloak can only store attributes at 255 chars
      // A user could require up to ~1000 characters worth of mailbox permissions in the current storage format
      let sfdsAuths = chunkString(sfdsAuthString, 255);
      for (let i = 0; i < 4; i++) {
        this.$store.commit("user/setSfdsAuth" + (i+1), sfdsAuths[i]);
      }
    }
  }
}
function chunkString(str, size) {
  return str.match(new RegExp('.{1,' + size + '}', 'g'));
}
</script>

<style scoped>
  .sub-permissions {
    margin: 20px 0px 20px 0px;
  }
  .sfds-authorizations-header {
    margin: 22px 0px 22px 0px;
  }
</style>