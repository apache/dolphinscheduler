# Aliyun EMR Serverless Spark

## Introduction

`Aliyun EMR Serverless Spark` task plugin submits spark job to
[`Aliyun EMR Serverless Spark`](https://help.aliyun.com/zh/emr/emr-serverless-spark/product-overview/what-is-emr-serverless-spark) service.

## Create Connections

- Click `Datasource -> Create Datasource -> ALIYUN_SERVERLESS_SPARK` to create a connection.

![demo-aliyun-serverless-spark-create-datasource-1](../../../../img/tasks/demo/aliyun_serverless_spark_1.png)

- Fill in `Datasource Name`, `Access Key Id`, `Access Key Secret`, `Region Id` and click `Confirm`.

![demo-aliyun-serverless-spark-create-datasource-2](../../../../img/tasks/demo/aliyun_serverless_spark_2.png)

## Create Tasks

- Click `Porject -> Workflow Definition -> Create Workflow` and drag the `ALIYUN_SERVERLESS_SPARK` task to the canvas.

![demo-aliyun-serverless-spark-create-task-1](../../../../img/tasks/demo/aliyun_serverless_spark_3.png)

- Fill in the task parameters and click `Confirm` to create the task node.

![demo-aliyun-serverless-spark-create-task-2](../../../../img/tasks/demo/aliyun_serverless_spark_4.png)

## Task Parameters

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

|     **Parameters**      |                                           **Description**                                           |
|-------------------------|-----------------------------------------------------------------------------------------------------|
| Datasource types        | The type of datasource the task uses, should be `ALIYUN_SERVERLESS_SPARK`.                          |
| Datasource instances    | The instance of `ALIYUN_SERVERLESS_SPARK` datasource.                                               |
| workspace id            | `Aliyun Serverless Spark` workspace id.                                                             |
| resource queue id       | `Aliyun Serverless Spark` resource queue the task uses to submit spark job.                         |
| code type               | `Aliyun Serverless Spark` code type, could be `JAR`, `PYTHON` or `SQL`.                             |
| job name                | `Aliyun Serverless Spark` job name.                                                                 |
| entry point             | The location of the job code such as jar package, python file, or sql file. OSS location supported. |
| entry point arguments   | Arguments of the job main program.                                                                  |
| spark submit parameters | Spark-submit related parameters.                                                                    |
| engine release version  | Spark engine release version.                                                                       |
| is production           | Whether the spark job runs in production or development environment.                                |

