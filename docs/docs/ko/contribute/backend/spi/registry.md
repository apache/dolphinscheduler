### DolphinScheduler 레지스트리 SPI 확장

#### 어떻게 사용하나요?

다음 구성을 수행합니다(사육사를 예로 들어 설명).

* 레지스트리 플러그인 구성, Zookeeper를 예로 들어 보겠습니다(registry.properties).
돌고래 스케줄러 서비스/src/main/resources/registry.properties  ```registry.properties
  registry.plugin.name=zookeeper
  registry.servers=127.0.0.1:2181
````

특정 구성 정보는 특정 플러그인에서 제공하는 매개변수 정보를 참조하세요. 예: zk: `org/apache/dolphinscheduler/plugin/registry/zookeeper/ZookeeperConfiguration.java`
모든 구성 정보 접두사는 +registry여야 합니다(예: base.sleep.time.ms). 이는 레지스트리에서 다음과 같이 구성되어야 합니다.

#### 확장 방법

`dolphinscheduler-registry-api`는 플러그인 구현을 위한 표준을 정의합니다.플러그인을 확장해야 하는 경우 `org.apache.dolphinscheduler.registry.api.RegistryFactory`만 구현하면 됩니다.

'dolphinscheduler-registry-plugin' 모듈 아래에는 현재 우리가 제공하는 레지스트리 플러그인이 있습니다.

#### FAQ

1: 레지스트리 연결 시간 초과

관련 시간 초과 매개변수를 늘릴 수 있습니다.