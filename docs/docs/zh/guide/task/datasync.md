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

- **任务名称** ：任务的名称，同一个工作流定义中的节点名称不能重复。
- **运行标志** ：标识这个节点是否需要调度执行，如果不需要执行，可以打开禁止执行开关。
- **描述** ：当前节点的功能描述。
- **任务优先级** ：worker线程数不足时，根据优先级从高到低依次执行任务，优先级一样时根据先到先得原则执行。
- **Worker 分组** ：设置分组后，任务会被分配给worker组的机器机执行。若选择Default，则会随机选择一个worker执行。
- **任务组名称** ：任务资源组，未配置则不生效。
- **组内优先级** ：一个任务组内此任务的优先级。
- **环境名称** ：配置任务执行的环境。
- **失败重试次数** ：任务失败重新提交的次数，可以在下拉菜单中选择或者手动填充。
- **失败重试间隔** ：任务失败重新提交任务的时间间隔，可以在下拉菜单中选择或者手动填充。
- **CPU 配额** ：为执行的任务分配指定的CPU时间配额，单位百分比，默认-1代表不限制，例如1个核心的CPU满载是100%，16个核心的是1600%。 [task.resource.limit.state](../../architecture/configuration.md)
- **最大内存** ：为执行的任务分配指定的内存大小，超过会触发OOM被Kill同时不会进行自动重试，单位MB，默认-1代表不限制。这个功能由 [task.resource.limit.state](../../architecture/configuration.md) 控制。
- **超时告警** ：勾选超时告警、超时失败，当任务超过"超时时长"后，会发送告警邮件并且任务执行失败.这个功能由 [task.resource.limit.state](../../architecture/configuration.md) 控制。
- **资源** ：任务执行时所需资源文件。
- **前置任务** ：设置当前任务的前置（上游）任务。
- **延时执行时间** ：任务延迟执行的时间，以分为单位。

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