<template>
  <div>
    <div class="flex">
      <div class="column">
        <div class="tile" style="width: 620px">
          <div class="heading">
            <p>Active User Count (Login Event Within 365 Days)</p>
          </div>
          <v-data-table id="Active-user-count-table" class="tile-table" :items="activeUserCount"
            :headers="headerActiveUserCount" hide-default-footer dense no-data-text=""
            :loading="activeUserCountLoadingStatus" :items-per-page="-1">
            <template #item.REALM="{ item }">
              <v-tooltip bottom :disabled="!item.REALM_DESCRIPTION" max-width="300px">
                <template v-slot:activator="{ on }">
                  <span v-on="on">{{ item.REALM }}</span>
                </template>
                <span>{{ item.REALM_DESCRIPTION }}</span>
              </v-tooltip>
            </template>
            <template #item.CLIENT="{ item }">
              <v-tooltip bottom :disabled="!item.DESCRIPTION" max-width="300px">
                <template v-slot:activator="{ on }">
                  <span v-on="on">{{ item.CLIENT }}</span>
                </template>
                <span>{{ item.DESCRIPTION }}</span>
              </v-tooltip>
            </template>
          </v-data-table>
        </div>
      </div>
      <div class="column">
        <div class="tile">
          <div class="heading">
            <p>Total Number of Users</p>
            <v-tooltip right max-width="300px">
              <template v-slot:activator="{ on }">
                <v-icon v-on="on" small>mdi-help-circle</v-icon>
              </template>
              <p class="tooltip">Total Unique User Count by IDP + MHSU Realms that do not use an IDP</p>
            </v-tooltip>
          </div>
          <p class="single-stat">{{ totalNumberOfUsers }}</p>
          <v-skeleton-loader v-if="totalNumberOfUsersLoadingStatus" ref="dashboardSkeleton" type="text" max-width="60px">
          </v-skeleton-loader>
        </div>

        <div class="tile">
          <div class="heading">
            <p>Unique User Count (By IDP)</p>
          </div>
          <PieChart :pieChartData="uniqueUserCountByIDP" v-if="!uniqueUserCountByIDPLoadingStatus" />
          <v-skeleton-loader v-if="uniqueUserCountByIDPLoadingStatus" ref="dashboardSkeleton" width='' type="image">
          </v-skeleton-loader>
        </div>

        <div class="tile">
          <div class="heading">
            <p>Unique User Count (By Realm)</p>
          </div>
          <PieChart :pieChartData="uniqueUserCountByRealm" v-if="!uniqueUserCountByRealmLoadingStatus" />
          <v-skeleton-loader v-if="uniqueUserCountByIDPLoadingStatus" ref="dashboardSkeleton" width='' type="image">
          </v-skeleton-loader>
        </div>
      </div>

    </div>
    <div class="flex">
      <div class="tile" style="width: 1100px">
        <div class="heading">
          <p>Active Total User Count</p>
        </div>
        <div class="btn-group">
          <v-btn class="btn" v-bind:class="getActiveClass('7D')" @click="loadActiveTotalUser('7D')" small
            rounded>7D</v-btn>
          <v-btn class="btn" v-bind:class="getActiveClass('1M')" small rounded
            @click="loadActiveTotalUser('1M')">1M</v-btn>
          <v-btn class="btn" v-bind:class="getActiveClass('6M')" small rounded
            @click="loadActiveTotalUser('6M')">6M</v-btn>
          <v-btn class="btn" v-bind:class="getActiveClass('1Y')" small rounded
            @click="loadActiveTotalUser('1Y')">1Y</v-btn>
        </div>
        <LineChart :lineChartData="totalUserCount" v-if="!totalUserCountLoadingStatus" />
        <v-skeleton-loader v-if="totalUserCountLoadingStatus" ref="dashboardSkeleton" width='' type="image">
        </v-skeleton-loader>
      </div>
    </div>
  </div>
</template>


<script>
import MetricsRepository from "@/api/MetricsRepository";
import RealmsRepository from "@/api/RealmsRepository";
import PieChart from '@/components/PieChart';
import LineChart from '@/components/LineChart';

export default {
  components: { PieChart, LineChart },
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
      uniqueUserCountByIDP: {},
      uniqueUserCountByRealm: {},
      totalUserCount: {},
      totalUserCountSelectedFormat: '7D',
      realmsInfo: [],
      totalNumberOfUsersLoadingStatus: true,
      activeUserCountLoadingStatus: true,
      uniqueUserCountByIDPLoadingStatus: true,
      uniqueUserCountByRealmLoadingStatus: true,
      totalUserCountLoadingStatus: true
    };
  },
  async created() {
    await this.loadActiveUserCount();
    await this.loadRealmsInfo();
    this.loadTotalNumberOfUsers();
    this.loadUniqueUserCountByIDP();
    this.loadUniqueUserCountByRealm();
    this.loadActiveTotalUser('7D');
    this.mergeActiveUsersCountWithRealmsInfo();
  },
  methods: {
    getActiveClass(name) {
      return this.totalUserCountSelectedFormat == name ? 'primary' : 'secondary';
    },
    async loadRealmsInfo() {
      const response = await RealmsRepository.get();
      this.realmsInfo = response.data;
      this.activeUserCountLoadingStatus = false;
    },
    async loadActiveUserCount() {
      const response = await MetricsRepository.get("active-user-count");
      this.activeUserCount = response.data;
      this.activeUserCountLoadingStatus = false;
    },
    async loadTotalNumberOfUsers() {
      const response = await MetricsRepository.get("total-number-of-users");
      this.totalNumberOfUsers = response.data;
      this.totalNumberOfUsersLoadingStatus = false;
    },
    async loadUniqueUserCountByIDP() {
      const response = await MetricsRepository.get("unique-user-count-by-idp");
      const labels = [];
      const dataset = [];
      for (var key of response.data.entries()) {
        labels.push(key[1]["IDP"].padEnd(20))
        dataset.push(key[1]["UNIQUE_USER_COUNT"])
      }

      this.uniqueUserCountByIDP["labels"] = labels;
      this.uniqueUserCountByIDP["UNIQUE_USER_COUNT"] = dataset;
      this.uniqueUserCountByIDPLoadingStatus = false;
    },
    async loadUniqueUserCountByRealm() {
      const response = await MetricsRepository.get("unique-user-count-by-realm");
      const labels = [];
      const dataset = [];
      for (var key of response.data.entries()) {
        labels.push(key[1]["REALM"].padEnd(20))
        dataset.push(key[1]["UNIQUE_USER_COUNT"])
      }

      this.uniqueUserCountByRealm["labels"] = labels;
      this.uniqueUserCountByRealm["UNIQUE_USER_COUNT"] = dataset;
      this.uniqueUserCountByRealmLoadingStatus = false;
    },
    async loadActiveTotalUser(format) {
      this.totalUserCountSelectedFormat = format;
      this.totalUserCountLoadingStatus = true;
      const response = await MetricsRepository.get("total-active-user-count/" + format);
      const labels = [];
      const dataset = [];
      for (var key of response.data.entries()) {
        labels.push(new Date(key[1]["DATEE"]).toISOString().split('T')[0]);
        dataset.push(key[1]["ACTIVE_USER_COUNT"]);
      }
      this.totalUserCount["DATEE"] = labels;
      this.totalUserCount["ACTIVE_USER_COUNT"] = dataset;
      this.totalUserCountLoadingStatus = false;
    },
    mergeActiveUsersCountWithRealmsInfo() {
      this.activeUserCount = this.activeUserCount.map(item => {
        return { ...item, REALM_DESCRIPTION: this.realmsInfo.find(realm => realm.REALM === item.REALM)?.DESCRIPTION }
      })
    },
    handleError(message, error) {
      this.$store.commit("alert/setAlert", {
        message: message + ": " + error,
        type: "error"
      });
      window.scrollTo(0, 0);
    },
    checkEmpty() {
      return true;
    }
  },
};
</script>

<style scoped>
.flex {
  display: flex;
  flex-direction: row;
  justify-content: center;
}

.column {
  display: flex;
  flex-direction: column;
}

.tile {
  width: 440px;
  padding: 16px 20px;

  margin: 20px;
  background: #f7f7f7;
  box-shadow: 1px 4px 4px rgba(0, 0, 0, 0.25);
}

.tile .heading {
  display: flex;
  flex-direction: row;
  width: fit-content;
  margin: 0 0 10px 0;
}

.tile .heading p {
  margin: 0 5px 0 0;
  font-family: "BCSans";
  font-style: normal;
  font-weight: bold;
  font-size: 15px;
  line-height: 20px;
  color: #003366;
}

.tooltip {
  color: white;
  margin: 0px;
}

.tile .tile-table {
  margin: 10px 0;
}

.theme--light.v-data-table>.v-data-table__wrapper>table>thead>tr>th,
tbody {
  color: #003366;
}

.tile .single-stat {
  margin: 10px 0;
  font-weight: bold;
  font-size: 25px;
  color: #003366;
}

.btn-group {
  margin-bottom: 10px;
}

.btn {
  margin-right: 12px;
}
</style>