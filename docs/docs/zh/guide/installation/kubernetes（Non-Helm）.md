# Kubernetes 非 Helm 部署

某些场景下，你的生产环境可能并不支持使用或者禁用 Helm 来部署 Kubernetes 应用，这时就需要使用原始的配置YAML文件的方式来部署 DolphinScheduler 服务到 Kubernetes 集群中。

本篇文档会介绍如何借助开发环境的 Helm 来生成 YAML 资源文件集，然后发布到生产进行部署。

## 先决条件

- [Helm](https://helm.sh/) 3.1.0+ (仅在开发环境或本地环境安装即可)
- [Kubernetes](https://kubernetes.io/) 1.12+
- PV 供应(需要基础设施支持)

## 安装 dolphinscheduler

下载官网 docker 镜像 

```
docker pull apache/dolphinscheduler-master:<version>
docker pull apache/dolphinscheduler-worker:<version>
docker pull apache/dolphinscheduler-tools:<version>
docker pull apache/dolphinscheduler-api:<version>
docker pull apache/dolphinscheduler-alert-server:<version>
```

基于官网镜像修改成内网 Harbor 镜像 

如果你的构建环境无法链接外网，需要提前准备内网可访问的 apt sources.list 文件，示例如下：
```shell
deb http://mirrors.tencent.com/ubuntu-ports/ focal main restricted
deb http://mirrors.tencent.com/ubuntu-ports/ focal-updates main restricted
deb http://mirrors.tencent.com/ubuntu-ports/ focal universe
deb http://mirrors.tencent.com/ubuntu-ports/ focal-updates universe
deb http://mirrors.tencent.com/ubuntu-ports/ focal multiverse
deb http://mirrors.tencent.com/ubuntu-ports/ focal-updates multiverse
deb http://mirrors.tencent.com/ubuntu-ports/ focal-backports main restricted universe multiverse
```

然后使用下面 Dockerfile 模版进行重新构建，建新的镜像完成之后，推送到自己的公司 Harbor 仓库

```Dockerfile
FROM apache/dolphinscheduler-<module>:<version>
# 开启非交互式命令
ENV DEBIAN_FRONTEND noninteractive
ENV DEBCONF_NOWARNINGS yes
# 替换apt源
COPY sources.list /etc/apt/
# 使用mysql driver ，注意如果是 dolphinscheduler-tools 镜像，则需要使用 /opt/dolphinscheduler/tools/libs 目录
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs
#  一些基础工具包，推荐安装，在出问题时方便调试和排查
RUN apt-get update && apt-get install -y sudo telnet iputils-ping curl dnsutils iproute2 vim traceroute procps nload
```

下载源码包 apache-dolphinscheduler-<version>-src.tar.gz ，借助本地 Helm 快速生成部署文件 下载地址: [下载](https://dolphinscheduler.apache.org/zh-cn/download)

下载解压之后，进入deploy/kubernetes/dolphinscheduler目录。

1, 修改 Chart.yaml 

移除 Chart 依赖的 postgresql，mysql，zookeeper 组件，推荐使用外置的单独安装的 mysql 和 zookeeper 组件

2，配置 Values.yaml

- 修改镜像地址为内网地址，包含 master，worker，api，alert，tools，busybox (辅助镜像探测db服务是否就绪)
- 配置访问数据库的账户和密码
- 配置访问 mysql，zookeeper的 host_ip, 不要使用localhost
- 开启ingress
- 如果没有HDFS环境，就把 resource.storage.type，改为NONE，避免加载Hadoop依赖失败
- 其他的 JVM 内存，探针，Pod副本，监控心跳间隔，滚动策略，可根据实际情况配置

3，生成部署 YAML 集
```shell
helm template --namespace dolphinscheduler  dolphinscheduler . --output-dir prod_yamls
```
上面的命令执行之后，dolphinscheduler 所有的资源对象的 YAML 配置都会生成在 prod_yamls 目录中

注意：生成后的 YAML 部署集，如果有资源对象的 apiVersion 版本有问题，就使用如下的命令再校验一遍 (在 values.yaml 的上级目录执行)：
```shell
helm install --dry-run --debug dolphinscheduler  ./dolphinscheduler
```
4，将生成的 YAML 目录，提交到Git代码仓库，然后在生产环境检出后部署

```shell
kubectl apply -f prod_yamls
```

## 访问 DolphinScheduler 前端页面

后续的步骤和用 Helm 步骤的一样，请参考部署章节的[kubernetes](./kubernetes.md) 访问 DolphinScheduler 前端页面


