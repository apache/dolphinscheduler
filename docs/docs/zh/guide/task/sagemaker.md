# SageMaker 节点

## 综述

[Amazon SageMaker](https://aws.amazon.com/cn/pm/sagemaker) 是一个云机器学习平台。 提供了完整的基础设施，工具和工作流来帮助用户可以创建、训练和发布机器学习模型。

[Amazon SageMaker Model Building Pipelines](https://docs.aws.amazon.com/sagemaker/latest/dg/pipelines.html) 是一个可以直接使用SageMaker各种集成的机器学习管道构建工具，用户可以使用使用 Amazon SageMaker Pipeline 来构建端到端的机器学习系统。

对于使用大数据与人工智能的用户，SageMaker 任务组件帮助用户可以串联起大数据工作流与SagaMaker的使用场景。

DolphinScheduler SageMaker 组件的功能: 
- 启动 SageMaker Pipeline Execution，并持续获取状态，直至Pipeline执行完成。 

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的 <img src="../../../../img/tasks/icons/sagemaker.png" width="15"/> 任务节点到画板中。


## 任务样例

首先介绍一些DS通用参数

- **节点名称** ：设置任务的名称。一个工作流定义中的节点名称是唯一的。
- **运行标志** ：标识这个节点是否能正常调度,如果不需要执行，可以打开禁止执行开关。
- **描述** ：描述该节点的功能。
- **任务优先级** ：worker 线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- **Worker 分组** ：任务分配给 worker 组的机器执行，选择 Default，会随机选择一台 worker 机执行。
- **环境名称** ：配置运行脚本的环境。
- **失败重试次数** ：任务失败重新提交的次数。
- **失败重试间隔** ：任务失败重新提交任务的时间间隔，以分钟为单位。
- **延迟执行时间** ：任务延迟执行的时间，以分钟为单位。
- **超时告警** ：勾选超时告警、超时失败，当任务超过"超时时长"后，会发送告警邮件并且任务执行失败。
- **前置任务** ：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

以上参数如无特殊需求，可以默认即可

- **SagemakerRequestJson**: 启动SageMakerPipeline的需要的请求参数，可见 [AWS API](https://docs.aws.amazon.com/sagemaker/latest/APIReference/API_StartPipelineExecution.html)


组件图示如下：

![sagemaker_pipeline](../../../../img/tasks/demo/sagemaker_pipeline.png)



## 环境配置

需要进行AWS的一些配置，修改`common.properties`中的`xxxxx`为你的配置信息
```yaml
# The AWS access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.access.key.id=<YOUR AWS ACCESS KEY>
# The AWS secret access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.secret.access.key=<YOUR AWS SECRET KEY>
# The AWS Region to use. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.region=<AWS REGION>
```