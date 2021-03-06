import { itsbAccessTeamUser } from "../roles/roles"
import AlertPage from "../pages/AlertPage";
import AdvancedSearchForm from "../pages/AdvancedSearchForm";

const SITE_UNDER_TEST = 'http://localhost:8080/#/users/';

fixture
    .disablePageCaching `Advanced Search`
    .beforeEach(async t => {
        await t
            .useRole(itsbAccessTeamUser);
    })
    .page `${SITE_UNDER_TEST}`;

test('Test adv search by Username', async t => {
    await t
        .click(AdvancedSearchForm.advSearchLink)
        .typeText(AdvancedSearchForm.usernameInput, 'testcafe')
        .click(AdvancedSearchForm.advSearchButton)
        .expect(AdvancedSearchForm.getNoResultsCount())
        .eql(0)
        .expect(AdvancedSearchForm.getResultsCount())
        .eql(1);
});

test('Test adv search by Organization', async t => {
    await t
        .click(AdvancedSearchForm.advSearchLink)
        .typeText(AdvancedSearchForm.organizationInput, '00000010')
        .pressKey('enter')
        .click(AdvancedSearchForm.advSearchButton)
        .expect(AdvancedSearchForm.getResultsCount())
        .gt(1);
});

test('Test adv search by Username and Organization', async t => {
    await t
        .click(AdvancedSearchForm.advSearchLink)
        .typeText(AdvancedSearchForm.usernameInput, '123')
        .typeText(AdvancedSearchForm.organizationInput, '00000010')
        .pressKey('enter')
        .click(AdvancedSearchForm.advSearchButton)
        .expect(AdvancedSearchForm.getResultsCount())
        .eql(1);
});

test('Test adv search no results', async t => {
    await t
        .click(AdvancedSearchForm.advSearchLink)
        .typeText(AdvancedSearchForm.lastnameInput, 'thisuser')
        .typeText(AdvancedSearchForm.firstnameInput, 'doesnotexist')
        .typeText(AdvancedSearchForm.organizationInput, '00000010')
        .pressKey('enter')
        .click(AdvancedSearchForm.advSearchButton)
        .expect(AdvancedSearchForm.getNoResultsCount())
        .eql(1);
});

test('Test adv search by Role', async t => {
    await t
        .click(AdvancedSearchForm.advSearchLink)
        .typeText(AdvancedSearchForm.selectClient, 'FMDB')
        .pressKey('enter')
        .click(AdvancedSearchForm.getRoleCheckbox('MOHUSER'))
        .click(AdvancedSearchForm.advSearchButton)
        .expect(AdvancedSearchForm.getResultsCount())
        .gt(0);
});

test('Test adv search by Email and Role', async t => {
    await t
        .click(AdvancedSearchForm.advSearchLink)
        .typeText(AdvancedSearchForm.emailInput, 'gmail')
        .typeText(AdvancedSearchForm.selectClient, 'FMDB')
        .pressKey('enter')
        .click(AdvancedSearchForm.getRoleCheckbox('MOHUSER'))
        .click(AdvancedSearchForm.advSearchButton)
        .expect(AdvancedSearchForm.getResultsCount())
        .eql(1);
});

test('Test adv search by Email and Role, then Basic Search', async t => {
    await t
        .click(AdvancedSearchForm.advSearchLink)
        .typeText(AdvancedSearchForm.emailInput, 'gmail')
        .typeText(AdvancedSearchForm.selectClient, 'FMDB')
        .pressKey('enter')
        .click(AdvancedSearchForm.getRoleCheckbox('MOHUSER'))
        .click(AdvancedSearchForm.advSearchButton)
        .expect(AdvancedSearchForm.getResultsCount())
        .eql(1)
        .click('#basic-search-link')
        .typeText('#user-search', 'gmail')
        .click('#search-button')
        .expect(AdvancedSearchForm.getResultsCount())
        .gt(1);
});
