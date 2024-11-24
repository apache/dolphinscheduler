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

