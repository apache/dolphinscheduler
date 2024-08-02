# Aliyun EMR Serverless Spark

## 简介

`Aliyun EMR Serverless Spark` 任务插件用于向
[`阿里云EMR Serverless Spark`](https://help.aliyun.com/zh/emr/emr-serverless-spark/product-overview/what-is-emr-serverless-spark) 服务提交作业。

## 创建链接

- 点击 `数据源 -> 创建数据源 -> ALIYUN_SERVERLESS_SPARK` 创建链接。

![demo-aliyun-serverless-spark-create-datasource-1](../../../../img/tasks/demo/aliyun_serverless_spark_1.png)

- 填入 `Datasource Name`, `Access Key Id`, `Access Key Secret`, `Region Id` 参数并且点击 `确认`.

![demo-aliyun-serverless-spark-create-datasource-2](../../../../img/tasks/demo/aliyun_serverless_spark_2.png)

## 创建任务节点

- 点击 `项目 -> 工作流定义 -> 创建工作流` 并且将 `ALIYUN_SERVERLESS_SPARK` 任务拖到画板中。

![demo-aliyun-serverless-spark-create-task-1](../../../../img/tasks/demo/aliyun_serverless_spark_3.png)

- 填入相关任务参数并且点击 `确认` 创建任务节点。

![demo-aliyun-serverless-spark-create-task-2](../../../../img/tasks/demo/aliyun_serverless_spark_4.png)

## 任务参数

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

|        **任务参数**         |                          **描述**                          |
|-------------------------|----------------------------------------------------------|
| Datasource types        | 链接类型，应该选择 `ALIYUN_SERVERLESS_SPARK`。                     |
| Datasource instances    | `ALIYUN_SERVERLESS_SPARK` 链接实例。                          |
| workspace id            | `Aliyun Serverless Spark` 工作空间id。                        |
| resource queue id       | `Aliyun Serverless Spark` 任务队列id。                        |
| code type               | `Aliyun Serverless Spark` 任务类型，可以是`JAR`、`PYTHON`或者`SQL`。 |
| job name                | `Aliyun Serverless Spark` 任务名。                           |
| entry point             | 任务代码（JAR包、PYTHON / SQL脚本）的位置，支持OSS中的文件。                  |
| entry point arguments   | 主程序入口参数。                                                 |
| spark submit parameters | Spark-submit相关参数。                                        |
| engine release version  | Spark引擎版本。                                               |
| is production           | Spark任务是否运行在生产环境中。                                       |

## 示例

### 提交jar类型任务

|         **参数名**         |                                                                                            **参数值 / 按钮操作**                                                                                            |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| region id               | cn-hangzhou                                                                                                                                                                                          |
| access key id           | <your-access-key-id>                                                                                                                                                                                 |
| access key secret       | <your-access-key-secret>                                                                                                                                                                             |
| resource queue id       | root_queue                                                                                                                                                                                           |
| code type               | JAR                                                                                                                                                                                                  |
| job name                | ds-emr-spark-jar                                                                                                                                                                                     |
| entry point             | oss://datadev-oss-hdfs-test/spark-resource/examples/jars/spark-examples_2.12-3.3.1.jar                                                                                                               |
| entry point arguments   | 100                                                                                                                                                                                                  |
| spark submit parameters | --class org.apache.spark.examples.SparkPi --conf spark.executor.cores=4 --conf spark.executor.memory=20g --conf spark.driver.cores=4 --conf spark.driver.memory=8g --conf spark.executor.instances=1 |
| engine release version  | esr-2.1-native (Spark 3.3.1, Scala 2.12, Native Runtime)                                                                                                                                             |
| is production           | 请您将按钮打开                                                                                                                                                                                              |

### 提交sql类型任务

|         **参数名**         |                                                                                                       **参数值 / 按钮操作**                                                                                                        |
|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| region id               | cn-hangzhou                                                                                                                                                                                                                 |
| access key id           | <your-access-key-id>                                                                                                                                                                                                        |
| access key secret       | <your-access-key-secret>                                                                                                                                                                                                    |
| resource queue id       | root_queue                                                                                                                                                                                                                  |
| code type               | SQL                                                                                                                                                                                                                         |
| job name                | ds-emr-spark-sql-1                                                                                                                                                                                                          |
| entry point             | 任意非空值                                                                                                                                                                                                                       |
| entry point arguments   | -e#show tables;show tables;                                                                                                                                                                                                 |
| spark submit parameters | --class org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver --conf spark.executor.cores=4 --conf spark.executor.memory=20g --conf spark.driver.cores=4 --conf spark.driver.memory=8g --conf spark.executor.instances=1 |
| engine release version  | esr-2.1-native (Spark 3.3.1, Scala 2.12, Native Runtime)                                                                                                                                                                    |
| is production           | 请您将按钮打开                                                                                                                                                                                                                     |

### 提交oss中的sql脚本任务

|         **参数名**         |                                                                                                        **参数值 / 按钮操作**                                                                                                        |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| region id               | cn-hangzhou                                                                                                                                                                                                                  |
| access key id           | <your-access-key-id>                                                                                                                                                                                                         |
| access key secret       | <your-access-key-secret>                                                                                                                                                                                                     |
| resource queue id       | root_queue                                                                                                                                                                                                                   |
| code type               | SQL                                                                                                                                                                                                                          |
| job name                | ds-emr-spark-sql-2                                                                                                                                                                                                           |
| entry point             | 任意非空值                                                                                                                                                                                                                        |
| entry point arguments   | -f#oss://datadev-oss-hdfs-test/spark-resource/examples/sql/show_db.sql                                                                                                                                                       |
| spark submit parameters | --class org.apache.spark.sql.hive.thriftserver.SparkSQLCLIDriver --conf spark.executor.cores=4 --conf spark.executor.memory=20g --conf spark.driver.cores=4 --conf spark.driver.memory=8g --conf spark.executor.instances=1" |
| engine release version  | esr-2.1-native (Spark 3.3.1, Scala 2.12, Native Runtime)                                                                                                                                                                     |
| is production           | 请您将按钮打开                                                                                                                                                                                                                      |

### 提交pyspark任务

|         **参数名**         |                                                                       **参数值 / 按钮操作**                                                                       |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| region id               | cn-hangzhou                                                                                                                                                |
| access key id           | <your-access-key-id>                                                                                                                                       |
| access key secret       | <your-access-key-secret>                                                                                                                                   |
| resource queue id       | root_queue                                                                                                                                                 |
| code type               | PYTHON                                                                                                                                                     |
| job name                | ds-emr-spark-python                                                                                                                                        |
| entry point             | oss://datadev-oss-hdfs-test/spark-resource/examples/src/main/python/pi.py                                                                                  |
| entry point arguments   | 100                                                                                                                                                        |
| spark submit parameters | --conf spark.executor.cores=4 --conf spark.executor.memory=20g --conf spark.driver.cores=4 --conf spark.driver.memory=8g --conf spark.executor.instances=1 |
| engine release version  | esr-2.1-native (Spark 3.3.1, Scala 2.12, Native Runtime)                                                                                                   |
| is production           | 请您将按钮打开                                                                                                                                                    |

