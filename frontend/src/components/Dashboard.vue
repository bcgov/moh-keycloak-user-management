<template>
  <div>
    <div class="dashboard">
      <div class="column">
        <div class="tile" style="width: 600px">
          <div class="heading">
            <p>Active User Count (Login Event Within 365 Days)</p>
          </div>
          <v-data-table
            id="Active-user-count-table"
            class="tile-table"
            :items="activeUserCount"
            :headers="headerActiveUserCount"
            hide-default-footer
            dense
            no-data-text=""
          />
        </div>
      </div>
      <div class="column">
        <div class="tile">
          <div class="heading">
            <p>Total Number of Users</p>
            <v-tooltip right>
              <template v-slot:activator="{ on }">
                <v-icon v-on="on" small>mdi-help-circle</v-icon>
              </template>
             <p class="tooltip"> Total Unique User Count by IDP</p>
            </v-tooltip>
          </div>
          <p class="single-stat">{{ totalNumberOfUsers }}</p>
        </div>

        <div class="tile">
          <div class="heading">
            <p>Unique User Count (By IDP)</p>
          </div>
          <v-data-table
            id="unique-user-idp-table"
            class="tile-table"
            :items="uniqueUserCountByIDP"
            :headers="headerUniqueUserCountByIDP"
            hide-default-header
            hide-default-footer
            dense
            no-data-text=""
          />
        </div>

        <div class="tile">
          <div class="heading">
            <p>Unique User Count (By Realm)</p>
          </div>
          <v-data-table
            id="unique-user-count-table"
            class="tile-table"
            :items="uniqueUserCountByRealm"
            :headers="headerUniqueUserCountByRealm"
            hide-default-header
            hide-default-footer
            dense
            no-data-text=""
          />
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import DashboardRepository from "@/api/DashboardRepository";

export default {
  data() {
    return {
      headerActiveUserCount: [
        { text: "Realm", value: "REALM" },
        { text: "Client", value: "CLIENT", groupable: false },
        {
          text: "Active User Count",
          value: "ACTIVE_USER_COUNT",
          groupable: false,
        },
      ],
      headerUniqueUserCountByIDP: [
        { text: "IDP", value: "IDP" },
        { text: "Unique User Count", value: "UNIQUE_USER_COUNT" },
      ],
      headerUniqueUserCountByRealm: [
        { text: "Realm", value: "REALM" },
        { text: "Unique User Count", value: "UNIQUE_USER_COUNT" },
      ],
      totalNumberOfUsers: "",
      activeUserCount: [],
      uniqueUserCountByIDP: [],
      uniqueUserCountByRealm: [],
    };
  },
  async created() {
      this.loadActiveUserCount();
      this.loadTotalNumberOfUsers();
      this.loadUniqueUserCountByIDP();
      this.loadUniqueUserCountByRealm();
  },
  methods: {
    async loadActiveUserCount() {
      const response = await DashboardRepository.get("active-user-count");
      this.activeUserCount = response.data;
    },
    async loadTotalNumberOfUsers() {
      const response = await DashboardRepository.get("total-number-of-users");
      this.totalNumberOfUsers = response.data;
    },
    async loadUniqueUserCountByIDP() {
      const response = await DashboardRepository.get(
        "unique-user-count-by-idp"
      );
      this.uniqueUserCountByIDP = response.data;
    },
    async loadUniqueUserCountByRealm() {
      const response = await DashboardRepository.get(
        "unique-user-count-by-realm");
      this.uniqueUserCountByRealm = response.data;
    },
    handleError(message, error) {
      this.$store.commit("alert/setAlert", {
        message: message + ": " + error,
        type: "error"
      });
      window.scrollTo(0, 0);
    },
  },
};
</script>



<style>
.dashboard {
  display: flex;
  flex-direction: row;
  justify-content: center;
}

.column {
  display: flex;
  flex-direction: column;
}

.tile {
  width: 300px;
  padding: 16px 20px;

  margin: 20px;
  background: #f7f7f7;
  box-shadow: 1px 4px 4px rgba(0, 0, 0, 0.25);
}

.tile .heading {
  display: flex;
  flex-direction: row;
  width: fit-content;
}

.tile .heading p {
  margin: 0 5px 0 0;
  font-family: "BC Sans";
  font-style: normal;
  font-weight: bold;
  font-size: 15px;
  line-height: 20px;
  color: #003366;
}

.tooltip{
    color:white;
    margin: 0px;
}

.tile .tile-table {
  margin: 10px 0;
}

.theme--light.v-data-table > .v-data-table__wrapper > table > thead > tr > th,
tbody {
  color: #003366;
}

.tile .single-stat {
  margin: 10px 0;
  font-weight: bold;
  font-size: 25px;
  color: #003366;
}
</style>