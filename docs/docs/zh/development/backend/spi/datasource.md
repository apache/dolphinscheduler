## DolphinScheduler Datasource SPI 主要设计

#### 如何使用数据源？

数据源中心默认支持POSTGRESQL、HIVE/IMPALA、SPARK、CLICKHOUSE、SQLSERVER数据源。

如果使用的是MySQL、ORACLE数据源则需要、把对应的驱动包放置lib目录下

#### 如何进行数据源插件开发？

org.apache.dolphinscheduler.spi.datasource.DataSourceChannel
org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory
org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient

1. 第一步数据源插件实现以上接口和继承通用client即可，具体可以参考sqlserver、mysql等数据源插件实现，所有RDBMS插件的添加方式都是一样的。
2. 在数据源插件pom.xml添加驱动配置

我们在 dolphinscheduler-datasource-api 模块提供了所有数据源对外访问的 API

#### **未来计划**

支持kafka、http、文件、sparkSQL、FlinkSQL等数据源

