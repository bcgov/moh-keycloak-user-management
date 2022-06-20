<template>
  <v-card  v-if="clientsWithRoles.length > 0" outlined class="subgroup">
    <h2 class="sfds-authorizations-header">Mailbox Authorizations</h2>

    <div class="nav-tabs" style="width: 100%">
      <a
      v-for="(item,index) in clientsWithRoles" v-bind:key="item.name"
      :class="(selectedClient > index || selectedClient < index)? 'tabActive': 'tabInactive'"  
      @click="selectedClient = index">
      {{item.name}}
      </a>
    </div>
    <user-update-sfds-auth
      :selectedClient="this.clientsWithRoles[selectedClient]"
    ></user-update-sfds-auth>
  </v-card>
</template>


<script>
import UserUpdateSfdsAuth from "@/components/UserUpdateSfdsAuth";
import UsersRepository from "../api/UsersRepository";
import ClientsRepository from "@/api/ClientsRepository";

export default {
  name: "UserMailboxAuthorizations",
  props: ["userId"],

  components: {
    UserUpdateSfdsAuth,
  },
  data() {
    return {
      MailboxClientNames: ["SFDS", "HSCIS"],
      clientsWithRoles: [],
      selectedClient: 0,
    };
  },
  async created() {
    this.$root.$refs.UserMailboxAuthorizations = this;
    this.getMailboxClients();
  },
  methods: {
    getMailboxClients: async function () {
      //get both SFSD and HSCIS
      let MailboxClients = [];
      let clients = await ClientsRepository.get();
      clients = clients.data;
      clients.map((client) => {
        if (this.MailboxClientNames.includes(client.name)) {
          MailboxClients.push(client);
        }
      });

      this.clientsWithRoles = [];
      MailboxClients.map((client) => {
        return UsersRepository.getUserEffectiveClientRoles(
          this.userId,
          client.id
        ).then((clientRoles) => {
          if (clientRoles.data.length > 0) {
            this.clientsWithRoles.push(client);
          }
        });
      });
    },
  },
};
</script>


<style scoped>
.tabActive, .tabInactive {
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
