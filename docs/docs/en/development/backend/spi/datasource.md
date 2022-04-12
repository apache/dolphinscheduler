## DolphinScheduler Datasource SPI main design

#### How do I use data sources?

The data source center supports POSTGRESQL, HIVE/IMPALA, SPARK, CLICKHOUSE, SQLSERVER data sources by default.

If you are using MySQL or ORACLE data source, you need to place the corresponding driver package in the lib directory

#### How to do Datasource plugin development?

org.apache.dolphinscheduler.spi.datasource.DataSourceChannel
org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory
org.apache.dolphinscheduler.plugin.datasource.api.client.CommonDataSourceClient

1. In the first step, the data source plug-in can implement the above interfaces and inherit the general client. For details, refer to the implementation of data source plug-ins such as sqlserver and mysql. The addition methods of all RDBMS plug-ins are the same.

2. Add the driver configuration in the data source plug-in pom.xml

We provide APIs for external access of all data sources in the dolphin scheduler data source API module

#### **Future plan**

Support data sources such as kafka, http, files, sparkSQL, FlinkSQL, etc.