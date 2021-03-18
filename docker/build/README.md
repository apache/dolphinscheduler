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

#### Or via Environment Variables **`DATABASE_HOST`** **`DATABASE_PORT`** **`DATABASE_DATABASE`** **`ZOOKEEPER_QUORUM`**

You can specify **existing postgres and zookeeper service**. Example:

```
$ docker run -d --name dolphinscheduler \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
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
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
apache/dolphinscheduler:latest master-server
```

* Start a **worker server** (including **logger server**), For example:

```
$ docker run -d --name dolphinscheduler-worker \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ALERT_LISTEN_HOST="dolphinscheduler-alert" \
-v dolphinscheduler-resource-local:/dolphinscheduler \
apache/dolphinscheduler:latest worker-server
```

* Start a **api server**, For example:

```
$ docker run -d --name dolphinscheduler-api \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
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

**Note**: You must be specify `DATABASE_HOST` `DATABASE_PORT` `DATABASE_DATABASE` `DATABASE_USERNAME` `DATABASE_PASSWORD` `ZOOKEEPER_QUORUM` when start a standalone dolphinscheduler server.

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

**`HADOOP_HOME`**

This environment variable sets `HADOOP_HOME`. The default value is `/opt/soft/hadoop`.

**`HADOOP_CONF_DIR`**

This environment variable sets `HADOOP_CONF_DIR`. The default value is `/opt/soft/hadoop/etc/hadoop`.

**`SPARK_HOME1`**

This environment variable sets `SPARK_HOME1`. The default value is `/opt/soft/spark1`.

**`SPARK_HOME2`**

This environment variable sets `SPARK_HOME2`. The default value is `/opt/soft/spark2`.

**`PYTHON_HOME`**

This environment variable sets `PYTHON_HOME`. The default value is `/usr`.

**`JAVA_HOME`**

This environment variable sets `JAVA_HOME`. The default value is `/usr/lib/jvm/java-1.8-openjdk`.

**`HIVE_HOME`**

This environment variable sets `HIVE_HOME`. The default value is `/opt/soft/hive`.

**`FLINK_HOME`**

This environment variable sets `FLINK_HOME`. The default value is `/opt/soft/flink`.

**`DATAX_HOME`**

This environment variable sets `DATAX_HOME`. The default value is `/opt/soft/datax`.

**`DOLPHINSCHEDULER_DATA_BASEDIR_PATH`**

User data directory path, self configuration, please make sure the directory exists and have read write permissions. The default value is `/tmp/dolphinscheduler`

**`DOLPHINSCHEDULER_OPTS`**

This environment variable sets java options. The default value is empty.

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

**`ZOOKEEPER_QUORUM`**

This environment variable sets zookeeper quorum for `master-server` and `worker-serverr`. The default value is `127.0.0.1:2181`.

**Note**: You must be specify it when start a standalone dolphinscheduler server. Like `master-server`, `worker-server`.

**`ZOOKEEPER_ROOT`**

This environment variable sets zookeeper root directory for dolphinscheduler. The default value is `/dolphinscheduler`.

**`MASTER_EXEC_THREADS`**

This environment variable sets exec thread num for `master-server`. The default value is `100`.

**`MASTER_EXEC_TASK_NUM`**

This environment variable sets exec task num for `master-server`. The default value is `20`.

**`MASTER_HEARTBEAT_INTERVAL`**

This environment variable sets heartbeat interval for `master-server`. The default value is `10`.

**`MASTER_TASK_COMMIT_RETRYTIMES`**

This environment variable sets task commit retry times for `master-server`. The default value is `5`.

**`MASTER_TASK_COMMIT_INTERVAL`**

This environment variable sets task commit interval for `master-server`. The default value is `1000`.

**`MASTER_MAX_CPULOAD_AVG`**

This environment variable sets max cpu load avg for `master-server`. The default value is `100`.

**`MASTER_RESERVED_MEMORY`**

This environment variable sets reserved memory for `master-server`. The default value is `0.1`.

**`MASTER_LISTEN_PORT`**

This environment variable sets port for `master-server`. The default value is `5678`.

**`WORKER_EXEC_THREADS`**

This environment variable sets exec thread num for `worker-server`. The default value is `100`.

**`WORKER_HEARTBEAT_INTERVAL`**

This environment variable sets heartbeat interval for `worker-server`. The default value is `10`.

**`WORKER_MAX_CPULOAD_AVG`**

This environment variable sets max cpu load avg for `worker-server`. The default value is `100`.

**`WORKER_RESERVED_MEMORY`**

This environment variable sets reserved memory for `worker-server`. The default value is `0.1`.

**`WORKER_LISTEN_PORT`**

This environment variable sets port for `worker-server`. The default value is `1234`.

**`WORKER_GROUPS`**

This environment variable sets groups for `worker-server`. The default value is `default`.

**`WORKER_HOST_WEIGHT`**

This environment variable sets weight for `worker-server`. The default value is `100`.

**`ALERT_LISTEN_HOST`**

This environment variable sets the host of `alert-server` for `worker-server`. The default value is `127.0.0.1`.

**`ALERT_PLUGIN_DIR`**

This environment variable sets the alert plugin directory for `alert-server`. The default value is `lib/plugin/alert`.

## Initialization scripts

If you would like to do additional initialization in an image derived from this one, add one or more environment variable under `/root/start-init-conf.sh`, and modify template files in `/opt/dolphinscheduler/conf/*.tpl`.

For example, to add an environment variable `API_SERVER_PORT` in `/root/start-init-conf.sh`:

```
export API_SERVER_PORT=5555
```

and to modify `/opt/dolphinscheduler/conf/application-api.properties.tpl` template file, add server port:
```
server.port=${API_SERVER_PORT}
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
RUN apk add --update --no-cache mysql-client
```

3. Build a new docker image including MySQL driver and client:

```
docker build -t apache/dolphinscheduler:mysql .
```

4. Modify all `image` fields to `apache/dolphinscheduler:mysql` in `docker-compose.yml`

> If you want to deploy dolphinscheduler on Docker Swarm, you need modify `docker-stack.yml`

5. Comment the `dolphinscheduler-postgresql` block in `docker-compose.yml`

6. Add `dolphinscheduler-mysql` service in `docker-compose.yml` (**Optional**, you can directly use a external MySQL database)

7. Modify all DATABASE environments in `docker-compose.yml`

```
DATABASE_TYPE: mysql
DATABASE_DRIVER: com.mysql.jdbc.Driver
DATABASE_HOST: dolphinscheduler-mysql
DATABASE_PORT: 3306
DATABASE_USERNAME: root
DATABASE_PASSWORD: root
DATABASE_DATABASE: dolphinscheduler
DATABASE_PARAMS: useUnicode=true&characterEncoding=UTF-8
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
