<template>
    <div>
      <v-text-field
          placeholder="User ID"
          v-model="searchUserId"
      />
      <v-text-field
          placeholder="Application"
      v-model="searchApplication"
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
          placeholder="Filter"
        ></v-text-field>
        <v-data-table
                :headers="headers"
                :items="events"
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
              <pre>User ID: {{ item.userId }}</pre>
            </td>
          </template>
        </v-data-table>
      <button @click="getAllEvents">Refresh</button>
    </div>
</template>

<script>
    import EventsRepository from "../api/EventsRepository";
    import UsersRepository from "../api/UsersRepository";

    const options = {dateStyle: 'short', timeStyle: 'short'};
    const formatDate = new Intl.DateTimeFormat(undefined, options).format;

    export default {
      name: "EventLog",
      data() {
        return {
          searchUserId: '',
          searchApplication: '',
          searchDateFrom: '',
          searchDateTo: '',
          filterEvents: '',
          loadingStatus: false,
          singleExpand: true,
          events: [],
          headers: [
            {text: 'Time', value: 'readableDate'},
            {text: 'User', value: 'username'},
            {text: 'Event type', value: 'type'},
            {text: 'Application', value: 'clientId'},
            {text: 'Details', value: 'data-table-expand'},
          ],
        }
      },

      created() {
        this.getAllEvents();
      },

      methods: {
        searchEvents: function () {
          const params = new URLSearchParams();
          [
            {name: 'user', value: this.searchUserId},
            {name: 'client', value: this.searchApplication},
            {name: 'dateFrom', value: this.searchDateFrom},
            {name: 'dateTo', value: this.searchDateTo},
          ].map(param => {
            if (param.value) params.append(param.name, param.value);
          });

          this.getEvents(() => EventsRepository.getEvents(params.toString()));
        },
        getAllEvents: function () {
          this.getEvents(EventsRepository.getEvents);
        },
        getEvents: async function (getEvents) {
          this.loadingStatus = true;
          try {
            let promise = await getEvents();
            this.events = promise.data;
            for (let [index, e] of this.events.entries()) {
              e.key = index;
              e.readableDate = formatDate(e.time);
              if (!e.details) {
                e.details = 'no details';
              }
            }
            await UsersRepository.addUsernamesToEvents(this.events);
          } finally {
            this.loadingStatus = false;
          }
        }
      }
    }
</script>