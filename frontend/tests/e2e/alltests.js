import { Role, Selector } from "testcafe";

const SITE_UNDER_TEST = "http://localhost:8080";

const TEST_CAFE_USERNAME = "testcafe";
const CLIENT_TO_TEST = "UMC-E2E-TESTS";

const regularAccUser = Role(SITE_UNDER_TEST, async (t) => {
  await t
    .click("#zocial-moh_idp")
    .typeText("#username", "testcafe")
    .typeText("#password", process.env.TESTCAFE_PASSWORD)
    .click("#kc-login");
});

fixture.disablePageCaching`All tests`.beforeEach(async (t) => {
  await t.useRole(regularAccUser);
}).page`${SITE_UNDER_TEST}`;

test("Smoke test", async (t) => {
  await t.expect(Selector("#search-button").visible).ok();
});

test("Test search", async (t) => {
  await t
    .typeText("#user-search", "testcafe")
    .click("#search-button")
    .expect(Selector("html").textContent)
    .contains(TEST_CAFE_USERNAME);
});

test("Test update user", async (t) => {
  // We use a random value just to make sure a change is actually made.
  const random_value = Math.ceil(Math.random() * 100).toString();
  await t
    .typeText("#user-search", "testcafe")
    .click("#search-button")
    .click(Selector("td").withText(TEST_CAFE_USERNAME))
    .typeText("#notes", random_value, { replace: true })
    .click("#submit-button")
    .expect(Selector("html").textContent)
    .contains("User updated");
});

test("Test update user role", async (t) => {
  await t
    .typeText("#user-search", "testcafe")
    .click("#search-button")
    .click(Selector("td").withText(TEST_CAFE_USERNAME))
    .click(
      Selector("tbody")
        .find("td")
        .withText(CLIENT_TO_TEST)
        .sibling("td")
        .find("i")
    )
    .click("#role-0")
    .click("#save-user-roles")
    .expect(Selector("#primary-alert").textContent)
    .contains("Roles updated successfully");
});
