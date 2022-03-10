<!--suppress XmlInvalidId -->
<template>
  <div id="organization">
    <v-card outlined class="subgroup">
      <h2>Organization Details</h2>
      <v-row no-gutters>
        <v-col class="col-7">
          <v-form ref="form">
            <label for="id" class="required">Organization ID</label>
            <v-text-field
              dense
              outlined
              id="ID"
              v-model="organization.id"
              required
              :rules="[
              v => !!v || 'ID is required',
              v => /^\d{8}$/.test(v) || 'ID must be made of 8 numerical characters',
              ]"
            />

            <label for="first-name" class="required">Organization Name</label>
            <v-text-field
              dense
              outlined
              id="first-name"
              v-model="organization.name"
              required
              :rules="[v => !!v || 'Organization Name is required']"
            />
          </v-form>
        </v-col>
      </v-row>
      <v-btn id="submit-button" class="primary" medium @click="updateOrganization">{{ updateOrCreate }} Organization</v-btn>
    </v-card>
  </div>
</template>

<script>

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
  methods: {
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
