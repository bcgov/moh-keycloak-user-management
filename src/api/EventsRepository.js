import { kcRequest } from "./Repository";

const resource = "/events";

export default {

    getEvents() {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
    }
}
