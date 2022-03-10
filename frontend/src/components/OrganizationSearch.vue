<!--suppress XmlInvalidId -->
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
          placeholder="Organization or ID"    
          @keyup.enter="searchOrganization('&search='+organizationSearchInput.replaceAll('\\','%5C'))"     
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
          :search = "organizationSearchInput"
          :headers="headers"
          :items="searchResults"
          :footer-props="footerProps"
          :loading="organizationSearchLoadingStatus"
          loading-text="Searching for organizations"
        >
          <!-- https://stackoverflow.com/questions/61394522/add-hyperlink-in-v-data-table-vuetify -->
          <template #item.id="{ item }">
              {{ item.id }}
          </template>
        </v-data-table>
      </v-col>
    </v-row>

  </div>
</template>

<script>
import OrganizationsRepository from "@/api/OrganizationsRepository";
import app_config from '@/loadconfig';
    
export default {
  name: "OrganizationSearch",
  data() {
    return {
      headers: [
        { text: "ID", value: "id", class: "table-header" },
        { text: "Name", value: "name", class: "table-header" },
      ],
      footerProps: { "items-per-page-options": [15] },
      searchResults: [],
      organizationSearchInput: "",
      organizationSearchLoadingStatus: false,
      newTab: false,
    };
  },
  async created() {
    this.searchOrganization();
  },
  computed: {
    maxResults() {
      return app_config.config.max_results ? app_config.config.max_results : 100;
    },
    // todo: note: this checks to see if user has permission to create users, not create orgs
    hasCreateOrganizationRole: function() {
      const umsClientId = "USER-MANAGEMENT-SERVICE";
      const createUserRoleName = "create-user";
      return !!this.$keycloak.tokenParsed.resource_access[umsClientId].roles.includes(createUserRoleName)
    }
  },
  methods: {
    searchOrganization : async function() {
        try {
            let results = (await OrganizationsRepository.get()).data;
            this.setSearchResults(results)
        }
        catch (error) {
            this.handleError("organization search failed", error);
        }
        finally {
        this.organizationSearchLoadingStatus = false;
      }
    },
    setSearchResults: function (results) {
        const maxRes = this.maxResults;
      if (results.length > maxRes) {
        this.searchResults = results.slice(0, maxRes);
        this.$store.commit("alert/setAlert", {
          message: "Your search returned more than the maximum number of results ("
                  + maxRes + "). Please consider refining the search criteria.",
          type: "warning"
        });
        window.scrollTo(0, 0);
      }
      else {
        this.searchResults = results;
      }
    }, 
    goToCreateOrganization: function() {
      this.$store.commit("alert/dismissAlert");
      this.$router.push({ name: "OrganizationCreate"});
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
#search-button {
  margin-top: 25px;
  margin-left: 20px;
}
#create-organization-button {
  float: right;
  margin-top: 25px;
}
</style>
