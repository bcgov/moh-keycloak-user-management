<!--suppress XmlInvalidId -->
<template>
  <div id="organization">
    <v-card outlined class="subgroup">
      <h2>Organization Details</h2>
      <v-row no-gutters>
        <v-col class="col-7">
          <v-form ref="form">
            <label for="id" :disabled="!!id || !editUserDetailsPermission" class="required">Organization ID</label>
            <v-text-field
              dense
              :disabled="!editUserDetailsPermission"
              outlined
              id="ID"
              v-model="organization.id"
              required
              :rules="[
              v => !!v || 'ID is required',
              v => /^\d{8}$/.test(v) || 'ID must be made of 8 numerical characters',
              ]"
            />

            <label :disabled="!editUserDetailsPermission" for="first-name" class="required">Organization Name</label>
            <v-text-field
              dense
              outlined
              :disabled="!editUserDetailsPermission"
              id="first-name"
              v-model="organization.name"
              required
              :rules="[v => !!v || 'Organization Name is required']"
            />
          </v-form>
        </v-col>
      </v-row>
      <v-btn id="submit-button" v-if="editUserDetailsPermission" class="primary" medium @click="updateOrganization">{{ updateOrCreate }} Organization</v-btn>
    </v-card>
  </div>
</template>

<script>
import organizations from "@/assets/organizations";


export default {
  name: "OrganizationDetails",
  props: ['id', 'updateOrCreate'],
  data() {
    return {

      organization: {
        id: '',
        name: '',
      },
      organizations :organizations,
    };
  },
  async created() {
    //Create global ref to allow role update from UserUpdateRoles
    this.$root.$refs.UserDetails = this;
    // TODO error handling
    this.$store.commit("user/resetState");
    if (this.id) {
      await this.getOrganization();
    }
  },
  computed: {
    editUserDetailsPermission: function() {
      const onCreateUserPage = !this.id;

      const umsClientId = "USER-MANAGEMENT-SERVICE";
      const manageUserDetailsRoleName = "manage-user-details";
      const createUserRoleName = "create-user";

      const hasManageUserDetails = this.$keycloak.tokenParsed.resource_access[umsClientId].roles.includes(manageUserDetailsRoleName);
      const hasCreateUser = this.$keycloak.tokenParsed.resource_access[umsClientId].roles.includes(createUserRoleName);

      if (onCreateUserPage && hasCreateUser) {
        return true
      } else if (!onCreateUserPage && hasManageUserDetails) {
        return true
      }
      return false
    }
  },
  methods: {
    getOrganization: function() {
      console.log("getting Org");
    },
    updateOrganization: function() {
      // Validate the User Details
      if (!this.$refs.form.validate()) {
        this.$store.commit("alert/setAlert", {
          message: "Please correct errors before submitting",
          type: "error"
        });
        window.scrollTo(0, 0);
        return;
      }
      this.$emit('submit-organization-updates', this.organization)
    }
  },
};
</script>

<style scoped>
#user-name-tooltip-icon {
  margin-left: 10px;
}
</style>
