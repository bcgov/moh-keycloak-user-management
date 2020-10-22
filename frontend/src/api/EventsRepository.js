import { kcRequest } from "./Repository";

const resource = "/events" +
    "?type=LOGIN" +
    "&type=LOGOUT&max=100";

export default {

    getEvents(queryParams) {
        const resourcePath = queryParams ? `${resource}&${queryParams}` : resource;
        return kcRequest().then(axiosInstance => axiosInstance.get(resourcePath));
    }
}
