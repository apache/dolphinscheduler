# Pseudo-Cluster Deployment

The purpose of the pseudo-cluster deployment is to deploy the DolphinScheduler service on a single machine. In this mode, DolphinScheduler's master, worker, API server, are all on the same machine.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow pseudo-cluster deployment. If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Preparation

Pseudo-cluster deployment of DolphinScheduler requires external software support:

- JDK：download [JDK][jdk] (1.8 or 11), install and configure environment variable `JAVA_HOME` and append `bin` dir (included in `JAVA_HOME`) to `PATH` variable. You can skip this step if it already exists in your environment.
- Binary package: Download the DolphinScheduler binary package at [download page](https://dolphinscheduler.apache.org/en-us/download)
- Database: [PostgreSQL](https://www.postgresql.org/download/) (8.2.15+) or [MySQL](https://dev.mysql.com/downloads/mysql/) (5.7+), you can choose one of the two, such as MySQL requires JDBC Driver 8.0.33
- Registry Center: [ZooKeeper](https://zookeeper.apache.org/releases.html) (3.8.0+), [MYSQL](https://www.mysql.com/)(8.0.33), [ETCD](https://etcd.io/)
- Process tree analysis
  - `pstree` for macOS
  - `psmisc` for Fedora/Red/Hat/CentOS/Ubuntu/Debian

## Download Plugins Dependencies

Starting from version 3.3.0, the binary package no longer provides plugin dependencies, and users need to download them by themselves. The plugin dependency package download address: [Plugin Dependency Package](https://repo.maven.apache.org/maven2/org/apache/dolphinscheduler)
You can also execute the following command to install plugin dependencies:

```shell
bash ./bin/install-plugins.sh 3.3.0
```

Usually, you do not need all connector plugins, you can specify the plugins you need by configuring `conf/plugins_config`. For example, if you only need the `dolphinscheduler-task-shell` plugin, you can modify the configuration file as follows:

```
--task-plugins--
dolphinscheduler-task-shell
--end--
```

## DolphinScheduler Startup Environment

> **_Note:_** DolphinScheduler itself does not depend on Hadoop, Hive, Spark, but if you need to run tasks that depend on them, you need to have the corresponding environment support.

### Configure User Exemption and Permissions

Create a deployment user, and make sure to configure `sudo` without password. Here make an example to create user `dolphinscheduler`:

```shell
# To create a user, login as root
useradd dolphinscheduler

# Add password
echo "dolphinscheduler" | passwd --stdin dolphinscheduler

# Configure sudo without password
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers

# Modify directory permissions and grant permissions for user you created above
chown -R dolphinscheduler:dolphinscheduler apache-dolphinscheduler-*-bin
chmod -R 755 apache-dolphinscheduler-*-bin
```

> **_NOTICE:_**
>
> - Due to DolphinScheduler's multi-tenant task switch user using command `sudo -u {linux-user} -i`, the deployment user needs to have `sudo` privileges and be password-free. If novice learners don’t understand, you can ignore this point for now.
> - If you find the line "Defaults requiretty" in the `/etc/sudoers` file, please comment the content.

### Prepare Zookeeper

If you use Zookeeper as the registry center, you need to install Zookeeper and start it first.

## Modify Configuration

After completing the preparation of the basic environment, you need to modify the configuration file according to the
environment you used. Change the environment configurations via `export <ENV_NAME>=<VALUE>`. The configuration files are located in directory `bin/env` as `dolphinscheduler_env.sh`.

### Modify `dolphinscheduler_env.sh`

File `./bin/env/dolphinscheduler_env.sh`,`api-server/conf/application.yaml`，
`master-server/conf/application.yaml`，`worker-server/conf/application.yaml`，`alert-server/conf/application.yaml` describes the following configurations:

- Database configuration of DolphinScheduler, see [Initialize the Database](#initialize-the-database) for detailed instructions.
- Some tasks which need external dependencies or libraries such as `JAVA_HOME` and `SPARK_HOME`.
- Default registry center is mysql.
- Server related configuration, such as cache type, timezone, etc.

You could ignore the task external dependencies if you do not use those tasks, but you have to change `JAVA_HOME`, registry center and database
related configurations based on your environment.

```shell
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
export PYTHON_LAUNCHER=${PYTHON_LAUNCHER:-/opt/soft/python/bin/python3}
export HIVE_HOME=${HIVE_HOME:-/opt/soft/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_LAUNCHER=${DATAX_LAUNCHER:-/opt/soft/datax/bin/datax.py}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_LAUNCHER:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_LAUNCHER:$PATH
```

> **_Note:_** If you use MySQL database, set `DATABASE` to `mysql`, and modify `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` to your database configuration.
>
> **_Note:_** The configuration in `dolphinscheduler_env.sh` will overwrite the configuration in each service's configuration file (application.yaml), so if you configure a parameter in the application.yaml file and also configure it in `dolphinscheduler_env.sh`,
> the configuration in `dolphinscheduler_env.sh` will take precedence. The configuration format in `dolphinscheduler_env.sh` is as follows:
> `SPRING_DATASOURCE_URL` in `application.yaml` is `spring.datasource.url`, and so on.

## Initialize the Database

Follow the instructions in [datasource-setting](datasource-setting.md) `Pseudo-Cluster/Cluster Initialize the Database` section to create and initialize database

## Start DolphinScheduler

The server log will be stored in the `xxx-server/logs` folder.

```
# Start api-server
bash ./bin/dolphinscheduler-daemon.sh start api-server

# Start master-server
bash ./bin/dolphinscheduler-daemon.sh start master-server

# Start worker-server
bash ./bin/dolphinscheduler-daemon.sh start worker-server

# Start alert-server
bash ./bin/dolphinscheduler-daemon.sh start alert-server
```

> **_Note:_** For the first time deployment, you can check the status of server through bash ./bin/dolphinscheduler-daemon.sh status xxx-server

## Login DolphinScheduler

Access address `http://localhost:12345/dolphinscheduler/ui` and login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**

## Start or Stop Server

```shell
# Check the status of DolphinScheduler server
bash ./bin/dolphinscheduler-daemon.sh status xxx-server

# Start or stop DolphinScheduler Master
bash ./bin/dolphinscheduler-daemon.sh stop master-server
bash ./bin/dolphinscheduler-daemon.sh start master-server

# Start or stop DolphinScheduler Worker
bash ./bin/dolphinscheduler-daemon.sh start worker-server
bash ./bin/dolphinscheduler-daemon.sh stop worker-server

# Start or stop DolphinScheduler Api
bash ./bin/dolphinscheduler-daemon.sh start api-server
bash ./bin/dolphinscheduler-daemon.sh stop api-server

# Start or stop Alert
bash ./bin/dolphinscheduler-daemon.sh start alert-server
bash ./bin/dolphinscheduler-daemon.sh stop alert-server
```

> **_Note1:_**: Each server have `dolphinscheduler_env.sh` file in path `<service>/conf/dolphinscheduler_env.sh` which
> for micro-services need. It means that you could start all servers by command `<service>/bin/start.sh` with different
> environment variable from `<service>/conf/dolphinscheduler_env.sh`. But it will use file `bin/env/dolphinscheduler_env.sh` overwrite
> `<service>/conf/dolphinscheduler_env.sh` if you start server with command `/bin/dolphinscheduler-daemon.sh start <service>`.
>
> **_Note2:_**: Please refer to the section of "System Architecture Design" for service usage. Python gateway service is
> started along with the api-server, and if you do not want to start Python gateway service please disabled it by changing
> the yaml config `python-gateway.enabled : false` in api-server's configuration path `api-server/conf/application.yaml`
> **_Note3:_**: DS uses the /tmp/dolphinscheduler directory as the resource center by default. If you need to change the directory of the resource center, change the resource items in the conf/common.properties file

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
[zookeeper]: https://zookeeper.apache.org/releases.html
[issue]: https://github.com/apache/dolphinscheduler/issues/6597

