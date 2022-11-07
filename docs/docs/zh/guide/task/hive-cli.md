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

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- 默认参数说明请参考[DolphinScheduler任务参数附录]&#40;appendix.md#默认任务参数&#41;`默认任务参数`一栏。)

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

|   **任务参数**    |                       **描述**                        |
|---------------|-----------------------------------------------------|
| Hive Cli 任务类型 | Hive Cli任务执行方式，可以选择`FROM_SCRIPT`或者`FROM_FILE`。      |
| Hive SQL 脚本   | 手动填入您的Hive SQL脚本语句。                                 |
| Hive Cli 选项   | Hive Cli的其他选项，如`--verbose`来查看任务结果。                  |
| 资源            | 如果您选择`FROM_FILE`作为Hive Cli任务类型，您需要在资源中选择Hive SQL文件。 |

## 任务样例

### Hive CLI任务样例

下面的样例演示了如何使用`Hive CLI`任务节点执行Hive SQL脚本语句：

![demo-hive-cli-from-script](../../../../img/tasks/demo/hive_cli_from_script.png)

下面的样例演示了如何使用`Hive CLI`任务节点从资源中心的Hive SQL

![demo-hive-cli-from-file](../../../../img/tasks/demo/hive_cli_from_file.png)

