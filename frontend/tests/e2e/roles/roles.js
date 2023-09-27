import { Role } from "testcafe";

const SITE_UNDER_TEST = "http://localhost:8080";

export const itsbAccessTeamUser = Role(SITE_UNDER_TEST, async (t) => {
  await t
    .click("#zocial-moh_idp")
    .typeText("#username", "testcafe")
    .typeText("#password", process.env.TESTCAFE_PASSWORD)
    .click("#kc-login");
});

export const dashboardUser = Role(SITE_UNDER_TEST, async (t) => {
  await t
    .click("#zocial-moh_idp")
    .typeText("#username", "testcafe-dashboard")
    .typeText("#password", process.env.TESTCAFE_PASSWORD)
    .click("#kc-login");
});

export const organizationUser = Role(SITE_UNDER_TEST, async (t) => {
  await t
    .click("#zocial-moh_idp")
    .typeText("#username", "testcafe-organizations")
    .typeText("#password", process.env.TESTCAFE_PASSWORD)
    .click("#kc-login");
});
