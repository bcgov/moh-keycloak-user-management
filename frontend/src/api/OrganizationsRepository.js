import { umsRequest } from "./Repository";

const resource = "/organizations";

export default {
  get() {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}`)
    );
  },
  getOrganization(organizationId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`${resource}/${organizationId}`)
    );
  },

  createOrganization(content) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.post(`${resource}`, content, {
        headers: { "Content-Type": "application/json" },
      })
    );
  },
  updateOrganization(organizationId, content) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.put(`${resource}/${organizationId}`, content)
    );
  },
};
