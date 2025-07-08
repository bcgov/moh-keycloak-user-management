import { umsRequest } from "./Repository";

const resource = "/groups";

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
  async getGroupMembers(groupId) {
    return umsRequest().then((axiosInstance) =>
      axiosInstance.get(`groups/${groupId}/members`)
    );
  },
};
