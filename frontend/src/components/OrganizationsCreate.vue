<template>
    <div id="organization">
      <v-card outlined class="subgroup">
        <h2>Organization Create</h2>
        <v-row no-gutters>
          <v-col class="col-7">
            <v-form ref="form">
              <label for="id" class="required">Organization ID</label>
              <v-text-field
                dense
                outlined
                id="ID"
                v-model="organization.organizationId"
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
        <v-btn id="submit-button" class="primary" medium @click="validateOrganization"> Create Organization</v-btn>
      </v-card>
    </div>
  </template>
  
  <script>
  import OrganizationsRepository from "../api/OrganizationsRepository";
  export default {
    name: "OrganizationsCreate",
    data() {
      return {
        organization: {
          organizationId: '',
          name: '',
        },
      };
    },
    methods: {
      validateOrganization: function() {
        if (!this.$refs.form.validate()) {
          this.$store.commit("alert/setAlert", {
            message: "Please correct errors before submitting",
            type: "error"
          });
          window.scrollTo(0, 0);
          return;
        }
        this.createOrganization();
      },
      createOrganization: function() {
          OrganizationsRepository.createOrganization(this.organization)
              .then(() => {
            this.$store.commit("alert/setAlert", {
              message: "Organization created successfully",
              type: "success"
            });
          }).then(() => {
            this.$organizations.push(this.organization)
          })
            .catch(error => {
            this.$store.commit("alert/setAlert", {
              message: "Error creating organization: " + error,
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