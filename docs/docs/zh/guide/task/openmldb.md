# OpenMLDB 节点

## 综述

[OpenMLDB](https://openmldb.ai/) 是一个优秀的开源机器学习数据库，提供生产级数据及特征开发全栈解决方案。

OpenMLDB任务组件可以连接OpenMLDB集群执行任务。

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的 <img src="../../../../img/tasks/icons/openmldb.png" width="15"/> 任务节点到画板中。

## 任务样例

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- 默认参数说明请参考[DolphinScheduler任务参数附录]&#40;appendix.md#默认任务参数&#41;`默认任务参数`一栏。)

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

|  **任务参数**   |                      **描述**                      |
|-------------|--------------------------------------------------|
| zookeeper地址 | OpenMLDB集群连接地址中的zookeeper地址, e.g. 127.0.0.1:2181 |
| zookeeper路径 | OpenMLDB集群连接地址中的zookeeper路径, e.g. /openmldb      |
| 执行模式        | 初始执行模式（离线/在线），你可以在sql语句中随时切换                     |
| SQL语句       | SQL语句                                            |

下面有几个例子：

#### 导入数据

![load data](../../../../img/tasks/demo/openmldb-load-data.png)

我们使用`LOAD DATA`语句导入数据到OpenMLDB集群。因为选择的是离线执行模式，所以将会导入数据到离线存储中。

#### 特征抽取

![fe](../../../../img/tasks/demo/openmldb-feature-extraction.png)

我们使用`SELECT INTO`进行特征抽取。因为选择的是离线执行模式，所以会使用离线引擎做特征计算。

## 环境准备

### OpenMLDB 启动

执行任务之前，你需要启动OpenMLDB集群。如果是在生产环境，请参考[deploy OpenMLDB](https://openmldb.ai/docs/zh/v0.5/deploy/install_deploy.html).

你可以参考[在docker中运行OpenMLDB集群](https://openmldb.ai/docs/zh/v0.5/quickstart/openmldb_quickstart.html#id11) 快速启动。

### Python 环境

OpenMLDB任务组件将使用OpenMLDB Python SDK来连接OpenMLDB。所以你需要Python环境。

我们默认使用`python3`，你可以通过配置`PYTHON_LAUNCHER`来设置自己的Python环境。

请确保已通过`pip install openmldb`，在worker server的主机中安装了OpenMLDB Python SDK。
