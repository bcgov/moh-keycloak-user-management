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
