## DolphinScheduler是什么?

一个分布式易扩展的可视化DAG工作流任务调度系统。致力于解决数据处理流程中错综复杂的依赖关系，使调度系统在数据处理流程中`开箱即用`。

GitHub URL: https://github.com/apache/incubator-dolphinscheduler

Official Website: https://dolphinscheduler.apache.org

![DolphinScheduler](https://dolphinscheduler.apache.org/img/hlogo_colorful.svg)

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)

## 先决条件

- [Docker](https://docs.docker.com/engine/) 1.13.1+
- [Docker Compose](https://docs.docker.com/compose/) 1.11.0+

## 如何使用docker镜像

#### 以 docker-compose 的方式启动dolphinscheduler(推荐)

```
$ docker-compose -f ./docker/docker-swarm/docker-compose.yml up -d
```

在`docker-compose.yml`文件中，默认的创建`Postgres`的用户、密码和数据库，默认值分别为：`root`、`root`、`dolphinscheduler`。

同时，默认的`Zookeeper`也会在`docker-compose.yml`文件中被创建。

访问前端页面：http://192.168.xx.xx:12345/dolphinscheduler

默认的用户是`admin`，默认的密码是`dolphinscheduler123`

> **提示**: 为了在docker中快速开始，你可以创建一个名为`ds`的租户，并将这个租户`ds`关联到用户`admin`

#### 或者通过环境变量 **`DATABASE_HOST`**, **`DATABASE_PORT`**, **`ZOOKEEPER_QUORUM`**

你可以指定已经存在的 **`Postgres`** 和 **`Zookeeper`** 服务. 如下:

```
$ docker run -d --name dolphinscheduler \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:latest all
```

访问前端页面：http://192.168.xx.xx:12345/dolphinscheduler

#### 或者运行dolphinscheduler中的部分服务

你能够运行dolphinscheduler中的部分服务。

* 创建一个 **本地卷** 用于资源存储，如下:

```
docker volume create dolphinscheduler-resource-local
```

* 启动一个 **master server**, 如下:

```
$ docker run -d --name dolphinscheduler-master \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:latest master-server
```

* 启动一个 **worker server** (包括 **logger server**), 如下:

```
$ docker run -d --name dolphinscheduler-worker \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-v dolphinscheduler-resource-local:/dolphinscheduler \
apache/dolphinscheduler:latest worker-server
```

* 启动一个 **api server**, 如下:

```
$ docker run -d --name dolphinscheduler-api \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-v dolphinscheduler-resource-local:/dolphinscheduler \
-p 12345:12345 \
apache/dolphinscheduler:latest api-server
```

* 启动一个 **alert server**, 如下:

```
$ docker run -d --name dolphinscheduler-alert \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
apache/dolphinscheduler:latest alert-server
```

**注意**: 当你运行dolphinscheduler中的部分服务时，你必须指定这些环境变量 `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_DATABASE`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `ZOOKEEPER_QUORUM`。

## 如何构建一个docker镜像

你能够在类Unix系统和Windows系统中构建一个docker镜像。

类Unix系统, 如下:

```bash
$ cd path/incubator-dolphinscheduler
$ sh ./docker/build/hooks/build
```

Windows系统, 如下:

```bat
C:\incubator-dolphinscheduler>.\docker\build\hooks\build.bat
```

如果你不理解这些脚本 `./docker/build/hooks/build` `./docker/build/hooks/build.bat`，请阅读里面的内容。

## 环境变量

DolphinScheduler Docker 容器通过环境变量进行配置，缺省时将会使用默认值

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

配置`dolphinscheduler`的`jvm options`，适用于`master-server`、`worker-server`、`api-server`、`alert-server`、`logger-server`，默认值 `""`、

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

**`YARN_RESOURCEMANAGER_HA_RM_IDS`**

配置`dolphinscheduler`的yarn resourcemanager ha rm ids，默认值 `空`。

**`YARN_APPLICATION_STATUS_ADDRESS`**

配置`dolphinscheduler`的yarn application status地址，默认值 `http://ds1:8088/ws/v1/cluster/apps/%s`。

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

配置`master-server`中的任务提交交互时间，默认值 `1000`。

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

### Logger Server

**`LOGGER_SERVER_OPTS`**

配置`logger-server`的`jvm options`，默认值 `-Xms512m -Xmx512m -Xmn256m`。

## 初始化脚本

如果你想在编译的时候或者运行的时候附加一些其它的操作及新增一些环境变量，你可以在`/root/start-init-conf.sh`文件中进行修改，同时如果涉及到配置文件的修改，请在`/opt/dolphinscheduler/conf/*.tpl`中修改相应的配置文件

例如，在`/root/start-init-conf.sh`添加一个环境变量`SECURITY_AUTHENTICATION_TYPE`：

```
export SECURITY_AUTHENTICATION_TYPE=PASSWORD
```

当添加以上环境变量后，你应该在相应的模板文件`application-api.properties.tpl`中添加这个环境变量配置:
```
security.authentication.type=${SECURITY_AUTHENTICATION_TYPE}
```

`/root/start-init-conf.sh`将根据模板文件动态的生成配置文件：

```sh
echo "generate dolphinscheduler config"
ls ${DOLPHINSCHEDULER_HOME}/conf/ | grep ".tpl" | while read line; do
eval "cat << EOF
$(cat ${DOLPHINSCHEDULER_HOME}/conf/${line})
EOF
" > ${DOLPHINSCHEDULER_HOME}/conf/${line%.*}
done
```

## FAQ

### 如何通过 docker-compose 停止 dolphinscheduler？

停止所有容器:

```
docker-compose stop
```

停止所有容器并移除所有容器，网络和存储卷:

```
docker-compose down -v
```

### 如何在 Docker Swarm 上部署 dolphinscheduler？

假设 Docker Swarm 集群已经部署（如果还没有创建 Docker Swarm 集群，请参考 [create-swarm](https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/)）

启动名为 dolphinscheduler 的 stack

```
docker stack deploy -c docker-stack.yml dolphinscheduler
```

启动并移除名为 dolphinscheduler 的 stack

```
docker stack rm dolphinscheduler
```

### 如何用 MySQL 替代 PostgreSQL 作为 DolphinScheduler 的数据库？

> 由于商业许可证的原因，我们不能直接使用 MySQL 的驱动包.
>
> 如果你要使用 MySQL, 你可以基于官方镜像 `apache/dolphinscheduler` 进行构建.

1. 下载 MySQL 驱动包 [mysql-connector-java-5.1.49.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar) (要求 `>=5.1.47`)

2. 创建一个新的 `Dockerfile`，用于添加 MySQL 的驱动包:

```
FROM apache/dolphinscheduler:latest
COPY mysql-connector-java-5.1.49.jar /opt/dolphinscheduler/lib
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

1. 下载 MySQL 驱动包 [mysql-connector-java-5.1.49.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar) (要求 `>=5.1.47`)

2. 创建一个新的 `Dockerfile`，用于添加 MySQL 驱动包:

```
FROM apache/dolphinscheduler:latest
COPY mysql-connector-java-5.1.49.jar /opt/dolphinscheduler/lib
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
FROM apache/dolphinscheduler:latest
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

### 如何支持 Python 2 pip 以及自定义 requirements.txt?

1. 创建一个新的 `Dockerfile`，用于安装 pip:

```
FROM apache/dolphinscheduler:latest
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
FROM apache/dolphinscheduler:latest
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
docker cp spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker:/opt/soft
```

因为存储卷 `dolphinscheduler-shared-local` 被挂载到 `/opt/soft`, 因此 `/opt/soft` 中的所有文件都不会丢失

4. 登录到容器并确保 `SPARK_HOME2` 存在

```bash
docker exec -it dolphinscheduler-worker bash
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # or just mv
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
- 主函数的Class: `org.apache.spark.examples.SparkPi`
- 主程序包: `spark-examples_2.11-2.4.7.jar`
- 部署方式: `local`

同样地, 检查任务日志是否包含输出 `Pi is roughly 3.146015`

7. 验证 Spark on YARN

Spark on YARN (部署方式为 `cluster` 或 `client`) 需要 Hadoop 支持. 类似于 Spark 支持, 支持 Hadoop 的操作几乎和前面的步骤相同

确保 `$HADOOP_HOME` 和 `$HADOOP_CONF_DIR` 存在

更多信息请查看 [incubator-dolphinscheduler](https://github.com/apache/incubator-dolphinscheduler.git) 文档.
