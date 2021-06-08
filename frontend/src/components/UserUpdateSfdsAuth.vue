<template>
  <v-data-table
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
      val || this.close()
    },
    deleteDialog (val) {
      val || this.closeDelete()
    }
  },
  computed: {
    dialogTitle () {
      return this.editedIndex === -1 ? 'New Authorization' : 'Edit Authorization'
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
      this.sfdsAuthorizations.splice(this.editedIndex, 1)
      this.commitAndUpdateUserWithSfdsAuthUpdates()
      this.closeDelete()
    },
    saveSfdsAuthorization: function() {
      if (this.editedIndex > -1) {
        Object.assign(this.sfdsAuthorizations[this.editedIndex], this.currentSfdsAuthorization)
      } else {
        this.sfdsAuthorizations.push(this.currentSfdsAuthorization)
      }
      this.commitAndUpdateUserWithSfdsAuthUpdates()
      this.close()
    },
    close: function () {
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
    commitAndUpdateUserWithSfdsAuthUpdates: function () {
      let sfdsAuthString = JSON.stringify(this.sfdsAuthorizations);
      // Keycloak can only store attributes at 255 chars
      // A user could require up to ~1000 characters worth of mailbox permissions in the current storage format
      let sfdsAuths = chunkString(sfdsAuthString, 255);
      for (let i = 0; i < 4; i++) {
          this.$store.commit("user/setSfdsAuth" + (i+1), sfdsAuths[i]);
      }
      UsersRepository.updateUser(this.$route.params.userid, this.$store.state.user);
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