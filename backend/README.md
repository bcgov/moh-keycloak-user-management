The Keycloak User Management Service functions as an API service layer for the Keycloak User Management application. 

The Keycloak APIs do not offer the level of fine-grained security needed to restrict users' access, allowing them to modify just their assigned Client ID's configuration information. To mitigate this potential risk, we propose creating Custom Service API that can restrict access to the underlying Keycloak APIs.
In some cases the Keycloak API’s are also limited in their ability to provide fine grained searching and filtering of data.

The Service API is meant to extend capabilities beyond what’s offered directly by Keycloak. 

Configuration
This application uses Spring Boot and requires JDK 11 and Maven to run.

To start the application run:
./mvnw spring-boot:run

To create an executable jar run:
mvn clean package
java -jar target/<jar-file-name>.jar

Authentication and Authorization

Uses
Keycloak 'realm-management' functionality currently included exposed by the API Service: 
view-groups
