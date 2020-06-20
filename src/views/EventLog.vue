<template>
    <div>
        <button @click="getEvents">Events</button> |
        <button @click="getAdminEvents">Admin Events</button>
        <v-data-table
                :headers="headers"
                :items="events"
                :items-per-page="5"
                class="elevation-1"
        ></v-data-table>
    </div>
</template>

<script>
    import {RepositoryFactory} from "./../api/RepositoryFactory";

    const EventsRepository = RepositoryFactory.get("events");
    const AdminEventsRepository = RepositoryFactory.get("adminEvents");

    const options = {dateStyle: 'short', timeStyle: 'short'};
    const formatDate = new Intl.DateTimeFormat(undefined, options).format;

    export default {
        name: "EventLog",
        data() {
            return {
                events: [],
                adminEvents: [],
                headers: [
                    { text: 'Time', value: 'readableDate'},
                    { text: 'User', value: 'details.username' },
                    { text: 'Event type', value: 'type' },
                    { text: 'Application', value: 'clientId' },
                ],
            }
        },

        methods: {
            getEvents: function () {
                return EventsRepository.getEvents()
                    .then(response => {
                        this.events = response.data;
                        this.events = this.events.filter(a => a.type === 'LOGIN');
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
                    });
            },
            getAdminEvents: function () {
                return AdminEventsRepository.getEvents()
                    .then(response => {
                        this.adminEvents = response.data;
                        for (let e of this.adminEvents) {
                            e.readableDate = formatDate(e.time);
                        }
                    })
                    .catch(e => {
                        console.log(e);
                        throw e;
                    });
            }
        }
    };
</script>