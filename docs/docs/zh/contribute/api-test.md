# DolphinScheduler — API 测试
## 前置知识：

### API 测试与单元测试的区别

API测试，它模仿用户调用API，从某个入口开始，逐步执行操作，直到完成某项工作。与单元测试不同，后者通常需要测试参数、参数类型、参数值、参数数量、返回值、抛出错误等，目的在于保证特定函数能够在任何情况下都稳定可靠完成工作。单元测试假定只要所有函数都正常工作，那么整个产品就能正常工作。

相对来说，API 测试关注的**一个完整的操作链是否能够完成**。

比如，租户管理界面的 API 测试，关注用户是否能够正常登录；登陆失败的话，是否能够正确显示错误信息。登陆之后时候能够通过携带的 SessionId 进行租户管理的操作等等。

## API 测试

### API-Pages

DolphinScheduler 的 API 测试使用 docker-compose 部署，当前测试的为单机模式，主要用于检验一些例如“增删改查”基本功能，后期如需做集群验证，例如不同服务之间的协作，或者各个服务之间的通讯机制，可参考 `deploy/docker/docker-compose.yml`来配置。

对于 API 测试，使用 [页面模型](https://www.selenium.dev/documentation/guidelines/page_object_models/) 的形式，主要为每一个页面建立一个对应的模型。下面以登录页为例：

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

在测试过程中，我们只针对所需要关注的接口进行测试，而非页面中的所有接口，所以在登陆页面只对用户名、密码和接口路径进行声明。

此外，在测试过程中，并不会直接去操作接口，一般选择封装对应的方法，以达到复用的效果。例如想要登录的话，直接传入用户名和密码，通过 `public LoginPage login()` 方法去操作所传入的信息，从而达到实现登录的效果。

在登陆页面（LoginPage）只定义接口请求的入参规范，对于接口请求出参只定义统一的基础响应结构，接口实际返回的data数据则再实际的测试用例中测试。主要测试接口的输入和输出是否能够符合测试用例的要求。


### API-Cases


下面以租户管理测试为例，前文已经说明，我们使用 docker-compose 进行部署，所以每个测试案例，都需要以注解的形式引入对应的文件。

使用 OkHttpClient 框架来进行 HTTP 请求。在每个测试案例开始之前都需要进行一些准备工作。比如：登录用户、创建对应的租户（根据具体的测试案例而定）。

```java
    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(user, password);

        sessionId = JSONUtils.convertValue(loginHttpResponse.body().data(), LoginResponseData.class).sessionId();
    }
```

在完成准备工作之后，就是正式的测试案例编写。我们使用 @Order() 注解的形式，用于模块化，确认测试顺序。在进行测试之后，使用断言来判断测试是否成功，如果断言返回 true，则表示创建租户成功。可参考创建租户的测试代码：

```java
    @Test
    @Order(1)
    public void testCreateTenant() {
        TenantPage tenantPage = new TenantPage();

        HttpResponse createTenantHttpResponse = tenantPage.createTenant(sessionId, tenant, 1, "");

        Assertions.assertTrue(createTenantHttpResponse.body().success());
    }
```

其余的都是类似的情况，可参考具体的源码来理解。

https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-api-test/dolphinscheduler-api-test-case/src/test/java/org/apache/dolphinscheduler/api.test/cases


## 补充

在本地运行的时候，首先需要启动相应的本地服务，可以参考该页面: [环境搭建](https://dolphinscheduler.apache.org/zh-cn/development/development-environment-setup.html)

在本地运行 API 测试的时候，可以配置 `-Dlocal=true` 参数，用于连接本地，方便对于 UI 界面的更改。

当前默认的请求超时时长为 10 秒，如无特殊需求不应修改此值。
