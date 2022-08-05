# DolphinScheduler API Automation Test

## Preparatory knowledge

### The difference between API Test and Unit Test

API test, which imitates the user calling API, starts from a certain entry and performs operations step by step until a certain work is completed. Different from unit testing, the latter usually needs to test parameters, parameter types, parameter values, parameter numbers, return values, throw errors, etc. in order to ensure that a specific function can complete its work stably and reliably in any case. Unit tests assume that the entire product will work as long as all functions work properly.

In contrast, API testing focuses on whether a complete operation chain can be completed

For example, the API test of the tenant management interface focuses on whether users can log in normally; If the login fails, whether the error message can be displayed correctly. After logging in, you can perform tenant management operations through the sessionid you carry.


## API Test

### API-Pages

DolphinScheduler's API tests are deployed using docker-compose. The current tests are in standalone mode and are mainly used to check some basic functions such as "add, delete, change and check". For further cluster validation, such as collaboration between services or communication mechanisms between services, refer to `deploy/docker/docker-compose.yml` for configuration.

For API test,  the [page model](https://www.selenium.dev/documentation/guidelines/page_object_models/) form is used, mainly to create a corresponding model for each page. The following is an example of a login page.

```java
package org.apache.dolphinscheduler.api.test.pages;


import org.apache.dolphinscheduler.api.test.entity.HttpResponse;
import org.apache.dolphinscheduler.api.test.utils.RequestClient;

import java.util.HashMap;
import java.util.Map;

public final class LoginPage {
    public HttpResponse login(String username, String password) {
        Map<String, Object> params = new HashMap<>();

        params.put("userName", username);
        params.put("userPassword", password);

        RequestClient requestClient = new RequestClient();

        return requestClient.post("/login", null, params);
    }
}
```

During the test process, we only test the interfaces we need to focus on, not all interfaces in the page. Therefore, we only declare the user name, password and interface path on the login page.

In addition, during the testing process, the interface are not requested directly. The general choice is to package the corresponding methods to achieve the effect of reuse. For example, if you want to log in, you input your username and password through the `public LoginPage login()` method to manipulate the elements you pass in to achieve the effect of logging in. That is, when the user finishes logging in, he or she achieve the effect of login.

On the login page, only the input parameter specification of the interface request is defined. For the output parameter of the interface request, only the unified basic response structure is defined. The data actually returned by the interface is tested in the actual test case. Whether the input and output of main test interfaces can meet the requirements of test cases.


### API-Cases

The following is an example of a tenant management test. As explained earlier, we use docker-compose for deployment, so for each test case, we need to import the corresponding file in the form of an annotation.

The interface is requested using the RemoteWebDriver provided with Selenium. Before each test case is started there is some preparation work that needs to be done. For example: logging in the user, jumping to the corresponding page (depending on the specific test case).

```java
    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(user, password);

        sessionId = JSONUtils.convertValue(loginHttpResponse.body().data(), LoginResponseData.class).sessionId();
    }
```

When the preparation is complete, it is time for the formal test case writing. We use a form of @Order() annotation for modularity, to confirm the order of the tests. After the tests have been run, assertions are used to determine if the tests were successful, and if the assertion returns true, the tenant creation was successful. The following code can be used as a reference:

```java
    @Test
    @Order(1)
    public void testCreateTenant() {
        TenantPage tenantPage = new TenantPage();

        HttpResponse createTenantHttpResponse = tenantPage.createTenant(sessionId, tenant, 1, "");

        Assertions.assertTrue(createTenantHttpResponse.body().success());
    }
```

The rest are similar cases and can be understood by referring to the specific source code.

https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-api-test/dolphinscheduler-api-test-case/src/test/java/org/apache/dolphinscheduler/api.test/cases

## Supplements

When running API tests locally, First, you need to start the local service, you can refer to this page: 
[development-environment-setup](https://dolphinscheduler.apache.org/en-us/development/development-environment-setup.html)

When running API tests locally, the `-Dlocal=true` parameter can be configured to connect locally and facilitate changes to the UI.

The current default request timeout length is 10 seconds. This value should not be modified without special requirements.
