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

<!--
修改数据库配置，并初始化

```properties
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true
# 如果你不是以 dolphinscheduler/dolphinscheduler 作为用户名和密码的，需要进行修改
spring.datasource.username=dolphinscheduler
spring.datasource.password=dolphinscheduler
```

修改并保存完后，执行 script 目录下的创建表及导入基础数据脚本

```shell
sh script/create-dolphinscheduler.sh
```
-->

## 修改相关配置

完成了基础环境的准备后，在运行部署命令前，还需要根据环境修改配置文件。配置文件在路径在`conf/config/install_config.conf`下，一般部署只需要修改**INSTALL MACHINE、DolphinScheduler ENV、Database、Registry Server**部分即可完成部署，下面对必须修改参数进行说明

```shell
# ---------------------------------------------------------
# INSTALL MACHINE
# ---------------------------------------------------------
# 因为是在单节点上部署master、worker、API server，所以服务器的IP均为机器IP或者localhost
ips="localhost"
masters="localhost"
workers="localhost:default"
alertServer="localhost"
apiServers="localhost"

# DolphinScheduler安装路径，如果不存在会创建
installPath="~/dolphinscheduler"

# 部署用户，填写在 **配置用户免密及权限** 中创建的用户
deployUser="dolphinscheduler"

# ---------------------------------------------------------
# DolphinScheduler ENV
# ---------------------------------------------------------
# JAVA_HOME 的路径，是在 **前置准备工作** 安装的JDK中 JAVA_HOME 所在的位置
javaHome="/your/java/home/here"

# ---------------------------------------------------------
# Database
# ---------------------------------------------------------
# 数据库的类型，用户名，密码，IP，端口，元数据库db。其中dbtype目前支持 mysql 和 postgresql
dbtype="mysql"
dbhost="localhost:3306"
# 如果你不是以 dolphinscheduler/dolphinscheduler 作为用户名和密码的，需要进行修改
username="dolphinscheduler"
password="dolphinscheduler"
dbname="dolphinscheduler"

# ---------------------------------------------------------
# Registry Server
# ---------------------------------------------------------
# 注册中心地址，zookeeper服务的地址
registryServers="localhost:2181"
```

## 初始化数据库

DolphinScheduler 元数据存储在关系型数据库中，目前支持 PostgreSQL 和 MySQL，如果使用 MySQL 则需要手动下载 [mysql-connector-java 驱动][mysql] (8.0.16) 并移动到 DolphinScheduler 的 lib目录下。下面以 MySQL 为例，说明如何初始化数据库

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# 修改 {user} 和 {password} 为你希望的用户名和密码
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';

mysql> flush privileges;
```

完成上述步骤后，您已经为 DolphinScheduler 创建一个新数据库，现在你可以通过快速的 Shell 脚本来初始化数据库

```shell
sh script/create-dolphinscheduler.sh
```

## 启动 DolphinScheduler

使用上面创建的**部署用户**运行以下命令完成部署，部署后的运行日志将存放在 logs 文件夹内

```shell
sh install.sh
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

> **_注意:_**：服务用途请具体参见《系统架构设计》小节

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
[zookeeper]: https://zookeeper.apache.org/releases.html
[mysql]: https://downloads.MySQL.com/archives/c-j/
[issue]: https://github.com/apache/dolphinscheduler/issues/6597
