### DolphinScheduler Registry SPI Extension

#### how to use?

Make the following configuration (take zookeeper as an example)

* Registry plug-in configuration, take Zookeeper as an example (registry.properties)
  dolphinscheduler-service/src/main/resources/registry.properties

  ```registry.properties
  registry.plugin.name=zookeeper
  registry.servers=127.0.0.1:2181
  ```

For specific configuration information, please refer to the parameter information provided by the specific plug-in, for example zk: `org/apache/dolphinscheduler/plugin/registry/zookeeper/ZookeeperConfiguration.java`
All configuration information prefixes need to be +registry, such as base.sleep.time.ms, which should be configured in the registry as follows: registry.base.sleep.time.ms=100

#### How to expand

`dolphinscheduler-registry-api` defines the standard for implementing plugins. When you need to extend plugins, you only need to implement `org.apache.dolphinscheduler.registry.api.RegistryFactory`.

Under the `dolphinscheduler-registry-plugin` module is the registry plugin we currently provide.

#### FAQ

1: registry connect timeout

You can increase the relevant timeout parameters.
