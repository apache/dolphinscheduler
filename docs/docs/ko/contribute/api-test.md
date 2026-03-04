# DolphinScheduler API 자동화 테스트

## 준비 지식

### API 테스트와 단위 테스트의 차이점

사용자가 API를 호출하는 모습을 흉내내는 API 테스트는 특정 항목부터 시작하여 특정 작업이 완료될 때까지 단계별로 작업을 수행합니다.단위 테스트와 달리 후자는 일반적으로 특정 함수가 어떤 경우에도 안정적이고 안정적으로 작업을 완료할 수 있는지 확인하기 위해 매개변수, 매개변수 유형, 매개변수 값, 매개변수 번호, 반환 값, 오류 발생 등을 테스트해야 합니다.단위 테스트에서는 모든 기능이 제대로 작동하는 한 전체 제품이 작동한다고 가정합니다.

이와 대조적으로 API 테스트는 전체 작업 체인이 완료될 수 있는지 여부에 중점을 둡니다.

예를 들어 테넌트 관리 인터페이스의 API 테스트는 사용자가 정상적으로 로그인할 수 있는지 여부에 중점을 둡니다.로그인에 실패할 경우 오류 메시지가 올바르게 표시될 수 있는지 여부입니다.로그인 후 보유하고 있는 세션아이디를 통해 테넌트 관리 작업을 수행할 수 있습니다.

## API 테스트

### API 페이지

DolphinScheduler의 API 테스트는 docker-compose를 사용하여 배포됩니다.현재 테스트는 독립형 모드로 주로 "추가, 삭제, 변경 및 확인"과 같은 일부 기본 기능을 확인하는 데 사용됩니다.서비스 간 협업이나 서비스 간 통신 메커니즘 등 추가 클러스터 검증을 위해서는 'deploy/docker/docker-compose.yml' 구성을 참조하세요.

API 테스트에는 [페이지 모델](https://www.selenium.dev/documentation/guidelines/page_object_models/) 형식을 사용하며, 주로 각 페이지에 해당하는 모델을 생성합니다.다음은 로그인 페이지의 예입니다.```java
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
````

테스트 과정에서는 페이지의 모든 인터페이스가 아닌 집중해야 할 인터페이스만 테스트합니다.따라서 로그인 페이지에서는 사용자 이름, 비밀번호 및 인터페이스 경로만 선언합니다.

또한 테스트 프로세스 중에는 인터페이스가 직접 요청되지 않습니다.일반적인 선택은 재사용 효과를 얻기 위해 해당 방법을 패키지하는 것입니다.예를 들어 로그인하려는 경우 `public LoginPage login()` 메소드를 통해 사용자 이름과 비밀번호를 입력하여 로그인 효과를 얻기 위해 전달한 요소를 조작합니다. 즉, 사용자가 로그인을 마치면 로그인 효과를 얻습니다.

로그인 페이지에서는 인터페이스 요청의 입력 매개변수 사양만 정의됩니다.인터페이스 요청의 출력 매개변수에는 통일된 기본 응답 구조만 정의됩니다.인터페이스에서 실제로 반환한 데이터는 실제 테스트 케이스에서 테스트됩니다.주요 테스트 인터페이스의 입력 및 출력이 테스트 사례의 요구 사항을 충족할 수 있는지 여부.

### API 사례

다음은 테넌트 관리 테스트의 예입니다.앞서 설명했듯이 배포를 위해 docker-compose를 사용하므로 각 테스트 케이스마다 해당 파일을 주석 형식으로 가져와야 합니다.

인터페이스는 Selenium과 함께 제공되는 RemoteWebDriver를 사용하여 요청됩니다.각 테스트 케이스가 시작되기 전에 수행해야 할 몇 가지 준비 작업이 있습니다.예: 사용자 로그인, 해당 페이지로 이동(특정 테스트 사례에 따라 다름).```java
    @BeforeAll
    public static void setup() {
        LoginPage loginPage = new LoginPage();
        HttpResponse loginHttpResponse = loginPage.login(user, password);

        sessionId = JSONUtils.convertValue(loginHttpResponse.body().data(), LoginResponseData.class).sessionId();
    }
````

준비가 완료되면 공식적인 테스트 케이스 작성을 위한 시간입니다.테스트 순서를 확인하기 위해 모듈화를 위해 @Order() 주석 형식을 사용합니다.테스트가 실행된 후 어설션을 사용하여 테스트가 성공했는지 확인하고, 어설션이 true를 반환하면 테넌트 생성이 성공한 것입니다.다음 코드를 참조로 사용할 수 있습니다.```java
    @Test
    @Order(1)
    public void testCreateTenant() {
        TenantPage tenantPage = new TenantPage();

        HttpResponse createTenantHttpResponse = tenantPage.createTenant(sessionId, tenant, 1, "");

        Assertions.assertTrue(createTenantHttpResponse.body().success());
    }
````

나머지도 비슷한 경우이며, 구체적인 소스코드를 참고하면 이해가 가능하다.

https://github.com/apache/dolphinscheduler/tree/dev/dolphinscheduler-api-test/dolphinscheduler-api-test-case/src/test/java/org/apache/dolphinscheduler/api/test/cases

## 보충제

로컬에서 API 테스트를 실행하려면 먼저 로컬 서비스를 시작해야 합니다. 다음 페이지를 참조하세요.
[개발-환경-설정](./development-environment-setup.md)

API 테스트를 로컬로 실행하는 경우 `-Dlocal=true` 매개변수를 구성하여 로컬로 연결하고 UI 변경을 용이하게 할 수 있습니다.

현재 기본 요청 제한 시간 길이는 10초입니다.이 값은 특별한 요구 사항 없이 수정되어서는 안 됩니다.