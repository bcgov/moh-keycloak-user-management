<template>
  <v-card outlined class="subgroup" :disabled="!adminUser">
    <h2>User Groups</h2>
    <v-row no-gutters>
      <v-col class="col-4">
        <v-checkbox
            hide-details="auto"
            v-for="(group, index) in allGroups"
            :id="'group-' + index"
            v-model="selectedGroups"
            :value="group"
            :label="group.name"
            :key="group.id"
        ></v-checkbox>
      </v-col>
    </v-row>
    <div class="my-6">
      <v-btn id="save-user-groups" class="secondary"
             medium v-on:click="updateUserGroups()"
             v-if="adminUser">
        Save User Groups
      </v-btn>
    </div>
  </v-card>
</template>

<script>
import UsersRepository from "@/api/UsersRepository";
import GroupsRepository from "@/api/GroupsRepository";

export default {
  name: "UserUpdateGroups",
  props: ['userId'],
  data() {
    return {
      adminUser: false,
      currentGroups: [],
      allGroups: [],
      selectedGroups: []
    };
  },
  async created() {
    this.checkIfAdmin();
    await this.getUserGroupsData();
  },
  methods: {
    checkIfAdmin: function() {
      if (this.$keycloak.tokenParsed.resource_access['user-management'] &&
          this.$keycloak.tokenParsed.resource_access['user-management'].roles.includes('user-management-admin')) {
        this.adminUser = true;
      }
    },
    getUserGroupsData: async function() {
      this.currentGroups = [];
      this.allGroups = [];
      this.selectedGroups = [];

      let userGroupResponses = await Promise.all([UsersRepository.getUserGroups(this.userId), GroupsRepository.get()])
          .catch(error => {
            this.$store.commit("alert/setAlert", {
              message: "Error updating loading user groups: " + error,
              type: "error"
            });
          });

      this.currentGroups.push(...userGroupResponses[0].data);
      this.selectedGroups.push(...userGroupResponses[0].data);
      this.allGroups.push(...userGroupResponses[1].data);
      // Keycloak "Groups" API returns a different object structure than users/groups
      // We need them to match for the checkboxes to map
      this.allGroups = this.allGroups.map(({id,name,path})=>({id,name,path}));
    },
    updateUserGroups: function() {
      let groupUpdates = [];
      for (let group of this.allGroups) {
        //It was originally a joined group but is no longer selected
        if (this.currentGroups.some(e => e.id === group.id) && !this.selectedGroups.some(e => e.id === group.id)) {
          groupUpdates.push(UsersRepository.removeGroupFromUser(this.userId, group.id))
        }
        //It was not originally a joined group but is now selected
        if (!this.currentGroups.some(e => e.id === group.id) && this.selectedGroups.some(e => e.id === group.id)) {
          groupUpdates.push(UsersRepository.addGroupToUser(this.userId, group.id))
        }
      }
      Promise.all(groupUpdates)
          .then(() => {
            this.checkIfAdmin()
            this.getUserGroupsData();
            this.$store.commit("alert/setAlert", {
              message: "User Groups updated successfully",
              type: "success"
            });
          })
          .catch((error) => {
            this.$store.commit("alert/setAlert", {
              message: "Error updating user groups: " + error,
              type: "error"
            });
          })
          .finally(() => {
            window.scrollTo(0, 0);
          });
    }
  }
}
</script>