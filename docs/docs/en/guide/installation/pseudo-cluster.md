# Pseudo-Cluster Deployment

The purpose of the pseudo-cluster deployment is to deploy the DolphinScheduler service on a single machine. In this mode, DolphinScheduler's master, worker, API server, are all on the same machine.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Preparation

Pseudo-cluster deployment of DolphinScheduler requires external software support:

* JDK：Download [JDK][jdk] (1.8+), and configure `JAVA_HOME` to and `PATH` variable. You can skip this step, if it already exists in your environment.
* Binary package: Download the DolphinScheduler binary package at [download page](https://dolphinscheduler.apache.org/en-us/download/download.html)
* Database: [PostgreSQL](https://www.postgresql.org/download/) (8.2.15+) or [MySQL](https://dev.mysql.com/downloads/mysql/) (5.7+), you can choose one of the two, such as MySQL requires JDBC Driver 8.0.16
* Registry Center: [ZooKeeper](https://zookeeper.apache.org/releases.html) (3.4.6+)，[download link][zookeeper]
* Process tree analysis
  * `pstree` for macOS
  * `psmisc` for Fedora/Red/Hat/CentOS/Ubuntu/Debian

> **_Note:_** DolphinScheduler itself does not depend on Hadoop, Hive, Spark, but if you need to run tasks that depend on them, you need to have the corresponding environment support.

## DolphinScheduler Startup Environment

### Configure User Exemption and Permissions

Create a deployment user, and make sure to configure `sudo` without password. Here make an example to create user `dolphinscheduler`:

```shell
# To create a user, login as root
useradd dolphinscheduler

# Add password
echo "dolphinscheduler" | passwd --stdin dolphinscheduler

# Configure sudo without password
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers

# Modify directory permissions and grant permissions for user you created above
chown -R dolphinscheduler:dolphinscheduler apache-dolphinscheduler-*-bin
```

> **_NOTICE:_**
>
> * Due to DolphinScheduler's multi-tenant task switch user using command `sudo -u {linux-user}`, the deployment user needs to have `sudo` privileges and be password-free. If novice learners don’t understand, you can ignore this point for now.
> * If you find the line "Defaults requirett" in the `/etc/sudoers` file, please comment the content.

### Configure Machine SSH Password-Free Login

Since resources need to be sent to different machines during installation, SSH password-free login is required between each machine. The following shows the steps to configure password-free login:

```shell
su dolphinscheduler

ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

> **_Notice:_** After the configuration is complete, you can run the command `ssh localhost` to test works or not. If you can login with ssh without password stands for successful.

### Start ZooKeeper

Go to the ZooKeeper installation directory, copy configure file `zoo_sample.cfg` to `conf/zoo.cfg`, and change value of dataDir in `conf/zoo.cfg` to `dataDir=./tmp/zookeeper`.

```shell
# Start ZooKeeper
./bin/zkServer.sh start
```

## Modify Configuration

After completing the preparation of the basic environment, you need to modify the configuration file according to the
environment you used. The configuration files are both in directory `bin/env` and named `install_env.sh` and `dolphinscheduler_env.sh`.

### Modify `install_env.sh`

File `install_env.sh` describes which machines will be installed DolphinScheduler and what server will be installed on
each machine. You could find this file in the path `bin/env/install_env.sh` and the detail of the configuration as below.

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

### Modify `dolphinscheduler_env.sh`

File  `./bin/env/dolphinscheduler_env.sh` describes the following configurations:

* Database configuration of DolphinScheduler, see [Initialize the Database](#initialize-the-database) for detailed instructions.
* Some tasks which need external dependencies or libraries such as `JAVA_HOME` and `SPARK_HOME`.
* Registry center `zookeeper`.
* Server related configuration, such as cache type, timezone, etc.

You could ignore the task external dependencies if you do not use those tasks, but you have to change `JAVA_HOME`, registry center and database
related configurations based on your environment.

```shell
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

## Initialize the Database

DolphinScheduler metadata is stored in the relational database. Currently, supports PostgreSQL and MySQL. If you use MySQL, you need to manually download [mysql-connector-java driver][mysql] (8.0.16) and move it to the lib directory of DolphinScheduler, which is `tools/libs/`. Let's take MySQL as an example for how to initialize the database:

For mysql 5.6 / 5.7

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# Replace {user} and {password} with your username and password
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';

mysql> flush privileges;
```

For mysql 8:

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# Replace {user} and {password} with your username and password
mysql> CREATE USER '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%';
mysql> CREATE USER '{user}'@'localhost' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost';
mysql> FLUSH PRIVILEGES;
``` 

Then, modify `./bin/env/dolphinscheduler_env.sh` to use mysql, change {user} and {password} to what you set in the previous step.

```shell
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver
export SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
```

After the above steps done you would create a new database for DolphinScheduler, then run the Shell script to init database:

```shell
sh tools/bin/create-schema.sh
```

## Start DolphinScheduler

Use **deployment user** you created above, running the following command to complete the deployment, and the server log will be stored in the logs folder.

```shell
sh ./bin/install.sh
```

> **_Note:_** For the first time deployment, there maybe occur five times of `sh: bin/dolphinscheduler-daemon.sh: No such file or directory` in the terminal,
 this is non-important information that you can ignore.

## Login DolphinScheduler

Access address `http://localhost:12345/dolphinscheduler` and login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**

## Start or Stop Server

```shell
# Stop all DolphinScheduler server
sh ./bin/stop-all.sh

# Start all DolphinScheduler server
sh ./bin/start-all.sh

# Start or stop DolphinScheduler Master
sh ./bin/dolphinscheduler-daemon.sh stop master-server
sh ./bin/dolphinscheduler-daemon.sh start master-server

# Start or stop DolphinScheduler Worker
sh ./bin/dolphinscheduler-daemon.sh start worker-server
sh ./bin/dolphinscheduler-daemon.sh stop worker-server

# Start or stop DolphinScheduler Api
sh ./bin/dolphinscheduler-daemon.sh start api-server
sh ./bin/dolphinscheduler-daemon.sh stop api-server

# Start or stop Alert
sh ./bin/dolphinscheduler-daemon.sh start alert-server
sh ./bin/dolphinscheduler-daemon.sh stop alert-server
```

> **_Note1:_**: Each server have `dolphinscheduler_env.sh` file in path `<server-name>/conf/dolphinscheduler_env.sh` which
> for micro-services need. It means that you could start all servers by command `<server-name>/bin/start.sh` with different
> environment variable from `bin/env/dolphinscheduler_env.sh`. But it will use file `bin/env/dolphinscheduler_env.sh` overwrite
> `<server-name>/conf/dolphinscheduler_env.sh` if you start server with command `/bin/dolphinscheduler-daemon.sh start <server-name>`.

> **_Note2:_**: Please refer to the section of "System Architecture Design" for service usage. Python gateway service is
> started along with the api-server, and if you do not want to start Python gateway service please disabled it by changing
> the yaml config `python-gateway.enabled : false` in api-server's configuration path `api-server/conf/application.yaml` 

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
[zookeeper]: https://zookeeper.apache.org/releases.html
[mysql]: https://downloads.MySQL.com/archives/c-j/
[issue]: https://github.com/apache/dolphinscheduler/issues/6597
