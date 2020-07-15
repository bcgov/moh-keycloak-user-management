[![Build Status](https://travis-ci.org/bcgov/moh-keycloak-user-management.svg?branch=master)](https://travis-ci.org/bcgov/moh-keycloak-user-management)

# moh-keycloak-user-management

The "MoH Keycloak User Management" application is a custom user management console for Keycloak implemented as an SPA using Vue.js. Keycloak already has a fully-featured administration console, but this console has been customized to suit our administrator's requirements around features like ease-of-use and data validation.

# Prerequisites

This application requires a Keycloak server. It uses the Keycloak server for OIDC authentication, and it also uses the Keycloak REST API for user management. Detailed Keycloak installation and configuration instructions are not in scope for this README.

# Configuration

 Before deploying the application, specify Keycloak details using the configuration file at `public/keycloak.json`. The included configuration file is valid for the MoH's development environment.

The Keycloak REST API uses roles in the [realm-management client](https://www.keycloak.org/docs/latest/server_admin/#_per_realm_admin_permissions). So for Keycloak client configuration you have two options: either create a new client that has access to the `realm-management` roles, or use the `realm-management` client directly.


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


The end-to-end tests require LDAP user credentials. The username `testcafe`, and the password is in the MoH KeePass. Set the password in an OS environment variable named `TESTCAFE_PASSWORD`. 

Note that these instructions only apply to the MoH's Keycloak server which has an LDAP identify provider configured. To use a different Keycloak server you would need to update the "login" portion of the tests to use your identity provider.

### Lints and fixes files
```
npm run lint
```

### Customize configuration
See [Configuration Reference](https://cli.vuejs.org/config/).
