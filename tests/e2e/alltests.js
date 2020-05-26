import { Selector } from 'testcafe';

fixture `All tests`
    .page `http://localhost:8080`;
    // .page `https://common-logon-dev.hlth.gov.bc.ca/webapp/`;

test('Test login', async t => {
    await t
        .click('#zocial-moh_idp')
        .typeText('#username', 'testcafe')
        .typeText('#password', process.env.TESTCAFE_PASSWORD)
        .click("#kc-login")
        .expect(Selector('#search-button').exists)
        .ok();
});