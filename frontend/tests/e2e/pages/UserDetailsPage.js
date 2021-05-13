import { Selector } from 'testcafe';

const label = Selector('label');

class UserDetailsPage {
    constructor () {
        this.usernameInput = Selector('#user-name');
        this.firstnameInput = Selector('#first-name');
        this.lastnameInput = Selector('#last-name');
        this.emailInput = Selector('#email');
        this.phoneInput = Selector('#phone');
        this.orgInput = Selector('#org-details');
        this.notesInput = Selector('#notes');

        this.submitButton = Selector('#submit-button');
    }
}

export default new UserDetailsPage();