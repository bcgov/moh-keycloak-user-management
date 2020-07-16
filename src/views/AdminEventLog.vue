<!--suppress XmlInvalidId -->
<template>
    <div>
      <div id="search-inputs" @keyup.enter="searchEvents" >
        <label for="user-id">Keycloak User ID</label>
        <v-text-field
            id="user-id"
            dense
            outlined
            v-model="searchUserId"
        />
        <label for="app-id">Application ID</label>
        <v-text-field
            id="app-id"
            dense
            outlined
            v-model="searchClientId"
        />
        <label for="admin-id">Administrator ID</label>
        <v-text-field
            id="admin-id"
            dense
            outlined
            v-model="searchAdministratorId"
        />
        <label for="from-date">Date (from)</label>
        <v-text-field
            id="from-date"
            dense
            outlined
            hint="yyyy-MM-dd"
            v-model="searchDateFrom"
        />
        <label for="to-date">Date (to)</label>
        <v-text-field
            id="to-date"
            dense
            outlined
            hint="yyyy-MM-dd"
            v-model="searchDateTo"
        />
      </div>

      <v-btn id="search-button" class="secondary" medium @click.native="searchEvents">Search</v-btn>

      <h1 style="margin-bottom: 10px;">Search Results</h1>

      <v-text-field
          v-model="filterEvents"
          append-icon="mdi-magnify"
          placeholder="Filter Results"
      ></v-text-field>
        <v-data-table
                :headers="headers"
                :items="adminEvents"
                :items-per-page="15"
                class="base-table"
                show-expand
                :single-expand="singleExpand"
                item-key="key"
                loading-text="Loading events"
                :loading="loadingStatus"
                :search="filterEvents"
        >
            <template v-slot:expanded-item="{ headers, item }">
                <td :colspan="headers.length">
                  <pre>User ID: {{ item.userId }}</pre>
                  <pre>Application ID: {{ item.clientId }}</pre>
                  <pre>Data: {{item.representation | pretty}}</pre>
                  <pre>Administrator: {{item.authDetails}}</pre>
                </td>
            </template>

        </v-data-table>
        <button @click="getAllEvents">Refresh</button>
    </div>
</template>

<script>
import AdminEventsRepository from "../api/AdminEventsRepository";
import UsersRepository from "../api/UsersRepository";
import ClientsRepository from "../api/ClientsRepository";

const options = {dateStyle: 'short', timeStyle: 'short'};
    const formatDate = new Intl.DateTimeFormat(undefined, options).format;

    export default {
        name: "AdminEventLog",
        data() {
            return {
                searchUserId: '',
                searchClientId: '',
                searchAdministratorId: '',
                searchDateFrom: '',
                searchDateTo: '',
                filterEvents: '',
                loadingStatus: false,
                singleExpand: true,
                adminEvents: [],
                headers: [
                    { text: 'Time', value: 'readableDate'},
                    { text: 'Administrator', value: 'authDetails.fullName'},
                    { text: 'Event type', value: 'operationType' },
                    { text: 'Resource type', value: 'resourceType' },
                    { text: 'Username', value: 'username' },
                    { text: 'Application', value: 'clientName' },
                    { text: 'Details', value: 'data-table-expand' },
                ],
            }
        },

        created() {
            this.getAllEvents();
        },

        methods: {
          searchEvents: function () {
            const params = buildQueryParameters.call(this);
            this.getEvents(() => AdminEventsRepository.getEvents(params));
          },
          getAllEvents: function () {
            this.getEvents(AdminEventsRepository.getEvents);
          },
          getEvents: async function(getEvents) {
            this.loadingStatus = true;
            try {
              let promise = await getEvents();
              this.adminEvents = promise.data;
              for (let [index, e] of this.adminEvents.entries()) {
                e.key = index;
                e.readableDate = formatDate(e.time);
                // Assumes format users/nnnnnnnn-nnnn-nnnn-nnnn-nnnnnnnnnnnn
                // If we ever display non-users path events, we'll need to change this.
                e.userId = e.resourcePath.substring(6, 42);
                // CREATE and UPDATE USER events aren't associated with an application, so check for the "clients" path before extracting it.
                if (e.resourcePath.includes('role-mappings/clients')) {
                  // Assumes format users/.../role-mappings/clients/nnnnnnnn-nnnn-nnnn-nnnn-nnnnnnnnnnnn/
                  e.clientId = e.resourcePath.substring(65, 101);
                }
              }
              await ClientsRepository.addClientNamesToEvents(this.adminEvents);
              await UsersRepository.addUsernamesToEvents(this.adminEvents);
              await UsersRepository.addAdminNamesToEvents(this.adminEvents);
            } finally {
              this.loadingStatus = false;
            }

          }
        },

        filters: {
            pretty: function(value) {
                if (!value) {
                    return 'No further details available';
                }
                return JSON.stringify(JSON.parse(value), null, 2);
            }
        }
    };


    function buildQueryParameters() {
      const params = new URLSearchParams();
      if (this.searchUserId && this.searchClientId) {
        params.append('resourcePath', `users/${this.searchUserId}/role-mappings/clients/${this.searchClientId}/`);
      } else if (this.searchUserId) {
        params.append('resourcePath', `users/${this.searchUserId}*`);
      } else if (this.searchClientId) {
        params.append('resourcePath', `*role-mappings/clients/${this.searchClientId}*`);
      }
      if (this.searchAdministratorId) {
        params.append('authUser', this.searchAdministratorId);
      }
      [
        {name: 'dateFrom', value: this.searchDateFrom},
        {name: 'dateTo', value: this.searchDateTo},
      ].map(param => {
        if (param.value) params.append(param.name, param.value);
      });
      return params;
    }
</script>

<style scoped>
#search-button {
  margin: 0px 0px 25px 0px
}
</style>