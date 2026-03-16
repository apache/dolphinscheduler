# Amazon EMR Serverless

## Overview

Amazon EMR Serverless task type, for submitting and monitoring job runs on [Amazon EMR Serverless](https://docs.aws.amazon.com/emr/latest/EMR-Serverless-UserGuide/emr-serverless.html) applications.
Unlike traditional EMR on EC2, EMR Serverless requires no cluster infrastructure management and automatically scales compute resources on demand, suitable for Spark and Hive workloads.

Using [aws-java-sdk](https://aws.amazon.com/cn/sdk-for-java/) in the background code, to transfer JSON parameters to a [StartJobRunRequest](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/emrserverless/model/StartJobRunRequest.html) object
and submit it to AWS via the [StartJobRun API](https://docs.aws.amazon.com/emr-serverless/latest/APIReference/API_StartJobRun.html), then poll job status via the [GetJobRun API](https://docs.aws.amazon.com/emr-serverless/latest/APIReference/API_GetJobRun.html) until completion.

## Create Task

- Click `Project Management -> Project Name -> Workflow Definition`, click the `Create Workflow` button to enter the DAG editing page.
- Drag `AmazonEMRServerless` task from the toolbar to the artboard to complete the creation.

## Task Parameters

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- Please refer to [DolphinScheduler Task Parameters Appendix]&#40;appendix.md#default-task-parameters&#41; `Default Task Parameters` section for default parameters.)

- Please refer to [DolphinScheduler Task Parameters Appendix](appendix.md) `Default Task Parameters` section for default parameters.

|      **Parameter**      |                                                                                                                                                                                                 **Description**                                                                                                                                                                                                  |
|-------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Application Id          | EMR Serverless application ID (e.g. `00fkht2eodujab09`), obtainable from the [EMR Serverless Console](https://console.aws.amazon.com/emr/home#/serverless)                                                                                                                                                                                                                                                       |
| Execution Role Arn      | ARN of the IAM role for job execution (e.g. `arn:aws:iam::123456789012:role/EMRServerlessRole`), this role needs permissions to access S3, Glue, and other services                                                                                                                                                                                                                                              |
| Job Name                | Job name (optional), used to identify the job in the EMR Serverless console                                                                                                                                                                                                                                                                                                                                      |
| StartJobRunRequest JSON | JSON corresponding to the `JobDriver` and `ConfigurationOverrides` portions of the [StartJobRunRequest](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/emrserverless/model/StartJobRunRequest.html), see examples below. **Note**: `ApplicationId` and `ExecutionRoleArn` do not need to be included in the JSON as they are automatically injected from the form parameters above |

![RUN_JOB_FLOW](../../../../img/tasks/demo/emr_serverless_create.png)

## Task Example

### Submit a Spark Job

This example shows how to create an `EMR_SERVERLESS` task node to submit a Spark job to an EMR Serverless application.

StartJobRunRequest JSON example (Spark):

```json
{
  "JobDriver": {
    "SparkSubmit": {
      "EntryPoint": "s3://my-bucket/scripts/my-spark-job.jar",
      "EntryPointArguments": [
        "s3://my-bucket/input/",
        "s3://my-bucket/output/"
      ],
      "SparkSubmitParameters": "--class com.example.MySparkApp --conf spark.executor.cores=4 --conf spark.executor.memory=8g --conf spark.executor.instances=10"
    }
  },
  "ConfigurationOverrides": {
    "MonitoringConfiguration": {
      "S3MonitoringConfiguration": {
        "LogUri": "s3://my-bucket/emr-serverless-logs/"
      }
    }
  }
}
```

### Submit a Hive Job

This example shows how to create an `EMR_SERVERLESS` task node to submit a Hive query job.

StartJobRunRequest JSON example (Hive):

```json
{
  "JobDriver": {
    "HiveSQL": {
      "Query": "s3://my-bucket/scripts/my-hive-query.sql",
      "Parameters": "--hiveconf hive.exec.dynamic.partition=true --hiveconf hive.exec.dynamic.partition.mode=nonstrict"
    }
  },
  "ConfigurationOverrides": {
    "MonitoringConfiguration": {
      "S3MonitoringConfiguration": {
        "LogUri": "s3://my-bucket/emr-serverless-logs/"
      }
    },
    "ApplicationConfiguration": [
      {
        "Classification": "hive-site",
        "Properties": {
          "hive.metastore.client.factory.class": "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory"
        }
      }
    ]
  }
}
```

## AWS Authentication Configuration

The EMR Serverless task reads AWS credentials from the DolphinScheduler `aws.yaml` configuration file, under the `aws.emr` section at `conf/aws.yaml`.

### Using IAM Role (Recommended)

If the DolphinScheduler Worker node runs on an EC2 instance with an attached IAM Role:

```yaml
aws:
  emr:
    credentials.provider.type: InstanceProfileCredentialsProvider
    region: us-east-1
```

### Using Access Key

If you need to authenticate using AK/SK:

```yaml
aws:
  emr:
    credentials.provider.type: AWSStaticCredentialsProvider
    access.key.id: your-access-key-id
    access.key.secret: your-secret-access-key
    region: us-east-1
```

> **Note**: The `aws.emr` section configuration is shared by both EMR on EC2 and EMR Serverless task types.

## Job State Transitions

After an EMR Serverless job is submitted, DolphinScheduler polls the job status every 10 seconds:

```
SUBMITTED → PENDING → SCHEDULED → RUNNING → SUCCESS
                                           → FAILED
                                           → CANCELLED
```

- When a job reaches `SUCCESS` state, the task is marked as successful
- When a job reaches `FAILED` or `CANCELLED` state, the task is marked as failed
- If a DolphinScheduler task is killed, it automatically calls the [CancelJobRun API](https://docs.aws.amazon.com/emr-serverless/latest/APIReference/API_CancelJobRun.html) to cancel the running job

## Notice

- The **Application Id** must correspond to a pre-existing EMR Serverless application (created via the AWS Console or API) in `STARTED` or `CREATED` state
- The **Execution Role** requires the following minimum permissions: `emr-serverless:StartJobRun`, `emr-serverless:GetJobRun`, `emr-serverless:CancelJobRun`, plus S3, Glue and other data access permissions required by the job
- `StartJobRunRequest JSON` should NOT include `ApplicationId` or `ExecutionRoleArn` fields — they are automatically injected from the form parameters
- EMR Serverless task supports failover: when a Worker node fails, a new Worker can recover tracking of running jobs through `appIds` (the `jobRunId`)

