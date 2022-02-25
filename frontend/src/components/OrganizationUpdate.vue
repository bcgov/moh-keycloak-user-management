<!--suppress XmlInvalidId -->
<template>
  <div>
    <v-skeleton-loader
        ref="skeleton"
        v-show="!this.$route.params.organizationId"
        type="article, button, article"
    >
    </v-skeleton-loader>
    <div id="organization-info" v-show="this.$route.params.organizationId">
      <h1>Update - {{ this.$route.params.organizationId }}</h1>
      
     <organization-details 
     ref="organizationDetails" 
     :organizationId="this.$route.params.organizationId"
     update-or-create="Update">
     </organization-details>
    </div>
  </div>
</template>

<script>
import OrganizationDetails from "@/components/OrganizationDetails.vue";

export default {
  name: "OrganizationUpdate",
  components: {
    OrganizationDetails,
  },
};
</script>
<style>
.v-skeleton-loader__button {
  margin-left: 16px;
}
</style>