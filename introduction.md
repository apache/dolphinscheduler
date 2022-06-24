This page describes details regarding Datasource screen in Apache DolphinScheduler. Here, you will see all the functions which can be handled in this screen. The following table explains commonly used terms in Apache DolphinScheduler:

| Glossary | |
| --- | ---|
| Datasource | A data source is the initial location where data is created or where information is first digitized. Also, the most refined data may be used as a source, as long as another process accesses and utilizes it. |

The data source center supports MySQL, POSTGRESQL, HIVE/IMPALA, SPARK, CLICKHOUSE, ORACLE, SQLSERVER and other data sources.

*   Click "DataSource Center -> Create DataSource" to create different types of data sources according to your needs.
*   Click "Test Connection" to test whether the data source can be successfully connected (the data source can only be saved after the data source passes the connectivity test).

Using a database incompatible with the Apache LICENSE V2 license
----------------------------------------------------------------

In the datasource center, DolphinScheduler has native support for some datasources, but some datasources require users to download the corresponding JDBC driver package and place it in the correct location for normal use. This will increase the user's usage cost, but we have to do it, because the JDBC driver of this part of the data source is not compatible with Apache LICENSE V2, so we cannot include them in the binary package distributed by DolphinScheduler. This part of the datasource mainly includes MySQL, Oracle, SQL Server, etc. Fortunately, we have provided solutions for the support of this part of the data source.

### Sample

Let us take MySQL as an example. If you want to use the MySQL data source, you need to first download the corresponding version of the JDBC driver in the [mysql maven repository](https://repo1.maven.org/maven2/mysql/mysql-connector-java) , move it into the and folder, and finally restart the and service to use the MySQL datasource. If you use the container to start DolphinScheduler, also mount the JDBC driver to the corresponding path of the above two services, and then restart the driver.`api-server/libs``worker-server/libs``api-server``worker-server`

> Note: If you just want to use MySQL in the datasource center, there is no requirement for the version of MySQL JDBC driver. If you want to use MySQL as the metabase of DolphinScheduler, only versions [8.0.16 and above](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar) are supported.