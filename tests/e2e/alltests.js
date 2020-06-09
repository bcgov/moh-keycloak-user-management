import { Selector } from 'testcafe';
import { Role } from 'testcafe';

const regularAccUser = Role('http://localhost:8080', async t => {
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
    .page `http://localhost:8080`;

test('Smoke test', async t => {
    await t
        .expect(Selector('#search-button').exists)
        .ok();
});

test('Test search', async t => {
    await t
        .typeText('#user-search', 'testcafe')
        .click('#search-button')
        .expect(Selector('html').textContent)
        // This is the ID of the testcafe user.
        .contains('3195a1bf-4bea-47c4-955d-cf52d4e2fc15');
});
