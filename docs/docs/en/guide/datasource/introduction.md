# DataSource

DataSource supports MySQL, PostgreSQL, Hive/Impala, Spark, ClickHouse, Oracle, SQL Server and other DataSources.

- Click bottom "Data Source Center -> Create Data Source" to create a new datasource.
- Click "Test Connection" to test whether the DataSource can connect successfully(datasource can be saved only if passed the
  connection test).

## Using datasource incompatible to Apache LICENSE V2 LICENSE

Some of datasource are native supported to DolphinScheduler while others need users download JDBC driver package manually,
because those JDBC driver incompatible to Apache LICENSE V2 LICENSE. For this reason we have to release DolphinScheduler's
distribute package without those packages, even if this will make more complicated for users. Datasource such as MySQL,
Oracle, SQL Server as the examples, but we have the solution to solve this

### Example

For example, if you want to use MySQL datasource, you need to download the correct JDBC driver from [mysql maven repository](https://repo1.maven.org/maven2/mysql/mysql-connector-java),
and move it into directory `api-server/libs` and `worker-server/libs`. After that, you could activate MySQL datasource by
restarting `api-server` and `worker-server`. Mount to container volume in the same path and restart it if you use container
like Docker.

> Note: If you only want to use MySQL in the datasource center, there is no requirement for the version of MySQL JDBC driver.
> But if you want to use MySQL as the metabase of DolphinScheduler, it only supports [8.0.16 and above](https:/ /repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar) version.
