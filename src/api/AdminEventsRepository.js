import { kcRequest } from "./Repository";

const resource = "/admin-events" +
    "?operationTypes=UPDATE" +
    "&operationTypes=CREATE" +
    "&operationTypes=DELETE" +
    "&resourceTypes=USER" +
    "&resourceTypes=CLIENT_ROLE_MAPPING" +
    "&max=100";

export default {

    getEvents(queryParams) {
        const resourcePath = queryParams ? `${resource}&${queryParams}` : resource;
        return kcRequest().then(axiosInstance => axiosInstance.get(resourcePath));
    }
}
