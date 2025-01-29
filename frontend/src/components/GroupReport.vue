<template>
  <div id="group-membership">
    <h1 class="heading">Group Memberships</h1>
    <v-divider></v-divider>
    <v-skeleton-loader
      v-if="userGroups.length === 0"
      type="table"
      :loading="true"
    ></v-skeleton-loader>
    <v-card-text v-for="group in userGroups" :key="group.id" v-else>
      <h2 class="group-name">{{ group.name }}</h2>
      <v-data-table
        density="compact"
        :headers="groupHeaders"
        :items="group.members"
        class="elevation-1 no-data-table-hover"
        :hide-default-footer="true"
        v-on:click:row="selectUser"
        :item-value="(item) => item"
      >
        <template v-slot:item.username="{ item }">
          <a
            target="_blank"
            :href="`#/users/${item.id}`"
            v-on:click="openNewTab"
          >
            {{ item.username }}
          </a>
          <v-icon size="small">mdi-open-in-new</v-icon>
        </template>
        <template v-slot:item.firstName="{ item }">
          <span>{{ item.firstName }}</span>
        </template>
        <template v-slot:item.lastName="{ item }">
          <span>{{ item.lastName }}</span>
        </template>
        <template v-slot:item.email="{ item }">
          <span>{{ item.email }}</span>
        </template>
        <template v-slot:bottom>
          <v-data-table-footer
            items-per-page="15"
            items-per-page-text=""
          ></v-data-table-footer>
        </template>
      </v-data-table>
    </v-card-text>
  </div>
</template>

<script>
  import GroupsRepository from "@/api/GroupsRepository";
  import UsersRepository from "@/api/UsersRepository";
  import { reactive } from "vue";

  export default {
    name: "GroupReport",
    data() {
      return {
        userGroups: reactive([]),
        groupHeaders: [
          { title: "Username", value: "username", sortable: true },
          { title: "First Name", value: "firstName", sortable: true },
          { title: "Last Name", value: "lastName", sortable: true },
          { title: "Email", value: "email", sortable: true },
        ],
      };
    },
    async created() {
      this.loadUserGroups();
    },
    methods: {
      async loadUserGroups() {
        try {
          const groups = await UsersRepository.getUserGroups(
            this.$keycloak.tokenParsed.sub
          );
          this.userGroups = groups.data;
          await this.loadGroupMembers();
        } catch (error) {
          this.handleError("Failed to load user groups", error);
        }
      },
      async loadGroupMembers() {
        try {
          for (let group of this.userGroups) {
            const groupMembers = await GroupsRepository.getGroupMembers(
              group.id
            );
            group.members = groupMembers.data;
          }
        } catch (error) {
          this.handleError("Failed to load group members", error);
        }
      },
      openNewTab() {
        this.newTab = true;
      },
      selectUser: function (click, row) {
        const user = row.item;
        if (this.newTab) {
          this.newTab = false;
          return;
        }
        this.$store.commit("alert/dismissAlert");
        this.$router.push({ name: "UserUpdate", params: { userid: user.id } });
      },
      handleError(message, error) {
        this.$store.commit("alert/setAlert", {
          message: message + ": " + error,
          type: "error",
        });
        window.scrollTo(0, 0);
      },
    },
  };
</script>

<style scoped>
  #group-membership {
    margin-top: 20px;
    padding: 16px;
  }

  .heading {
    font-size: 2rem;
    font-weight: bold;
    color: #333;
    margin-bottom: 20px;
  }

  .group-name {
    font-size: 1.5rem;
    font-weight: 600;
    margin-top: 20px;
    color: #444;
  }
</style>
