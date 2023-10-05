import Organization from "../pages/Organization";
import { organizationUser } from "../roles/roles";

const SITE_UNDER_TEST = "http://localhost:8080/#/organizations/";

const ORGANIZATION_NAME = "E2E Organization";
const VALID_ORGANIZATION_ID = "87654321";
const INVALID_ORGANIZATION_ID = "1234test";
const EXISTING_ORGANIZATION_ID = "00000010";
const API_RESPONSES = {
  notFound: "No matching records found",
  idRequired: "ID is required",
  idConstraintNotSatisfied: "ID must be made of 8 numerical characters",
  nameRequired: "Organization Name is required",
  duplicateId:
    "Error creating organization: Request failed with status code 409. Organization with given ID already exists.",
  success:
    "Organization created successfully! Details: 87654321, E2E Organization",
};

fixture.disablePageCaching`Organizations`.beforeEach(async (t) => {
  await t.useRole(organizationUser);
}).page`${SITE_UNDER_TEST}`;

test("Test Successful Organization Search", async (t) => {
  await t
    .click(Organization.organizationInput)
    .typeText(Organization.organizationInput, EXISTING_ORGANIZATION_ID)
    .expect(Organization.getResultsCount())
    .eql(1)
    .expect(Organization.getResults())
    .eql(EXISTING_ORGANIZATION_ID);
});

test("Test Unsuccessful Organization Search", async (t) => {
  await t
    .click(Organization.organizationInput)
    .typeText(Organization.organizationInput, INVALID_ORGANIZATION_ID)
    .expect(Organization.getNoResultsCount())
    .eql(1)
    .expect(Organization.getNoResultsMessage())
    .eql(API_RESPONSES.notFound);
});

test("Test Organization Id Required", async (t) => {
  await t
    .click(Organization.createOrganizationLink)
    .click(Organization.nameInput)
    .typeText(Organization.nameInput, ORGANIZATION_NAME)
    .click(Organization.createOrganizationButton)
    .expect(Organization.getInputErrorMessage())
    .eql(API_RESPONSES.idRequired);
});

test("Test Organization Id Contraints", async (t) => {
  await t
    .click(Organization.createOrganizationLink)
    .click(Organization.idInput)
    .typeText(Organization.idInput, INVALID_ORGANIZATION_ID)
    .click(Organization.nameInput)
    .typeText(Organization.nameInput, ORGANIZATION_NAME)
    .expect(Organization.getInputErrorMessage())
    .eql(API_RESPONSES.idConstraintNotSatisfied);
});

test("Test Organization Name Required", async (t) => {
  await t
    .click(Organization.createOrganizationLink)
    .click(Organization.idInput)
    .typeText(Organization.idInput, "11111111")
    .click(Organization.createOrganizationButton)
    .expect(Organization.getInputErrorMessage())
    .eql(API_RESPONSES.nameRequired);
});

test("Test Create Organization Duplicate Id", async (t) => {
  await t
    .click(Organization.createOrganizationLink)
    .click(Organization.idInput)
    .typeText(Organization.idInput, EXISTING_ORGANIZATION_ID)
    .click(Organization.nameInput)
    .typeText(Organization.nameInput, ORGANIZATION_NAME)
    .click(Organization.createOrganizationButton)
    .expect(Organization.getOrgCreateMessage())
    .contains(API_RESPONSES.duplicateId);
});

//Skip this test until we have functionality to delete a organization afterwards
test.skip("Test Create Organization Successful", async (t) => {
  await t
    .click(Organization.createOrganizationLink)
    .click(Organization.idInput)
    .typeText(Organization.idInput, VALID_ORGANIZATION_ID)
    .click(Organization.nameInput)
    .typeText(Organization.nameInput, ORGANIZATION_NAME)
    .click(Organization.createOrganizationButton)
    .expect(Organization.getOrgCreateMessage())
    .contains(API_RESPONSES.success);
});
