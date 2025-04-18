<template>
  <div>
    <!-- Basic Search -->
    <v-row no-gutters>
      <v-col class="col-6">
        <label for="organization-search">
          Search
          <v-tooltip location="right">
            <template v-slot:activator="{ props }">
              <v-icon v-bind="props" size="x-small" class="help-icon">
                mdi-help-circle
              </v-icon>
            </template>
            <span>Search by organization name or ID</span>
          </v-tooltip>
        </label>

        <v-text-field
          id="organization-search"
          variant="outlined"
          density="compact"
          v-model.trim="organizationSearchInput"
          placeholder="Organization ID"
        />
      </v-col>
      <v-col class="col-6">
        <v-btn
          id="create-organization-button"
          class="bg-success"
          size="default"
          @click="goToCreateOrganization"
        >
          Create New organization
        </v-btn>
      </v-col>
    </v-row>
    <!-- table -->
    <v-data-table
      density="compact"
      id="organizations-table"
      class="base-table select-table"
      :headers="headers"
      :search="organizationSearchInput"
      :items="organizations"
      :footer-props="footerProps"
      :loading="organizationSearchLoadingStatus"
      loading-text="Searching for organizations"
    >
      <template #item.actions="{ item }">
        <v-icon size="small" @click="openEditOrganizationDialog(item)">
          mdi-pencil
        </v-icon>
      </template>
    </v-data-table>

    <v-dialog content-class="updateOrganizationDialog" v-model="dialog">
      <v-card>
        <v-card-title>
          <span style="float: left" class="text-h5">Edit organization</span>
        </v-card-title>
        <v-card-text>
          <v-form ref="form">
            <label for="id" class="required">Organization ID</label>
            <v-text-field
              disabled
              density="compact"
              variant="outlined"
              id="ID"
              v-model.trim="organizationToEdit.organizationId"
            ></v-text-field>

            <label for="name" class="required">Organization Name</label>
            <v-text-field
              density="compact"
              variant="outlined"
              id="name"
              v-model.trim="organizationToEdit.name"
              required
              :rules="[
                (v) => !!v || 'Organization Name is required',
                (v) => (v && !!v.trim()) || 'Organization Name cannot be blank',
              ]"
            />
          </v-form>
        </v-card-text>
        <v-card-actions>
          <v-btn
            id="save-organization"
            class="bg-primary"
            size="default"
            v-on:click="validateOrganizationToBeSaved()"
          >
            Save Changes
          </v-btn>
          <v-btn variant="outlined" class="text-primary" @click="close()">
            Cancel
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
  import OrganizationsRepository from "../api/OrganizationsRepository";
  export default {
    name: "OrganizationsSearch",
    data() {
      return {
        dialog: false,
        headers: [
          {
            title: "ID",
            value: "organizationId",
            class: "table-header",
            sortable: true,
          },
          {
            title: "Name",
            value: "name",
            class: "table-header",
            sortable: true,
          },
          // { text: "Actions", value: "actions", class: "table-header"} disabled until further talks about org edit
        ],
        organizations: this.$organizations,
        organizationToEdit: {
          organizationId: "",
          name: "",
        },
        footerProps: { "items-per-page-options": [15] },
        searchResults: [],
        organizationSearchInput: "",
        organizationSearchLoadingStatus: false,
        newTab: false,
      };
    },
    methods: {
      openEditOrganizationDialog: function (org) {
        this.dialog = true;
        Object.assign(this.organizationToEdit, org);
      },
      close: function () {
        this.dialog = false;
      },
      updateOrganizationsInApplicationMemory: function () {
        const indexOfUpdatedOrg = this.$organizations.findIndex(
          (org) => org.organizationId === this.organizationToEdit.organizationId
        );
        this.$organizations.splice(
          indexOfUpdatedOrg,
          1,
          this.organizationToEdit
        );
        this.organizationToEdit = { organizationId: "", name: "" };
        this.organizations = this.$organizations;
      },
      validateOrganizationToBeSaved: async function () {
        const { valid: isFormValid } = await this.$refs.form.validate();
        if (!isFormValid) {
          this.$store.commit("alert/setAlert", {
            message: "Please correct errors before submitting",
            type: "error",
          });
          window.scrollTo(0, 0);
          return;
        }
        this.updateOrganization();
      },
      updateOrganization: function () {
        OrganizationsRepository.updateOrganization(
          this.organizationToEdit.organizationId,
          this.organizationToEdit
        )
          .then(() => {
            this.$store.commit("alert/setAlert", {
              message: "Organization updated successfully",
              type: "success",
            });
            this.close();
            this.updateOrganizationsInApplicationMemory();
          })
          .catch((error) => {
            this.$store.commit("alert/setAlert", {
              message: "Error updating organization: " + error,
              type: "error",
            });
          })
          .finally(() => {
            window.scrollTo(0, 0);
          });
      },
      goToCreateOrganization: function () {
        this.$store.commit("alert/dismissAlert");
        this.$router.push({ name: "OrganizationsCreate" });
      },
    },
  };
</script>

<style>
  #create-organization-button {
    float: right;
    margin-top: 25px;
  }
  .updateOrganizationDialog {
    margin: 24px;
    overflow-y: auto;
    overflow-x: hidden;
    max-width: 95%;
    min-width: 450px;
    width: 900px;
    z-index: inherit;
  }
  .v-btn {
    text-transform: none;
    font-weight: 600;
  }
</style>
