# DataSync 节点

## 综述

[AWS DataSync](https://console.aws.amazon.com/datasync/) 是一种在线数据传输服务，可简化、自动化和加速本地存储系统和 AWS Storage 服务之间，以及不同 AWS Storage 服务之间的数据移动。

DataSync 支持的组件:

- Network File System (NFS) file servers
- Server Message Block (SMB) file servers
- Hadoop Distributed File System (HDFS)
- Object storage systems
- Amazon Simple Storage Service (Amazon S3) buckets
- Amazon EFS file systems
- Amazon FSx for Windows File Server file systems
- Amazon FSx for Lustre file systems
- Amazon FSx for OpenZFS file systems
- Amazon FSx for NetApp ONTAP file systems
- AWS Snowcone devices

DolphinScheduler DataSync 组件的功能: 

- 创建 AWS DataSync 任务并启动，持续获取状态，直至任务执行完成。 


## 创建任务

- 点击项目管理-项目名称-工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的 <img src="../../../../img/tasks/icons/datasync.png" width="15"/> 任务节点到画板中。


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

- **name**: 任务名称
- **destinationLocationArn**: 目标 AWS 存储资源位置的 Amazon Resource Name (ARN) ，可见 [AWS API](https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html#DataSync-CreateTask-request-DestinationLocationArn)
- **sourceLocationArn**: 源 AWS 存储资源位置的 Amazon Resource Name (ARN) ，可见 [AWS API](https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html#DataSync-CreateTask-request-SourceLocationArn)
- **cloudWatchLogGroupArn**: 用来监控任务的Amazon CloudWatch任务组的 Amazon Resource Name (ARN) ，可见 [AWS API](https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html#DataSync-CreateTask-request-CloudWatchLogGroupArn)

或

- **json**: 创建 datasync 任务的JSON结构任务参数，可以支持options等参数，可见 [AWS CreateTask API] 的 Request Syntax (https://docs.aws.amazon.com/datasync/latest/userguide/API_CreateTask.html)


组件图示如下：

![datasync](../../../../img/tasks/demo/datasync_task01.png)



## 环境配置

需要进行AWS的一些配置，修改`common.properties`中的`xxxxx`为你的配置信息

```yaml
# Defines AWS access key and is required
resource.aws.access.key.id=<YOUR AWS ACCESS KEY>
# Defines AWS secret access key and is required
resource.aws.secret.access.key=<YOUR AWS SECRET KEY>
# Defines  AWS Region to use and is required
resource.aws.region=<AWS REGION>
```