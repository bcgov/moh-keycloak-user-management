import { umsRequest } from "./Repository";

const resource = "/clients";

export default {
  get() {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}`)
    );
  },

  getRoles(clientId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}/${clientId}/roles`)
    );
  },

  getUsersInRole(clientId, roleName, maxResults) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(
        `${resource}/${clientId}/roles/${roleName}/users?first=0&max=${maxResults}`
      )
    );
  },

  /**
   * Some clients request that their application is referred to by a different name than Keycloak's Client Id
   * Because changing Client Id requires updating authentication flow on the client side, UMC client aliases are introduced.
   * Define entry in the below array to display alias instead of clientID for a given client.
   */
  clientAliases: [
    { clientId: "PHO-RSC", alias: "POSIT-USER-ROLES" },
    { clientId: "PHO-RSC-GROUPS", alias: "POSIT-GROUP-ROLES" },
  ],

  assignClientAliases(clients) {
    return clients.map((client) => {
      const aliasMapping = this.clientAliases.find(
        (clientAlias) => clientAlias.clientId === client.clientId
      );
      if (aliasMapping) {
        return { ...client, clientId: aliasMapping.alias };
      }
      return client;
    });
  },
};
