# 介绍

这个插件将使用 Zookeeper 作为注册中心。

# 如何使用

如果要将注册中心设置为 Zookeeper，需要在 master/worker/api 的 application.yml 中设置属性

```yaml
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
```

完成这个配置后，你可以启动你的 DolphinScheduler 集群，你的集群将使用 Zookeeper 作为注册中心存储服务器元数据。
