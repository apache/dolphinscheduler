# DMS 节点

## 综述

[AWS Database Migration Service (AWS DMS)](https://aws.amazon.com/cn/dms) 可帮助您快速并安全地将数据库迁移至 AWS。
源数据库在迁移过程中可继续正常运行，从而最大程度地减少依赖该数据库的应用程序的停机时间。
AWS Database Migration Service 可以在广泛使用的开源商业数据库之间迁移您的数据。

DMS任务组件帮助用户在DolphinScheduler中创建和启动DMS任务。

组件主要包含两个功能：
- 创建并启动迁移任务
- 重启已存在的迁移任务

组件的使用方式有两种：
- 通过界面创建
- 通过Json数据创建

DolphinScheduler 在 启动DMS 任务后，会跟中DMS任务状态，直至DMS任务完成后才将任务设为成功状态。除了以下情况：

不跟踪无结束时间的CDC任务，即 当迁移类型为 `full-load-and-cdc` 或者 `cdc` 时，且没有配置 `cdcStopPosition` 参数时，DolphinScheduler 在成功启动任务后，则会将任务状态设为 成功。

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的 <img src="../../../../img/tasks/icons/dms.png" width="15"/> 任务节点到画板中。

## 任务样例

组件图示如下：

**创建并启动迁移任务（通过界面）**

![dms](../../../../img/tasks/demo/dms_create_and_start.png)

**重启已存在的迁移任务（通过界面）**

![dms](../../../../img/tasks/demo/dms_restart.png)

**创建并启动迁移任务（通过Json数据）**

![dms](../../../../img/tasks/demo/dms_create_and_start_json.png)

**重启已存在的迁移任务（通过Json数据）**

![dms](../../../../img/tasks/demo/dms_restart_json.png)

### 首先介绍一些DS通用参数

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- 默认参数说明请参考[DolphinScheduler任务参数附录]&#40;appendix.md#默认任务参数&#41;`默认任务参数`一栏。)

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

### DMS组件独有的参数

- **isRestartTask**：是否重启已存在的迁移任务
- **isJsonFormat**：是否使用Json格式的数据创建任务
- **jsonData**：Json格式的数据, 是有`isJsonFormat`为true时才会生效

创建并启动迁移任务时参数

- **migrationType**：迁移类型, 可选值为：[ `full-load`, `full-load-and-cdc`, `cdc`]
- **replicationTaskIdentifier**：迁移任务标识符, 任务名称
- **replicationInstanceArn**：迁移实例的ARN
- **sourceEndpointArn**：源端点的ARN
- **targetEndpointArn**：目标端点的ARN
- **tableMappings**：表映射

重启已存在的迁移任务时参数

- **replicationTaskArn**：迁移任务的ARN

## 环境配置

需要进行AWS的一些配置，修改`aws.yml`中的以下配置信息

```yaml
dms:
  # The AWS credentials provider type. support: AWSStaticCredentialsProvider, InstanceProfileCredentialsProvider
  # AWSStaticCredentialsProvider: use the access key and secret key to authenticate
  # InstanceProfileCredentialsProvider: use the IAM role to authenticate
  credentials.provider.type: AWSStaticCredentialsProvider
  access.key.id: <access.key.id>
  access.key.secret: <access.key.secret>
  region: <region>
  endpoint: <endpoint>
```

