import { kcRequest } from "./Repository";

const resource = "/groups";

export default {
    get(queryParams) {
        if (queryParams) {
            return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}${queryParams}`));
        } else {
            return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
        }
    },
}