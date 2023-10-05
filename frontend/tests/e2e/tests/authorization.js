import { Selector } from "testcafe";
import {
  organizationUser as userNoDashboardAccess,
  dashboardUser as userNoOrganizationAccess,
} from "../roles/roles";

const UNAUTHORIZED_RESPONSE = "Access Denied";

fixture.disablePageCaching`Authorization`.page`http://localhost:8080`;

test("Test Dashboard Access Unauthorized", async (t) => {
  await t
    .useRole(userNoDashboardAccess)
    .navigateTo("http://localhost:8080/#/dashboard/")
    .expect(Selector("#error-container > h3").textContent)
    .contains(UNAUTHORIZED_RESPONSE);
});

test("Test Organization Access Unauthorized", async (t) => {
  await t
    .useRole(userNoOrganizationAccess)
    .navigateTo("http://localhost:8080/#/organizations/")
    .expect(Selector("#error-container > h3").textContent)
    .contains(UNAUTHORIZED_RESPONSE);
});
