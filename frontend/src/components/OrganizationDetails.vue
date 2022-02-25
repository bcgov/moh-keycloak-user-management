<!--suppress XmlInvalidId -->
<template>
  <div id="organization">
    <v-card outlined class="subgroup">
      <h2>Organization Details</h2>
      <v-row no-gutters>
        <v-col class="col-7">
          <v-form ref="form">
            <label for="id" :disabled=" !!organizationId || !editUserDetailsPermission" class="required">Organization ID</label>
            <v-text-field
              dense
              :disabled="!!organizationId ||!editUserDetailsPermission"
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
      <v-btn id="submit-button"  v-if="editUserDetailsPermission && !organizationId" class="primary" medium @click="updateOrganization">{{ updateOrCreate }} Organization</v-btn>
    </v-card>
  </div>
</template>

<script>
import OrganizationsRepository from "@/api/OrganizationsRepository";


export default {
  name: "OrganizationDetails",
  props: ['organizationId', 'updateOrCreate'],
  data() {
    return {

      organization: {
        id: '',
        name: '',
      },
    };
  },
  async created() {
    if(this.organizationId){
      await this.getOrganization();
      }
  },
  computed: {
    // todo: this checks permission to edit users, not orgs
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
      OrganizationsRepository.getOrganization(this.organizationId)
        .then(response => {
          console.log(response.data);
          this.organization = response.data;
        })
        .catch(e => {
          console.log(e);
        });
    },
    updateOrganization: function() {
      // Validate the Organization Details
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
</style>
