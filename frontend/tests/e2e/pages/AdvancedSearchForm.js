import { Selector } from "testcafe";

class AdvancedSearchForm {
  constructor() {
    this.advSearchLink = Selector("#advancedSearchLink");
    this.lastnameInput = Selector("#adv-search-last-name");
    this.firstnameInput = Selector("#adv-search-first-name");
    this.usernameInput = Selector("#adv-search-username");
    this.emailInput = Selector("#adv-search-email");
    this.organizationInput = Selector("#org-details");
    this.selectClient = Selector("#select-client");
    this.advSearchButton = Selector("#adv-search-button");
    this.bulkRemoveButton = Selector("#remove-access-button");
    this.selectAllCheckbox = Selector(
      "thead .v-data-table__checkbox.v-simple-checkbox"
    ).nth(0);
  }

  getRoleCheckbox(roleName) {
    return Selector("label")
      .withText(roleName)
      .parent()
      .find("input[type=checkbox]");
  }

  getNoResultsCount() {
    return Selector("#users-table").find(
      "tbody > tr.v-data-table__empty-wrapper"
    ).count;
  }

  getResultsCount() {
    return Selector("#users-table").find("tbody > tr").count;
  }

  getDropdownResult(name) {
    return Selector("div.v-list-item__title").withText(name);
  }
}

export default new AdvancedSearchForm();
