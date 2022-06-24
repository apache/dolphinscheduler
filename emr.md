# Amazon EMR

Overview
--------

Amazon EMR task type, for creating EMR clusters on AWS and running computing tasks. Using [aws-java-sdk](https://aws.amazon.com/cn/sdk-for-java/) in the background code, to transfer JSON parameters to [RunJobFlowRequest](https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/elasticmapreduce/model/RunJobFlowRequest.html) object and submit to AWS.

Create Task
-----------

*   Click `Project Management -> Project Name -> Workflow Definition`, click the "`Create Workflow`" button to enter the DAG editing page.
*   Drag `AmazonEMR` task from the toolbar to the artboard to complete the creation.

Task Parameters
---------------

<table class="relative-table wrapped confluenceTable" style="width: 100.0%;"><colgroup><col style="width: 12.8234%;"><col style="width: 87.1905%;"></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">Node Name</td><td class="confluenceTd"><p>Set the name of the task.<span>&nbsp;</span>Node names within a workflow definition are unique.</p></td></tr><tr><td class="confluenceTd">Run flag</td><td class="confluenceTd">Indicates whether the node can be scheduled normally. If it is not necessary to execute, you can turn on the prohibiting execution switch.</td></tr><tr><td class="confluenceTd">Description</td><td class="confluenceTd">Describes the function of this node.</td></tr><tr><td colspan="1" class="confluenceTd">Task priority</td><td colspan="1" class="confluenceTd">When the number of worker threads is insufficient, they are executed in order from high to low according to the priority, and they are executed according to the first-in, first-out principle when the priority is the same.</td></tr><tr><td colspan="1" class="confluenceTd">Worker group</td><td colspan="1" class="confluenceTd">The task is assigned to the machines in the worker group for execution. If Default is selected, a worker machine will be randomly selected for execution.</td></tr><tr><td colspan="1" class="confluenceTd">Environment Name</td><td colspan="1" class="confluenceTd">Configure the environment in which to run the script.</td></tr><tr><td colspan="1" class="confluenceTd">Number of failed retries</td><td colspan="1" class="confluenceTd">The number of times the task is resubmitted after failure. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Failure Retry Interval</td><td colspan="1" class="confluenceTd">The time interval for resubmitting the task if the task fails. It supports drop-down and manual filling.</td></tr><tr><td colspan="1" class="confluenceTd">Timeout alarm</td><td colspan="1" class="confluenceTd">Check Timeout Alarm and Timeout Failure. When the task exceeds the "timeout duration", an alarm email will be sent and the task execution will fail.</td></tr><tr><td colspan="1" class="confluenceTd">JSON</td><td colspan="1" class="confluenceTd"><ul><li>JSON corresponding to the<span>&nbsp;</span><a style="text-decoration: none;" href="https://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/elasticmapreduce/model/RunJobFlowRequest.html" class="external-link" rel="nofollow">RunJobFlowRequest</a><span>&nbsp;</span>object, for details refer to<span>&nbsp;</span><a href="https://docs.aws.amazon.com/emr/latest/APIReference/API_RunJobFlow.html#API_RunJobFlow_Examples" style="text-decoration: none;" class="external-link" rel="nofollow">API_RunJobFlow_Examples</a>.</li></ul></td></tr></tbody></table>

JSON example
------------

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