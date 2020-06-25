import { kcRequest } from "./Repository";

const resource = "/admin-events";

export default {

    getEvents() {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
    }
}
