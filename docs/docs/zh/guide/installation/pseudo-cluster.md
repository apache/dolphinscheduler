# 伪集群部署

伪集群部署目的是在单台机器部署 DolphinScheduler 服务，该模式下 master、worker、api server 都在同一台机器上

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用[Standalone](standalone.md)方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用伪集群部署。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

## 前置准备工作

伪分布式部署 DolphinScheduler 需要有外部软件的支持

- JDK：下载[JDK][jdk] (1.8 或者 11)，安装并配置 `JAVA_HOME` 环境变量，并将其下的 `bin` 目录追加到 `PATH` 环境变量中。如果你的环境中已存在，可以跳过这步。
- 二进制包：在[下载页面](https://dolphinscheduler.apache.org/zh-cn/download)下载 DolphinScheduler 二进制包
- 数据库：[PostgreSQL](https://www.postgresql.org/download/) (8.2.15+) 或者 [MySQL](https://dev.mysql.com/downloads/mysql/) (5.7+)，两者任选其一即可，如 MySQL 则需要 JDBC Driver 8.0.33
- 注册中心：当前支持 [ZooKeeper](https://zookeeper.apache.org/releases.html) (3.8.0)，[MYSQL](https://www.mysql.com/)(8.0.33)，[ETCD](https://etcd.io/)
- 进程树分析
  - macOS 安装`pstree`
  - Fedora/Red/Hat/CentOS/Ubuntu/Debian 安装`psmisc`

## 下载插件依赖

从 3.3.0 版本开始，二进制包不再提供插件依赖，需要用户自行下载。插件依赖包下载地址：[插件依赖包](https://repo.maven.apache.org/maven2/org/apache/dolphinscheduler)
你也可以执行以下命令来安装插件依赖:

```shell
bash ./bin/install-plugins.sh 3.3.0
```

通常你并不需要所有的连接器插件，可以通过配置 `conf/plugins_config` 来指定你所需要的插件，例如，你只需要 `dolphinscheduler-task-shell` 插件，那么您可以修改配置文件如下：

```
--task-plugins--
dolphinscheduler-task-shell
--end--
```

## 准备 DolphinScheduler 启动环境

> **_注意:_** DolphinScheduler 本身不依赖 Hadoop、Hive、Spark，但如果你运行的任务需要依赖他们，就需要有对应的环境支持

### 配置用户免密及权限

创建部署用户，并且一定要配置 `sudo` 免密。以创建 dolphinscheduler 用户为例

```shell
# 创建用户需使用 root 登录
useradd dolphinscheduler

# 添加密码
echo "dolphinscheduler" | passwd --stdin dolphinscheduler

# 配置 sudo 免密
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers

# 修改目录权限，使得部署用户对二进制包解压后的 apache-dolphinscheduler-*-bin 目录有操作权限
chown -R dolphinscheduler:dolphinscheduler apache-dolphinscheduler-*-bin
chmod -R 755 apache-dolphinscheduler-*-bin
```

> **_注意:_**
>
> - 因为任务执行服务是以 `sudo -u {linux-user} -i` 切换不同 linux 用户的方式来实现多租户运行作业，所以部署用户需要有 sudo 权限，而且是免密的。初学习者不理解的话，完全可以暂时忽略这一点
> - 如果发现 `/etc/sudoers` 文件中有 "Defaults requiretty" 这行，也请注释掉

### 准备 zookeeper

如果使用 Zookeeper 作为注册中心，需要先安装 Zookeeper 并启动。

## 修改相关配置

完成基础环境的准备后，需要根据你的机器环境修改配置文件。配置文件可以在目录 `bin/env/dolphinscheduler_env.sh`，`api-server/conf/application.yaml`，
`master-server/conf/application.yaml`，`worker-server/conf/application.yaml`，`alert-server/conf/application.yaml` 中找到。

### 修改 `dolphinscheduler_env.sh` 文件

文件 `./bin/env/dolphinscheduler_env.sh` 描述了下列配置：

- DolphinScheduler 的数据库配置，详细配置方法见[初始化数据库]
- 一些任务类型外部依赖路径或库文件，如 `JAVA_HOME` 和 `SPARK_HOME`都是在这里定义的
- 默认的注册中心是 mysql
- 服务器相关配置，如缓存类型、时区等

如果您不使用某些任务类型，您可以忽略任务外部依赖项，但您必须根据您的环境更改 `JAVA_HOME`、注册中心和数据库相关配置。

```sh
# JAVA_HOME, will use it to start DolphinScheduler server
export JAVA_HOME=${JAVA_HOME:-/opt/soft/java}

# Database related configuration, set database type, username and password
export DATABASE=${DATABASE:-postgresql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/dolphinscheduler"
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}

# DolphinScheduler server related configuration
export SPRING_CACHE_TYPE=${SPRING_CACHE_TYPE:-none}
export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-UTC}

# Registry center configuration, determines the type and link of the registry center
export REGISTRY_TYPE=${REGISTRY_TYPE:-zookeeper}
export REGISTRY_ZOOKEEPER_CONNECT_STRING=${REGISTRY_ZOOKEEPER_CONNECT_STRING:-localhost:2181}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/opt/soft/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/opt/soft/hadoop/etc/hadoop}
export SPARK_HOME=${SPARK_HOME:-/opt/soft/spark}
export PYTHON_LAUNCHER=${PYTHON_LAUNCHER:-/opt/soft/python}
export HIVE_HOME=${HIVE_HOME:-/opt/soft/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_LAUNCHER=${DATAX_LAUNCHER:-/opt/soft/datax/bin/python3}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_LAUNCHER:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_LAUNCHER:$PATH
```

> **_注意:_** 如果您使用的是 MySQL 数据库，需要将 `DATABASE` 设置为 `mysql`，并且修改 `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME` 和 `SPRING_DATASOURCE_PASSWORD` 为您的数据库配置
>
> **_注意:_** dolphinscheduler_env.sh 文件中的配置会覆盖各个服务的配置文件(application.yaml)中的配置，所以如果您在配置文件中配置了某个参数，
> 而且在 dolphinscheduler_env.sh 文件中也配置了，那么以 dolphinscheduler_env.sh 文件中的配置为准。dolphinscheduler_env.sh 里的配置项格式样例:
> SPRING_DATASOURCE_URL 在 application.yaml 为 spring.datasource.url，以此类推

## 初始化数据库

请参考 [数据源配置](datasource-setting.md) `伪分布式/分布式安装初始化数据库` 创建并初始化数据库

## 启动 DolphinScheduler

部署后的运行日志将存放在 `xxx-server/logs` 文件夹内

```
# 启动 api-server
bash ./bin/dolphinscheduler-daemon.sh start api-server

# 启动 master-server
bash ./bin/dolphinscheduler-daemon.sh start master-server

# 启动 worker-server
bash ./bin/dolphinscheduler-daemon.sh start worker-server

# 启动 alert-server
bash ./bin/dolphinscheduler-daemon.sh start alert-server

```

> **_注意:_** 第一次部署的话，可以通过 bash ./bin/dolphinscheduler-daemon.sh status xxx-server 来进行服务状态查询

## 登录 DolphinScheduler

浏览器访问地址 http://localhost:12345/dolphinscheduler/ui 即可登录系统 UI。默认的用户名和密码是 **admin/dolphinscheduler123**

## 启停服务

```shell
# 查询服务状态
bash ./bin/dolphinscheduler-daemon.sh status xxx-server

# 启停 Master
bash ./bin/dolphinscheduler-daemon.sh stop master-server
bash ./bin/dolphinscheduler-daemon.sh start master-server

# 启停 Worker
bash ./bin/dolphinscheduler-daemon.sh start worker-server
bash ./bin/dolphinscheduler-daemon.sh stop worker-server

# 启停 Api
bash ./bin/dolphinscheduler-daemon.sh start api-server
bash ./bin/dolphinscheduler-daemon.sh stop api-server

# 启停 Alert
bash ./bin/dolphinscheduler-daemon.sh start alert-server
bash ./bin/dolphinscheduler-daemon.sh stop alert-server
```

> **_注意 1:_**: 每个服务在路径 `<service>/conf/dolphinscheduler_env.sh` 中都有 `dolphinscheduler_env.sh` 文件，这是可以为微
> 服务需求提供便利。意味着您可以基于不同的环境变量来启动各个服务，只需要在对应服务中配置 `<service>/conf/dolphinscheduler_env.sh` 然后通过 `<service>/bin/start.sh`
> 命令启动即可。但是如果您使用命令 `/bin/dolphinscheduler-daemon.sh start <service>` 启动服务器，它将会用文件 `bin/env/dolphinscheduler_env.sh`
> 覆盖 `<service>/conf/dolphinscheduler_env.sh` 然后启动服务，目的是为了减少用户修改配置的成本.
>
> **_注意 2:_**：服务用途请具体参见《系统架构设计》小节。Python gateway service 默认与 api-server 一起启动，如果您不想启动 Python gateway service
> 请通过更改 api-server 配置文件 `api-server/conf/application.yaml` 中的 `python-gateway.enabled : false` 来禁用它。
> **_注意 3:_**: DS默认使用本地模式的目录 /tmp/dolphinscheduler 作为资源中心, 如果需要修改资源中心目录, 请修改配置文件 conf/common.properties 中 resource 的相关配置项

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
[zookeeper]: https://zookeeper.apache.org/releases.html
[issue]: https://github.com/apache/dolphinscheduler/issues/6597

