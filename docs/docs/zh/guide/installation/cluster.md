# 集群部署(Cluster)

集群部署目的是在多台机器部署 DolphinScheduler 服务，用于运行大量任务情况。

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用[Standalone](standalone.md)方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用[伪集群部署](pseudo-cluster.md)。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

## 部署步骤

集群部署(Cluster)使用的脚本和配置文件与[伪集群部署](pseudo-cluster.md)中的配置一样，所以所需要的步骤也与伪集群部署大致一样。区别就是伪集群部署针对的是一台机器，而集群部署(Cluster)需要针对多台机器，且两者“修改相关配置”步骤区别较大

### 前置准备工作 && 准备 DolphinScheduler 启动环境

其中除了[伪集群部署](pseudo-cluster.md)中的“前置准备工作”，“准备启动环境”除了“启动zookeeper”以及“初始化数据库”外，别的都需要在每台机器中进行配置

### 修改相关配置

这个是与[伪集群部署](pseudo-cluster.md)差异较大的一步，因为部署脚本会通过 `scp` 的方式将安装需要的资源传输到各个机器上，所以这一步我们仅需要修改运行`install.sh`脚本的所在机器的配置即可。配置文件在路径在`conf/config/install_config.conf`下，此处我们仅需修改**INSTALL MACHINE**，**DolphinScheduler ENV、Database、Registry Server**与伪集群部署保持一致，下面对必须修改参数进行说明

```shell
# ---------------------------------------------------------
# INSTALL MACHINE
# ---------------------------------------------------------
# 需要配置master、worker、API server，所在服务器的IP均为机器IP或者localhost
# 如果是配置hostname的话，需要保证机器间可以通过hostname相互链接
# 如下图所示，部署 DolphinScheduler 机器的 hostname 为 ds1,ds2,ds3,ds4,ds5，其中 ds1,ds2 安装 master 服务，ds3,ds4,ds5安装 worker 服务，alert server安装在ds4中，api server 安装在ds5中
ips="ds1,ds2,ds3,ds4,ds5"
masters="ds1,ds2"
workers="ds3:default,ds4:default,ds5:default"
alertServer="ds4"
apiServers="ds5"
```

## 启动 DolphinScheduler && 登录 DolphinScheduler && 启停服务

[与伪集群部署](pseudo-cluster.md)保持一致
