<template>
  <v-card outlined class="user-groups">
    <v-card-title>User Groups and Members</v-card-title>
    <v-card-text>
      <v-list dense>
        <v-list-item v-for="group in userGroups" :key="group.id">
          <v-list-item-content>
            <v-list-item-title>{{ group.name }}</v-list-item-title>
            <v-list dense v-if="group.members">
              <v-list-item v-for="member in group.members" :key="member.id">
                <v-list-item-content>
                  <v-list-item-title>{{ member.username }}</v-list-item-title>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-card-text>
  </v-card>
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
.user-groups {
  margin-top: 30px;
  padding: 16px;
}
</style>
