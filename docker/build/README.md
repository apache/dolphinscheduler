## What is DolphinScheduler?

DolphinScheduler is a distributed and easy-to-expand visual DAG workflow scheduling system, dedicated to solving the complex dependencies in data processing, making the scheduling system out of the box for data processing.

GitHub URL: https://github.com/apache/incubator-dolphinscheduler

Official Website: https://dolphinscheduler.apache.org

![DolphinScheduler](https://dolphinscheduler.apache.org/img/hlogo_colorful.svg)

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)

## Prerequisites

- [Docker](https://docs.docker.com/engine/) 1.13.1+
- [Docker Compose](https://docs.docker.com/compose/) 1.11.0+

## How to use this docker image

#### You can start a dolphinscheduler by docker-compose (recommended)

```
$ docker-compose -f ./docker/docker-swarm/docker-compose.yml up -d
```

The default **postgres** user `root`, postgres password `root` and database `dolphinscheduler` are created in the `docker-compose.yml`.

The default **zookeeper** is created in the `docker-compose.yml`.

Access the Web UI: http://192.168.xx.xx:12345/dolphinscheduler

The default username is `admin` and the default password is `dolphinscheduler123`

> **Tip**: For quick start in docker, you can create a tenant named `ds` and associate the user `admin` with the tenant `ds`

#### Or via Environment Variables **`DATABASE_HOST`**, **`DATABASE_PORT`**, **`ZOOKEEPER_QUORUM`**

You can specify **existing postgres and zookeeper service**. Example:

```
$ docker run -d --name dolphinscheduler \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:latest all
```

Access the Web UI：http://192.168.xx.xx:12345/dolphinscheduler

#### Or start a standalone dolphinscheduler server

You can start a standalone dolphinscheduler server.

* Create a **local volume** for resource storage, For example:

```
docker volume create dolphinscheduler-resource-local
```

* Start a **master server**, For example:

```
$ docker run -d --name dolphinscheduler-master \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:latest master-server
```

* Start a **worker server** (including **logger server**), For example:

```
$ docker run -d --name dolphinscheduler-worker \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-v dolphinscheduler-resource-local:/dolphinscheduler \
apache/dolphinscheduler:latest worker-server
```

* Start a **api server**, For example:

```
$ docker run -d --name dolphinscheduler-api \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-v dolphinscheduler-resource-local:/dolphinscheduler \
-p 12345:12345 \
apache/dolphinscheduler:latest api-server
```

* Start a **alert server**, For example:

```
$ docker run -d --name dolphinscheduler-alert \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
apache/dolphinscheduler:latest alert-server
```

**Note**: You must be specify `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_DATABASE`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `ZOOKEEPER_QUORUM` when start a standalone dolphinscheduler server.

## How to build a docker image

You can build a docker image in A Unix-like operating system, You can also build it in Windows operating system.

In Unix-Like, Example:

```bash
$ cd path/incubator-dolphinscheduler
$ sh ./docker/build/hooks/build
```

In Windows, Example:

```bat
C:\incubator-dolphinscheduler>.\docker\build\hooks\build.bat
```

Please read `./docker/build/hooks/build` `./docker/build/hooks/build.bat` script files if you don't understand

## Environment Variables

The DolphinScheduler Docker container is configured through environment variables, and the default value will be used if an environment variable is not set.

### Database

**`DATABASE_TYPE`**

This environment variable sets the type for database. The default value is `postgresql`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_DRIVER`**

This environment variable sets the type for database. The default value is `org.postgresql.Driver`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_HOST`**

This environment variable sets the host for database. The default value is `127.0.0.1`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_PORT`**

This environment variable sets the port for database. The default value is `5432`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_USERNAME`**

This environment variable sets the username for database. The default value is `root`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_PASSWORD`**

This environment variable sets the password for database. The default value is `root`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_DATABASE`**

This environment variable sets the database for database. The default value is `dolphinscheduler`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_PARAMS`**

This environment variable sets the database for database. The default value is `characterEncoding=utf8`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

### ZooKeeper

**`ZOOKEEPER_QUORUM`**

This environment variable sets zookeeper quorum. The default value is `127.0.0.1:2181`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`, `api-server`.

**`ZOOKEEPER_ROOT`**

This environment variable sets zookeeper root directory for dolphinscheduler. The default value is `/dolphinscheduler`.

### Common

**`DOLPHINSCHEDULER_OPTS`**

This environment variable sets jvm options for dolphinscheduler, suitable for `master-server`, `worker-server`, `api-server`, `alert-server`, `logger-server`. The default value is empty.

**`DATA_BASEDIR_PATH`**

User data directory path, self configuration, please make sure the directory exists and have read write permissions. The default value is `/tmp/dolphinscheduler`

**`RESOURCE_STORAGE_TYPE`**

This environment variable sets resource storage type for dolphinscheduler like `HDFS`, `S3`, `NONE`. The default value is `HDFS`.

**`RESOURCE_UPLOAD_PATH`**

This environment variable sets resource store path on HDFS/S3 for resource storage. The default value is `/dolphinscheduler`.

**`FS_DEFAULT_FS`**

This environment variable sets fs.defaultFS for resource storage like `file:///`, `hdfs://mycluster:8020` or `s3a://dolphinscheduler`. The default value is `file:///`.

**`FS_S3A_ENDPOINT`**

This environment variable sets s3 endpoint for resource storage. The default value is `s3.xxx.amazonaws.com`.

**`FS_S3A_ACCESS_KEY`**

This environment variable sets s3 access key for resource storage. The default value is `xxxxxxx`.

**`FS_S3A_SECRET_KEY`**

This environment variable sets s3 secret key for resource storage. The default value is `xxxxxxx`.

**`HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE`**

This environment variable sets whether to startup kerberos. The default value is `false`.

**`JAVA_SECURITY_KRB5_CONF_PATH`**

This environment variable sets java.security.krb5.conf path. The default value is `/opt/krb5.conf`.

**`LOGIN_USER_KEYTAB_USERNAME`**

This environment variable sets login user from keytab username. The default value is `hdfs@HADOOP.COM`.

**`LOGIN_USER_KEYTAB_PATH`**

This environment variable sets login user from keytab path. The default value is `/opt/hdfs.keytab`.

**`KERBEROS_EXPIRE_TIME`**

This environment variable sets kerberos expire time, the unit is hour. The default value is `2`.

**`HDFS_ROOT_USER`**

This environment variable sets hdfs root user when resource.storage.type=HDFS. The default value is `hdfs`.

**`YARN_RESOURCEMANAGER_HA_RM_IDS`**

This environment variable sets yarn resourcemanager ha rm ids. The default value is empty.

**`YARN_APPLICATION_STATUS_ADDRESS`**

This environment variable sets yarn application status address. The default value is `http://ds1:8088/ws/v1/cluster/apps/%s`.

**`SKYWALKING_ENABLE`**

This environment variable sets whether to enable skywalking. The default value is `false`.

**`SW_AGENT_COLLECTOR_BACKEND_SERVICES`**

This environment variable sets agent collector backend services for skywalking. The default value is `127.0.0.1:11800`.

**`SW_GRPC_LOG_SERVER_HOST`**

This environment variable sets grpc log server host for skywalking. The default value is `127.0.0.1`.

**`SW_GRPC_LOG_SERVER_PORT`**

This environment variable sets grpc log server port for skywalking. The default value is `11800`.

**`HADOOP_HOME`**

This environment variable sets `HADOOP_HOME`. The default value is `/opt/soft/hadoop`.

**`HADOOP_CONF_DIR`**

This environment variable sets `HADOOP_CONF_DIR`. The default value is `/opt/soft/hadoop/etc/hadoop`.

**`SPARK_HOME1`**

This environment variable sets `SPARK_HOME1`. The default value is `/opt/soft/spark1`.

**`SPARK_HOME2`**

This environment variable sets `SPARK_HOME2`. The default value is `/opt/soft/spark2`.

**`PYTHON_HOME`**

This environment variable sets `PYTHON_HOME`. The default value is `/usr/bin/python`.

**`JAVA_HOME`**

This environment variable sets `JAVA_HOME`. The default value is `/usr/local/openjdk-8`.

**`HIVE_HOME`**

This environment variable sets `HIVE_HOME`. The default value is `/opt/soft/hive`.

**`FLINK_HOME`**

This environment variable sets `FLINK_HOME`. The default value is `/opt/soft/flink`.

**`DATAX_HOME`**

This environment variable sets `DATAX_HOME`. The default value is `/opt/soft/datax`.

### Master Server

**`MASTER_SERVER_OPTS`**

This environment variable sets jvm options for `master-server`. The default value is `-Xms1g -Xmx1g -Xmn512m`.

**`MASTER_EXEC_THREADS`**

This environment variable sets exec thread number for `master-server`. The default value is `100`.

**`MASTER_EXEC_TASK_NUM`**

This environment variable sets exec task number for `master-server`. The default value is `20`.

**`MASTER_DISPATCH_TASK_NUM`**

This environment variable sets dispatch task number for `master-server`. The default value is `3`.

**`MASTER_HOST_SELECTOR`**

This environment variable sets host selector for `master-server`. Optional values include `Random`, `RoundRobin` and `LowerWeight`. The default value is `LowerWeight`.

**`MASTER_HEARTBEAT_INTERVAL`**

This environment variable sets heartbeat interval for `master-server`. The default value is `10`.

**`MASTER_TASK_COMMIT_RETRYTIMES`**

This environment variable sets task commit retry times for `master-server`. The default value is `5`.

**`MASTER_TASK_COMMIT_INTERVAL`**

This environment variable sets task commit interval for `master-server`. The default value is `1000`.

**`MASTER_MAX_CPULOAD_AVG`**

This environment variable sets max cpu load avg for `master-server`. The default value is `-1`.

**`MASTER_RESERVED_MEMORY`**

This environment variable sets reserved memory for `master-server`, the unit is G. The default value is `0.3`.

### Worker Server

**`WORKER_SERVER_OPTS`**

This environment variable sets jvm options for `worker-server`. The default value is `-Xms1g -Xmx1g -Xmn512m`.

**`WORKER_EXEC_THREADS`**

This environment variable sets exec thread number for `worker-server`. The default value is `100`.

**`WORKER_HEARTBEAT_INTERVAL`**

This environment variable sets heartbeat interval for `worker-server`. The default value is `10`.

**`WORKER_MAX_CPULOAD_AVG`**

This environment variable sets max cpu load avg for `worker-server`. The default value is `-1`.

**`WORKER_RESERVED_MEMORY`**

This environment variable sets reserved memory for `worker-server`, the unit is G. The default value is `0.3`.

**`WORKER_GROUPS`**

This environment variable sets groups for `worker-server`. The default value is `default`.

### Alert Server

**`ALERT_SERVER_OPTS`**

This environment variable sets jvm options for `alert-server`. The default value is `-Xms512m -Xmx512m -Xmn256m`.

**`XLS_FILE_PATH`**

This environment variable sets xls file path for `alert-server`. The default value is `/tmp/xls`.

**`MAIL_SERVER_HOST`**

This environment variable sets mail server host for `alert-server`. The default value is empty.

**`MAIL_SERVER_PORT`**

This environment variable sets mail server port for `alert-server`. The default value is empty.

**`MAIL_SENDER`**

This environment variable sets mail sender for `alert-server`. The default value is empty.

**`MAIL_USER=`**

This environment variable sets mail user for `alert-server`. The default value is empty.

**`MAIL_PASSWD`**

This environment variable sets mail password for `alert-server`. The default value is empty.

**`MAIL_SMTP_STARTTLS_ENABLE`**

This environment variable sets SMTP tls for `alert-server`. The default value is `true`.

**`MAIL_SMTP_SSL_ENABLE`**

This environment variable sets SMTP ssl for `alert-server`. The default value is `false`.

**`MAIL_SMTP_SSL_TRUST`**

This environment variable sets SMTP ssl truest for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_ENABLE`**

This environment variable sets enterprise wechat enable for `alert-server`. The default value is `false`.

**`ENTERPRISE_WECHAT_CORP_ID`**

This environment variable sets enterprise wechat corp id for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_SECRET`**

This environment variable sets enterprise wechat secret for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_AGENT_ID`**

This environment variable sets enterprise wechat agent id for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_USERS`**

This environment variable sets enterprise wechat users for `alert-server`. The default value is empty.

### Api Server

**`API_SERVER_OPTS`**

This environment variable sets jvm options for `api-server`. The default value is `-Xms512m -Xmx512m -Xmn256m`.

### Logger Server

**`LOGGER_SERVER_OPTS`**

This environment variable sets jvm options for `logger-server`. The default value is `-Xms512m -Xmx512m -Xmn256m`.

## Initialization scripts

If you would like to do additional initialization in an image derived from this one, add one or more environment variables under `/root/start-init-conf.sh`, and modify template files in `/opt/dolphinscheduler/conf/*.tpl`.

For example, to add an environment variable `SECURITY_AUTHENTICATION_TYPE` in `/root/start-init-conf.sh`:

```
export SECURITY_AUTHENTICATION_TYPE=PASSWORD
```

and to modify `application-api.properties.tpl` template file, add the `SECURITY_AUTHENTICATION_TYPE`:
```
security.authentication.type=${SECURITY_AUTHENTICATION_TYPE}
```

`/root/start-init-conf.sh` will dynamically generate config file:

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

### How to stop dolphinscheduler by docker-compose?

Stop containers:

```
docker-compose stop
```

Stop containers and remove containers, networks and volumes:

```
docker-compose down -v
```

### How to deploy dolphinscheduler on Docker Swarm?

Assuming that the Docker Swarm cluster has been created (If there is no Docker Swarm cluster, please refer to [create-swarm](https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/))

Start a stack named dolphinscheduler

```
docker stack deploy -c docker-stack.yml dolphinscheduler
```

Stop and remove the stack named dolphinscheduler

```
docker stack rm dolphinscheduler
```

### How to use MySQL as the DolphinScheduler's database instead of PostgreSQL?

> Because of the commercial license, we cannot directly use the driver and client of MySQL.
>
> If you want to use MySQL, you can build a new image based on the `apache/dolphinscheduler` image as follows.

1. Download the MySQL driver [mysql-connector-java-5.1.49.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar) (require `>=5.1.47`)

2. Create a new `Dockerfile` to add MySQL driver and client:

```
FROM apache/dolphinscheduler:latest
COPY mysql-connector-java-5.1.49.jar /opt/dolphinscheduler/lib
RUN apt-get update && \
    apt-get install -y --no-install-recommends default-mysql-client && \
    rm -rf /var/lib/apt/lists/*
```

3. Build a new docker image including MySQL driver and client:

```
docker build -t apache/dolphinscheduler:mysql .
```

4. Modify all `image` fields to `apache/dolphinscheduler:mysql` in `docker-compose.yml`

> If you want to deploy dolphinscheduler on Docker Swarm, you need modify `docker-stack.yml`

5. Comment the `dolphinscheduler-postgresql` block in `docker-compose.yml`

6. Add `dolphinscheduler-mysql` service in `docker-compose.yml` (**Optional**, you can directly use a external MySQL database)

7. Modify DATABASE environments in `config.env.sh`

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

> If you have added `dolphinscheduler-mysql` service in `docker-compose.yml`, just set `DATABASE_HOST` to `dolphinscheduler-mysql`

8. Run a dolphinscheduler (See **How to use this docker image**)

### How to support MySQL datasource in `Datasource manage`?

> Because of the commercial license, we cannot directly use the driver of MySQL.
>
> If you want to add MySQL datasource, you can build a new image based on the `apache/dolphinscheduler` image as follows.

1. Download the MySQL driver [mysql-connector-java-5.1.49.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar) (require `>=5.1.47`)

2. Create a new `Dockerfile` to add MySQL driver:

```
FROM apache/dolphinscheduler:latest
COPY mysql-connector-java-5.1.49.jar /opt/dolphinscheduler/lib
```

3. Build a new docker image including MySQL driver:

```
docker build -t apache/dolphinscheduler:mysql-driver .
```

4. Modify all `image` fields to `apache/dolphinscheduler:mysql-driver` in `docker-compose.yml`

> If you want to deploy dolphinscheduler on Docker Swarm, you need modify `docker-stack.yml`

5. Run a dolphinscheduler (See **How to use this docker image**)

6. Add a MySQL datasource in `Datasource manage`

### How to support Oracle datasource in `Datasource manage`?

> Because of the commercial license, we cannot directly use the driver of Oracle.
>
> If you want to add Oracle datasource, you can build a new image based on the `apache/dolphinscheduler` image as follows.

1. Download the Oracle driver [ojdbc8.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/) (such as `ojdbc8-19.9.0.0.jar`)

2. Create a new `Dockerfile` to add Oracle driver:

```
FROM apache/dolphinscheduler:latest
COPY ojdbc8-19.9.0.0.jar /opt/dolphinscheduler/lib
```

3. Build a new docker image including Oracle driver:

```
docker build -t apache/dolphinscheduler:oracle-driver .
```

4. Modify all `image` fields to `apache/dolphinscheduler:oracle-driver` in `docker-compose.yml`

> If you want to deploy dolphinscheduler on Docker Swarm, you need modify `docker-stack.yml`

5. Run a dolphinscheduler (See **How to use this docker image**)

6. Add a Oracle datasource in `Datasource manage`

For more information please refer to the [incubator-dolphinscheduler](https://github.com/apache/incubator-dolphinscheduler.git) documentation.
