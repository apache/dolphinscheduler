# 건강검진

## 배경

상태 확인은 DolphinScheduler 서비스의 상태를 확인하는 고유한 방법을 제공하도록 설계되었습니다.DB, 캐시, 네트워크 등 모듈의 상태를 포함합니다.

## 엔드포인트

### API-서버```shell
curl --request GET 'http://localhost:12345/dolphinscheduler/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
````

### 마스터 서버```shell
curl --request GET 'http://localhost:5679/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
````

### 작업자-서버```shell
curl --request GET 'http://localhost:1235/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
````

### 경고 서버```shell
curl --request GET 'http://localhost:50053/actuator/health'

{"status":"UP","components":{"db":{"status":"UP","details":{"database":"H2","validationQuery":"isValid()"}}}}
````

> 주의사항: 기본 서비스 포트 및 주소를 수정하는 경우 IP+Port를 수정된 값으로 수정해야 합니다.