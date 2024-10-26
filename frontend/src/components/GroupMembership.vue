<template>
  <div id="group-membership">
    <h1 class="heading">Group Memberships</h1>
    <v-divider></v-divider>
    <v-card-text>
      <v-list dense>
        <v-list-item v-for="group in userGroups" :key="group.id">
          <v-list-item-content>
            <v-card class="group-card" outlined>
              <h2 class="group-name">{{ group.name }}</h2>
              <v-divider></v-divider>
              <v-list dense v-if="group.members">
                <div class="group-members-header">
                  <span class="column-header">
                    <v-icon small>mdi-account</v-icon> Username
                  </span>
                  <span class="column-header">
                    <v-icon small>mdi-account</v-icon> First Name
                  </span>
                  <span class="column-header">
                    <v-icon small>mdi-account</v-icon> Last Name
                  </span>
                  <span class="column-header">
                    <v-icon small>mdi-email</v-icon> Email
                  </span>
                </div>
                <v-list-item v-for="member in group.members" :key="member.id" class="member-row">
                  <v-list-item-content>
                    <span class="member-username">{{ member.username }}</span>
                    <span class="member-first-name">{{ member.firstName }}</span>
                    <span class="member-last-name">{{ member.lastName }}</span>
                    <span class="member-email">{{ member.email }}</span>
                  </v-list-item-content>
                </v-list-item>
              </v-list>
            </v-card>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-card-text>
  </div>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";
import GroupsRepository from "@/api/GroupsRepository";

export default {
  name: "GroupMembership",
  data() {
    return {
      userGroups: [],
    };
  },
  async created() {
    this.loadUserGroups();
  },
  methods: {
    async loadUserGroups() {
      try {
        const groups = await UsersRepository.getUserGroups(this.$keycloak.tokenParsed.sub);
        this.userGroups = groups.data;
        await this.loadGroupMembers();
      } catch (error) {
        this.handleError("Failed to load user groups", error);
      }
    },
    async loadGroupMembers() {
      try {
        for (let group of this.userGroups) {
          const members = await GroupsRepository.getGroupMembers(group.id);
          this.$set(group, 'members', members.data);
        }
      } catch (error) {
        this.handleError("Failed to load group members", error);
      }
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

.group-card {
  margin-top: 10px;
  margin-bottom: 10px;
  padding: 16px;
  border: 1px solid #e0e0e0;
  background-color: #f9f9f9;
}

.group-name {
  font-size: 1.4rem;
  font-weight: 600;
  color: #333;
}

.group-members-header {
  display: flex;
  font-weight: bold;
  color: #555;
  margin-bottom: 10px;
  padding: 8px 12px;
  border-radius: 4px;
}

.column-header {
  flex: 1;
  text-align: left;
}

.member-row {
  display: flex;
  margin-bottom: 5px;
  padding: 8px 12px;
  border-radius: 4px;
  border-bottom: 1px solid #e0e0e0;
}

.member-row:nth-child(even) {
  background-color: #f2f2f2;
}

.member-username,
.member-first-name,
.member-last-name,
.member-email {
  flex: 1;
  color: #555;
  font-size: 1rem;
  margin-left: 10px;
}

.member-username {
  margin-left: 0;
}

/*.member-row:hover {*/
/*  background-color: #e8f4fc;*/
/*}*/
</style>
