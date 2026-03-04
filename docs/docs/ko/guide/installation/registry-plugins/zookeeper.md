# 소개

이 플러그인은 사육사를 레지스트리 센터로 사용합니다.

# 사용방법

레지스트리 센터를 사육사로 설정하려면 master/worker/api의 application.yml에서 레지스트리 속성을 설정해야 합니다.```yaml
registry:
  type: zookeeper
  zookeeper:
    namespace: dolphinscheduler
    connect-string: localhost:2181
    retry-policy:
      base-sleep-time: 60ms
      max-sleep: 300ms
      max-retries: 5
    session-timeout: 30s
    connection-timeout: 9s
    block-until-connected: 600ms
    # The following options are set according to personal needs    
    digest: ~
````

이 구성을 수행한 후 DolphinScheduler 클러스터를 시작할 수 있습니다. 클러스터는 사육사를 레지스트리 센터로 사용하여
서버 메타데이터를 저장합니다.