import { Selector } from 'testcafe';
import { Role } from 'testcafe';

const SITE_UNDER_TEST = 'http://localhost:8080';

const TEST_CAFE_USER_ID = '3195a1bf-4bea-47c4-955d-cf52d4e2fc15';
const TEST_CAFE_USERNAME = 'testcafe';
const CLIENT_TO_TEST = 'FMDB';

const regularAccUser = Role(SITE_UNDER_TEST, async t => {
    await t
        .click('#zocial-moh_idp')
        .typeText('#username', 'testcafe')
        .typeText('#password', process.env.TESTCAFE_PASSWORD)
        .click("#kc-login");
});

fixture
    .disablePageCaching `All tests`
    .beforeEach(async t => {
        await t
            .useRole(regularAccUser);
    })
    .page `${SITE_UNDER_TEST}`;

test('Smoke test', async t => {
    await t
        .expect(Selector('#search-button').exists)
        .ok();
});

test('Test search', async t => {
    await t.typeText('#user-search', 'testcafe')
        .click('#search-button')
        .expect(Selector('html').textContent)
        // This is the ID of the testcafe user.
        // TODO Replace ID with Username
//        .contains(TEST_CAFE_USER_ID);
        .contains(TEST_CAFE_USERNAME);
});

test('Test update user', async t => {
    // We use a random value just to make sure a change is actually made.
    const random_value = Math.ceil((Math.random() * 100)).toString();
    await t
        .typeText('#user-search', 'testcafe')
        .click('#search-button')
        // This is the ID of the testcafe user.
        // TODO Replace ID with Username
//        .click(Selector('td').withText(TEST_CAFE_USER_ID))
        .click(Selector('td').withText(TEST_CAFE_USERNAME))
        .typeText('#org-details', random_value, { replace: true })
        .click('#submit-button')
        .expect(Selector('html').textContent)
        .contains('User updated');
});

test('Test update user role', async t => {
    await t
        .typeText('#user-search', 'testcafe')
        .click('#search-button')        
// TODO Replace ID with Username
//        .click(Selector('td').withText(TEST_CAFE_USER_ID))
        .click(Selector('td').withText(TEST_CAFE_USERNAME))
        .typeText('#select-client', CLIENT_TO_TEST, { replace: true })
        .click(Selector('.v-list-item').withText(CLIENT_TO_TEST))
        .click('#role-0')
        .click('#save-user-roles')
        .expect(Selector('#primary-alert').textContent)
        .contains('Roles updated successfully');
});

test('Test search by administrator', async t => {
    await t
        .click('#admin-event-log-link')
        // TODO Replace ID with Username => TO TEST
      .typeText('#admin-id', TEST_CAFE_USER_ID)
//        .typeText('#admin-id', TEST_CAFE_USERNAME)
        .pressKey('enter')
        .expect(Selector('#search-results').textContent)
        .contains('Test Cafe');
});



