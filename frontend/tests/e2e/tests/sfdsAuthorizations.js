import { itsbAccessTeamUser } from "../roles/roles"
import { Selector } from "testcafe";

const SFDS_CLIENT = 'SFDS';
const TEST_CAFE_USER_ID = '3195a1bf-4bea-47c4-955d-cf52d4e2fc15';
const TEST_CAFE_USERNAME = 'testcafe';
const SFDS_MAILBOX_TO_ADD = 'bcma';
const SFDS_USE_TO_ADD = 'bcma';
const SFDS_PERMISSION_TO_ADD = 'send';


fixture
    .disablePageCaching `SFDS Authorizations`
    .beforeEach(async t => {
        await t
            .useRole(itsbAccessTeamUser);
    })
    .page `http://localhost:8080`;

// Skip this one for now, can't get the use selector to work
test.skip('Test Add SFDS Authorization', async t => {
    await t
        //Select a user
        .typeText('#user-search', 'testcafe')
        .click('#search-button')
        // Search with Username
        .click(Selector('td').withText(TEST_CAFE_USERNAME))
        // Select the SFDS Client
        .typeText('#select-client', SFDS_CLIENT, { replace: true })
        .click(Selector('.v-list-item').withText(SFDS_CLIENT))
        // Add a new SFDS Auth
        .click('#new-sfds-auth-btn')
        // Mailbox
        .typeText('#sfds-mailboxes', SFDS_MAILBOX_TO_ADD, { replace: true })
        .click(Selector('.v-list-item').withText(SFDS_MAILBOX_TO_ADD))
        // Use
        .click(Selector(() => document.querySelectorAll('#sfds-uses-label')[0]))
        .click(Selector('.v-list-item').withText(SFDS_USE_TO_ADD))
        // Permission
        .click('#sfds-permissions')
        .click(Selector('.v-list-item').withText(SFDS_PERMISSION_TO_ADD))
        .click('#save-sfds-auth-btn')
        .expect(Selector('#sfds-authorizations-table').textContent)
        .contains(SFDS_MAILBOX_TO_ADD)
        .expect(Selector('#sfds-authorizations-table').textContent)
        .contains(SFDS_USE_TO_ADD)
        .expect(Selector('#sfds-authorizations-table').textContent)
        .contains(SFDS_PERMISSION_TO_ADD)
    ;
});

// This test works but requires an SFDS Auth to be created first (which doesn't currently work)
// To run this test first create an SFDS auth with mailbox id = SFDS_MAILBOX_TO_ADD
test.skip('Test Delete SFDS Authorization', async t => {
    await t
        //Select a user
        .typeText('#user-search', 'testcafe')
        .click('#search-button')
        // Search with Username 
        .click(Selector('td').withText(TEST_CAFE_USERNAME))
        // Select the SFDS Client
        .typeText('#select-client', SFDS_CLIENT, { replace: true })
        .click(Selector('.v-list-item').withText(SFDS_CLIENT))
        // Open delete SFDS Auth
        .click('#' + SFDS_MAILBOX_TO_ADD + '-delete-btn')
        .click('#confirm-delete-sfds-btn')
        .expect(Selector('#sfds-authorizations-table').textContent).contains("No data available")
    ;
});