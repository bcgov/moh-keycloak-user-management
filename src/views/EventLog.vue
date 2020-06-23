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
    import {RepositoryFactory} from "./../api/RepositoryFactory";

    const EventsRepository = RepositoryFactory.get("events");

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
            getEvents: function () {
                this.loadingStatus = true;
                return EventsRepository.getEvents()
                    .then(response => {
                        this.events = response.data;
                        // TODO What events should we show? All that are recorded? It might make sense not to. Perhaps we want full audit enabled on Keycloak, but the Access Team is only interested in (and understands) a smaller set of events.
                        // this.events = this.events.filter(a => a.type === 'LOGIN');
                        for (let e of this.events) {
                            e.readableDate = formatDate(e.time);
                            if (!e.details) {
                                e.details = 'no details';
                            }
                        }
                    })
                    .catch(e => {
                        console.log(e);
                        throw e;
                    })
                    .finally(() => this.loadingStatus = false);
            }
        }
    };
</script>