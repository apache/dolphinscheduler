# Docker 快速使用教程

本教程使用三种不同的方式通过 Docker 完成 DolphinScheduler 的部署，如果你想要快速体验，推荐使用 standalone-server 镜像，
如果你想要体验比较完成的服务，推荐使用 docker-compose 启动服务。如果你已经有自己的数据库或者 Zookeeper 服务
你想要沿用这些基础服务，你可以参考沿用已有的 PostgreSQL 和 ZooKeeper 服务完成部署。

## 前置条件

- [Docker](https://docs.docker.com/engine/install/) 1.13.1+
- [Docker Compose](https://docs.docker.com/compose/) 1.28.0+

## 启动服务

### 使用 standalone-server 镜像

使用 standalone-server 镜像启动一个 DolphinScheduler standalone-server 容器应该是最快体验 DolphinScheduler 的方法。通过这个方式
你可以最快速的体验到 DolphinScheduler 的大部分功能，了解主要和概念和内容。

```shell
$ DOLPHINSCHEDULER_VERSION=<version>
$ docker run --name dolphinscheduler-standalone-server -p 12345:12345 -p 25333:25333 -d apache/dolphinscheduler-standalone-server:"${DOLPHINSCHEDULER_VERSION}"
```

> 注意：请不要将 apache/dolphinscheduler-standalone-server 镜像作为生产镜像，应该仅仅作为快速体验 DolphinScheduler 的功能的途径。
> 除了因为他将全部服务运行在一个进程中外，还因为其使用内存数据库 H2 储存其元数据，当服务停止时内存数据库中的数据将会被清空。另外
> apache/dolphinscheduler-standalone-server 仅包含 DolphinScheduler 核心服务，部分任务组件（如 Spark 和 Flink 等），
> 告警组件（如 Telegram 和 Dingtalk 等）需要外部的组件或对应的配置后

### 使用 docker-compose 启动服务

使用 docker-compose 启动服务相比 standalone-server 的优点是 DolphinScheduler 的各个是独立的容器和进程，相互影响降到最小，且能够在
服务重启的时候保留元数据（如需要挂载到本地路径需要做指定）。他更健壮，能保证用户体验更加完整的 DolphinScheduler 服务。这种方式需要先安装
[docker-compose](https://docs.docker.com/compose/install/)，链接适用于 Mac，Linux，Windows。

安装完成 docker-compose 后我们需要修改部分配置以便能更好体验 DolphinScheduler 服务，我们需要配置不少于 4GB 的空闲内存：

- Mac：点击 `Docker Desktop -> Preferences -> Resources -> Memory` 调整内存大小
- Windows Docker Desktop：
  - Hyper-V 模式：点击 `Docker Desktop -> Settings -> Resources -> Memory` 调整内存大小
  - WSL 2 模式 模式：参考 [WSL 2 utility VM](https://docs.microsoft.com/zh-cn/windows/wsl/wsl-config#configure-global-options-with-wslconfig) 调整内存大小

配置完成后我们需要获取 `docker-compose.yaml` 文件，通过[下载页面](/zh-cn/download/download.html)下载对应版本源码包可能是最快的方法，
源码包对应的值为 "Total Source Code"。当下载完源码后就可以运行命令进行部署了。

```shell
$ DOLPHINSCHEDULER_VERSION=<version>
$ tar -zxf apache-dolphinscheduler-"${DOLPHINSCHEDULER_VERSION}"-src.tar.gz
# Mac Linux 用户
$ cd apache-dolphinscheduler-"${DOLPHINSCHEDULER_VERSION}"-src/deploy/docker
#  Windows 用户, `cd apache-dolphinscheduler-"${DOLPHINSCHEDULER_VERSION}"-src\deploy\docker`

# 如果需要初始化或者升级数据库结构，需要指定profile为schema
$ docker-compose --profile schema up -d

# 启动dolphinscheduler所有服务，指定profile为all
$ docker-compose --profile all up -d
```

> 提醒：通过 docker-compose 启动服务时，除了会启动 DolphinScheduler 对应的服务外，还会启动必要依赖服务，如数据库 PostgreSQL(用户
> `root`, 密码 `root`, 数据库 `dolphinscheduler`) 和 服务发现 ZooKeeper。

### 沿用已有的 PostgreSQL 和 ZooKeeper 服务

使用 docker-compose 启动服务会新启动数据库，以及 ZooKeeper 服务。如果你已经有在运行中的数据库，或者
ZooKeeper 且不想启动新的服务，可以使用这个方式分别启动 DolphinScheduler 容器。

```shell
$ DOLPHINSCHEDULER_VERSION=<version>
# 初始化数据库，其确保数据库 <DATABASE> 已经存在
$ docker run -d --name dolphinscheduler-tools \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/<DATABASE>" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    --net host \
    apache/dolphinscheduler-tools:"${DOLPHINSCHEDULER_VERSION}" tools/bin/upgrade-schema.sh
# 启动 DolphinScheduler 对应的服务
$ docker run -d --name dolphinscheduler-master \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/dolphinscheduler" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-master:"${DOLPHINSCHEDULER_VERSION}"
$ docker run -d --name dolphinscheduler-worker \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/dolphinscheduler" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-worker:"${DOLPHINSCHEDULER_VERSION}"
$ docker run -d --name dolphinscheduler-api \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/dolphinscheduler" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-api:"${DOLPHINSCHEDULER_VERSION}"
$ docker run -d --name dolphinscheduler-alert-server \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/dolphinscheduler" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-alert-server:"${DOLPHINSCHEDULER_VERSION}"
```

> 注意：如果你本地还没有对应的数据库和 ZooKeeper 服务，但是想要尝试这个启动方式，可以先安装并启动
> [PostgreSQL](https://www.postgresql.org/download/)(8.2.15+) 以及 [ZooKeeper](https://zookeeper.apache.org/releases.html)(3.4.6+)

## 登录系统

不管你是用那种方式启动的服务，只要服务启动后，你都可以通过 [http://localhost:12345/dolphinscheduler/ui](http://localhost:12345/dolphinscheduler/ui)
访问 DolphinScheduler。访问上述链接后会跳转到登陆页面，DolphinScheduler 默认的用户和密码分别为 `admin` 和 `dolphinscheduler123`。
想要了解更多操作请参考用户手册[快速上手](../start/quick-start.md)。

![login](../../../../img/new_ui/dev/quick-start/login.png)

> 注意：如果你使用沿用已有的 PostgreSQL 和 ZooKeeper 服务方式启动服务，且服务分布在多台机器中，
> 请将上述的地址改成你 API 容器启动的 hostname 或者 IP。

## 环境变量

可以通过环境变量来修改 Docker 运行的配置，我们在沿用已有的 PostgreSQL 和 ZooKeeper 服务中就通过环境变量修改了 Docker 的数据库配置和
注册中心配置，关于全部的配置环境可以查看对应组件的 application.yaml 文件了解 <!-- markdown-link-check-disable-line -->
