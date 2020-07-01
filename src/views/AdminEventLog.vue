<template>
    <div>
      <v-text-field
          placeholder="User ID"
          v-model="searchUserId"
      />
      <v-text-field
          placeholder="Application ID"
          v-model="searchClientId"
      />
      <v-text-field
          placeholder="Date (from)"
          hint="yyyy-MM-dd"
          v-model="searchDateFrom"
      />
      <v-text-field
          placeholder="Date (to)"
          hint="yyyy-MM-dd"
          v-model="searchDateTo"
      />
      <v-btn id="search-button" class="secondary" medium @click.native="searchEvents">Search</v-btn>
      <v-text-field
          v-model="filterEvents"
          append-icon="mdi-magnify"
          label=" Search"
      ></v-text-field>
        <v-data-table
                :headers="headers"
                :items="adminEvents"
                :items-per-page="15"
                class="elevation-1"
                show-expand
                :single-expand="singleExpand"
                item-key="key"
                loading-text="Loading events"
                :loading="loadingStatus"
                :search="filterEvents"
        >
            <template v-slot:expanded-item="{ headers, item }">
                <td :colspan="headers.length">
                  <pre>Data: {{item.representation | pretty}}</pre>
                  <pre>User ID: {{ item.userId }}</pre>
                  <pre>Client ID: {{ item.clientId }}</pre>
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
                searchDateFrom: '',
                searchDateTo: '',
                filterEvents: '',
                loadingStatus: false,
                singleExpand: true,
                adminEvents: [],
                headers: [
                    { text: 'Time', value: 'readableDate'},
                    { text: 'Event type', value: 'operationType' },
                    { text: 'Resource type', value: 'resourceType' },
                    { text: 'User', value: 'username' },
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
      [
        {name: 'dateFrom', value: this.searchDateFrom},
        {name: 'dateTo', value: this.searchDateTo},
      ].map(param => {
        if (param.value) params.append(param.name, param.value);
      });
      return params;
    }
</script>