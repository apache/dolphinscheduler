# Introduction

This module is the etcd registry plugin module, this plugin will use etcd as the registry center.

# How to use

If you want to set the registry center as etcd, you need to set the registry properties in master/worker/api's appplication.yml

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

If your etcd server has configured with ssl, about certification files you can see [here](https://github.com/etcd-io/jetcd/blob/main/docs/SslConfig.md) for how to convert.

> If you need ssl certification, you need to make sure your jdk version is newer than Java 8u252 (April 2020), jdk11 works well too. 
>
> By the way, the jdk version in docker images `FROM eclipse-temurin:8-jre` now is 8u362 works well, don't need change.
>
> Because after version 8u252 has native support for ALPN. Detail you can see:
> 
> https://github.com/grpc/grpc-java/issues/5369#issuecomment-751885384

After do this config, you can start your DolphinScheduler cluster, your cluster will use etcd as registry center to
store server metadata.
