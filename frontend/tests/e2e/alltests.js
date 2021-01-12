import { Selector } from 'testcafe';
import { Role } from 'testcafe';

const SITE_UNDER_TEST = 'http://localhost:8080';

const TEST_CAFE_USER_ID = '3195a1bf-4bea-47c4-955d-cf52d4e2fc15';

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
        .contains(TEST_CAFE_USER_ID);
});

test('Test update user', async t => {
    // We use a random value just to make sure a change is actually made.
    const random_value = Math.ceil((Math.random() * 100)).toString();
    await t
        .typeText('#user-search', 'testcafe')
        .click('#search-button')
        // This is the ID of the testcafe user.
        .click(Selector('td').withText(TEST_CAFE_USER_ID))
        .typeText('#org-details', random_value, { replace: true })
        .click('#submit-button')
        .expect(Selector('html').textContent)
        .contains('User updated');
});

test('Test update user role', async t => {
    const client = 'FMDB'
    await t
        .typeText('#user-search', 'testcafe')
        .click('#search-button')
        .click(Selector('td').withText(TEST_CAFE_USER_ID))
        .typeText('#select-client', client, { replace: true })
        .click(Selector('.v-list-item').withText('FMDB'))
        .click('#role-0')
        .click('#save-user-roles')
        .expect(Selector('#primary-alert').textContent)
        .contains('Roles updated successfully');
});

test('Test search by administrator', async t => {
    await t
        .click('#admin-event-log-link')
        .typeText('#admin-id', TEST_CAFE_USER_ID)
        .pressKey('enter')
        .expect(Selector('#search-results').textContent)
        .contains('Test Cafe');
});



