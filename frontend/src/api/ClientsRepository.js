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

  assignClientAliases(clients) {
    let clientAliases = [
      { clientId: "PHO-RSC", alias: "POSIT-USER-ROLES" },
      { clientId: "PHO-RSC-GROUPS", alias: "POSIT-GROUP-ROLES" },
    ];
    return clients.map((client) => {
      const aliasMapping = clientAliases.find(
        (clientAlias) => clientAlias.clientId === client.clientId
      );
      if (aliasMapping) {
        return { ...client, clientId: aliasMapping.alias };
      }
      return client;
    });
  },
};
