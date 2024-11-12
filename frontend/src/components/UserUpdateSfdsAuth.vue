<template>
  <v-data-table
    id="sfds-authorizations-table"
    :headers="sfdsTableHeaders"
    :items="filteredSfdsAuthorizations"
    class="break"
  >
    <template v-slot:top>
      <v-toolbar flat>
        <v-spacer></v-spacer>

        <v-dialog v-model="editDialog" max-width="840px">
          <template v-slot:activator="{ on, attrs }">
            <v-btn
              v-bind="attrs"
              id="new-sfds-auth-btn"
              color="primary"
              darkclass="mb-2"
              v-on="on"
            >
              Add Mailbox Authorization
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="headline">{{ dialogTitle }}</span>
            </v-card-title>
            <v-card-text>
              <v-alert
                id="sfds-auth-alert"
                v-model="alertStatus"
                :type="alertType"
                dismissible
              >
                {{ alertMessage }}
              </v-alert>
              <v-container>
                <v-form ref="sfdsForm">
                  <v-row class="right-gutters">
                    <v-col class="col-4">
                      <label class="required" for="sfds-mailboxes">
                        Mailbox
                      </label>
                      <v-autocomplete
                        id="sfds-mailboxes"
                        v-model="currentSfdsAuthorization.m"
                        :items="sfdsMailboxes"
                        item-text="label"
                        item-value="id"
                        required
                        :rules="mailboxRules"
                        outlined
                        dense
                      ></v-autocomplete>
                    </v-col>
                    <v-col class="col-4">
                      <label
                        class="required"
                        for="sfds-uses"
                        id="sfds-uses-label"
                      >
                        Use
                      </label>
                      <v-autocomplete
                        id="sfds-uses"
                        class="sfds-uses"
                        v-model="currentSfdsAuthorization.u"
                        :items="filteredSfdsUses"
                        item-text="label"
                        item-value="id"
                        required
                        :rules="[
                          (v) => !!v || 'At least one use is required',
                          (v) => v.length > 0 || 'At least one use is required',
                        ]"
                        outlined
                        dense
                        multiple
                        chips
                      ></v-autocomplete>
                    </v-col>
                    <v-col class="col-4">
                      <label class="required" for="sfds-permissions">
                        Permission
                      </label>
                      <v-select
                        id="sfds-permissions"
                        v-model="currentSfdsAuthorization.p"
                        :items="sfdsPermissions"
                        required
                        :rules="[(v) => !!v || 'A permission is required']"
                        outlined
                        dense
                      ></v-select>
                    </v-col>
                  </v-row>
                </v-form>
              </v-container>
            </v-card-text>
            <v-card-actions>
              <v-btn
                id="save-sfds-auth-btn"
                class="primary"
                @click="saveSfdsAuthorization"
              >
                Save Mailbox Authorization
              </v-btn>
              <v-btn outlined class="primary--text" @click="closeDialog">
                Cancel
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>

        <v-dialog v-model="deleteDialog" max-width="650px">
          <v-card>
            <v-card-title class="headline">
              Are you sure you want to delete this authorization?
            </v-card-title>
            <v-card-text class="black--text">
              <v-alert
                id="sfds-delete-auth-alert"
                v-model="alertStatus"
                :type="alertType"
                dismissible
              >
                {{ alertMessage }}
              </v-alert>
              <strong>Mailbox:</strong>
              {{ currentSfdsAuthorization.m }}
              <br />
              <strong>Use:</strong>
              {{ currentSfdsAuthorization.u }}
              <br />
              <strong>Permission:</strong>
              {{ currentSfdsAuthorization.p }}
            </v-card-text>
            <v-card-actions>
              <v-btn
                id="confirm-delete-sfds-btn"
                class="red white--text"
                @click="deleteItemConfirm"
              >
                Delete
              </v-btn>
              <v-btn outlined class="primary--text" @click="closeDialog">
                Cancel
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
      </v-toolbar>
    </template>

    <template v-slot:item.actions="{ item }">
      <v-icon small class="mr-2" @click="editItem(item)">mdi-pencil</v-icon>
      <v-icon :id="item.m + '-delete-btn'" small @click="deleteItem(item)">
        mdi-delete
      </v-icon>
    </template>
  </v-data-table>
</template>

<script>
  import SfdsRepository from "@/api/SfdsRepository";
  import UsersRepository from "@/api/UsersRepository";

  export default {
    name: "UserUpdateSfdsAuth",
    props: ["selectedClient"],
    data() {
      return {
        sfdsTableHeaders: [
          { text: "Mailbox", value: "m" },
          { text: "Uses", value: "u", width: "60%", class: "break" },
          { text: "Permission", value: "p" },
          { text: "Actions", value: "actions", sortable: false },
        ],
        sfdsAuthorizations: [],
        sfdsAuthorizationsToSet: [],
        sfdsUses: [],
        sfdsMailboxes: [],
        sfdsPermissions: [
          "get",
          "send",
          "get-send",
          "get-delete",
          "get-send-delete",
        ],
        currentSfdsAuthorization: {},
        editDialog: false,
        deleteDialog: false,
        editedIndex: -1,
        defaultAuthorization: {
          m: "",
          u: [],
          p: "",
        },
        alertStatus: false,
        alertType: "error",
        alertMessage: "",
      };
    },
    async created() {
      await this.getUses();
      await this.getAccounts();

      let sfdsAuthString = "";

      if (
        this.$store.state.user.attributes.sfds_auth_1 &&
        this.$store.state.user.attributes.sfds_auth_1.length > 0
      ) {
        sfdsAuthString = sfdsAuthString.concat(
          this.$store.state.user.attributes.sfds_auth_1[0]
        );
      }
      if (
        this.$store.state.user.attributes.sfds_auth_2 &&
        this.$store.state.user.attributes.sfds_auth_2.length > 0
      ) {
        sfdsAuthString = sfdsAuthString.concat(
          this.$store.state.user.attributes.sfds_auth_2[0]
        );
      }
      if (
        this.$store.state.user.attributes.sfds_auth_3 &&
        this.$store.state.user.attributes.sfds_auth_3.length > 0
      ) {
        sfdsAuthString = sfdsAuthString.concat(
          this.$store.state.user.attributes.sfds_auth_3[0]
        );
      }
      if (
        this.$store.state.user.attributes.sfds_auth_4 &&
        this.$store.state.user.attributes.sfds_auth_4.length > 0
      ) {
        sfdsAuthString = sfdsAuthString.concat(
          this.$store.state.user.attributes.sfds_auth_4[0]
        );
      }
      if (
        this.$store.state.user.attributes.sfds_auth_5 &&
        this.$store.state.user.attributes.sfds_auth_5.length > 0
      ) {
        sfdsAuthString = sfdsAuthString.concat(
          this.$store.state.user.attributes.sfds_auth_5[0]
        );
      }

      if (sfdsAuthString !== "") {
        this.sfdsAuthorizations = JSON.parse(sfdsAuthString);
      } else {
        this.sfdsAuthorizations = [];
      }
    },
    watch: {
      editDialog(val) {
        val || this.closeDialog();
      },
      deleteDialog(val) {
        val || this.closeDialog();
      },
    },
    computed: {
      filteredSfdsAuthorizations() {
        return this.sfdsAuthorizations.filter((item) =>
          this.selectedClient.clientId == "HSCIS"
            ? item.u.includes("hscis")
            : !item.u.includes("hscis")
        );
      },
      filteredSfdsUses() {
        const hscisLabels = ["hscis", "rpt"];
        const containsHscisLabel = (item) =>
          hscisLabels.some((l) => item.label.includes(l));
        return this.sfdsUses.filter((item) =>
          this.selectedClient.clientId == "HSCIS"
            ? containsHscisLabel(item)
            : !containsHscisLabel(item)
        );
      },
      dialogTitle() {
        return this.editedIndex === -1
          ? "Add Mailbox Authorization"
          : "Edit Mailbox Authorization";
      },
      mailboxRules() {
        const rules = [];
        const rule1 = (v) => !!v || "A mailbox is required";
        rules.push(rule1);

        if (this.sfdsAuthorizations.length > 0) {
          // Check all existing SFDS authorizations for unique mailbox
          if (this.editedIndex === -1) {
            const rule2 = (v) =>
              !this.sfdsAuthorizations.some(
                (sfdsAuth) => sfdsAuth["m"] === v
              ) || "Mailbox must be unique";
            rules.push(rule2);
            // Check all existing SFDS authorizations except the currently selected one for unique mailbox
          } else {
            const authsToMatch = JSON.parse(
              JSON.stringify(this.sfdsAuthorizations)
            );
            authsToMatch.splice(this.editedIndex, 1);
            const rule2 = (v) =>
              !authsToMatch.some((sfdsAuth) => sfdsAuth["m"] === v) ||
              "Mailbox must be unique";
            rules.push(rule2);
          }
        }
        return rules;
      },
    },
    methods: {
      getUses: function () {
        console.log("get SFDS Uses API call");
        return SfdsRepository.getUses()
          .then((response) => {
            this.sfdsUses = response.data
              .map((item) => {
                item.label = `${item.id} - ${item.name}`;
                return item;
              })
              .sort((a, b) => (a.label > b.label && 1) || -1);
          })
          .catch((e) => {
            console.log(e);
          });
      },
      getAccounts: function () {
        console.log("get SFDS Accounts API call");
        SfdsRepository.getAccounts()
          .then((response) => {
            this.sfdsMailboxes = response.data
              .map((item) => {
                item.label = `${item.id} - ${item.name}`;
                return item;
              })
              .sort((a, b) => (a.label > b.label && 1) || -1);
          })
          .catch((e) => {
            console.log(e);
          });
      },
      editItem: function (item) {
        this.editedIndex = this.sfdsAuthorizations.indexOf(item);
        this.currentSfdsAuthorization = Object.assign({}, item);
        // if Mailbox does not exist anymore in SFDS, then the permission need to be updated/deleted
        if (
          !this.sfdsMailboxes.some(
            (mailbox) => mailbox["id"] === this.currentSfdsAuthorization.m
          )
        ) {
          this.alertStatus = true;
          this.alertType = "error";
          this.alertMessage =
            "This Mailbox has been deleted from SFDS. Please edit/remove this set of SFDS Authorizations.";
        }
        this.editDialog = true;
      },
      deleteItem: function (item) {
        this.editedIndex = this.sfdsAuthorizations.indexOf(item);
        this.currentSfdsAuthorization = Object.assign({}, item);
        this.deleteDialog = true;
      },
      deleteItemConfirm: function () {
        this.alertStatus = false;
        this.sfdsAuthorizationsToSet = JSON.parse(
          JSON.stringify(this.sfdsAuthorizations)
        );
        this.sfdsAuthorizationsToSet.splice(this.editedIndex, 1);
        this.commitAndUpdateUserWithSfdsAuthUpdates();
      },
      saveSfdsAuthorization: function () {
        this.alertStatus = false;
        if (!this.$refs.sfdsForm.validate()) {
          return;
        }

        // JSON stringify and parse for deep clone of an array with simple objects
        // Clone the array so that the datatable presented to the user isn't updated until the request is successfully submitted
        // After a success response we'll copy the new resulting array back.
        this.sfdsAuthorizationsToSet = JSON.parse(
          JSON.stringify(this.sfdsAuthorizations)
        );
        if (this.editedIndex > -1) {
          Object.assign(
            this.sfdsAuthorizationsToSet[this.editedIndex],
            this.currentSfdsAuthorization
          );
        } else {
          this.sfdsAuthorizationsToSet.push(this.currentSfdsAuthorization);
        }

        // Check global error
        var isGlobalErrorRaised = this.isGlobalErrorRaised(
          this.sfdsAuthorizationsToSet
        );
        if (isGlobalErrorRaised) {
          return;
        }

        this.commitAndUpdateUserWithSfdsAuthUpdates();
      },
      closeDialog: function () {
        if (this.deleteDialog === true) {
          this.deleteDialog = false;
        } else {
          this.$refs.sfdsForm.resetValidation();
          this.editDialog = false;
        }
        this.alertStatus = false;
        this.$nextTick(() => {
          this.currentSfdsAuthorization = Object.assign(
            {},
            this.defaultAuthorization
          );
          this.editedIndex = -1;
        });
      },
      commitAndUpdateUserWithSfdsAuthUpdates: function () {
        this.setSfdsAuthStoreState(this.sfdsAuthorizationsToSet);
        UsersRepository.updateUser(
          this.$route.params.userid,
          this.$store.state.user
        )
          .then(() => {
            // JSON stringify and parse for deep clone of an array with simple objects
            this.sfdsAuthorizations = JSON.parse(
              JSON.stringify(this.sfdsAuthorizationsToSet)
            );
            this.alertStatus = true;
            this.alertType = "success";
            this.alertMessage = "Mailbox Authorizations updated sucessfully";
            setTimeout(this.closeDialog, 2000);
          })
          .catch((error) => {
            this.alertStatus = true;
            this.alertType = "error";
            this.alertMessage =
              "Error updating user SFDS Authorizations: " + error;
            this.setSfdsAuthStoreState(this.sfdsAuthorizations);
          });
      },
      isGlobalErrorRaised: function (sfdsAuthz) {
        // 1 - check if Mailbox does not exist anymore in SFDS
        // Need this here too, as we need to prevent the user to save the faulty set of permission again
        if (
          !this.sfdsMailboxes.some(
            (mailbox) => mailbox["id"] === this.currentSfdsAuthorization.m
          )
        ) {
          this.alertStatus = true;
          this.alertType = "error";
          this.alertMessage =
            "This Mailbox has been deleted from SFDS. Please edit/remove this set of SFDS Authorizations.";
          this.setSfdsAuthStoreState(this.sfdsAuthorizations);
          return true;
        }
        // 2 - check length of KC attributes
        let sfdsAuthString = JSON.stringify(sfdsAuthz);
        // Keycloak can store 255 characters in an attribute. The UMC is coded to save 5 attributes. 5 attributes * 255 characters = 1275.
        if (sfdsAuthString.length > 1275) {
          this.alertStatus = true;
          this.alertType = "error";
          this.alertMessage =
            "Error: There are too many SFDS Authorizations for this user, please remove some before adding more.";
          this.setSfdsAuthStoreState(this.sfdsAuthorizations);
          return true;
        }
        return false;
      },
      setSfdsAuthStoreState: function (sfdsAuthz) {
        let sfdsAuthString = JSON.stringify(sfdsAuthz);
        // Keycloak can only store attributes at 255 chars
        // A user could require up to ~1000 characters worth of mailbox permissions in the current storage format
        let sfdsAuths = chunkString(sfdsAuthString, 255);
        for (let i = 0; i < 5; i++) {
          this.$store.commit("user/setSfdsAuth" + (i + 1), sfdsAuths[i]);
        }
      },
    },
  };
  function chunkString(str, size) {
    return str.match(new RegExp(".{1," + size + "}", "g"));
  }
</script>

<style scoped>
  .sub-permissions {
    margin: 20px 0 20px 0;
  }
  .sfds-authorizations-header {
    margin: 22px 0 22px 0;
  }
  .break {
    word-break: break-all;
  }
</style>
