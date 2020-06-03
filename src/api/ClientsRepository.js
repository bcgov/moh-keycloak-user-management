import { kcRequest } from "./Repository";

const resource = "/clients";

export default {
    get() {
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}`));
    },
    
    getRoles(clientId){
        return kcRequest().then(axiosInstance => axiosInstance.get(`${resource}/${clientId}/roles`));
    }
}