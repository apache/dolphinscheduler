# 集群部署(Cluster)

集群部署目的是在多台机器部署 DolphinScheduler 服务，用于运行大量任务情况。

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用[Standalone](standalone.md)方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用[伪集群部署](pseudo-cluster.md)。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

## 部署步骤

集群部署(Cluster)使用的脚本和配置文件与[伪集群部署](pseudo-cluster.md)中的配置一样，所以所需要的步骤也与伪集群部署大致一样。区别就是伪集群部署针对的是一台机器，而集群部署(Cluster)需要针对多台机器，且两者“修改相关配置”步骤区别较大

### 前置准备工作 && 准备 DolphinScheduler 启动环境

其中除了[伪集群部署](pseudo-cluster.md)中的“前置准备工作”，“准备启动环境”除了“启动zookeeper”以及“初始化数据库”外，别的都需要在每台机器中进行配置

### 修改相关配置

这个是与[伪集群部署](pseudo-cluster.md)差异较大的一步，请使用 scp 等方式将配置文件分发到各台机器上，然后修改配置文件

## 启动 DolphinScheduler && 登录 DolphinScheduler && 启停服务

[与伪集群部署](pseudo-cluster.md)保持一致
