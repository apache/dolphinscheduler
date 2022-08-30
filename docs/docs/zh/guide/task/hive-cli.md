# Hive CLI

## 综述

使用`Hive Cli任务插件`创建`Hive Cli`类型的任务执行SQL脚本语句或者SQL任务文件。
执行任务的worker会通过`hive -e`命令执行hive SQL脚本语句或者通过`hive -f`命令执行`资源中心`中的hive SQL文件。

## Hive CLI任务 VS 连接Hive数据源的SQL任务

在DolphinScheduler中，我们有`Hive CLI任务插件`和`使用Hive数据源的SQL插件`提供用户在不同场景下使用，您可以根据需要进行选择。

- `Hive CLI任务插件`直接连接`HDFS`和`Hive Metastore`来执行hive类型的任务，所以需要能够访问到对应的服务。
  执行任务的worker节点需要有相应的`Hive` jar包以及`Hive`和`HDFS`的配置文件。
  但是在生产调度中，`Hive CLI任务插件`能够提供更可靠的稳定性。
- `使用Hive数据源的SQL插件`不需要您在worker节点上有相应的`Hive` jar包以及`Hive`和`HDFS`的配置文件，而且支持 `Kerberos`认证。
  但是在生产调度中，若调度压力很大，使用这种方式可能会遇到`HiveServer2`服务过载失败等问题。

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击"创建工作流"按钮，进入DAG编辑页面。
- 工具栏中拖动 <img src="../../../../img/tasks/icons/hivecli.png" width="15"/> 到画板中，即可完成创建。

## 任务参数

- 前置任务：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

|   **任务参数**    |                                                               **描述**                                                                |
|---------------|-------------------------------------------------------------------------------------------------------------------------------------|
| 任务名称          | 设置任务的名称。一个工作流定义中的节点名称是唯一的。                                                                                                          |
| 运行标志          | 标识这个节点是否需要正常调度，如果不需要执行，可以打开禁止执行开关。                                                                                                  |
| 描述            | 描述该节点的功能。                                                                                                                           |
| 任务优先级         | worker线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。                                                                                        |
| Worker分组      | 任务分配给worker组的机器机执行，选择Default，会随机选择一台worker机执行。                                                                                      |
| 任务组名称         | 任务资源组，如果没有配置的话就不会生效。                                                                                                                |
| 环境名称          | 配置任务执行的环境。                                                                                                                          |
| 失败重试次数        | 任务失败重新提交的次数，支持下拉和手填。                                                                                                                |
| 失败重试间隔        | 任务失败重新提交任务的时间间隔，支持下拉和手填。                                                                                                            |
| CPU 配额        | 为执行的任务分配指定的CPU时间配额，单位百分比，默认-1代表不限制，例如1个核心的CPU满载是100%，16个核心的是1600%。 [task.resource.limit.state](../../architecture/configuration.md) |
| 最大内存          | 为执行的任务分配指定的内存大小，超过会触发OOM被Kill同时不会进行自动重试，单位MB，默认-1代表不限制。这个功能由 [task.resource.limit.state](../../architecture/configuration.md) 控制。   |
| 超时告警          | 勾选超时告警、超时失败，当任务超过"超时时长"后，会发送告警邮件并且任务执行失败.这个功能由 [task.resource.limit.state](../../architecture/configuration.md) 控制。                 |
| Hive Cli 任务类型 | Hive Cli任务执行方式，可以选择`FROM_SCRIPT`或者`FROM_FILE`。                                                                                      |
| Hive SQL 脚本   | 手动填入您的Hive SQL脚本语句。                                                                                                                 |
| Hive Cli 选项   | Hive Cli的其他选项，如`--verbose`。                                                                                                         |
| 资源            | 如果您选择`FROM_FILE`作为Hive Cli任务类型，您需要在资源中选择Hive SQL文件。                                                                                 |

## 任务样例

### Hive CLI任务样例

下面的样例演示了如何使用`Hive CLI`任务节点执行Hive SQL脚本语句：

![demo-hive-cli-from-script](../../../../img/tasks/demo/hive_cli_from_script.png)

下面的样例演示了如何使用`Hive CLI`任务节点从资源中心的Hive SQL

![demo-hive-cli-from-file](../../../../img/tasks/demo/hive_cli_from_file.png)

