<template>
    <div>
      <v-text-field
          placeholder="User ID"
          v-model="searchUserId"
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
                <td :colspan="headers.length"><pre>{{item.representation | pretty}}</pre></td>
            </template>
        </v-data-table>
        <button @click="getAllEvents">Refresh</button>
    </div>
</template>

<script>
    import AdminEventsRepository from "../api/AdminEventsRepository";

    const options = {dateStyle: 'short', timeStyle: 'short'};
    const formatDate = new Intl.DateTimeFormat(undefined, options).format;

    export default {
        name: "AdminEventLog",
        data() {
            return {
                searchUserId: '',
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
                    { text: 'User', value: 'resourcePath' },
                    { text: 'Details', value: 'data-table-expand' },
                ],
            }
        },

        created() {
            this.getAllEvents();
        },

        methods: {
          searchEvents: function () {
            const params = new URLSearchParams();
            if (this.searchUserId) {
              params.append('resourcePath', `users/${this.searchUserId}*`);
            }
            [
              {name: 'dateFrom', value: this.searchDateFrom},
              {name: 'dateTo', value: this.searchDateTo},
            ].map(param => {
              if (param.value) params.append(param.name, param.value);
            });

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
              }
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
</script>