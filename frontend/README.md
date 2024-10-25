[![Build Status](https://travis-ci.org/bcgov/moh-keycloak-user-management.svg?branch=master)](https://travis-ci.org/bcgov/moh-keycloak-user-management)

# User Management Console

The "User Management Console" is custom user management console for Keycloak. Keycloak already has a fully-featured administration console, but this console has been customized to suit our administrator's requirements for ease-of-use, data validation, and fine-grained administrative access control.

# Prerequisites

To run UMC locally you need to have `node v18.19.0 and npm v10.2.3` installed.

This application requires a Keycloak server and the User Management Service. It uses the Keycloak server for OIDC authentication, and it uses the User Management Service for user management. Installation instructions for the User Management Service are at [/backend/README.md](../backend). Keycloak installation and configuration instructions are not in scope for this README.

(The User Management Service implements the same API as the Keycloak Administration REST API, so it's possible to configure the User Management Console to use Keycloak without deploying the User Management Service. This is only noted for posterity and is not supported or tested.)

# Configuration

Specify Keycloak details using the configuration file at [public/keycloak.json](public/keycloak.json). Specify User Management Service details at [public/config.json](public/config.json). The included configuration files are valid for the MoH's development environment.

Please note that opening the frontend application in browsers like Chrome might make the web page refresh every five seconds. In this case, add `checkLoginIframe: false` to the `initOptions` object defined in [src/keycloak/index.js](src/keycloak/index.js).

## Project setup

```
npm install
```

### Compiles and hot-reloads for development

```
npm run serve
```

### Compiles and minifies for production

```
npm run build
```

### Run your unit tests

```
npm run test:unit
```

### Run your end-to-end tests

In one tab, start the server with `npm run serve`. In another tab, run:

```
npm run test:e2e
```

To run a single test (useful for test development and debugging), run:

```
testcafe chrome tests/e2e/alltests.js -t "Test update user"
# alltests.js contains a test named "Test update user"
```

Make sure you have the `checkLoginIframe: false` flag set, as described above. The end-to-end tests require LDAP user credentials. The username `testcafe`, and the password is in the MoH KeePass. Additional users used in E2E tests are called `testcafe-dashboard` and `testcafe-organizations`. All three users share the password. Set the password in an OS environment variable named `TESTCAFE_PASSWORD`. You might also need to add another environment variable named `NODE_ENV` with the value `Development` in case some of the tests fail.

The client used for E2E tests is called `UMC-E2E-TESTS`.

Note that these instructions only apply to the MoH's Keycloak server which has an LDAP identify provider configured. To use a different Keycloak server you would need to update the "login" portion of the tests to use your identity provider.

### Lints and format files

Using VSCode, install the recommended extension (prettier) to be able to auto-format to the standard.

To check lint on all files :
`npm run lint`

To check if all files are formated :
`npm run format:check`

To format all the files to the standard :
`npm run format:fix`

### Customize configuration

See [Configuration Reference](https://cli.vuejs.org/config/).
