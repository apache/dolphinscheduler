# 快速试用 Docker 部署

## 先决条件

- [Docker](https://docs.docker.com/engine/install/) 1.13.1+
- [Docker Compose](https://docs.docker.com/compose/) 1.11.0+

## 如何使用 Docker 镜像

有 3 种方式可以快速试用 DolphinScheduler

### 一、以 docker-compose 的方式启动 DolphinScheduler (推荐)

这种方式需要先安装 [docker-compose](https://docs.docker.com/compose/), docker-compose 的安装网上已经有非常多的资料，请自行安装即可

对于 Windows 7-10，你可以安装 [Docker Toolbox](https://github.com/docker/toolbox/releases)。对于 Windows 10 64-bit，你可以安装 [Docker Desktop](https://docs.docker.com/docker-for-windows/install/)，并且注意[系统要求](https://docs.docker.com/docker-for-windows/install/#system-requirements)

#### 0、请配置内存不少于 4GB

对于 Mac 用户，点击 `Docker Desktop -> Preferences -> Resources -> Memory`

对于 Windows Docker Toolbox 用户，有两项需要配置：

- **内存**：打开 Oracle VirtualBox Manager，如果你双击 Docker Quickstart Terminal 并成功运行 Docker Toolbox，你将会看到一个名为 `default` 的虚拟机. 点击 `设置 -> 系统 -> 主板 -> 内存大小`
- **端口转发**：点击 `设置 -> 网络 -> 高级 -> 端口转发 -> 添加`. `名称`，`主机端口` 和 `子系统端口` 都填写 `12345`，不填 `主机IP` 和 `子系统IP`

对于 Windows Docker Desktop 用户
- **Hyper-V 模式**：点击 `Docker Desktop -> Settings -> Resources -> Memory`
- **WSL 2 模式**：参考 [WSL 2 utility VM](https://docs.microsoft.com/zh-cn/windows/wsl/wsl-config#configure-global-options-with-wslconfig)

#### 1、下载源码包

请下载源码包 apache-dolphinscheduler-1.3.8-src.tar.gz，下载地址: [下载](/zh-cn/download/download.html)

#### 2、拉取镜像并启动服务

> 对于 Mac 和 Linux 用户，打开 **Terminal**
> 对于 Windows Docker Toolbox 用户，打开 **Docker Quickstart Terminal**
> 对于 Windows Docker Desktop 用户，打开 **Windows PowerShell**

```
$ tar -zxvf apache-dolphinscheduler-1.3.8-src.tar.gz
$ cd apache-dolphinscheduler-1.3.8-src/docker/docker-swarm
$ docker pull dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
$ docker tag apache/dolphinscheduler:1.3.8 apache/dolphinscheduler:latest
$ docker-compose up -d
```

> PowerShell 应该使用 `cd apache-dolphinscheduler-1.3.8-src\docker\docker-swarm`

**PostgreSQL** (用户 `root`, 密码 `root`, 数据库 `dolphinscheduler`) 和 **ZooKeeper** 服务将会默认启动

#### 3、登录系统

访问前端页面：http://localhost:12345/dolphinscheduler，如果有需要请修改成对应的 IP 地址

默认的用户是`admin`，默认的密码是`dolphinscheduler123`

![login](/img/new_ui/dev/quick-start/login.png)

请参考用户手册章节的[快速上手](../start/quick-start.md)查看如何使用DolphinScheduler

### 二、通过指定已存在的 PostgreSQL 和 ZooKeeper 服务

这种方式需要先安装 [docker](https://docs.docker.com/engine/install/), docker 的安装网上已经有非常多的资料，请自行安装即可

#### 1、基础软件安装 (请自行安装)

- [PostgreSQL](https://www.postgresql.org/download/) (8.2.15+)
- [ZooKeeper](https://zookeeper.apache.org/releases.html) (3.4.6+)
- [Docker](https://docs.docker.com/engine/install/) (1.13.1+)

#### 2、请登录 PostgreSQL 数据库，创建名为 `dolphinscheduler` 数据库

#### 3、初始化数据库，导入 `sql/dolphinscheduler_postgre.sql` 进行创建表及基础数据导入

#### 4、下载 DolphinScheduler 镜像

我们已将面向用户的 DolphinScheduler 镜像上传至 docker 仓库，用户无需在本地构建镜像，直接执行以下命令从 docker 仓库 pull 镜像：

```
docker pull dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
```

#### 5、运行一个 DolphinScheduler 实例

```
$ docker run -d --name dolphinscheduler \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:1.3.8 all
```

注：数据库用户 test 和密码 test 需要替换为实际的 PostgreSQL 用户和密码，192.168.x.x 需要替换为 PostgreSQL 和 ZooKeeper 的主机 IP

#### 6、登录系统

同上

### 三、运行 DolphinScheduler 中的独立服务

在容器启动时，会自动启动以下服务：

```
    MasterServer         ----- master 服务
    WorkerServer         ----- worker 服务
    ApiApplicationServer ----- api 服务
    AlertServer          ----- alert 服务
```

如果你只是想运行 dolphinscheduler 中的部分服务,你可以够通执行以下命令来运行 dolphinscheduler 中的部分服务

* 启动一个 **master server**, 如下:

```
$ docker run -d --name dolphinscheduler-master \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:1.3.8 master-server
```

* 启动一个 **worker server**, 如下:

```
$ docker run -d --name dolphinscheduler-worker \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:1.3.8 worker-server
```

* 启动一个 **api server**, 如下:

```
$ docker run -d --name dolphinscheduler-api \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:1.3.8 api-server
```

* 启动一个 **alert server**, 如下:

```
$ docker run -d --name dolphinscheduler-alert \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
apache/dolphinscheduler:1.3.8 alert-server
```

**注意**: 当你运行 dolphinscheduler 中的部分服务时，你必须指定这些环境变量 `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_DATABASE`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `ZOOKEEPER_QUORUM`。

## 环境变量

Docker 容器通过环境变量进行配置，[附录-环境变量](#appendix-environment-variables) 列出了 DolphinScheduler 的可配置环境变量及其默认值 <!-- markdown-link-check-disable-line -->

特别地，在 Docker Compose 和 Docker Swarm 中，可以通过环境变量配置文件 `config.env.sh` 进行配置

## 支持矩阵

| Type                                                         | 支持     | 备注                  |
| ------------------------------------------------------------ | ------- | --------------------- |
| Shell                                                        | 是      |                       |
| Python2                                                      | 是      |                       |
| Python3                                                      | 间接支持 | 详见 FAQ               |
| Hadoop2                                                      | 间接支持 | 详见 FAQ               |
| Hadoop3                                                      | 尚未确定 | 尚未测试                |
| Spark-Local(client)                                          | 间接支持 | 详见 FAQ               |
| Spark-YARN(cluster)                                          | 间接支持 | 详见 FAQ               |
| Spark-Standalone(cluster)                                    | 尚不    |                        |
| Spark-Kubernetes(cluster)                                    | 尚不    |                        |
| Flink-Local(local>=1.11)                                     | 尚不    | Generic CLI 模式尚未支持 |
| Flink-YARN(yarn-cluster)                                     | 间接支持 | 详见 FAQ               |
| Flink-YARN(yarn-session/yarn-per-job/yarn-application>=1.11) | 尚不    | Generic CLI 模式尚未支持 |
| Flink-Standalone(default)                                    | 尚不    |                        |
| Flink-Standalone(remote>=1.11)                               | 尚不    | Generic CLI 模式尚未支持 |
| Flink-Kubernetes(default)                                    | 尚不    |                        |
| Flink-Kubernetes(remote>=1.11)                               | 尚不    | Generic CLI 模式尚未支持 |
| Flink-NativeKubernetes(kubernetes-session/application>=1.11) | 尚不    | Generic CLI 模式尚未支持 |
| MapReduce                                                    | 间接支持 | 详见 FAQ               |
| Kerberos                                                     | 间接支持 | 详见 FAQ               |
| HTTP                                                         | 是      |                       |
| DataX                                                        | 间接支持 | 详见 FAQ               |
| Sqoop                                                        | 间接支持 | 详见 FAQ               |
| SQL-MySQL                                                    | 间接支持 | 详见 FAQ               |
| SQL-PostgreSQL                                               | 是      |                       |
| SQL-Hive                                                     | 间接支持 | 详见 FAQ               |
| SQL-Spark                                                    | 间接支持 | 详见 FAQ               |
| SQL-ClickHouse                                               | 间接支持 | 详见 FAQ               |
| SQL-Oracle                                                   | 间接支持 | 详见 FAQ               |
| SQL-SQLServer                                                | 间接支持 | 详见 FAQ               |
| SQL-DB2                                                      | 间接支持 | 详见 FAQ               |

## FAQ

### 如何通过 docker-compose 管理 DolphinScheduler？

启动、重启、停止或列出所有容器:

```
docker-compose start
docker-compose restart
docker-compose stop
docker-compose ps
```

停止所有容器并移除所有容器、网络:

```
docker-compose down
```

停止所有容器并移除所有容器、网络和存储卷:

```
docker-compose down -v
```

### 如何查看一个容器的日志？

列出所有运行的容器:

```
docker ps
docker ps --format "{{.Names}}" # 只打印名字
```

查看名为 docker-swarm_dolphinscheduler-api_1 的容器的日志:

```
docker logs docker-swarm_dolphinscheduler-api_1
docker logs -f docker-swarm_dolphinscheduler-api_1 # 跟随日志输出
docker logs --tail 10 docker-swarm_dolphinscheduler-api_1 # 显示倒数10行日志
```

### 如何通过 docker-compose 扩缩容 master 和 worker？

扩缩容 master 至 2 个实例:

```
docker-compose up -d --scale dolphinscheduler-master=2 dolphinscheduler-master
```

扩缩容 worker 至 3 个实例:

```
docker-compose up -d --scale dolphinscheduler-worker=3 dolphinscheduler-worker
```

### 如何在 Docker Swarm 上部署 DolphinScheduler？

假设 Docker Swarm 集群已经部署（如果还没有创建 Docker Swarm 集群，请参考 [create-swarm](https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/)）

启动名为 dolphinscheduler 的 stack:

```
docker stack deploy -c docker-stack.yml dolphinscheduler
```

列出名为 dolphinscheduler 的 stack 的所有服务:

```
docker stack services dolphinscheduler
```

停止并移除名为 dolphinscheduler 的 stack:

```
docker stack rm dolphinscheduler
```

移除名为 dolphinscheduler 的 stack 的所有存储卷:

```
docker volume rm -f $(docker volume ls --format "{{.Name}}" | grep -e "^dolphinscheduler")
```

### 如何在 Docker Swarm 上扩缩容 master 和 worker？

扩缩容名为 dolphinscheduler 的 stack 的 master 至 2 个实例:

```
docker service scale dolphinscheduler_dolphinscheduler-master=2
```

扩缩容名为 dolphinscheduler 的 stack 的 worker 至 3 个实例:

```
docker service scale dolphinscheduler_dolphinscheduler-worker=3
```

### 如何构建一个 Docker 镜像？

#### 从源码构建 (需要 Maven 3.3+ & JDK 1.8+)

类 Unix 系统，在 Terminal 中执行:

```bash
$ bash ./docker/build/hooks/build
```

Windows 系统，在 cmd 或 PowerShell 中执行:

```bat
C:\dolphinscheduler-src>.\docker\build\hooks\build.bat
```

如果你不理解 `./docker/build/hooks/build` `./docker/build/hooks/build.bat` 这些脚本，请阅读里面的内容

#### 从二进制包构建 (不需要 Maven 3.3+ & JDK 1.8+)

请下载二进制包 apache-dolphinscheduler-1.3.8-bin.tar.gz，下载地址: [下载](/zh-cn/download/download.html). 然后将 apache-dolphinscheduler-1.3.8-bin.tar.gz 放到 `apache-dolphinscheduler-1.3.8-src/docker/build` 目录里，在 Terminal 或 PowerShell 中执行:

```
$ cd apache-dolphinscheduler-1.3.8-src/docker/build
$ docker build --build-arg VERSION=1.3.8 -t apache/dolphinscheduler:1.3.8 .
```

> PowerShell 应该使用 `cd apache-dolphinscheduler-1.3.8-src/docker/build`

#### 构建多平台架构镜像

目前支持构建 `linux/amd64` 和 `linux/arm64` 平台架构的镜像，要求：

1. 支持 [docker buildx](https://docs.docker.com/engine/reference/commandline/buildx/)
2. 具有 https://hub.docker.com/r/apache/dolphinscheduler 的 push 权限（**务必谨慎**: 构建命令默认会自动将多平台架构镜像推送到 apache/dolphinscheduler 的 docker hub）

执行:

```bash
$ docker login # 登录, 用于推送 apache/dolphinscheduler
$ bash ./docker/build/hooks/build x
```

### 如何为 Docker 添加一个环境变量？

如果你想在编译的时候或者运行的时候附加一些其它的操作及新增一些环境变量，你可以在`/root/start-init-conf.sh`文件中进行修改，同时如果涉及到配置文件的修改，请在`/opt/dolphinscheduler/conf/*.tpl`中修改相应的配置文件

例如，在`/root/start-init-conf.sh`添加一个环境变量`SECURITY_AUTHENTICATION_TYPE`：

```
export SECURITY_AUTHENTICATION_TYPE=PASSWORD
```

当添加以上环境变量后，你应该在相应的模板文件`application-api.properties.tpl`中添加这个环境变量配置:

```
security.authentication.type=${SECURITY_AUTHENTICATION_TYPE}
```

`/root/start-init-conf.sh` 将根据模板文件动态的生成配置文件：

```sh
echo "generate dolphinscheduler config"
ls ${DOLPHINSCHEDULER_HOME}/conf/ | grep ".tpl" | while read line; do
eval "cat << EOF
$(cat ${DOLPHINSCHEDULER_HOME}/conf/${line})
EOF
" > ${DOLPHINSCHEDULER_HOME}/conf/${line%.*}
done
```

### 如何用 MySQL 替代 PostgreSQL 作为 DolphinScheduler 的数据库？

> 由于商业许可证的原因，我们不能直接使用 MySQL 的驱动包.
>
> 如果你要使用 MySQL, 你可以基于官方镜像 `apache/dolphinscheduler` 进行构建.

1. 下载 MySQL 驱动包 [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)

2. 创建一个新的 `Dockerfile`，用于添加 MySQL 的驱动包:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/lib
```

3. 构建一个包含 MySQL 驱动包的新镜像:

```
docker build -t apache/dolphinscheduler:mysql-driver .
```

4. 修改 `docker-compose.yml` 文件中的所有 image 字段为 `apache/dolphinscheduler:mysql-driver`

> 如果你想在 Docker Swarm 上部署 dolphinscheduler，你需要修改 `docker-stack.yml`

5. 注释 `docker-compose.yml` 文件中的 `dolphinscheduler-postgresql` 块

6. 在 `docker-compose.yml` 文件中添加 `dolphinscheduler-mysql` 服务（**可选**，你可以直接使用一个外部的 MySQL 数据库）

7. 修改 `config.env.sh` 文件中的 DATABASE 环境变量

```
DATABASE_TYPE=mysql
DATABASE_DRIVER=com.mysql.jdbc.Driver
DATABASE_HOST=dolphinscheduler-mysql
DATABASE_PORT=3306
DATABASE_USERNAME=root
DATABASE_PASSWORD=root
DATABASE_DATABASE=dolphinscheduler
DATABASE_PARAMS=useUnicode=true&characterEncoding=UTF-8
```

> 如果你已经添加了 `dolphinscheduler-mysql` 服务，设置 `DATABASE_HOST` 为 `dolphinscheduler-mysql` 即可

8. 运行 dolphinscheduler (详见**如何使用docker镜像**)

### 如何在数据源中心支持 MySQL 数据源？

> 由于商业许可证的原因，我们不能直接使用 MySQL 的驱动包.
>
> 如果你要添加 MySQL 数据源, 你可以基于官方镜像 `apache/dolphinscheduler` 进行构建.

1. 下载 MySQL 驱动包 [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)

2. 创建一个新的 `Dockerfile`，用于添加 MySQL 驱动包:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/lib
```

3. 构建一个包含 MySQL 驱动包的新镜像:

```
docker build -t apache/dolphinscheduler:mysql-driver .
```

4. 将 `docker-compose.yml` 文件中的所有 `image` 字段修改为 `apache/dolphinscheduler:mysql-driver`

> 如果你想在 Docker Swarm 上部署 dolphinscheduler，你需要修改 `docker-stack.yml`

5. 运行 dolphinscheduler (详见**如何使用docker镜像**)

6. 在数据源中心添加一个 MySQL 数据源

### 如何在数据源中心支持 Oracle 数据源？

> 由于商业许可证的原因，我们不能直接使用 Oracle 的驱动包.
>
> 如果你要添加 Oracle 数据源, 你可以基于官方镜像 `apache/dolphinscheduler` 进行构建.

1. 下载 Oracle 驱动包 [ojdbc8.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/) (such as `ojdbc8-19.9.0.0.jar`)

2. 创建一个新的 `Dockerfile`，用于添加 Oracle 驱动包:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY ojdbc8-19.9.0.0.jar /opt/dolphinscheduler/lib
```

3. 构建一个包含 Oracle 驱动包的新镜像:

```
docker build -t apache/dolphinscheduler:oracle-driver .
```

4. 将 `docker-compose.yml` 文件中的所有 `image` 字段修改为 `apache/dolphinscheduler:oracle-driver`

> 如果你想在 Docker Swarm 上部署 dolphinscheduler，你需要修改 `docker-stack.yml`

5. 运行 dolphinscheduler (详见**如何使用docker镜像**)

6. 在数据源中心添加一个 Oracle 数据源

### 如何支持 Python 2 pip 以及自定义 requirements.txt？

1. 创建一个新的 `Dockerfile`，用于安装 pip:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY requirements.txt /tmp
RUN apt-get update && \
    apt-get install -y --no-install-recommends python-pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    rm -rf /var/lib/apt/lists/*
```

这个命令会安装默认的 **pip 18.1**. 如果你想升级 pip, 只需添加一行

```
    pip install --no-cache-dir -U pip && \
```

2. 构建一个包含 pip 的新镜像:

```
docker build -t apache/dolphinscheduler:pip .
```

3. 将 `docker-compose.yml` 文件中的所有 `image` 字段修改为 `apache/dolphinscheduler:pip`

> 如果你想在 Docker Swarm 上部署 dolphinscheduler，你需要修改 `docker-stack.yml`

4. 运行 dolphinscheduler (详见**如何使用docker镜像**)

5. 在一个新 Python 任务下验证 pip

### 如何支持 Python 3？

1. 创建一个新的 `Dockerfile`，用于安装 Python 3:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 && \
    rm -rf /var/lib/apt/lists/*
```

这个命令会安装默认的 **Python 3.7.3**. 如果你也想安装 **pip3**, 将 `python3` 替换为 `python3-pip` 即可

```
    apt-get install -y --no-install-recommends python3-pip && \
```

2. 构建一个包含 Python 3 的新镜像:

```
docker build -t apache/dolphinscheduler:python3 .
```

3. 将 `docker-compose.yml` 文件中的所有 `image` 字段修改为 `apache/dolphinscheduler:python3`

> 如果你想在 Docker Swarm 上部署 dolphinscheduler，你需要修改 `docker-stack.yml`

4. 修改 `config.env.sh` 文件中的 `PYTHON_HOME` 为 `/usr/bin/python3`

5. 运行 dolphinscheduler (详见**如何使用docker镜像**)

6. 在一个新 Python 任务下验证 Python 3

### 如何支持 Hadoop, Spark, Flink, Hive 或 DataX？

以 Spark 2.4.7 为例:

1. 下载 Spark 2.4.7 发布的二进制包 `spark-2.4.7-bin-hadoop2.7.tgz`

2. 运行 dolphinscheduler (详见**如何使用docker镜像**)

3. 复制 Spark 2.4.7 二进制包到 Docker 容器中

```bash
docker cp spark-2.4.7-bin-hadoop2.7.tgz docker-swarm_dolphinscheduler-worker_1:/opt/soft
```

因为存储卷 `dolphinscheduler-shared-local` 被挂载到 `/opt/soft`, 因此 `/opt/soft` 中的所有文件都不会丢失

4. 登录到容器并确保 `SPARK_HOME2` 存在

```bash
docker exec -it docker-swarm_dolphinscheduler-worker_1 bash
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # 或者 mv
$SPARK_HOME2/bin/spark-submit --version
```

如果一切执行正常，最后一条命令将会打印 Spark 版本信息

5. 在一个 Shell 任务下验证 Spark

```
$SPARK_HOME2/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME2/examples/jars/spark-examples_2.11-2.4.7.jar
```

检查任务日志是否包含输出 `Pi is roughly 3.146015`

6. 在一个 Spark 任务下验证 Spark

文件 `spark-examples_2.11-2.4.7.jar` 需要先被上传到资源中心，然后创建一个 Spark 任务并设置:

- Spark版本: `SPARK2`
- 主函数的 Class: `org.apache.spark.examples.SparkPi`
- 主程序包: `spark-examples_2.11-2.4.7.jar`
- 部署方式: `local`

同样地, 检查任务日志是否包含输出 `Pi is roughly 3.146015`

7. 验证 Spark on YARN

Spark on YARN (部署方式为 `cluster` 或 `client`) 需要 Hadoop 支持. 类似于 Spark 支持, 支持 Hadoop 的操作几乎和前面的步骤相同

确保 `$HADOOP_HOME` 和 `$HADOOP_CONF_DIR` 存在

### 如何支持 Spark 3？

事实上，使用 `spark-submit` 提交应用的方式是相同的, 无论是 Spark 1, 2 或 3. 换句话说，`SPARK_HOME2` 的语义是第二个 `SPARK_HOME`, 而非 `SPARK2` 的 `HOME`, 因此只需设置 `SPARK_HOME2=/path/to/spark3` 即可

以 Spark 3.1.1 为例:

1. 下载 Spark 3.1.1 发布的二进制包 `spark-3.1.1-bin-hadoop2.7.tgz`

2. 运行 dolphinscheduler (详见**如何使用docker镜像**)

3. 复制 Spark 3.1.1 二进制包到 Docker 容器中

```bash
docker cp spark-3.1.1-bin-hadoop2.7.tgz docker-swarm_dolphinscheduler-worker_1:/opt/soft
```

4. 登录到容器并确保 `SPARK_HOME2` 存在

```bash
docker exec -it docker-swarm_dolphinscheduler-worker_1 bash
cd /opt/soft
tar zxf spark-3.1.1-bin-hadoop2.7.tgz
rm -f spark-3.1.1-bin-hadoop2.7.tgz
ln -s spark-3.1.1-bin-hadoop2.7 spark2 # 或者 mv
$SPARK_HOME2/bin/spark-submit --version
```

如果一切执行正常，最后一条命令将会打印 Spark 版本信息

5. 在一个 Shell 任务下验证 Spark

```
$SPARK_HOME2/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME2/examples/jars/spark-examples_2.12-3.1.1.jar
```

检查任务日志是否包含输出 `Pi is roughly 3.146015`

### 如何在 Master、Worker 和 Api 服务之间支持共享存储？

> **注意**: 如果是在单机上通过 docker-compose 部署，则步骤 1 和 2 可以直接跳过，并且执行命令如 `docker cp hadoop-3.2.2.tar.gz docker-swarm_dolphinscheduler-worker_1:/opt/soft` 将 Hadoop 放到容器中的共享目录 /opt/soft 下

例如, Master、Worker 和 Api 服务可能同时使用 Hadoop

1. 修改 `docker-compose.yml` 文件中的 `dolphinscheduler-shared-local` 存储卷，以支持 nfs

> 如果你想在 Docker Swarm 上部署 dolphinscheduler，你需要修改 `docker-stack.yml`

```yaml
volumes:
  dolphinscheduler-shared-local:
    driver_opts:
      type: "nfs"
      o: "addr=10.40.0.199,nolock,soft,rw"
      device: ":/path/to/shared/dir"
```

2. 将 Hadoop 放到 nfs

3. 确保 `$HADOOP_HOME` 和 `$HADOOP_CONF_DIR` 正确

### 如何支持本地文件存储而非 HDFS 和 S3？

> **注意**: 如果是在单机上通过 docker-compose 部署，则步骤 2 可以直接跳过

1. 修改 `config.env.sh` 文件中下面的环境变量:

```
RESOURCE_STORAGE_TYPE=HDFS
FS_DEFAULT_FS=file:///
```

2. 修改 `docker-compose.yml` 文件中的 `dolphinscheduler-resource-local` 存储卷，以支持 nfs

> 如果你想在 Docker Swarm 上部署 dolphinscheduler，你需要修改 `docker-stack.yml`

```yaml
volumes:
  dolphinscheduler-resource-local:
    driver_opts:
      type: "nfs"
      o: "addr=10.40.0.199,nolock,soft,rw"
      device: ":/path/to/resource/dir"
```

### 如何支持 S3 资源存储，例如 MinIO？

以 MinIO 为例: 修改 `config.env.sh` 文件中下面的环境变量

```
RESOURCE_STORAGE_TYPE=S3
RESOURCE_UPLOAD_PATH=/dolphinscheduler
FS_DEFAULT_FS=s3a://BUCKET_NAME
FS_S3A_ENDPOINT=http://MINIO_IP:9000
FS_S3A_ACCESS_KEY=MINIO_ACCESS_KEY
FS_S3A_SECRET_KEY=MINIO_SECRET_KEY
```

`BUCKET_NAME`, `MINIO_IP`, `MINIO_ACCESS_KEY` 和 `MINIO_SECRET_KEY` 需要被修改为实际值

> **注意**: `MINIO_IP` 只能使用 IP 而非域名, 因为 DolphinScheduler 尚不支持 S3 路径风格访问 (S3 path style access)

### 如何配置 SkyWalking？

修改 `config.env.sh` 文件中的 SKYWALKING 环境变量

```
SKYWALKING_ENABLE=true
SW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800
SW_GRPC_LOG_SERVER_HOST=127.0.0.1
SW_GRPC_LOG_SERVER_PORT=11800
```

## 附录-环境变量

### 数据库

**`DATABASE_TYPE`**

配置`database`的`TYPE`， 默认值 `postgresql`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DATABASE_DRIVER`**

配置`database`的`DRIVER`， 默认值 `org.postgresql.Driver`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DATABASE_HOST`**

配置`database`的`HOST`， 默认值 `127.0.0.1`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DATABASE_PORT`**

配置`database`的`PORT`， 默认值 `5432`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DATABASE_USERNAME`**

配置`database`的`USERNAME`， 默认值 `root`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DATABASE_PASSWORD`**

配置`database`的`PASSWORD`， 默认值 `root`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DATABASE_DATABASE`**

配置`database`的`DATABASE`， 默认值 `dolphinscheduler`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`DATABASE_PARAMS`**

配置`database`的`PARAMS`， 默认值 `characterEncoding=utf8`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`、`alert-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

### ZooKeeper

**`ZOOKEEPER_QUORUM`**

配置`dolphinscheduler`的`Zookeeper`地址, 默认值 `127.0.0.1:2181`。

**注意**: 当运行`dolphinscheduler`中`master-server`、`worker-server`、`api-server`这些服务时，必须指定这个环境变量，以便于你更好的搭建分布式服务。

**`ZOOKEEPER_ROOT`**

配置`dolphinscheduler`在`zookeeper`中数据存储的根目录，默认值 `/dolphinscheduler`。

### 通用

**`DOLPHINSCHEDULER_OPTS`**

配置`dolphinscheduler`的`jvm options`，适用于`master-server`、`worker-server`、`api-server`、`alert-server`，默认值 `""`、

**`DATA_BASEDIR_PATH`**

用户数据目录, 用户自己配置, 请确保这个目录存在并且用户读写权限， 默认值 `/tmp/dolphinscheduler`。

**`RESOURCE_STORAGE_TYPE`**

配置`dolphinscheduler`的资源存储类型，可选项为 `HDFS`、`S3`、`NONE`，默认值 `HDFS`。

**`RESOURCE_UPLOAD_PATH`**

配置`HDFS/S3`上的资源存储路径，默认值 `/dolphinscheduler`。

**`FS_DEFAULT_FS`**

配置资源存储的文件系统协议，如 `file:///`, `hdfs://mycluster:8020` or `s3a://dolphinscheduler`，默认值 `file:///`。

**`FS_S3A_ENDPOINT`**

当`RESOURCE_STORAGE_TYPE=S3`时，需要配置`S3`的访问路径，默认值 `s3.xxx.amazonaws.com`。

**`FS_S3A_ACCESS_KEY`**

当`RESOURCE_STORAGE_TYPE=S3`时，需要配置`S3`的`s3 access key`，默认值 `xxxxxxx`。

**`FS_S3A_SECRET_KEY`**

当`RESOURCE_STORAGE_TYPE=S3`时，需要配置`S3`的`s3 secret key`，默认值 `xxxxxxx`。

**`HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE`**

配置`dolphinscheduler`是否启用kerberos，默认值 `false`。

**`JAVA_SECURITY_KRB5_CONF_PATH`**

配置`dolphinscheduler`的java.security.krb5.conf路径，默认值 `/opt/krb5.conf`。

**`LOGIN_USER_KEYTAB_USERNAME`**

配置`dolphinscheduler`登录用户的keytab用户名，默认值 `hdfs@HADOOP.COM`。

**`LOGIN_USER_KEYTAB_PATH`**

配置`dolphinscheduler`登录用户的keytab路径，默认值 `/opt/hdfs.keytab`。

**`KERBEROS_EXPIRE_TIME`**

配置`dolphinscheduler`的kerberos过期时间，单位为小时，默认值 `2`。

**`HDFS_ROOT_USER`**

当`RESOURCE_STORAGE_TYPE=HDFS`时，配置`dolphinscheduler`的hdfs的root用户名，默认值 `hdfs`。

**`RESOURCE_MANAGER_HTTPADDRESS_PORT`**

配置`dolphinscheduler`的resource manager httpaddress 端口，默认值 `8088`。

**`YARN_RESOURCEMANAGER_HA_RM_IDS`**

配置`dolphinscheduler`的yarn resourcemanager ha rm ids，默认值 `空`。

**`YARN_APPLICATION_STATUS_ADDRESS`**

配置`dolphinscheduler`的yarn application status地址，默认值 `http://ds1:%s/ws/v1/cluster/apps/%s`。

**`SKYWALKING_ENABLE`**

配置`skywalking`是否启用. 默认值 `false`。

**`SW_AGENT_COLLECTOR_BACKEND_SERVICES`**

配置`skywalking`的collector后端地址. 默认值 `127.0.0.1:11800`。

**`SW_GRPC_LOG_SERVER_HOST`**

配置`skywalking`的grpc服务主机或IP. 默认值 `127.0.0.1`。

**`SW_GRPC_LOG_SERVER_PORT`**

配置`skywalking`的grpc服务端口. 默认值 `11800`。

**`HADOOP_HOME`**

配置`dolphinscheduler`的`HADOOP_HOME`，默认值 `/opt/soft/hadoop`。

**`HADOOP_CONF_DIR`**

配置`dolphinscheduler`的`HADOOP_CONF_DIR`，默认值 `/opt/soft/hadoop/etc/hadoop`。

**`SPARK_HOME1`**

配置`dolphinscheduler`的`SPARK_HOME1`，默认值 `/opt/soft/spark1`。

**`SPARK_HOME2`**

配置`dolphinscheduler`的`SPARK_HOME2`，默认值 `/opt/soft/spark2`。

**`PYTHON_HOME`**

配置`dolphinscheduler`的`PYTHON_HOME`，默认值 `/usr/bin/python`。

**`JAVA_HOME`**

配置`dolphinscheduler`的`JAVA_HOME`，默认值 `/usr/local/openjdk-8`。

**`HIVE_HOME`**

配置`dolphinscheduler`的`HIVE_HOME`，默认值 `/opt/soft/hive`。

**`FLINK_HOME`**

配置`dolphinscheduler`的`FLINK_HOME`，默认值 `/opt/soft/flink`。

**`DATAX_HOME`**

配置`dolphinscheduler`的`DATAX_HOME`，默认值 `/opt/soft/datax`。

### Master Server

**`MASTER_SERVER_OPTS`**

配置`master-server`的`jvm options`，默认值 `-Xms1g -Xmx1g -Xmn512m`。

**`MASTER_EXEC_THREADS`**

配置`master-server`中的执行线程数量，默认值 `100`。

**`MASTER_EXEC_TASK_NUM`**

配置`master-server`中的执行任务数量，默认值 `20`。

**`MASTER_DISPATCH_TASK_NUM`**

配置`master-server`中的派发任务数量，默认值 `3`。

**`MASTER_HOST_SELECTOR`**

配置`master-server`中派发任务时worker host的选择器，可选值为`Random`, `RoundRobin`和`LowerWeight`，默认值 `LowerWeight`。

**`MASTER_HEARTBEAT_INTERVAL`**

配置`master-server`中的心跳交互时间，默认值 `10`。

**`MASTER_TASK_COMMIT_RETRYTIMES`**

配置`master-server`中的任务提交重试次数，默认值 `5`。

**`MASTER_TASK_COMMIT_INTERVAL`**

配置`master-server`中的任务提交交互时间，默认值 `1`。

**`MASTER_MAX_CPULOAD_AVG`**

配置`master-server`中的CPU中的`load average`值，默认值 `-1`。

**`MASTER_RESERVED_MEMORY`**

配置`master-server`的保留内存，单位为G，默认值 `0.3`。

### Worker Server

**`WORKER_SERVER_OPTS`**

配置`worker-server`的`jvm options`，默认值 `-Xms1g -Xmx1g -Xmn512m`。

**`WORKER_EXEC_THREADS`**

配置`worker-server`中的执行线程数量，默认值 `100`。

**`WORKER_HEARTBEAT_INTERVAL`**

配置`worker-server`中的心跳交互时间，默认值 `10`。

**`WORKER_MAX_CPULOAD_AVG`**

配置`worker-server`中的CPU中的最大`load average`值，默认值 `-1`。

**`WORKER_RESERVED_MEMORY`**

配置`worker-server`的保留内存，单位为G，默认值 `0.3`。

**`WORKER_GROUPS`**

配置`worker-server`的分组，默认值 `default`。

### Alert Server

**`ALERT_SERVER_OPTS`**

配置`alert-server`的`jvm options`，默认值 `-Xms512m -Xmx512m -Xmn256m`。

**`XLS_FILE_PATH`**

配置`alert-server`的`XLS`文件的存储路径，默认值 `/tmp/xls`。

**`MAIL_SERVER_HOST`**

配置`alert-server`的邮件服务地址，默认值 `空`。

**`MAIL_SERVER_PORT`**

配置`alert-server`的邮件服务端口，默认值 `空`。

**`MAIL_SENDER`**

配置`alert-server`的邮件发送人，默认值 `空`。

**`MAIL_USER=`**

配置`alert-server`的邮件服务用户名，默认值 `空`。

**`MAIL_PASSWD`**

配置`alert-server`的邮件服务用户密码，默认值 `空`。

**`MAIL_SMTP_STARTTLS_ENABLE`**

配置`alert-server`的邮件服务是否启用TLS，默认值 `true`。

**`MAIL_SMTP_SSL_ENABLE`**

配置`alert-server`的邮件服务是否启用SSL，默认值 `false`。

**`MAIL_SMTP_SSL_TRUST`**

配置`alert-server`的邮件服务SSL的信任地址，默认值 `空`。

**`ENTERPRISE_WECHAT_ENABLE`**

配置`alert-server`的邮件服务是否启用企业微信，默认值 `false`。

**`ENTERPRISE_WECHAT_CORP_ID`**

配置`alert-server`的邮件服务企业微信`ID`，默认值 `空`。

**`ENTERPRISE_WECHAT_SECRET`**

配置`alert-server`的邮件服务企业微信`SECRET`，默认值 `空`。

**`ENTERPRISE_WECHAT_AGENT_ID`**

配置`alert-server`的邮件服务企业微信`AGENT_ID`，默认值 `空`。

**`ENTERPRISE_WECHAT_USERS`**

配置`alert-server`的邮件服务企业微信`USERS`，默认值 `空`。

### Api Server

**`API_SERVER_OPTS`**

配置`api-server`的`jvm options`，默认值 `-Xms512m -Xmx512m -Xmn256m`。
