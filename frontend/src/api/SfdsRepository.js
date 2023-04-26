import { sfdsRequest } from "./Repository";

const uses = "/uses";
const accounts = "/accounts";

export default {
  getUses() {
    return sfdsRequest().then((axiosInstance) => axiosInstance.get(`${uses}`));
  },

  getAccounts() {
    return sfdsRequest().then((axiosInstance) =>
      axiosInstance.get(`${accounts}`)
    );
  },
};
