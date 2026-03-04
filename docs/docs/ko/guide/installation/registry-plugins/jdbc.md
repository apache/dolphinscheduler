# 소개

이 플러그인은 jdbc를 레지스트리 센터로 사용합니다.데이터베이스를 사용할 예정
api'yaml 기본값의 DolphinScheduler와 구성이 동일합니다.

# 사용방법

1. 데이터베이스 테이블 초기화

- Mysql을 사용하는 경우 SQL 스크립트 `src/main/resources/mysql_registry_init.sql`을 직접 실행할 수 있습니다.

- Postgresql을 사용하는 경우 SQL 스크립트 `src/main/resources/postgresql_registry_init.sql`을 직접 실행할 수 있습니다.

2. 구성 변경

master/worker/api의 application.yml에서 레지스트리 속성을 설정해야 합니다.```yaml
registry:
  type: jdbc
````

이 두 단계를 수행한 후 DolphinScheduler 클러스터를 시작할 수 있습니다. 클러스터는 mysql을 레지스트리 센터로 사용하여
서버 메타데이터를 저장합니다.

참고: mysql 데이터베이스를 사용하는 경우 `mysql-connector-java.jar`을 DS 클래스 경로에 추가해야 합니다.
이 드라이버를 배포판에 묶습니다.
자세하게 알 수 있어요
<a href="https://dolphinscheduler.apache.org/en-us/docs/3.1.2/guide/installation/pseudo-cluster">초기화 정보
데이터베이스</a>.

## 선택적 구성```yaml
registry:
  type: jdbc
  # Used to schedule refresh the heartbeat.
  heartbeat-refresh-interval: 3s
  # Once the client's heartbeat is not refresh in this time, the server will consider the client is offline.
  session-timeout: 60s
````

## 작업자용 DataSource 설정

작업자 서버에는 데이터 소스가 포함되어 있지 않으므로 작업자용 데이터 소스를 구성해야 합니다.

작업자의 application.yml에서 레지스트리 hikari-config 속성을 설정해야 합니다.

### MySQL을 레지스트리 센터로 사용```yaml
registry:
  type: jdbc
  heartbeat-refresh-interval: 3s
  session-timeout: 60s
  hikari-config:
    jdbc-url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler
    username: root
    password: root
    maximum-pool-size: 5
    connection-timeout: 9000
    idle-timeout: 600000
````

### Postgresql을 레지스트리 센터로 사용```yaml
registry:
  type: jdbc
  heartbeat-refresh-interval: 3s
  session-timeout: 60s
  hikari-config:
    jdbc-url: jdbc:postgresql://localhost:5432/dolphinscheduler
    username: root
    password: root
    maximum-pool-size: 5
    connection-timeout: 9000
    idle-timeout: 600000
````