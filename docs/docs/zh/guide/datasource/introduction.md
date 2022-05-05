# 数据源

数据源中心支持MySQL、POSTGRESQL、HIVE/IMPALA、SPARK、CLICKHOUSE、ORACLE、SQLSERVER等数据源。

- 点击"数据源中心->创建数据源"，根据需求创建不同类型的数据源
- 点击"测试连接"，测试数据源是否可以连接成功（只有当数据源通过连接性测试后才能保存数据源）。

## 使用不兼容 Apache LICENSE V2 许可的数据库

数据源中心里，DolphinScheduler 对部分数据源有原生的支持，但是部分数据源需要用户下载对应的 JDBC 驱动包并放置到正确的位置才能正常使用。
这对用户会增加用户的使用成本，但是我们不得不这么做，因为这部分数据源的 JDBC 驱动和 Apache LICENSE V2 不兼容，所以我们无法在 
DolphinScheduler 分发的二进制包中包含他们。这部分数据源主要包括 MySQL，Oracle，SQL Server 等，幸运的是我们为这部分数据源的支持给出了解决方案。

### 样例

我们以 MySQL 为例，如果你想要使用 MySQL 数据源，你需要先在 [mysql maven 仓库](https://repo1.maven.org/maven2/mysql/mysql-connector-java)
中下载对应版本的 JDBC 驱动，将其移入 `api-server/libs` 以及 `worker-server/libs` 文件夹中，最后重启 `api-server` 和 `worker-server`
服务，即可使用 MySQL 数据源。如果你使用容器启动 DolphinScheduler，同样也是将 JDBC 驱动挂载放到以上两个服务的对应路径下后，重启驱动即可。

> 注意：如果你只是想要在数据源中心使用 MySQL，则对 MySQL JDBC 驱动的版本没有要求，如果你想要将 MySQL 作为 DolphinScheduler 的元数据库，
> 则仅支持 [8.0.16 及以上](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)的版本。
