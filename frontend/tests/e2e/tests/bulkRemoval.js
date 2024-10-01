import AdvancedSearchForm from "../pages/AdvancedSearchForm";
import { itsbAccessTeamUser } from "../roles/roles";

const SITE_UNDER_TEST = "http://localhost:8080/#/users/";

fixture.disablePageCaching`Bulk Removal`.beforeEach(async (t) => {
  await t.useRole(itsbAccessTeamUser);
}).page`${SITE_UNDER_TEST}`;

test("Test bulk removal button should be invisible", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.usernameInput, "123")
    .typeText(AdvancedSearchForm.organizationInput, "00000010")
    .click(AdvancedSearchForm.getDropdownResult("00000010"))
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .eql(1)
    .expect(AdvancedSearchForm.bulkRemoveButton().exists)
    .notOk();
});

test("Test bulk removal button should be disabled", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.usernameInput, "testcafe")
    .typeText(AdvancedSearchForm.selectClient, "UMC-E2E-TESTS")
    .click(AdvancedSearchForm.getDropdownResult("UMC-E2E-TESTS"))
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .gt(0)
    .expect(AdvancedSearchForm.bulkRemoveButton().hasAttribute("disabled"))
    .ok();
});

test("Test bulk removal button should be enabled", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.usernameInput, "testcafe")
    .typeText(AdvancedSearchForm.selectClient, "UMC-E2E-TESTS")
    .click(AdvancedSearchForm.getDropdownResult("UMC-E2E-TESTS"))
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .gt(0)
    .click(AdvancedSearchForm.selectAllCheckbox)
    .expect(AdvancedSearchForm.bulkRemoveButton().hasAttribute("disabled"))
    .notOk();
});
