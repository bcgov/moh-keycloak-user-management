<template>
    <div>
        <button @click="getAdminEvents">Refresh</button>

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
        >
            <template v-slot:expanded-item="{ headers, item }">
                <td :colspan="headers.length"><pre>{{item.representation | pretty}}</pre></td>
            </template>
        </v-data-table>
    </div>
</template>

<script>
    import {RepositoryFactory} from "./../api/RepositoryFactory";

    const AdminEventsRepository = RepositoryFactory.get("adminEvents");

    const options = {dateStyle: 'short', timeStyle: 'short'};
    const formatDate = new Intl.DateTimeFormat(undefined, options).format;

    export default {
        name: "AdminEventLog",
        data() {
            return {
                loadingStatus: false,
                singleExpand: true,
                adminEvents: [],
                headers: [
                    { text: 'Time', value: 'readableDate'},
                    { text: 'Event type', value: 'operationType' },
                    { text: 'User', value: 'resourcePath' },
                    // { text: 'Data', value: 'representation' },
                ],
            }
        },

        created() {
            this.getAdminEvents();
        },

        methods: {
            getAdminEvents: function () {
                this.loadingStatus = true;
                return AdminEventsRepository.getEvents()
                    .then(response => {
                        this.adminEvents = response.data;
                        this.adminEvents = this.adminEvents.filter(value => value.resourceType === 'USER');
                        for (let [index, e] of this.adminEvents.entries()) {
                            e.key = index;
                            e.readableDate = formatDate(e.time);
                        }
                    })
                    .catch(e => {
                        console.log(e);
                        throw e;
                    })
                    .finally(() => this.loadingStatus = false);
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