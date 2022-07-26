# Standalone极速体验版

Standalone 仅适用于 DolphinScheduler 的快速体验.

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用Standalone方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用[伪集群部署](pseudo-cluster.md)。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

> **_注意:_** Standalone仅建议20个以下工作流使用，因为其采用内存式的H2 Database, Zookeeper Testing Server，任务过多可能导致不稳定，并且如果重启或者停止standalone-server会导致内存中数据库里的数据清空。
> 如果您要连接外部数据库，比如mysql或者postgresql，请看[配置数据库](#配置数据库)

## 前置准备工作

* JDK：下载[JDK][jdk] (1.8+)，并将 `JAVA_HOME` 配置到以及 `PATH` 变量中。如果你的环境中已存在，可以跳过这步。
* 二进制包：在[下载页面](https://dolphinscheduler.apache.org/zh-cn/download/download.html)下载 DolphinScheduler 二进制包

## 启动 DolphinScheduler Standalone Server

### 解压并启动 DolphinScheduler

二进制压缩包中有 standalone 启动的脚本，解压后即可快速启动。切换到有sudo权限的用户，运行脚本

```shell
# 解压并运行 Standalone Server
tar -xvzf apache-dolphinscheduler-*-bin.tar.gz
cd apache-dolphinscheduler-*-bin
bash ./bin/dolphinscheduler-daemon.sh start standalone-server
```

### 登录 DolphinScheduler

浏览器访问地址 http://localhost:12345/dolphinscheduler/ui 即可登录系统UI。默认的用户名和密码是 **admin/dolphinscheduler123**

## 启停服务

脚本 `./bin/dolphinscheduler-daemon.sh` 除了可以快捷启动 standalone 外，还能停止服务运行，全部命令如下

```shell
# 启动 Standalone Server 服务
bash ./bin/dolphinscheduler-daemon.sh start standalone-server
# 停止 Standalone Server 服务
bash ./bin/dolphinscheduler-daemon.sh stop standalone-server
```

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html

## 配置数据库

Standalone server 使用 H2 数据库作为其元数据存储数据，这是为了上手简单，用户在启动服务器之前不需要启动数据库。但是如果用户想将元数据库存储在
MySQL 或 PostgreSQL 等其他数据库中，他们必须更改一些配置。请参考 [数据源配置](../howto/datasource-setting.md) `Standalone 切换元数据库` 创建并初始化数据库
