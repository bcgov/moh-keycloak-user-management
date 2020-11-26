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
        <label for="app-id">Application name</label>
        <v-text-field
            id="app-id"
            dense
            outlined
            v-model="searchApplicationName"
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
              :items="events"
              :items-per-page="15"
              class="base-table"
              show-expand
              :single-expand="singleExpand"
              item-key="key"
              loading-text="Loading events"
              :loading="loadingStatus"
              :search="filterEvents"
              :page.sync="pageValue"
              :expanded.sync="expandedValues"
      >
        <template v-slot:expanded-item="{ headers, item }">
          <td :colspan="headers.length">
            <pre>User ID: {{ item.userId }}</pre>
          </td>
        </template>
      </v-data-table>
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
          searchApplicationName: '',
          searchDateFrom: '',
          searchDateTo: '',
          filterEvents: '',
          loadingStatus: false,
          singleExpand: true,
          events: [],
          headers: [
            {text: 'Time', value: 'readableDate'},
            {text: 'Username', value: 'username'},
            {text: 'Event type', value: 'type'},
            {text: 'Application', value: 'clientId'},
            {text: 'Details', value: 'data-table-expand'},
          ],
          pageValue: 1,
          expandedValues: [],
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
            {name: 'client', value: this.searchApplicationName},
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
            // https://github.com/vuetifyjs/vuetify/issues/10949
            this.pageValue = 1;
            this.expandedValues = [];
          }
        }
      }
    }
</script>

<style scoped>
#search-button {
  margin: 0px 0px 25px 0px
}
</style>