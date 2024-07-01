# Amazon EMR

## 综述

Amazon EMR 任务类型，用于在AWS上操作EMR集群并执行计算任务。
后台使用 [aws-java-sdk](https://aws.amazon.com/cn/sdk-for-java/) 将JSON参数转换为任务对象，提交到AWS，目前支持两种程序类型：

* `RUN_JOB_FLOW` 使用 [API_RunJobFlow](https://docs.aws.amazon.com/emr/latest/APIReference/API_RunJobFlow.html#API_RunJobFlow_Examples) 提交 [RunJobFlowRequest](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/elasticmapreduce/model/RunJobFlowRequest.html) 对象
* `ADD_JOB_FLOW_STEPS` 使用 [API_AddJobFlowSteps](https://docs.aws.amazon.com/emr/latest/APIReference/API_AddJobFlowSteps.html#API_AddJobFlowSteps_Examples) 提交 [AddJobFlowStepsRequest](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/elasticmapreduce/model/AddJobFlowStepsRequest.html) 对象

## 任务参数

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- 默认参数说明请参考[DolphinScheduler任务参数附录]&#40;appendix.md#默认任务参数&#41;`默认任务参数`一栏。)

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

|     **任务参数**      |                                                                                                                                                          **描述**                                                                                                                                                          |
|-------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| 程序类型              | 选择程序类型，如果是`RUN_JOB_FLOW`，则需要填写`jobFlowDefineJson`，如果是`ADD_JOB_FLOW_STEPS`，则需要填写`stepsDefineJson`                                                                                                                                                                                                                         |
| jobFlowDefineJson | [RunJobFlowRequest](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/elasticmapreduce/model/RunJobFlowRequest.html) 对象对应的JSON，详细JSON定义参见 [API_RunJobFlow_Examples](https://docs.aws.amazon.com/emr/latest/APIReference/API_RunJobFlow.html#API_RunJobFlow_Examples)                          |
| stepsDefineJson   | [AddJobFlowStepsRequest](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/elasticmapreduce/model/AddJobFlowStepsRequest.html) 对象对应的JSON，详细JSON定义参见 [API_AddJobFlowSteps_Examples](https://docs.aws.amazon.com/emr/latest/APIReference/API_AddJobFlowSteps.html#API_AddJobFlowSteps_Examples) |

## 任务样例

### 创建EMR集群并运行Steps

该样例展示了如何创建`RUN_JOB_FLOW`类型`EMR`任务节点，以执行`SparkPi`为例，该任务会创建一个`EMR`集群，并且执行`SparkPi`示例程序。
![RUN_JOB_FLOW](../../../../img/tasks/demo/emr_run_job_flow.png)

jobFlowDefineJson 参数样例

```json
{
  "Name": "SparkPi",
  "ReleaseLabel": "emr-5.34.0",
  "Applications": [
    {
      "Name": "Spark"
    }
  ],
  "Instances": {
    "InstanceGroups": [
      {
        "Name": "Primary node",
        "InstanceRole": "MASTER",
        "InstanceType": "m4.xlarge",
        "InstanceCount": 1
      }
    ],
    "KeepJobFlowAliveWhenNoSteps": false,
    "TerminationProtected": false
  },
  "Steps": [
    {
      "Name": "calculate_pi",
      "ActionOnFailure": "CONTINUE",
      "HadoopJarStep": {
        "Jar": "command-runner.jar",
        "Args": [
          "/usr/lib/spark/bin/run-example",
          "SparkPi",
          "15"
        ]
      }
    }
  ],
  "JobFlowRole": "EMR_EC2_DefaultRole",
  "ServiceRole": "EMR_DefaultRole"
}
```

### 向运行中的EMR集群添加Step

该样例展示了如何创建`ADD_JOB_FLOW_STEPS`类型`EMR`任务节点，以执行`SparkPi`为例，该任务会向运行中的`EMR`集群添加一个`SparkPi`示例程序。
![ADD_JOB_FLOW_STEPS](../../../../img/tasks/demo/emr_add_job_flow_steps.png)
![JobFlowId](../../../../img/tasks/demo/emr_jobFlowId.png)

stepsDefineJson 参数样例

```json
{
  "JobFlowId": "j-3V628TKAERHP8",
  "Steps": [
    {
      "Name": "calculate_pi",
      "ActionOnFailure": "CONTINUE",
      "HadoopJarStep": {
        "Jar": "command-runner.jar",
        "Args": [
          "/usr/lib/spark/bin/run-example",
          "SparkPi",
          "15"
        ]
      }
    }
  ]
}
```

## 注意事项：

- EMR 任务类型的故障转移尚未实现。目前，DolphinScheduler 仅支持对 yarn task type 进行故障转移。其他任务类型，如 EMR 任务、k8s 任务尚未准备好。
- `stepsDefineJson` 一个任务定义仅支持关联单个step，这样可以更好的保证任务状态的可靠性。

