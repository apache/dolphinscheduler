# 介绍

这个插件将使用 ETCD 作为注册中心。

# 如何使用

如果要将注册中心设置为 ETCD，需要在 master/worker/api 的 application.yml 中设置属性

```yaml
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
```

如果你的 ETCD 服务器配置了 SSL，关于认证文件你可以看[这里](https://github.com/etcd-io/jetcd/blob/main/docs/SslConfig.md)了解如何转换。

> 如果您需要ssl认证，您需要确保您的 JDK 版本比 Java 8u 252(2020年4月) 更新，JDK11 也可以很好地工作.
>
> 另外，docker images ` from eclipse-temurin:8-JRE ` 中的jdk版本现在是 8u362 运行良好，不需要改动。
>
> 因为8u252之后的版本已经有了对ALPN的原生支持，您可以看到的细节:
>
> https://github.com/grpc/grpc-java/issues/5369#issuecomment-751885384

完成这个配置后，你可以启动你的 DolphinScheduler 集群，你的集群将使用 ETCD 作为注册中心存储服务器元数据。
