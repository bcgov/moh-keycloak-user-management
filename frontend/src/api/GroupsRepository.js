import ClientsRepository from "./ClientsRepository";
import { umsRequest } from "./Repository";

const resource = "/groups";

export default {
  get(queryParams) {
    if (queryParams) {
      return umsRequest().then((axiosInstance) =>
        axiosInstance.get(`${resource}${queryParams}`)
      );
    } else {
      return umsRequest().then((axiosInstance) =>
        axiosInstance.get(`${resource}`)
      );
    }
  },
  modifyGroupDescriptions(groups) {
    let clientAliases = ClientsRepository.clientAliases;

    return groups.map((group) => {
      let newDescription = group.description;
      clientAliases.forEach((alias) => {
        let regex = new RegExp(`\\b${alias.clientId.toLowerCase()}\\b`, "g");
        newDescription = newDescription.replace(
          regex,
          alias.alias.toLowerCase()
        );
      });
      return {
        ...group,
        description: newDescription,
      };
    });
  },
};
