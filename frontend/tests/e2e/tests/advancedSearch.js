import AdvancedSearchForm from "../pages/AdvancedSearchForm";
import { itsbAccessTeamUser } from "../roles/roles";

const SITE_UNDER_TEST = "http://localhost:8080/#/users/";

fixture.disablePageCaching`Advanced Search`.beforeEach(async (t) => {
  await t.useRole(itsbAccessTeamUser);
}).page`${SITE_UNDER_TEST}`;

test("Test adv search by Username", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.usernameInput, "testcafe-dashboard")
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getNoResultsCount())
    .eql(0)
    .expect(AdvancedSearchForm.getResultsCount())
    .eql(1);
});

test("Test adv search by Organization", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.organizationInput, "00000010")
    .click(AdvancedSearchForm.getDropdownResult("00000010"))
    .pressKey("enter")
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .gt(1);
});

test("Test adv search by Username and Organization", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.usernameInput, "123")
    .typeText(AdvancedSearchForm.organizationInput, "00000010")
    .click(AdvancedSearchForm.getDropdownResult("00000010"))
    .pressKey("enter")
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .eql(1);
});

test("Test adv search no results", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.lastnameInput, "thisuser")
    .typeText(AdvancedSearchForm.firstnameInput, "doesnotexist")
    .pressKey("enter")
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getNoResultsCount())
    .eql(1); //eql 1 because we're counting row that displays no data found
});

test("Test adv search by Role", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.selectClient, "UMC-E2E-TESTS")
    .click(AdvancedSearchForm.getDropdownResult("UMC-E2E-TESTS"))
    .pressKey("enter")
    .click(AdvancedSearchForm.getRoleCheckbox("E2E-ROLE-2"))
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .gt(0);
});

test("Test adv search by Email and Role", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.emailInput, "david.a.sharpe@gmail.com")
    .typeText(AdvancedSearchForm.selectClient, "UMC-E2E-TESTS")
    .click(AdvancedSearchForm.getDropdownResult("UMC-E2E-TESTS"))
    .pressKey("enter")
    .click(AdvancedSearchForm.getRoleCheckbox("E2E-ROLE-2"))
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .eql(1);
});

test("Test adv search by Email and Role, then Basic Search", async (t) => {
  await t
    .click(AdvancedSearchForm.advSearchLink)
    .typeText(AdvancedSearchForm.emailInput, "david.a.sharpe@gmail.com")
    .typeText(AdvancedSearchForm.selectClient, "UMC-E2E-TESTS")
    .click(AdvancedSearchForm.getDropdownResult("UMC-E2E-TESTS"))
    .pressKey("enter")
    .click(AdvancedSearchForm.getRoleCheckbox("E2E-ROLE-2"))
    .click(AdvancedSearchForm.advSearchButton)
    .expect(AdvancedSearchForm.getResultsCount())
    .eql(1)
    .click("#basic-search-link")
    .typeText("#user-search", "david.a.sharpe@gmail.com")
    .click("#search-button")
    .expect(AdvancedSearchForm.getResultsCount())
    .gt(1);
});
