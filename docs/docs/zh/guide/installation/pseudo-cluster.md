# 伪集群部署

伪集群部署目的是在单台机器部署 DolphinScheduler 服务，该模式下master、worker、api server 都在同一台机器上

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用[Standalone](standalone.md)方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用[伪集群部署](pseudo-cluster.md)。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

## 前置准备工作

伪分布式部署 DolphinScheduler 需要有外部软件的支持

* JDK：下载[JDK][jdk] (1.8+)，并将 JAVA_HOME 配置到以及 PATH 变量中。如果你的环境中已存在，可以跳过这步。
* 二进制包：在[下载页面](https://dolphinscheduler.apache.org/zh-cn/download/download.html)下载 DolphinScheduler 二进制包
* 数据库：[PostgreSQL](https://www.postgresql.org/download/) (8.2.15+) 或者 [MySQL](https://dev.mysql.com/downloads/mysql/) (5.7+)，两者任选其一即可，如 MySQL 则需要 JDBC Driver 8.0.16
* 注册中心：[ZooKeeper](https://zookeeper.apache.org/releases.html) (3.4.6+)，[下载地址][zookeeper]
* 进程树分析
  * macOS安装`pstree`
  * Fedora/Red/Hat/CentOS/Ubuntu/Debian安装`psmisc`

> **_注意:_** DolphinScheduler 本身不依赖 Hadoop、Hive、Spark，但如果你运行的任务需要依赖他们，就需要有对应的环境支持

## 准备 DolphinScheduler 启动环境

### 配置用户免密及权限

创建部署用户，并且一定要配置 `sudo` 免密。以创建 dolphinscheduler 用户为例

```shell
# 创建用户需使用 root 登录
useradd dolphinscheduler

# 添加密码
echo "dolphinscheduler" | passwd --stdin dolphinscheduler

# 配置 sudo 免密
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers

# 修改目录权限，使得部署用户对二进制包解压后的 apache-dolphinscheduler-*-bin 目录有操作权限
chown -R dolphinscheduler:dolphinscheduler apache-dolphinscheduler-*-bin
```

> **_注意:_**
>
> * 因为任务执行服务是以 `sudo -u {linux-user}` 切换不同 linux 用户的方式来实现多租户运行作业，所以部署用户需要有 sudo 权限，而且是免密的。初学习者不理解的话，完全可以暂时忽略这一点
> * 如果发现 `/etc/sudoers` 文件中有 "Defaults requirett" 这行，也请注释掉

### 配置机器SSH免密登陆

由于安装的时候需要向不同机器发送资源，所以要求各台机器间能实现SSH免密登陆。配置免密登陆的步骤如下

```shell
su dolphinscheduler

ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

> **_注意:_** 配置完成后，可以通过运行命令 `ssh localhost` 判断是否成功，如果不需要输入密码就能ssh登陆则证明成功

### 启动zookeeper

进入 zookeeper 的安装目录，将 `zoo_sample.cfg` 配置文件复制到 `conf/zoo.cfg`，并将 `conf/zoo.cfg` 中 dataDir 中的值改成 `dataDir=./tmp/zookeeper`

```shell
# 启动 zookeeper
./bin/zkServer.sh start
```

## 修改相关配置

完成基础环境的准备后，需要根据你的机器环境修改配置文件。配置文件可以在目录 `bin/env` 中找到，他们分别是 并命名为 `install_env.sh` 和 `dolphinscheduler_env.sh`。

### 修改 `install_env.sh` 文件

文件 `install_env.sh` 描述了哪些机器将被安装 DolphinScheduler 以及每台机器对应安装哪些服务。您可以在路径 `bin/env/install_env.sh` 中找到此文件，配置详情如下。

```shell
# ---------------------------------------------------------
# INSTALL MACHINE
# ---------------------------------------------------------
# Due to the master, worker, and API server being deployed on a single node, the IP of the server is the machine IP or localhost
ips="localhost"
masters="localhost"
workers="localhost:default"
alertServer="localhost"
apiServers="localhost"

# DolphinScheduler installation path, it will auto-create if not exists
installPath="~/dolphinscheduler"

# Deploy user, use the user you create in section **Configure machine SSH password-free login**
deployUser="dolphinscheduler"
```

### 修改 `dolphinscheduler_env.sh` 文件

文件 `./bin/env/dolphinscheduler_env.sh` 描述了下列配置：

* DolphinScheduler 的数据库配置，详细配置方法见[初始化数据库](#初始化数据库)
* 一些任务类型外部依赖路径或库文件，如 `JAVA_HOME` 和 `SPARK_HOME`都是在这里定义的
* 注册中心`zookeeper`
* 服务端相关配置，比如缓存，时区设置等

如果您不使用某些任务类型，您可以忽略任务外部依赖项，但您必须根据您的环境更改 `JAVA_HOME`、注册中心和数据库相关配置。

```sh
# JAVA_HOME, will use it to start DolphinScheduler server
export JAVA_HOME=${JAVA_HOME:-/opt/soft/java}

# Database related configuration, set database type, username and password
export DATABASE=${DATABASE:-postgresql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
export SPRING_DATASOURCE_URL=jdbc:postgresql://127.0.0.1:5432/dolphinscheduler
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}

# DolphinScheduler server related configuration
export SPRING_CACHE_TYPE=${SPRING_CACHE_TYPE:-none}
export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-UTC}
export MASTER_FETCH_COMMAND_NUM=${MASTER_FETCH_COMMAND_NUM:-10}

# Registry center configuration, determines the type and link of the registry center
export REGISTRY_TYPE=${REGISTRY_TYPE:-zookeeper}
export REGISTRY_ZOOKEEPER_CONNECT_STRING=${REGISTRY_ZOOKEEPER_CONNECT_STRING:-localhost:2181}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/opt/soft/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/opt/soft/hadoop/etc/hadoop}
export SPARK_HOME1=${SPARK_HOME1:-/opt/soft/spark1}
export SPARK_HOME2=${SPARK_HOME2:-/opt/soft/spark2}
export PYTHON_HOME=${PYTHON_HOME:-/opt/soft/python}
export HIVE_HOME=${HIVE_HOME:-/opt/soft/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_HOME=${DATAX_HOME:-/opt/soft/datax}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH
```

## 初始化数据库

DolphinScheduler 元数据存储在关系型数据库中，目前支持 PostgreSQL 和 MySQL，如果使用 MySQL 则需要手动下载 [mysql-connector-java 驱动][mysql] (8.0.16) 并移动到 DolphinScheduler 的 lib目录下（`tools/libs/`）。下面以 MySQL 为例，说明如何初始化数据库

对于mysql 5.6 / 5.7：

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# 修改 {user} 和 {password} 为你希望的用户名和密码
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';

mysql> flush privileges;
```

对于mysql 8：

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# 修改 {user} 和 {password} 为你希望的用户名和密码
mysql> CREATE USER '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%';
mysql> CREATE USER '{user}'@'localhost' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost';
mysql> FLUSH PRIVILEGES;
```

然后修改`./bin/env/dolphinscheduler_env.sh`，将username和password改成你在上一步中设置的用户名{user}和密码{password}

```shell
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
export SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
```  

完成上述步骤后，您已经为 DolphinScheduler 创建一个新数据库，现在你可以通过快速的 Shell 脚本来初始化数据库

```shell
sh tools/bin/create-schema.sh
```

## 启动 DolphinScheduler

使用上面创建的**部署用户**运行以下命令完成部署，部署后的运行日志将存放在 logs 文件夹内

```shell
sh ./bin/install.sh
```

> **_注意:_** 第一次部署的话，可能出现 5 次`sh: bin/dolphinscheduler-daemon.sh: No such file or directory`相关信息，次为非重要信息直接忽略即可

## 登录 DolphinScheduler

浏览器访问地址 http://localhost:12345/dolphinscheduler 即可登录系统UI。默认的用户名和密码是 **admin/dolphinscheduler123**

## 启停服务

```shell
# 一键停止集群所有服务
sh ./bin/stop-all.sh

# 一键开启集群所有服务
sh ./bin/start-all.sh

# 启停 Master
sh ./bin/dolphinscheduler-daemon.sh stop master-server
sh ./bin/dolphinscheduler-daemon.sh start master-server

# 启停 Worker
sh ./bin/dolphinscheduler-daemon.sh start worker-server
sh ./bin/dolphinscheduler-daemon.sh stop worker-server

# 启停 Api
sh ./bin/dolphinscheduler-daemon.sh start api-server
sh ./bin/dolphinscheduler-daemon.sh stop api-server

# 启停 Alert
sh ./bin/dolphinscheduler-daemon.sh start alert-server
sh ./bin/dolphinscheduler-daemon.sh stop alert-server
```

> **_注意1:_**: 每个服务在路径 `<server-name>/conf/dolphinscheduler_env.sh` 中都有 `dolphinscheduler_env.sh` 文件，这是可以为微
> 服务需求提供便利。意味着您可以基于不同的环境变量来启动各个服务，只需要在对应服务中配置 `bin/env/dolphinscheduler_env.sh` 然后通过 `<server-name>/bin/start.sh`
> 命令启动即可。但是如果您使用命令 `/bin/dolphinscheduler-daemon.sh start <server-name>` 启动服务器，它将会用文件 `bin/env/dolphinscheduler_env.sh`
> 覆盖 `<server-name>/conf/dolphinscheduler_env.sh` 然后启动服务，目的是为了减少用户修改配置的成本.

> **_注意2:_**：服务用途请具体参见《系统架构设计》小节。Python gateway service 默认与 api-server 一起启动，如果您不想启动 Python gateway service
> 请通过更改 api-server 配置文件 `api-server/conf/application.yaml` 中的 `python-gateway.enabled : false` 来禁用它。

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
[zookeeper]: https://zookeeper.apache.org/releases.html
[mysql]: https://downloads.MySQL.com/archives/c-j/
[issue]: https://github.com/apache/dolphinscheduler/issues/6597
