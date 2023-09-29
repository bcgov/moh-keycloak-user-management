import { Role } from "testcafe";

const SITE_UNDER_TEST = "http://localhost:8080";

export const itsbAccessTeamUser = Role(SITE_UNDER_TEST, async (t) => {
  await t
    .click("#zocial-moh_idp")
    .typeText("#username", "testcafe")
    .typeText("#password", process.env.TESTCAFE_PASSWORD)
    .click("#kc-login");
});
