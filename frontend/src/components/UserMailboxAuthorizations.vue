<template>
  <v-card v-if="clientsWithRoles.length > 0" border class="subgroup">
    <h2 class="sfds-authorizations-header">Mailbox Authorizations</h2>

    <div class="nav-tabs" style="width: 100%">
      <a
        v-for="(item, index) in clientsWithRoles"
        v-bind:key="item.name"
        :class="
          selectedClient > index || selectedClient < index
            ? 'tabActive'
            : 'tabInactive'
        "
        @click="selectedClient = index"
      >
        {{ item.name }}
      </a>
    </div>
    <user-update-sfds-auth
      :selectedClient="this.clientsWithRoles[selectedClient]"
    ></user-update-sfds-auth>
  </v-card>
</template>

<script>
  import ClientsRepository from "@/api/ClientsRepository";
  import UserUpdateSfdsAuth from "@/components/UserUpdateSfdsAuth";
  import UsersRepository from "../api/UsersRepository";

  export default {
    name: "UserMailboxAuthorizations",
    props: ["userId"],

    components: {
      UserUpdateSfdsAuth,
    },
    data() {
      return {
        mailboxClientNames: ["SFDS", "HSCIS"],
        clientsWithRoles: [],
        selectedClient: 0,
      };
    },
    async created() {
      // this.$root.$refs.UserMailboxAuthorizations = this;
      await this.getMailboxClients();
    },
    methods: {
      getMailboxClients: async function () {
        this.clientsWithRoles = [];

        ClientsRepository.get().then((clients) => {
          clients.data.map((client) => {
            if (this.mailboxClientNames.includes(client.name)) {
              UsersRepository.getUserEffectiveClientRoles(
                this.userId,
                client.id
              ).then((clientRoles) => {
                if (clientRoles.data.length > 0) {
                  this.clientsWithRoles.push(client);
                }
              });
            }
          });
        });
      },
    },
  };
</script>

<style scoped>
  .tabActive,
  .tabInactive {
    border-radius: 4px 4px 0 0;
    color: #ffffff;
    display: inline-block;
    padding: 0 20px;
    height: 36px;
    font-size: 16px;
    font-weight: bold;
    line-height: 40px;
    margin-right: 10px;
    text-decoration: none;
  }
  .tabActive {
    background: #9e9e9e;
  }

  .tabInactive {
    background: #003366;
  }

  a:hover {
    background: #666666;
  }
  .nav-tabs {
    border-bottom: 2px solid #003366;
    margin-bottom: 20px;
    font-size: 15px;
  }
</style>
