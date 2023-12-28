<!--suppress XmlInvalidId -->
<template>
  <div id="user">
    <v-card outlined class="subgroup">
      <h2>User Details</h2>
      <v-row no-gutters>
        <v-col class="col-7">
          <v-form ref="form">
            <label
              for="user-name"
              :disabled="!!userId || !editUserDetailsPermission"
              class="required"
            >
              Username
            </label>
            <v-tooltip right v-if="!userId">
              <template v-slot:activator="{ on }">
                <v-icon id="user-name-tooltip-icon" v-on="on" small>
                  mdi-help-circle
                </v-icon>
              </template>
              <span>
                Username should include the corresponding prefix or suffix in
                alignment with the id type.
                <ul>
                  <li>
                    IDIR:
                    <span>username</span>
                    <strong>@idir</strong>
                  </li>
                  <li>
                    Business BCeID:
                    <span>username</span>
                    <strong>@bceid_business</strong>
                  </li>
                  <li>
                    BC Provider:
                    <span>username</span>
                    <strong>@bcp</strong>
                    <br />
                    <span class="tooltip-note">
                      Note: The username will already contain an '@domain' that
                      the '@bcp' will be appended to.
                    </span>
                  </li>
                  <li>
                    BC Services Card:
                    <span>username</span>
                    <strong>@bcsc</strong>
                  </li>
                  <li>
                    FNHA:
                    <span>username</span>
                    <strong>@fnha</strong>
                    <br />
                    <span class="tooltip-note">
                      Note: The username will already contain an '@domain' that
                      the '@fnha' will be appended to.
                    </span>
                  </li>
                  <li>
                    Fraser Health:
                    <strong>sfhr\</strong>
                    <span>username</span>
                  </li>
                  <li style="display: inline-block">
                    Interior Health:
                    <strong>iha\</strong>
                    <span>username</span>
                  </li>
                  <li>
                    Northern Health:
                    <strong>nirhb\</strong>
                    <span>username</span>
                  </li>
                  <li>
                    Provincial Health:
                    <strong>phsabc\</strong>
                    <span>username</span>
                  </li>
                  <li>
                    Vancouver Coastal Health:
                    <strong>vch\</strong>
                    <span>username</span>
                    or
                    <strong>vrhb\</strong>
                    <span>username</span>
                  </li>
                  <li>
                    Vancouver Island Health:
                    <strong>viha\</strong>
                    <span>username</span>
                  </li>
                </ul>
              </span>
            </v-tooltip>
            <v-text-field
              dense
              :disabled="!!userId || !editUserDetailsPermission"
              outlined
              id="user-name"
              v-model.trim="user.username"
              required
              :rules="[(v) => !!v || 'Username is required']"
            />

            <label
              :disabled="!editUserDetailsPermission"
              for="first-name"
              class="required"
            >
              First Name
            </label>
            <v-text-field
              dense
              outlined
              :disabled="!editUserDetailsPermission"
              id="first-name"
              v-model.trim="user.firstName"
              required
              :rules="[(v) => !!v || 'First Name is required']"
            />

            <label
              :disabled="!editUserDetailsPermission"
              for="last-name"
              class="required"
            >
              Last Name
            </label>
            <v-text-field
              dense
              outlined
              :disabled="!editUserDetailsPermission"
              id="last-name"
              v-model.trim="user.lastName"
              required
              :rules="[(v) => !!v || 'Last Name is required']"
            />

            <label
              :disabled="!editUserDetailsPermission"
              for="email"
              class="required"
            >
              Email Address
            </label>
            <v-text-field
              dense
              outlined
              :disabled="!editUserDetailsPermission"
              id="email"
              v-model.trim="user.email"
              required
              :rules="emailRules"
              type="email"
            />

            <label :disabled="!editUserDetailsPermission" for="phone">
              Telephone Number
            </label>
            <v-text-field
              dense
              outlined
              :disabled="!editUserDetailsPermission"
              id="phone"
              v-model.trim="user.attributes.phone"
            />

            <label :disabled="!editUserDetailsPermission" for="org-details">
              Organization
            </label>
            <v-autocomplete
              id="org-details"
              :disabled="!editUserDetailsPermission"
              v-model="user.attributes.org_details"
              :items="organizations"
              dense
              outlined
            ></v-autocomplete>

            <label :disabled="!editUserDetailsPermission" for="notes">
              Notes
            </label>
            <v-textarea
              outlined
              dense
              id="notes"
              :disabled="!editUserDetailsPermission"
              v-model="user.attributes.access_team_notes"
              maxlength="255"
            />
          </v-form>
        </v-col>
        <v-col
          class="col-4"
          style="
            margin-left: 30px;
            padding-left: 20px;
            border-left: 1px solid #efefef;
          "
          v-if="this.userId"
        >
          <label for="linked-idps">Linked Identity Types</label>
          <ul id="linked-idps" style="margin-top: 5px; list-style: square">
            <li v-for="identity in user.federatedIdentities" :key="identity.id">
              <span>
                {{ identity.identityProvider | formatIdentityProvider }} [{{
                  identity.userName
                }}]
              </span>
              <v-tooltip right>
                <template v-slot:activator="{ on, attrs }">
                  <v-btn
                    icon
                    v-bind="attrs"
                    v-on="on"
                    @click="
                      openResetIdentityProviderLinkDialog(
                        identity.identityProvider
                      )
                    "
                  >
                    <v-icon small style="vertical-align: middle">
                      mdi-link-variant-off
                    </v-icon>
                  </v-btn>
                </template>
                <span>Reset Identity Provider Link</span>
              </v-tooltip>
            </li>
          </ul>
        </v-col>
      </v-row>
      <v-btn
        id="submit-button"
        v-if="editUserDetailsPermission"
        class="primary"
        medium
        @click="updateUser"
      >
        {{ updateOrCreate }} User
      </v-btn>
    </v-card>
    <v-dialog v-model="dialog" content-class="resetUserIdentityLinksDialog">
      <v-card>
        <v-card-title>
          <span class="headline">
            Identity Provider Link Reset Confirmation
          </span>
        </v-card-title>
        <v-card-text>
          <br />
          Are you sure you want to reset the
          {{ this.selectedIdentityProvider | formatIdentityProvider }} linked
          identity for this user?
          <br />
          <br />
          Resetting a linked identity will retain all existing roles and details
          for the user and the identity will be re-linked upon the user's next
          Keycloak login event.
        </v-card-text>
        <v-card-actions>
          <v-btn class="primary" @click="resetIdentityProviderLink">
            Reset Identity Provider Link
          </v-btn>
          <v-btn
            outlined
            class="primary--text"
            @click="closeResetIdentityProviderLinkDialog"
          >
            Cancel
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
  import UsersRepository from "@/api/UsersRepository";

  export default {
    name: "UserDetails",
    props: ["userId", "updateOrCreate"],
    data() {
      return {
        organizations: this.$organizations.map((item) => {
          item.value = `{"id":"${item.organizationId}","name":"${item.name}"}`;
          item.text = `${item.organizationId} - ${item.name}`;
          return item;
        }),
        emailRules: [
          (v) => !!v || "Email is required",
          (v) => /^\S+@\S+$/.test(v) || "Email is not valid",
        ],
        user: {
          username: "",
          firstName: "",
          lastName: "",
          email: "",
          enabled: true,
          attributes: {
            phone: null,
            org_details: null,
            access_team_notes: null,
          },
          federatedIdentities: null,
        },
        dialog: false,
        selectedIdentityProvider: "",
      };
    },
    async created() {
      //Create global ref to allow role update from UserUpdateRoles
      this.$root.$refs.UserDetails = this;
      // TODO error handling
      this.$store.commit("user/resetState");
      if (this.userId) {
        await this.getUser();
      }
    },
    computed: {
      editUserDetailsPermission: function () {
        const onCreateUserPage = !this.userId;

        const umsClientId = "USER-MANAGEMENT-SERVICE";
        const manageUserDetailsRoleName = "manage-user-details";
        const createUserRoleName = "create-user";

        const hasManageUserDetails = this.$keycloak.tokenParsed.resource_access[
          umsClientId
        ].roles.includes(manageUserDetailsRoleName);
        const hasCreateUser =
          this.$keycloak.tokenParsed.resource_access[
            umsClientId
          ].roles.includes(createUserRoleName);

        if (onCreateUserPage && hasCreateUser) {
          return true;
        } else if (!onCreateUserPage && hasManageUserDetails) {
          return true;
        }
        return false;
      },
    },
    methods: {
      openResetIdentityProviderLinkDialog: function (provider) {
        this.dialog = true;
        if (this.dialog) {
          this.selectedIdentityProvider = provider;
        }
      },
      closeResetIdentityProviderLinkDialog: function () {
        this.dialog = false;
        this.selectedIdentityProvider = "";
      },
      updateUserFederatedIdetities: function (deletedIdentityProvider) {
        console.log(deletedIdentityProvider);
        console.log(this.user.federatedIdentities);
        this.user.federatedIdentities = this.user.federatedIdentities.filter(
          (fi) => fi.identityProvider !== deletedIdentityProvider
        );
        console.log(this.user.federatedIdentities);
      },
      resetIdentityProviderLink: function () {
        const userIdIdpRealm = this.user.federatedIdentities.find(
          (fi) => fi.identityProvider === this.selectedIdentityProvider
        ).userId;
        console.log(userIdIdpRealm);
        UsersRepository.resetUserIdentityProviderLink(
          this.userId,
          this.selectedIdentityProvider,
          userIdIdpRealm
        )
          .then(() => {
            this.$store.commit("alert/setAlert", {
              message: "Identity provider link was reset successfully",
              type: "success",
            });
            this.updateUserFederatedIdetities(this.selectedIdentityProvider);
            this.closeResetIdentityProviderLinkDialog();
          })
          .catch((error) => {
            this.$store.commit("alert/setAlert", {
              message: "Error resetting identity provider link: " + error,
              type: "error",
            });
          })
          .finally(() => {
            window.scrollTo(0, 0);
            this.selectedIdentityProvider = "";
          });
      },

      getUser: function () {
        return UsersRepository.getUser(this.userId)
          .then((response) => {
            this.user = response.data;

            if (this.user.federatedIdentities) {
              const predicate = (fi) => fi.identityProvider?.includes("bcsc");

              let bcscLikeIdentities = this.user.federatedIdentities
                .filter((fi) => predicate(fi))
                .map((fi) => fi.identityProvider);
              console.log(bcscLikeIdentities);

              let bcscLikeCounter = 0;

              this.user.federatedIdentities =
                this.user.federatedIdentities.filter((fi) => {
                  if (predicate(fi)) {
                    bcscLikeCounter++;
                    fi.identityProvider = bcscLikeIdentities.join();
                    console.log(fi.identityProvider);
                    return bcscLikeCounter <= 1;
                  }
                  return true;
                });
            }

            if (!this.user.attributes) {
              this.user.attributes = {};
            }

            // Keycloak returns attributes as arrays which doesn't work with the autocomplete for org details
            if (this.user.attributes.org_details) {
              this.user.attributes.org_details = formatOrganization(
                this.user.attributes.org_details
              );
            }
            this.$store.commit("user/setUser", this.user);
          })
          .catch((e) => {
            console.log(e);
          });
      },
      updateUser: function () {
        // Validate the User Details
        if (!this.$refs.form.validate()) {
          this.$store.commit("alert/setAlert", {
            message: "Please correct errors before submitting",
            type: "error",
          });
          window.scrollTo(0, 0);
          return;
        }
        this.$emit("submit-user-updates", this.user);
      },
    },
    filters: {
      // The IDP alias in keycloak doesn't always match what's known by users
      // Formatted to match standard naming conventions
      formatIdentityProvider: function (idp) {
        let formattedIdentityProviders = {
          bceid_business: "BCeID Business",
          bcprovider_aad: "BC Provider",
          idir: "IDIR",
          idir_aad: "IDIR AzureAD",
          moh_idp: "Keycloak",
          phsa: "Health Authority",
          phsa_aad: "Health Authority AzureAD",
        };

        if (idp.startsWith("bcsc")) {
          return "BC Services Card for " + mapBcscToClientName(idp);
        }

        return formattedIdentityProviders[idp] || idp;
      },
    },
  };
  function mapBcscToClientName(federatedIdentityString) {
    const bcscMapping = {
      bcsc: "PIDP",
      bcsc_prime: "PRIME",
      bcsc_mspdirect: "MSPDIRECT",
      bcsc_hcap: "HCAP",
    };

    const federatedIdentityArray = federatedIdentityString.split(",");

    const clientNames = federatedIdentityArray.map((bcscAlias) => {
      return bcscMapping[bcscAlias] || bcscAlias;
    });

    return clientNames.join(", ");
  }

  function formatOrganization(organization) {
    if (Array.isArray(organization)) {
      if (organization[0] === null) return "";
      return `{"id":"${JSON.parse(organization[0]).id}","name":"${
        JSON.parse(organization[0]).name
      }"}`;
    } else {
      return organization;
    }
  }
</script>
<style>
  .resetUserIdentityLinksDialog {
    margin: 24px;
    overflow-y: auto;
    overflow-x: hidden;
    max-width: 95%;
    min-width: 450px;
    width: 900px;
    z-index: inherit;
  }
</style>
<style scoped>
  #user-name-tooltip-icon {
    margin-left: 10px;
  }
  .tooltip-note {
    padding-left: 20px;
  }
</style>
