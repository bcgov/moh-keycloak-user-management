import Dashboard from "../pages/dashboard";
import { dashboardUser } from "../roles/roles";

const SITE_UNDER_TEST = "http://localhost:8080/#/dashboard/";

fixture.disablePageCaching`Dashboard`.beforeEach(async (t) => {
  await t.useRole(dashboardUser);
}).page`${SITE_UNDER_TEST}`;

test("Test Total Number of Users", async (t) => {
  await t
    .expect(await Dashboard.getTotalNumberOfUsers())
    .gt(0, { allowUnawaitedPromise: true });
});

test("Test Unique User Count by IDP", async (t) => {
  await t.expect(Dashboard.pieChartIDP.visible).ok();
});

test("Test Unique User Count by Realm", async (t) => {
  await t.expect(Dashboard.pieChartRealm.visible).ok();
});

test("Test User Login Events", async (t) => {
  await t.expect(Dashboard.lineChart.visible).ok();
});

test("Test Active User Count", async (t) => {
  await t.expect(Dashboard.getActiveUserRowCount()).gt(1, { timeout: 10000 });
});
