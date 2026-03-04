# OIDC 로컬 개발 설정(Keycloak 사용)

OIDC 인증 기능을 개발하거나 테스트하는 경우 로컬 OIDC 공급자가 필요합니다.이 가이드에서는 Docker를 사용하여 **Keycloak**을 설정하고 DolphinScheduler 개발을 위해 구성하는 방법을 설명합니다.

## 전제 조건

* [Docker](https://www.docker.com/products/docker-desktop/)가 설치되어 실행 중입니다.
* 이미 DolphinScheduler 저장소를 복제했으며 프로젝트를 빌드할 수 있습니다.

## 1단계: 사전 구성된 영역으로 Keycloak 시작

편의를 위해 필요한 클라이언트, 사용자 및 그룹을 설정하는 사전 구성된 Keycloak 영역 내보내기를 제공합니다.

1. Keycloak 구성이 있는 **API 테스트 리소스 디렉터리로 이동**합니다.```bash
cd dolphinscheduler-api-test/dolphinscheduler-api-test-case/src/test/resources/docker/oidc-login/
````

2. **Docker Compose를 사용하여 Keycloak 시작**:
이 디렉터리에 제공된 `docker-compose.yaml`은 Keycloak을 시작하고 영역을 자동으로 가져오도록 구성됩니다.```bash
docker-compose up -d keycloak
````

이 명령은 다른 서비스와의 충돌을 피하기 위해 '8081' 포트에서 Keycloak 컨테이너를 시작하고 'realm-export.json'을 가져옵니다.

## 2단계: Keycloak 액세스 및 확인

1. 브라우저를 열고 **Keycloak 관리 콘솔**: `http://localhost:8081`로 이동합니다.
2. 사용자 이름 `admin`과 비밀번호 `admin`으로 로그인합니다.
3. 왼쪽 상단에서 '마스터' 영역에서 '돌핀스케줄러' 영역으로 전환합니다.
4. **클라이언트**(`dolphinscheduler-client`), **사용자**(`admin_user`, `general_user`) 및 **그룹**(`dolphinscheduler-admins`)을 탐색하여 가져온 구성을 확인할 수 있습니다.

## 3단계: DolphinScheduler API 서버 구성

`dolphinscheduler-api/src/main/resources/application.yaml`을 수정하여 OIDC를 활성화하고 로컬 Keycloak 인스턴스에 연결하세요.```yaml
security:
    authentication:
        type: OIDC
        oidc:
            enable: true
            providers:
                keycloak:
                    display-name: "Login with Keycloak"
                    # Point to your local Keycloak realm
                    issuer-uri: http://localhost:8080/realms/dolphinscheduler
                    client-id: dolphinscheduler-client
                    client-secret: dolphinscheduler-client-secret
                    scope: openid, profile, email, groups
                    user-name-attribute: preferred_username
                    groups-claim: groups
            user:
                auto-create: true
                default-tenant-code: "default"
                default-queue: "default"
                user-type: "ADMIN_USER"
````

> **참고**:
> 1. Keycloak 컨테이너의 외부 포트가 '8081'임에도 불구하고 내부 발급자 URL은 여전히 '8080' 포트를 기반으로 합니다.Keycloak 자체 내에서 발급자 URL을 수정하지 않은 한 `application.yaml`의 구성은 `http://localhost:8080`을 사용해야 합니다.
> 2. `범위: openid, 프로필, 이메일, 그룹`
> - `openid`: OIDC의 경우 필수입니다.
> - `profile`: `preferred_username` 또는 `name`과 같이 사용자 이름에 사용되는 클레임을 제공하는 경우가 많습니다.
> - `email`: `email` 클레임을 제공합니다.
> - `groups`: 사용자의 역할/그룹 멤버십을 검색하는 데 필요한 공통(때때로 맞춤 설정) 범위

## 4단계: DolphinScheduler 서비스 시작

"일반 모드" 가이드에 설명된 대로 IDE에서 백엔드 서비스를 시작하고 최소한 다음을 시작했는지 확인하세요.
* `마스터서버`
* `ApiApplicationServer`

또는

* `StandaloneServer`만 시작합니다(독립형 모드를 선호하는 경우).

## 5단계: 프런트엔드 서버 시작

프런트엔드 개발 서버를 실행합니다.```bash
cd dolphinscheduler-ui
pnpm install
pnpm run dev
````

이제 'http://localhost:5173'에서 UI에 액세스할 수 있으며, 여기에 'Keycloak으로 로그인' 버튼이 표시됩니다.

## 개발자 추천

코드를 GitHub에 푸시하기 전에 `사전 커밋`을 설정하는 것이 좋습니다(`docs/docs/en/contribute/development-environment-setup.md` 참조).'사전 커밋' 관련 문제가 발생하는 경우 CI 오류를 방지하고 코드 품질을 유지하려면 항상 다음 검사를 수동으로 수행하세요.

* **백엔드 변경 사항**: Spotless를 실행하여 Java 코드 형식을 지정하세요.```bash
./mvnw spotless:apply
````

* **프런트엔드 변경 사항**: Linter를 실행하여 TypeScript/Vue 코드 형식을 지정합니다.```bash
cd dolphinscheduler-ui
pnpm run lint
````

* **보안**: 잠재적인 보안 취약점을 조기에 검색하려면 IDE에서 SonarQube 플러그인을 사용하는 것이 좋습니다.