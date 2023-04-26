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
          :disabled="isCheckboxDisabled(group.name)"
        >
          <template v-slot:label>
            <v-tooltip right max-width="300px">
              <template v-slot:activator="{ on }">
                <span v-on="on">{{ group.name }}</span>
              </template>
              <span class="white-space-fix">{{ group.description }}</span>
            </v-tooltip>
          </template>
        </v-checkbox>
      </v-col>
    </v-row>
    <div class="my-6">
      <v-btn
        id="save-user-groups"
        class="primary"
        medium
        v-on:click="updateUserGroups()"
        v-if="adminUser && allGroups.length > 0"
      >
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
    props: ["userId"],
    data() {
      return {
        adminUser: false,
        adminRole: "",
        currentGroups: [],
        allGroups: [],
        selectedGroups: [],
        ownGroups: [],
        MANAGE_ALL_GROUPS: "manage-all-groups",
        MANAGE_OWN_GROUPS: "manage-own-groups",
      };
    },
    async created() {
      this.checkAdminPermissions();
      await this.getUserGroupsData();
    },
    methods: {
      isCheckboxDisabled: function (groupName) {
        const canManageOwnGroups = this.adminRole === this.MANAGE_OWN_GROUPS;
        const isPartOfGroup = this.ownGroups.some(
          (group) => group.name === groupName
        );
        return canManageOwnGroups && !isPartOfGroup;
      },
      checkAdminPermissions: function () {
        if (
          this.$keycloak.tokenParsed.resource_access[
            "USER-MANAGEMENT-SERVICE"
          ]?.roles.includes(this.MANAGE_ALL_GROUPS)
        ) {
          this.adminRole = this.MANAGE_ALL_GROUPS;
          this.adminUser = true;
        } else if (
          this.$keycloak.tokenParsed.resource_access[
            "USER-MANAGEMENT-SERVICE"
          ]?.roles.includes(this.MANAGE_OWN_GROUPS)
        ) {
          this.adminRole = this.MANAGE_OWN_GROUPS;
          this.adminUser = true;
        }
      },
      getUserGroupsData: async function () {
        this.currentGroups = [];
        this.allGroups = [];
        this.selectedGroups = [];

        let userGroupResponses = await Promise.all([
          UsersRepository.getUserGroups(this.userId),
          GroupsRepository.get(),
        ]).catch((error) => {
          this.$store.commit("alert/setAlert", {
            message: "Error loading user groups: " + error,
            type: "error",
          });
        });
        // Keycloak "Groups" API returns a different object structure than users/groups
        // We need them to match for the checkboxes to map
        const allGroups = userGroupResponses[1].data;
        const searchedUserGroups = userGroupResponses[0].data.map(
          ({ id, name, path }) => ({
            id,
            name,
            path,
            description: allGroups.find((g) => g.id === id).description,
          })
        );
        this.currentGroups.push(...searchedUserGroups);
        this.selectedGroups.push(...searchedUserGroups);
        if (this.adminRole === this.MANAGE_ALL_GROUPS) {
          this.allGroups.push(...allGroups);
        } else if (this.adminRole === this.MANAGE_OWN_GROUPS) {
          const ownGroups = allGroups.filter((group) =>
            this.$keycloak.tokenParsed.groups?.some(
              (name) => name === group.name
            )
          );
          this.allGroups.push(...ownGroups);
          this.ownGroups.push(...ownGroups);
        } else {
          this.allGroups.push(...searchedUserGroups);
        }
      },
      updateUserGroups: function () {
        let groupUpdates = [];

        for (let group of this.allGroups) {
          //It was originally a joined group but is no longer selected
          if (
            this.currentGroups.some((e) => e.id === group.id) &&
            !this.selectedGroups.some((e) => e.id === group.id)
          ) {
            groupUpdates.push(
              UsersRepository.removeGroupFromUser(
                this.userId,
                group.id,
                group.name
              )
            );
          }
          //It was not originally a joined group but is now selected
          if (
            !this.currentGroups.some((e) => e.id === group.id) &&
            this.selectedGroups.some((e) => e.id === group.id)
          ) {
            groupUpdates.push(
              UsersRepository.addGroupToUser(this.userId, group.id, group.name)
            );
          }
        }
        Promise.all(groupUpdates)
          .then(() => {
            this.checkAdminPermissions();
            this.getUserGroupsData();
            this.$store.commit("alert/setAlert", {
              message: "User Groups updated successfully",
              type: "success",
            });
            this.$root.$refs.UserUpdateRoles.loadUserRoles();
          })
          .catch((error) => {
            this.$store.commit("alert/setAlert", {
              message: "Error updating user groups: " + error,
              type: "error",
            });
          })
          .finally(() => {
            window.scrollTo(0, 0);
          });
      },
    },
  };
</script>
<style>
  .white-space-fix {
    white-space: pre-wrap; /* or pre-line */
  }
</style>
