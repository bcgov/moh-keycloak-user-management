<template>
    <div>
        <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="Search"
        ></v-text-field>
        <v-data-table
                :headers="headers"
                :items="events"
                :items-per-page="15"
                class="elevation-1"
                loading-text="Loading events"
                :loading="loadingStatus"
                :search="search"
        ></v-data-table>
      <button @click="getEvents">Refresh</button>
    </div>
</template>

<script>
    import EventsRepository from "../api/EventsRepository";

    const options = {dateStyle: 'short', timeStyle: 'short'};
    const formatDate = new Intl.DateTimeFormat(undefined, options).format;

    export default {
        name: "EventLog",
        data() {
            return {
                search: '',
                loadingStatus: false,
                events: [],
                headers: [
                    { text: 'Time', value: 'readableDate'},
                    { text: 'User', value: 'details.username' },
                    { text: 'Event type', value: 'type' },
                    { text: 'Application', value: 'clientId' },
                ],
            }
        },

        created() {
            this.getEvents();
        },

        methods: {
            getEvents: async function () {
              this.loadingStatus = true;
              try {
                let promise = await EventsRepository.getEvents();
                this.events = promise.data;
                for (let e of this.events) {
                  e.readableDate = formatDate(e.time);
                  if (!e.details) {
                    e.details = 'no details';
                  }
                }
              } finally {
                this.loadingStatus = false;
              }
            }
        }
    };
</script>