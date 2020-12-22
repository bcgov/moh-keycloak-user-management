[![Build Status](https://travis-ci.org/bcgov/moh-keycloak-user-management.svg?branch=master)](https://travis-ci.org/bcgov/moh-keycloak-user-management)

# User Management Console

The "User Management Console" is custom user management console for Keycloak. Keycloak already has a fully-featured administration console, but this console has been customized to suit our administrator's requirements for ease-of-use, data validation, and fine-grained administrative access control.

# Prerequisites

This application requires a Keycloak server and the User Management Service. It uses the Keycloak server for OIDC authentication, and it uses the User Management Service API for user management. Keycloak installation and configuration instructions are not in scope for this README. Installation instructions for the User Management Service are at [/backend/README.md](../backend/README.md).

(The User Management Service implements the same API as the Keycloak Administration REST API, so it is possible to configure the User Management Console to use Keycloak without deploying the User Management Service. This is only noted for posterity and is not supported or tested.)

# Configuration

Specify Keycloak details using the configuration file at [public/keycloak.json](public/keycloak.json). Specify User Management Service Details at [public/config.json](public/config.json). The included configuration files are valid for the MoH's development environment.


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
