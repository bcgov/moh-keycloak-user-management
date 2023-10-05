import { Selector } from "testcafe";

class Dashboard {
  constructor() {
    this.pieChartIDP = Selector("#unique-user-count-by-idp");
    this.pieChartRealm = Selector("#unique-user-count-by-realm");
    this.lineChart = Selector("#line-chart");
  }

  getActiveUserRowCount() {
    return Selector("td").find("span").withText("moh_applications").count;
  }

  async getTotalNumberOfUsers() {
    const numberOfUsers = await Selector("p.single-stat").filterVisible()
      .textContent;
    return parseInt(numberOfUsers);
  }
}

export default new Dashboard();
