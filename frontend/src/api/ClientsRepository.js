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
};
