import { Selector } from 'testcafe';

const label = Selector('label');

class LockoutStatus {
    constructor(text) {
        this.label = label.withText(text);
        this.radioButton = this.label.find('input[type=radio]')
    }
}

class UserDetailsPage {
    constructor () {
        this.usernameInput = Selector('#user-name');
        this.firstnameInput = Selector('#first-name');
        this.lastnameInput = Selector('#last-name');
        this.emailInput = Selector('#email');
        this.phoneInput = Selector('#phone');
        this.orgInput = Selector('#org-details');
        this.notesInput = Selector('#notes');

        this.enabledStatus = new LockoutStatus('Enabled');
        this.revokedStatus = new LockoutStatus('Revoked');
        this.lockedStatus = new LockoutStatus('Locked');

        this.lockoutReasonInput = Selector('#lockout-reason');

        this.submitButton = Selector('#submit-button');
    }
}

export default new UserDetailsPage();