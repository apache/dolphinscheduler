# 集群部署(Cluster)

集群部署目的是在多台机器部署 DolphinScheduler 服务，用于运行大量任务情况。

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用[Standalone](standalone.md)方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用[伪集群部署](pseudo-cluster.md)。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

## 部署步骤

集群部署(Cluster)使用的脚本和配置文件与[伪集群部署](pseudo-cluster.md)中的配置一样，所以所需要的步骤也与伪集群部署大致一样。区别就是伪集群部署针对的是一台机器，而集群部署(Cluster)需要针对多台机器，且两者“修改相关配置”步骤区别较大

### 前置准备工作 && 准备 DolphinScheduler 启动环境

需要将安装包分发至每台集群的每台服务器上，并且需要在每台机器中进行配置执行[伪集群部署](pseudo-cluster.md)中的所有执行项

> **_注意:_** 请确保每台机器的配置文件都是一致的，否则会导致集群无法正常工作
> **_注意:_** 每个服务都是无状态且互相独立的，所以可以在每台机器上部署多个服务，但是需要注意端口冲突问题
> **_注意_**: DS默认使用本地模式的目录 /tmp/dolphinscheduler 作为资源中心, 如果需要修改资源中心目录, 请修改配置文件 conf/common.properties 中 resource 的相关配置项

### 修改相关配置

这个是与[伪集群部署](pseudo-cluster.md)差异较大的一步，请使用 scp 等方式将配置文件分发到各台机器上，然后修改配置文件

## 启动 DolphinScheduler && 登录 DolphinScheduler && 启停服务

[与伪集群部署](pseudo-cluster.md)保持一致
