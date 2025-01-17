<!--suppress XmlInvalidId -->
<template>
  <div id="user">
    <v-card border class="subgroup">
      <h2>User Details</h2>
      <v-row no-gutters>
        <v-col cols="7">
          <v-form ref="form">
            <label
              for="user-name"
              :disabled="!!userId || !editUserDetailsPermission"
              class="required"
            >
              Username
            </label>
            <v-tooltip location="right" v-if="!userId">
              <template v-slot:activator="{ props }">
                <v-icon id="user-name-tooltip-icon" v-bind="props" size="small">
                  mdi-help-circle
                </v-icon>
              </template>
              <span>
                Username should include the corresponding prefix or suffix in
                alignment with the id type.
                <ul>
                  <li v-for="idp in identityProviders" :key="idp.name">
                    <template>
                      {{ idp.name }}:
                      <span v-html="getTooltipUsername(idp)"></span>
                      <template v-if="idp.domainNote">
                        <br />
                        <span class="tooltip-note">
                          Note: The username will already contain an '@domain'
                          that the '{{ idp.alias }}' will be appended to.
                          <br />
                          <span
                            class="tooltip-note"
                            v-if="idp.alias === '@phsa'"
                          >
                            This applies to all Health Authority users, for
                            example: username@interiorhealth.ca@phsa or
                            username@phsa.ca@phsa
                          </span>
                        </span>
                      </template>
                    </template>
                  </li>
                </ul>
              </span>
            </v-tooltip>
            <v-text-field
              density="compact"
              :disabled="!!userId || !editUserDetailsPermission"
              variant="outlined"
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
              density="compact"
              variant="outlined"
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
              density="compact"
              variant="outlined"
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
              density="compact"
              variant="outlined"
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
              density="compact"
              variant="outlined"
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
              density="compact"
              variant="outlined"
            ></v-autocomplete>

            <label :disabled="!editUserDetailsPermission" for="notes">
              Notes
            </label>
            <v-textarea
              variant="outlined"
              density="compact"
              id="notes"
              :disabled="!editUserDetailsPermission"
              v-model="user.attributes.access_team_notes"
              maxlength="255"
            />
          </v-form>
        </v-col>
        <v-col
          cols="4"
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
                {{ formatIdp(identity.identityProvider) }} [{{
                  identity.userName
                }}]
              </span>
              <v-tooltip location="right">
                <template v-slot:activator="{ props }">
                  <v-btn
                    class="idp-link-button"
                    icon
                    size="small"
                    v-bind="props"
                    @click="
                      openResetIdentityProviderLinkDialog(
                        identity.identityProvider
                      )
                    "
                  >
                    <v-icon size="small" style="vertical-align: middle">
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
        class="bg-primary"
        size="default"
        @click="updateUser"
      >
        {{ updateOrRegister }}
      </v-btn>
    </v-card>
    <v-dialog v-model="dialog" content-class="resetUserIdentityLinksDialog">
      <v-card>
        <v-card-title>
          <span class="text-h5">Identity Provider Link Reset Confirmation</span>
        </v-card-title>
        <v-card-text>
          <br />
          Are you sure you want to reset the
          {{ formatIdp(this.selectedIdentityProvider) }} linked identity for
          this user?
          <br />
          <br />
          Resetting a linked identity will retain all existing roles and details
          for the user and the identity will be re-linked upon the user's next
          Keycloak login event.
        </v-card-text>
        <v-card-actions>
          <v-btn class="bg-primary" @click="resetIdentityProviderLink">
            Reset Identity Provider Link
          </v-btn>
          <v-btn
            variant="outlined"
            class="text-primary"
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
    props: ["userId", "updateOrRegister"],
    data() {
      return {
        identityProviders: [
          { name: "IDIR", suffix: true, alias: "@idir" },
          { name: "Business BCeID", suffix: true, alias: "@bceid_business" },
          {
            name: " BC Provider",
            suffix: true,
            alias: "@bcp",
            domainNote: true,
          },
          { name: "BC Services Card", suffix: true, alias: "@bcsc" },
          { name: "FNHA", suffix: true, alias: "@fnha", domainNote: true },
          {
            name: "Health Authority ID",
            suffix: true,
            alias: "@phsa",
            domainNote: true,
          },
        ],
        organizations: this.$organizations.map((item) => {
          item.value = `{"id":"${item.organizationId}","name":"${item.name}"}`;
          item.title = `${item.organizationId} - ${item.name}`;
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
      userAttributes() {
        return this.$store.state.user.attributes;
      },
    },
    watch: {
      userAttributes: {
        handler(updatedAttributes) {
          if ("sfds_auth_1" in updatedAttributes) {
            this.user.attributes.sfds_auth_1 = updatedAttributes.sfds_auth_1;
          }
          if ("sfds_auth_2" in updatedAttributes) {
            this.user.attributes.sfds_auth_2 = updatedAttributes.sfds_auth_2;
          }
          if ("sfds_auth_3" in updatedAttributes) {
            this.user.attributes.sfds_auth_3 = updatedAttributes.sfds_auth_3;
          }
          if ("sfds_auth_4" in updatedAttributes) {
            this.user.attributes.sfds_auth_4 = updatedAttributes.sfds_auth_4;
          }
          if ("sfds_auth_5" in updatedAttributes) {
            this.user.attributes.sfds_auth_5 = updatedAttributes.sfds_auth_5;
          }
        },
        deep: true,
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
      },
      resetIdentityProviderLink: function () {
        const userIdIdpRealm = this.user.federatedIdentities.find(
          (fi) => fi.identityProvider === this.selectedIdentityProvider
        ).userId;
        UsersRepository.resetUserIdentityProviderLink(
          this.userId,
          this.selectedIdentityProvider.split(",")[0],
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
            this.filterFederatedIdentities();

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
      updateUser: async function () {
        // Validate the User Details
        const { valid: isFormValid } = await this.$refs.form.validate();
        if (!isFormValid) {
          this.$store.commit("alert/setAlert", {
            message: "Please correct errors before submitting",
            type: "error",
          });
          window.scrollTo(0, 0);
          return;
        }
        this.$emit("submit-user-updates", this.user);
      },
      filterFederatedIdentities: function () {
        if (this.user.federatedIdentities) {
          const isBcscLike = (fi) => fi.identityProvider?.includes("bcsc");

          let bcscLikeIdentities = this.user.federatedIdentities
            .filter(isBcscLike)
            .map((fi) => fi.identityProvider);

          let bcscLikeCounter = 0;

          this.user.federatedIdentities = this.user.federatedIdentities.filter(
            (fi) => {
              if (isBcscLike(fi)) {
                bcscLikeCounter++;
                fi.identityProvider = bcscLikeIdentities.join();
                return bcscLikeCounter <= 1;
              }
              return true;
            }
          );
        }
      },
      getTooltipUsername: function (identityProvider) {
        return `username${identityProvider.alias.bold()}`;
      },
      formatIdp: function (idp) {
        let formattedIdentityProviders = {
          bceid_business: "BCeID Business",
          bcprovider_aad: "BC Provider",
          fnha_aad: "FNHA MFA",
          idir: "IDIR",
          idir_aad: "IDIR MFA",
          moh_idp: "Keycloak",
          phsa: "Health Authority ID",
          phsa_aad: "Health Authority ID MFA",
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
  .idp-link-button {
    box-shadow: none;
  }
</style>
