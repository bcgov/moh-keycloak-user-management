<template>
    <div>
      <!-- Basic Search -->
      <v-row no-gutters>
        <v-col class="col-6">
          <label for="organization-search" >
            Search
            <v-tooltip right>
              <template v-slot:activator="{ on }">
                <v-icon v-on="on" small>mdi-help-circle</v-icon>
              </template>
              <span>Search by organization name or ID</span>
            </v-tooltip>
          </label>
  
          <v-text-field
            id="organization-search"
            outlined
            dense
            v-model="organizationSearchInput"
            placeholder="Organization ID"     
          />
        </v-col>
        <v-col class="col-6">
          <v-btn id="create-organization-button" class="success" medium @click.native="goToCreateOrganization">Create New organization</v-btn>
        </v-col>
      </v-row>
  
      <!-- table -->
      <v-row no-gutters>
        <v-col class="col-12">
          <v-data-table
            id="organizations-table"
            class="base-table select-table"
            :headers="headers"
            :search = "organizationSearchInput"
            :items="organizations"
            :footer-props="footerProps"
            :loading="organizationSearchLoadingStatus"
            loading-text="Searching for organizations"
          >
            <!-- https://stackoverflow.com/questions/61394522/add-hyperlink-in-v-data-table-vuetify -->
            <template #item.id="{ item }">
                {{ item.id }}
            </template>
            <template #item.actions="{ item }">
              <v-icon small @click="editOrganization(item)">
                mdi-pencil
              </v-icon>
              <v-dialog content-class="updateRolesDialog" v-model="dialog">
              <v-card>
               <v-card-title>
                <span class="headline">Edit organization</span>
              </v-card-title>
              <v-card-text>
             
              <label style="padding-left: 12px;" class="required" for="select-client">Organization name</label>
              <v-icon style="float: right" @click="close()">mdi-close</v-icon>
             
              </v-card-text>
              <v-card-actions >
              <v-btn
                      id="save-oeganization"
                      class="primary"
                      medium
                      v-on:click="updateOrganization()"
                      >Save Changes</v-btn
                    >
              <v-btn outlined class="primary--text" @click="close()">
                Cancel
              </v-btn>
            </v-card-actions>
            </v-card>
            </v-dialog>
            </template>
           
           
          </v-data-table>
        </v-col>
      </v-row>
  
    </div>
  </template>
  
  <script>
  
  export default {
    name: "OrganizationsSearch",
    data() {
      return {
        dialog: false,
        organizations: this.$organizations
          .map((item) => {
            item.value = `{"id":"${item.organizationId}","name":"${item.name}"}`
            item.text = `${item.organizationId} - ${item.name}`;
            return item;
          }),
        headers: [
          { text: "ID", value: "organizationId", class: "table-header" },
          { text: "Name", value: "name", class: "table-header" },
          { text: "Actions", value: "actions", class: "table-header"}
        ],
        footerProps: { "items-per-page-options": [15] },
        searchResults: [],
        organizationSearchInput: "",
        organizationSearchLoadingStatus: false,
        newTab: false,
      };
    },
    methods: {
      editOrganization: function (organization) {
        console.log(organization);
      this.dialog = true;
      console.log(this.dialog);
    },
    close: function() {
      this.dialog = false;
    },
      goToCreateOrganization: function() {
        this.$store.commit("alert/dismissAlert");
        this.$router.push({ name: "OrganizationsCreate"});
      },
      handleError(message, error) {
        this.$store.commit("alert/setAlert", {
          message: message + ": " + error,
          type: "error"
        });
        window.scrollTo(0, 0);
      }
    }
  };
  </script>
  
  <style scoped>
  #create-organization-button {
    float: right;
    margin-top: 25px;
  }
  .updateOrganizationssDialog {
    margin: 24px;
    overflow-y: auto;
    overflow-x: hidden;
    max-width: 95%;
    min-width: 450px;
    width: 900px;
    z-index: inherit;
}
  </style>