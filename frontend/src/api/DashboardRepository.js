import { umsRequest } from "./Repository";

const resource = "/dashboard";

export default {
    get(queryParams) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${queryParams}`));
    }
}