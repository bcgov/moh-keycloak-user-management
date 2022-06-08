import { umsRequest } from "./Repository";

const resource = "/metrics";

export default {
    get(queryParams) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${queryParams}`));
    }
}