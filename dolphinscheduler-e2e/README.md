# DolphinScheduler End-to-End Test

## Page Object Model

DolphinScheduler End-to-End test respects
the [Page Object Model (POM)](https://www.selenium.dev/documentation/guidelines/page_object_models/) design pattern.
Every page of DolphinScheduler is abstracted into a class for better maintainability.

### Example

The login page is abstracted
as [`LoginPage`](dolphinscheduler-e2e-case/src/test/java/org/apache/dolphinscheduler/e2e/pages/LoginPage.java), with the
following fields,

```java
public final class LoginPage {
    @FindBy(id = "inputUsername")
    private WebElement inputUsername;

    @FindBy(id = "inputPassword")
    private WebElement inputPassword;

    @FindBy(id = "btnLogin")
    private WebElement buttonLogin;
}
```

where `inputUsername`, `inputPassword` and `buttonLogin` are the main elements on UI that we are interested in. They are
annotated with `FindBy` so that the test framework knows how to locate the elements on UI. You can locate the elements
by `id`, `className`, `css` selector, `tagName`, or even `xpath`, please refer
to [the JavaDoc](https://www.selenium.dev/selenium/docs/api/java/org/openqa/selenium/support/FindBy.html).

**Note:** for better maintainability, it's essential to add some convenient `id` or `class` on UI for the wanted
elements if needed, avoid using too complex `xpath` selector or `css` selector that is not maintainable when UI have
styles changes.

With those fields declared, we should also initialize them with a web driver. Here we pass the web driver into the
constructor and invoke `PageFactory.initElements` to initialize those fields,

```java
public final class LoginPage {
    // ...
    public LoginPage(RemoteWebDriver driver) {
        this.driver = driver;

        PageFactory.initElements(driver, this);
    }
}
```

then, all those UI elements are properly filled in.

## Test Environment Setup

DolphinScheduler End-to-End test uses [testcontainers](https://www.testcontainers.org) to set up the testing
environment, with docker compose.

Typically, every test case needs one or more `docker-compose.yaml` files to set up all needed components, and expose the
DolphinScheduler UI port for testing. You can use `@DolphinScheduler(composeFiles = "")` and pass
the `docker-compose.yaml` files to automatically set up the environment in the test class.

```java

@DolphinScheduler(composeFiles = "docker/tenant/docker-compose.yaml")
class TenantE2ETest {
}
```

You can get the web driver that is ready for testing in the class by adding a field of type `RemoteWebDriver`, which
will be automatically injected via the testing framework.

```java

@DolphinScheduler(composeFiles = "docker/tenant/docker-compose.yaml")
class TenantE2ETest {
    private RemoteWebDriver browser;
}
```

Then the field `browser` can be used in the test methods.

```java

@DolphinScheduler(composeFiles = "docker/tenant/docker-compose.yaml")
class TenantE2ETest {
    private RemoteWebDriver browser;

    @Test
    void testLogin() {
        final LoginPage page = new LoginPage(browser); // <<-- use the browser injected
    }
}
```

## Notes

- For UI tests, it's common that the pages might need some time to load, or the operations might need some time to
  complete, we can use `await().untilAsserted(() -> {})` to wait for the assertions.

