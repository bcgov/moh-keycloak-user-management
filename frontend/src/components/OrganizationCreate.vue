<!--suppress XmlInvalidId -->
<template>
  <div id="organization">
    <h1>Create New Organization</h1>
    <organization-details ref="organizationDetails" update-or-create="Create" @submit-organization-updates="createOrganization"></organization-details>
  </div>
</template>

<script>
import OrganizationRepository from "@/api/OrganizationsRepository";
import OrganizationDetails from "@/components/OrganizationDetails.vue";

export default {
  name: "OrganizationCreate",
  components: {
    OrganizationDetails
  },
  methods: {
    createOrganization: function(organizationDetails) {
        OrganizationRepository.createOrganization(organizationDetails)
            .then(() => {
          this.$store.commit("alert/setAlert", {
            message: "Organization updated successfully",
            type: "success"
          });
        })
          .catch(error => {
          this.$store.commit("alert/setAlert", {
            message: "Error updating organization: " + error,
            type: "error"
          });
        })
        .finally(() => {
          window.scrollTo(0, 0);
        });
    }
  }
};
</script>