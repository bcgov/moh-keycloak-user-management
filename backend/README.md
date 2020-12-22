# User Management Service

The "User Management Service" is the API backend for the [User Management Console](../frontend/README.md). It's also a service proxy for the [Keycloak Administration REST API](https://www.keycloak.org/docs-api/9.0/rest-api/index.html).

It provides the following design benefits over using the Keycloak REST API directly:
* Ability to implement custom fine-grained access control.
* Doesn't expose the Keycloak REST API which could allow more access than intended.
* Ability to extended or enhance the Keycloak REST API for future use cases.

(Note that Keycloak does have a "technology preview" feature called [Fine Grain Admin Permissions](https://www.keycloak.org/docs/latest/server_admin/#_fine_grain_permissions). We evaluated this feature before implementing this application.)

# Prerequisites

Tested with:
* Java 11
* Maven 3.6.1
* Keycloak 9.0.2

# Configuration

For local development, specify Keycloak details using the configuration file at [src/main/resources/application.yaml](src/main/resources/application.yaml). For deploys to other environments, we suggest [external properties](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config-files).

# Run

To start the application run:
```
./mvnw spring-boot:run
```

To create an executable JAR run:
```
mvn clean package
```

To run the JAR:
```
java -jar target/<jar-file-name>.jar
```

# Integration tests

The tests depend on the MoH Development Keycloak server. You can run the tests with:
```
mvn test
```