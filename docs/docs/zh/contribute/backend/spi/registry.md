### DolphinScheduler Registry SPI 扩展

#### 如何使用？

进行以下配置（以 zookeeper 为例）

* 注册中心插件配置, 以Zookeeper 为例 (registry.properties)
  dolphinscheduler-service/src/main/resources/registry.properties

  ```registry.properties
  registry.plugin.name=zookeeper
  registry.servers=127.0.0.1:2181
  ```

具体配置信息请参考具体插件提供的参数信息，例如 zk：`org/apache/dolphinscheduler/plugin/registry/zookeeper/ZookeeperConfiguration.java`
所有配置信息前缀需要 +registry，如 base.sleep.time.ms，在 registry 中应该这样配置：registry.base.sleep.time.ms=100

#### 如何扩展

`dolphinscheduler-registry-api` 定义了实现插件的标准，当你需要扩展插件的时候只需要实现 `org.apache.dolphinscheduler.registry.api.RegistryFactory` 即可。

`dolphinscheduler-registry-plugin` 模块下是我们目前所提供的注册中心插件。

#### FAQ

1：registry connect timeout

可以增加相关超时参数。
