import { umsRequest } from "./Repository";

const resource = "/realms";

export default {
    get() {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
    }
}