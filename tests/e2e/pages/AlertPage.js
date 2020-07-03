import { Selector } from 'testcafe';

class AlertPage {
    constructor () {
        this.alertBannerText = Selector('#primary-alert');
    }
}

export default new AlertPage();