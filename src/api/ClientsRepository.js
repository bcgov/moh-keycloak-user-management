import { kcRequest } from "./Repository";

const resource = "/clients";

export default {
    get() {
        return kcRequest().get(`${resource}`);
    },
    
    getRoles(clientId){
        return kcRequest().get(`${resource}/${clientId}/roles`)
    }
}