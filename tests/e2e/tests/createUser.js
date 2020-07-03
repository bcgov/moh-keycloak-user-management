import UserDetailsPage from "../pages/UserDetailsPage";
import AlertPage from "../pages/AlertPage";
import { itsbAccessTeamUser } from "../roles/roles"

const ERROR_MESSAGE = 'Please correct errors before submitting';

fixture `CreateUser`
    .disablePageCaching `All tests`
    .page `http://localhost:8080/#/users/create`;

//Skip this test until we have functionality to delete a user afterwards
test.skip('User created successfully all fields', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.usernameInput, 'uitestusername')
        .typeText(UserDetailsPage.firstnameInput, 'uiTestFirst')
        .typeText(UserDetailsPage.lastnameInput, 'uiTestLast')
        .typeText(UserDetailsPage.emailInput, 'uitestemail@email.com')
        .typeText(UserDetailsPage.phoneInput, '1234567890')
        .typeText(UserDetailsPage.notesInput, 'these are some notes')
        .click(UserDetailsPage.lockedStatus.label)
        .typeText(UserDetailsPage.lockoutReasonInput, 'this is a lockout reason')
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains('User created successfully')
        .expect(UserDetailsPage.usernameInput.value).eql('uitestusername')
        .expect(UserDetailsPage.firstnameInput.value).eql('uiTestFirst')
        .expect(UserDetailsPage.lastnameInput.value).eql('uiTestLast')
        .expect(UserDetailsPage.emailInput.value).eql('uitestemail@email.com')
        .expect(UserDetailsPage.phoneInput.value).eql('1234567890')
        .expect(UserDetailsPage.notesInput.value).eql('these are some notes')
        .expect(UserDetailsPage.lockoutReasonInput.value).eql('this is a lockout reason')
});
test('Error when no username', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.firstnameInput, 'uiTestFirst')
        .typeText(UserDetailsPage.lastnameInput, 'uiTestLast')
        .typeText(UserDetailsPage.emailInput, 'uiTestEmail@email.com')
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains(ERROR_MESSAGE)
});
test('Error when no first name', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.usernameInput, 'uiTestUsername')
        .typeText(UserDetailsPage.lastnameInput, 'uiTestLast')
        .typeText(UserDetailsPage.emailInput, 'uiTestEmail@email.com')
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains(ERROR_MESSAGE)
});
test('Error when no last name', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.usernameInput, 'uiTestUsername')
        .typeText(UserDetailsPage.firstnameInput, 'uiTestFirst')
        .typeText(UserDetailsPage.emailInput, 'uiTestEmail@email.com')
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains(ERROR_MESSAGE)
});
test('Error when no email', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.usernameInput, 'uiTestUsername')
        .typeText(UserDetailsPage.firstnameInput, 'uiTestFirst')
        .typeText(UserDetailsPage.lastnameInput, 'uiTestLast')
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains(ERROR_MESSAGE)
});
test('Error when invalid email', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.usernameInput, 'uiTestUsername')
        .typeText(UserDetailsPage.firstnameInput, 'uiTestFirst')
        .typeText(UserDetailsPage.lastnameInput, 'uiTestLast')
        .typeText(UserDetailsPage.emailInput, 'uiTestEmail')
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains(ERROR_MESSAGE)
});
test('Error when revoked selected and no reason', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.usernameInput, 'uiTestUsername')
        .typeText(UserDetailsPage.firstnameInput, 'uiTestFirst')
        .typeText(UserDetailsPage.lastnameInput, 'uiTestLast')
        .typeText(UserDetailsPage.emailInput, 'uiTestEmail@email.com')
        .click(UserDetailsPage.revokedStatus.label)
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains(ERROR_MESSAGE)
});
test('Error when locked selected and no reason', async t => {
    await t
        .useRole(itsbAccessTeamUser)
        .typeText(UserDetailsPage.usernameInput, 'uiTestUsername')
        .typeText(UserDetailsPage.firstnameInput, 'uiTestFirst')
        .typeText(UserDetailsPage.lastnameInput, 'uiTestLast')
        .typeText(UserDetailsPage.emailInput, 'uiTestEmail@email.com')
        .click(UserDetailsPage.lockedStatus.label)
        .click(UserDetailsPage.submitButton)
        .expect(AlertPage.alertBannerText.textContent).contains(ERROR_MESSAGE)
});