import { umsRequest } from "./Repository";
const resource = "/organizations";

export default {

    get(queryParams) {
        if (queryParams) {
            return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}${queryParams}`));
        } else {
            return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
        } 
    },

    getOrganization(organizationId) {
        return umsRequest().then(axiosInstance => axiosInstance.get(`${resource}/${organizationId}`));
    },

    createOrganization(content) {
        return umsRequest().then(axiosInstance => axiosInstance.post(`${resource}`, content));
    },
}