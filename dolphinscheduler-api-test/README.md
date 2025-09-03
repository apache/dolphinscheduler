# DolphinScheduler Backend API Test

## Page Object Model

DolphinScheduler API test respects
the [Page Object Model (POM)](https://www.selenium.dev/documentation/guidelines/page_object_models/) design pattern.
Every page of DolphinScheduler's api is abstracted into a class for better maintainability.

### Example

The login page's api is abstracted
as [`LoginPage`](dolphinscheduler-api-test-case/src/test/java/org/apache/dolphinscheduler/api/test/pages/LoginPage.java)
, with the following fields,

```java
public HttpResponse login(String username, String password) {
    Map<String, Object> params = new HashMap<>();

    params.put("userName", username);
    params.put("userPassword", password);

    RequestClient requestClient = new RequestClient();

    return requestClient.post("/login", null, params);
}
```

where `userName`, `userPassword` are the main elements on UI that we are interested in.

## Test Environment Setup

DolphinScheduler API test uses [testcontainers](https://www.testcontainers.org) to set up the testing
environment, with docker compose.

Typically, every test case needs one or more `docker-compose.yaml` files to set up all needed components, and expose the
DolphinScheduler UI port for testing. You can use `@DolphinScheduler(composeFiles = "")` and pass
the `docker-compose.yaml` files to automatically set up the environment in the test class.

```java

@DolphinScheduler(composeFiles = "docker/tenant/docker-compose.yaml")
class TenantAPITest {
}
```

## Notes

## Local development

### Mac M1
Add VM options to the test configuration in IntelliJ IDEA:
```
# In this mode you need to install docker desktop for mac and run it with locally
-Dm1_chip=true
```

### Running locally(without Docker)
```
# In this mode you need to start frontend and backend services locally
-Dlocal=true
```

### Running locally(with Docker)
```
# In this mode you only need to install docker locally
```

- To run the tests locally, you need to have the DolphinScheduler running locally. You should add `dolphinscheduler-api-test/pom.xml` to the maven project
  Since it does not participate in project compilation, it is not in the main project.
- Running run test class `org.apache.dolphinscheduler.api.test.cases.TenantAPITest` in the IDE.

---
## Running the OIDC API Test

The OIDC feature includes a dedicated API test suite that uses Testcontainers to spin up a full environment, including DolphinScheduler and a Keycloak OIDC provider.

### Step 1: Build the Local Docker Image

The API test requires a Docker image of DolphinScheduler that includes your latest code changes.

1.  **Build the image**: From the root of the project, run the following command. This creates a standalone server image tagged as `ci`.
    ```bash
    ./mvnw -B clean package -Dmaven.test.skip=true -Dspotless.skip=true -Ddocker.tag=ci -Pdocker,release
    ```
2.  **Verify the image**: Ensure the image `apache/dolphinscheduler-standalone-server:ci` was created successfully.
    ```bash
    docker images | grep "dolphinscheduler-standalone-server"
    ```

### Step 2: Run the Test Environment with Docker Compose

You can manually start the full test stack to inspect it before running the test cases.

1.  Navigate to the OIDC test resource directory:
    ```bash
    cd dolphinscheduler-api-test/dolphinscheduler-api-test-case/src/test/resources/docker/oidc-login/
    ```
2.  Start the services:
    ```bash
    docker-compose up --build
    ```
  * The `--build` flag is recommended to ensure the latest configurations are used.
  * To completely reset the environment (including the database), first run `docker-compose down -v`.

### Step 3: Run the API Test from your IDE

1.  Ensure the `dolphinscheduler-api-test` module is included in your Maven projects in your IDE.
2.  It's recommended to use **Java 11** for running the API tests.
> **Notes**: Ensure that you run the required `RegistryTestCase` and `TenantAPITest` before running the OidcLoginAPITest, as they set up the necessary prerequisites.
3. Navigate to the `OidcLoginAPITest.java` file and run it as a JUnit test. The test framework will automatically manage the Docker containers.
> **Note**: Before running the API test using the IDE run button, ensure that port `8081` is free, as Keycloak will use this port. Also, make sure Docker Desktop is running. If you previously started the environment with `docker-compose up --build`, run `docker-compose down -v` to avoid port conflicts. The API test will automatically manage the required containers.

### Customizing the API Test Environment

You can easily customize the test environment for your own needs:

* **To modify DolphinScheduler's configuration**: Edit the `environment` section for the `dolphinscheduler` service in `docker/oidc-login/docker-compose.yaml` to change OIDC settings or other parameters.
* **To modify the OIDC provider's behavior**: Edit the `docker/oidc-login/realm-export.json` file. You can change client settings, add new test users, or modify group mappers to test different scenarios. After making changes, restart the environment with `docker-compose up --build` to apply them.
