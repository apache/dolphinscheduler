# 소개

이 플러그인은 etcd를 레지스트리 센터로 사용합니다.

# 사용방법

레지스트리 센터를 etcd로 설정하려면 master/worker/api의 application.yml에서 레지스트리 속성을 설정해야 합니다.```yaml
registry:
  type: etcd
  endpoints: "http://etcd0:2379, http://etcd1:2379, http://etcd2:2379"
  # The options below have default values
  namespace: dolphinscheduler
  connection-timeout: 9s
  # The unit is milliseconds
  retry-delay: 60ms
  retry-max-delay: 300ms
  retry-max-duration: 1500ms
  # The following ssl options are set according to personal needs
  cert-file: "deploy/kubernetes/dolphinscheduler/etcd-certs/ca.crt"
  key-cert-chain-file: "deploy/kubernetes/dolphinscheduler/etcd-certs/client.crt"
  key-file: "deploy/kubernetes/dolphinscheduler/etcd-certs/client.pem"
  # The following auth options are set according to personal needs
  user: ""
  password: ""
  authority: ""
  load-balancer-policy: ""
````

etcd 서버가 SSL로 구성된 경우, 인증 파일에 대한 변환 방법은 [여기](https://github.com/etcd-io/jetcd/blob/main/docs/SslConfig.md)에서 확인할 수 있습니다.

> SSL 인증이 필요한 경우 jdk 버전이 Java 8u252(2020년 4월)보다 최신인지 확인해야 하며, jdk11도 잘 작동합니다.
>
> 그런데 docker 이미지 `FROM eclipse-temurin:8-jre`의 jdk 버전은 이제 8u362이므로 잘 작동하므로 변경할 필요가 없습니다.
>
> 버전 8u252 이후에는 ALPN이 기본적으로 지원되기 때문입니다.당신이 볼 수 있는 세부 사항:
>
> https://github.com/grpc/grpc-java/issues/5369#issuecomment-751885384

이 구성을 수행한 후 DolphinScheduler 클러스터를 시작할 수 있으며 클러스터는 etcd를 레지스트리 센터로 사용하여
서버 메타데이터를 저장합니다.