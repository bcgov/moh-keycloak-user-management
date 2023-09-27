import { Selector } from "testcafe";

class Organization {
  constructor() {
    this.organizationInput = Selector("#organization-search");
    this.idInput = Selector("#id");
    this.nameInput = Selector("#name");
    this.createOrganizationLink = Selector("#create-organization-button");
    this.createOrganizationButton = Selector("#submit-button");
  }

  getResultsCount() {
    return Selector("tbody > tr").count;
  }

  getResults() {
    return Selector("tbody > tr").child(0).textContent;
  }

  getNoResultsCount() {
    return Selector("tbody > tr.v-data-table__empty-wrapper").count;
  }

  getNoResultsMessage() {
    return Selector("tr.v-data-table__empty-wrapper > td").textContent;
  }

  getInputErrorMessage() {
    return Selector("div.v-messages__message").textContent;
  }

  getOrgCreateMessage() {
    return Selector("#primary-alert").find("div.v-alert__content").textContent;
  }
}

export default new Organization();
