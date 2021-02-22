import { Selector } from 'testcafe';

class AdvancedSearchForm {

    constructor() {
        this.advSearchLink = Selector('#advancedSearchLink');
        this.lastnameInput = Selector('#adv-search-last-name');
        this.firstnameInput = Selector('#adv-search-first-name');
        this.usernameInput = Selector('#adv-search-username');
        this.emailInput = Selector('#adv-search-email');
        this.organizationInput = Selector('#org-details');
        this.roleInput = Selector('#adv-search-role');
        this.advSearchButton = Selector('#adv-search-button');
    }

    getNoResultsCount() {
        return Selector('#users-table')
                .find('tbody > tr.v-data-table__empty-wrapper')
                .count;
    }

    getResultsCount() {
        return Selector('#users-table')
                .find('tbody > tr')
                .count;
    }

}

export default new AdvancedSearchForm();
