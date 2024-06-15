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
  /**
   * This function transforms group descriptions.
   * It swaps references to clientID with aliases, defined in ClientsRepository.clientAliases.
   * For example "Manage pho-rsc -> Manage posit-user-roles"
   */
  modifyGroupDescriptions(groups) {
    let clientAliases = ClientsRepository.clientAliases;
    return groups.map((group) => {
      let newDescription = group.description;
      clientAliases.forEach((alias) => {
        /**
         * Group descriptions can contain "-" which are not treated as word characters. For example "Manage pho-rsc".
         * Negative Lookbehind (?<!\\w|-): This ensures that the clientId is not preceded by a word character (letters, digits, underscores) or a hyphen.
         * Negative Lookahead (?!\\w|-): This ensures that the clientId is not followed by a word character or a hyphen.
         */
        let regex = new RegExp(
          `(?<!\\w|-)${alias.clientId.toLowerCase()}(?!\\w|-)`,
          "g"
        );
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
