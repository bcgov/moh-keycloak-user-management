<template>
    <div>
        <button @click="getEvents">Events</button> |
        <button @click="getAdminEvents">Admin Events</button>
        <ul>
            <li v-for="(event, index) in events" :key="index">
                {{event.niceDate}} | {{ event.type }} | {{ event.userId }} | {{event.details.username}}
            </li>
        </ul>
        <ul>
            <li v-for="(event, index) in adminEvents" :key="index">
                {{event.niceDate}} | {{event.operationType}} | {{ event.resourceType }}
            </li>
        </ul>
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
                adminEvents: []
            }
        },

        methods: {
            getEvents: function () {
                return EventsRepository.getEvents()
                    .then(response => {
                        this.events = response.data;
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