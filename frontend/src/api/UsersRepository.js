import router from "../router";
import ClientsRepository from "./ClientsRepository";
import { umsRequest } from "./Repository";

const resource = "/users";
const clientRoleMappings = "role-mappings/clients";
const groups = "groups";
const identityProviderLinks = "federated-identity";

export default {
  get(queryParams) {
    if (queryParams) {
      return umsRequest().then((axiosInstance) =>
        axiosInstance.get(`${resource}${queryParams}`)
      );
    } else {
      return umsRequest().then((axiosInstance) =>
        axiosInstance.get(`${resource}`)
      );
    }
  },

  getUser(userId) {
    return umsRequest().then((axiosInstance) => {
      axiosInstance.interceptors.response.use(null, (error) => {
        switch (error.response.status) {
          case 403:
            router.replace({
              path: "/unauthorized",
            });
            break;
          case 404:
            router.replace({
              path: "/notFound",
            });
            break;
        }
        return Promise.reject(error);
      });

      return axiosInstance.get(`${resource}/${userId}`);
    });
  },

  createUser(content) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.post(`${resource}`, content)
    );
  },

  updateUser(userId, content) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.put(`${resource}/${userId}`, content)
    );
  },

  /*Get available client-level roles that can be mapped to the user*/
  getUserAvailableClientRoles(userId, clientId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(
        `${resource}/${userId}/${clientRoleMappings}/${clientId}/available`
      )
    );
  },

  /* Get active client-level role mappings. Active roles include only those that have been directly added to the user and can be removed */
  getUserActiveClientRoles(userId, clientId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(
        `${resource}/${userId}/${clientRoleMappings}/${clientId}`
      )
    );
  },

  /* Get effective client-level role mappings. Effective roles are all client-level roles assigned to a user including those provided by group membership or by composite roles */
  getUserEffectiveClientRoles(userId, clientId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(
        `${resource}/${userId}/${clientRoleMappings}/${clientId}/composite`
      )
    );
  },

  /* Get the last login date for each client for a user*/
  getUserLogins(userId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}/${userId}/last-logins`)
    );
  },

  addUserClientRoles(userId, clientId, content) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.post(
        `${resource}/${userId}/${clientRoleMappings}/${clientId}/`,
        content
      )
    );
  },

  deleteUserClientRoles(userId, clientId, content) {
    //Keycloak expects the roles that will be removed in the body of the request which Axios doesn't do by default
    const deleteContent = { data: content };
    return umsRequest().then((axiosInstance) =>
      axiosInstance.delete(
        `${resource}/${userId}/${clientRoleMappings}/${clientId}/`,
        deleteContent
      )
    );
  },

  /* User Groups */
  getUserGroups(userId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}/${userId}/${groups}`)
    );
  },
  addGroupToUser(userId, groupId, groupName) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.put(
        `${resource}/${userId}/${groups}/${groupId}`,
        groupName,
        { headers: { "Content-Type": "text/plain" } }
      )
    );
  },
  removeGroupFromUser(userId, groupId, groupName) {
    const nameOfGroupToBeDeleted = { data: groupName };
    return umsRequest().then((axiosInstance) =>
      axiosInstance.delete(
        `${resource}/${userId}/${groups}/${groupId}`,
        nameOfGroupToBeDeleted
      )
    );
  },
  resetUserIdentityProviderLink(userId, identityProvider, userIdIdpRealm) {
    const userIdIdpRealmData = { data: userIdIdpRealm };
    return umsRequest().then((axiosInstance) =>
      axiosInstance.delete(
        `${resource}/${userId}/${identityProviderLinks}/${identityProvider}`,
        userIdIdpRealmData
      )
    );
  },
  /** User Payee */
  getUserPayee(userId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}/${userId}/payee`)
    );
  },
  updateUserPayee(userId, payee) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.put(`${resource}/${userId}/payee`, payee, {
        headers: { "Content-Type": "text/plain" },
      })
    );
  },
  /**
   * This function transforms last-logins object.
   * It swaps references to clientID with aliases, defined in ClientsRepository.clientAliases.
   * For example "PHO-RSC": 1711383701558 -> "PHO-USER-ROLES": 1711383701558
   * This is required, so that Last Log In field is populated in Update User Roles component.
   */
  mapLastLoginsClientAliases(lastLogins) {
    let clientAliases = ClientsRepository.clientAliases;
    for (const [key, value] of Object.entries(lastLogins)) {
      const aliasMapping = clientAliases.find(
        (clientAlias) => clientAlias.clientId === key
      );
      if (aliasMapping) {
        lastLogins[aliasMapping.alias] = value;
        delete lastLogins[key];
      }
    }
    return lastLogins;
  },
};
