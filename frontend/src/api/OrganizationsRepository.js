import { umsRequest } from "./Repository";

const resource = "/organizations";

export default {
    get() {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
    }
}