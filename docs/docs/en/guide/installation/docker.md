# QuickStart in Docker

## Prerequisites

 - [Docker](https://docs.docker.com/engine/install/) version: 1.13.1+
 - [Docker Compose](https://docs.docker.com/compose/) version: 1.11.0+

## How to Use this Docker Image

Here are 3 ways to quickly install DolphinScheduler:

### Start DolphinScheduler by Docker Compose (Recommended)

In this way, you need to install [docker-compose](https://docs.docker.com/compose/) as a prerequisite, please install it yourself according to the rich docker-compose installation guidance on the Internet.

For Windows 7-10, you can install [Docker Toolbox](https://github.com/docker/toolbox/releases). For Windows 10 64-bit, you can install [Docker Desktop](https://docs.docker.com/docker-for-windows/install/), and meet the [system requirements](https://docs.docker.com/docker-for-windows/install/#system-requirements).

#### Configure Memory not Less Than 4GB

For Mac user, click `Docker Desktop -> Preferences -> Resources -> Memory`.

For Windows Docker Toolbox users, configure the following two settings:

 - **Memory**: Open Oracle VirtualBox Manager, if you double-click `Docker Quickstart Terminal` and successfully run `Docker Toolbox`, you will see a Virtual Machine named `default`. And click `Settings -> System -> Motherboard -> Base Memory`
 - **Port Forwarding**: Click `Settings -> Network -> Advanced -> Port Forwarding -> Add`. fill `Name`, `Host Port` and `Guest Port` forms with `12345`, regardless of `Host IP` and `Guest IP`

For Windows Docker Desktop user
 - **Hyper-V Mode**: Click `Docker Desktop -> Settings -> Resources -> Memory`
 - **WSL 2 Mode**: Refer to [WSL 2 utility VM](https://docs.microsoft.com/en-us/windows/wsl/wsl-config#configure-global-options-with-wslconfig)

#### Download the Source Code Package

Please download the source code package `apache-dolphinscheduler-1.3.8-src.tar.gz`, download address: [download address](/en-us/download/download.html).

#### Pull Image and Start the Service

> For Mac and Linux users, open **Terminal**
> For Windows Docker Toolbox user, open **Docker Quickstart Terminal**
> For Windows Docker Desktop user, open **Windows PowerShell**

```
$ tar -zxvf apache-dolphinscheduler-1.3.8-src.tar.gz
$ cd apache-dolphinscheduler-1.3.8-src/docker/docker-swarm
$ docker pull dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
$ docker tag apache/dolphinscheduler:1.3.8 apache/dolphinscheduler:latest
$ docker-compose up -d
```

> PowerShell should run `cd apache-dolphinscheduler-1.3.8-src\docker\docker-swarm`

The **PostgreSQL** (with username `root`, password `root` and database `dolphinscheduler`) and **ZooKeeper** services will start by default.

#### Login

Visit the Web UI: http://localhost:12345/dolphinscheduler (Modify the IP address if needed).

The default username is `admin` and the default password is `dolphinscheduler123`.

<p align="center">
  <img src="/img/login_en.png" width="60%" />
</p>

Please refer to the [Quick Start](../start/quick-start.md) to explore how to use DolphinScheduler.

### Start via Existing PostgreSQL and ZooKeeper Service

In this way, you need to install [docker](https://docs.docker.com/engine/install/) as a prerequisite, please install it yourself according to the rich docker installation guidance on the Internet.

#### Basic Required Software

 - [PostgreSQL](https://www.postgresql.org/download/) (version 8.2.15+)
 - [ZooKeeper](https://zookeeper.apache.org/releases.html) (version 3.4.6+)
 - [Docker](https://docs.docker.com/engine/install/) (version 1.13.1+)

#### Login to the PostgreSQL Database and Create a Database Named `dolphinscheduler`

#### Initialize the Database, Import `sql/dolphinscheduler_postgre.sql` to Create Tables and Initial Data

#### Download the DolphinScheduler Image

We have already uploaded the user-oriented DolphinScheduler image to the Docker repository so that you can pull the image from the docker repository:

```
docker pull dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
```

#### 5. Run a DolphinScheduler Instance

```
$ docker run -d --name dolphinscheduler \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:1.3.8 all
```

Note: database test username and password need to be replaced with your actual PostgreSQL username and password, 192.168.x.x need to be replaced with your related PostgreSQL and ZooKeeper host IP.

#### Login

Same as above

### Start a Standalone DolphinScheduler Server

The following services automatically start when the container starts:

```
     MasterServer         ----- master service
     WorkerServer         ----- worker service
     ApiApplicationServer ----- api service
     AlertServer          ----- alert service
```

If you just want to run part of the services in the DolphinScheduler, you can start a single service in DolphinScheduler by running the following commands.

* Start a **master server**, For example:

```
$ docker run -d --name dolphinscheduler-master \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:1.3.8 master-server
```

* Start a **worker server**, For example:

```
$ docker run -d --name dolphinscheduler-worker \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:1.3.8 worker-server
```

* Start an **api server**, For example:

```
$ docker run -d --name dolphinscheduler-api \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:1.3.8 api-server
```

* Start an **alert server**, For example:

```
$ docker run -d --name dolphinscheduler-alert \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
apache/dolphinscheduler:1.3.8 alert-server
```

**Note**: You must specify environment variables `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_DATABASE`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `ZOOKEEPER_QUORUM` when start a single DolphinScheduler server.

## Environment Variables

The Docker container is configured through environment variables, and the [Appendix-Environment Variables](#appendix-environment-variables) lists the configurable environment variables of the DolphinScheduler and their default values.

Especially, it can be configured through the environment variable configuration file `config.env.sh` in Docker Compose and Docker Swarm.

## Support Matrix

| Type                                                         | Support      | Notes                                 |
| ------------------------------------------------------------ | ------------ | ------------------------------------- |
| Shell                                                        | Yes          |                                       |
| Python2                                                      | Yes          |                                       |
| Python3                                                      | Indirect Yes | Refer to FAQ                          |
| Hadoop2                                                      | Indirect Yes | Refer to FAQ                          |
| Hadoop3                                                      | Not Sure     | Not tested                            |
| Spark-Local(client)                                          | Indirect Yes | Refer to FAQ                          |
| Spark-YARN(cluster)                                          | Indirect Yes | Refer to FAQ                          |
| Spark-Standalone(cluster)                                    | Not Yet      |                                       |
| Spark-Kubernetes(cluster)                                    | Not Yet      |                                       |
| Flink-Local(local>=1.11)                                     | Not Yet      | Generic CLI mode is not yet supported |
| Flink-YARN(yarn-cluster)                                     | Indirect Yes | Refer to FAQ                          |
| Flink-YARN(yarn-session/yarn-per-job/yarn-application>=1.11) | Not Yet      | Generic CLI mode is not yet supported |
| Flink-Standalone(default)                                    | Not Yet      |                                       |
| Flink-Standalone(remote>=1.11)                               | Not Yet      | Generic CLI mode is not yet supported |
| Flink-Kubernetes(default)                                    | Not Yet      |                                       |
| Flink-Kubernetes(remote>=1.11)                               | Not Yet      | Generic CLI mode is not yet supported |
| Flink-NativeKubernetes(kubernetes-session/application>=1.11) | Not Yet      | Generic CLI mode is not yet supported |
| MapReduce                                                    | Indirect Yes | Refer to FAQ                          |
| Kerberos                                                     | Indirect Yes | Refer to FAQ                          |
| HTTP                                                         | Yes          |                                       |
| DataX                                                        | Indirect Yes | Refer to FAQ                          |
| Sqoop                                                        | Indirect Yes | Refer to FAQ                          |
| SQL-MySQL                                                    | Indirect Yes | Refer to FAQ                          |
| SQL-PostgreSQL                                               | Yes          |                                       |
| SQL-Hive                                                     | Indirect Yes | Refer to FAQ                          |
| SQL-Spark                                                    | Indirect Yes | Refer to FAQ                          |
| SQL-ClickHouse                                               | Indirect Yes | Refer to FAQ                          |
| SQL-Oracle                                                   | Indirect Yes | Refer to FAQ                          |
| SQL-SQLServer                                                | Indirect Yes | Refer to FAQ                          |
| SQL-DB2                                                      | Indirect Yes | Refer to FAQ                          |

## FAQ

### How to Manage DolphinScheduler by Docker Compose?

Start, restart, stop or list containers:

```
docker-compose start
docker-compose restart
docker-compose stop
docker-compose ps
```

Stop containers and remove containers, networks:

```
docker-compose down
```

Stop containers and remove containers, networks and volumes:

```
docker-compose down -v
```

### How to View the Logs of a Container?

List all running containers logs:

```
docker ps
docker ps --format "{{.Names}}" # only print names
```

View the logs of a container named docker-swarm_dolphinscheduler-api_1:

```
docker logs docker-swarm_dolphinscheduler-api_1
docker logs -f docker-swarm_dolphinscheduler-api_1 # follow log output
docker logs --tail 10 docker-swarm_dolphinscheduler-api_1 # show last 10 lines from the end of the logs
```

### How to Scale Master and Worker by Docker Compose?

Scale master to 2 instances:

```
docker-compose up -d --scale dolphinscheduler-master=2 dolphinscheduler-master
```

Scale worker to 3 instances:

```
docker-compose up -d --scale dolphinscheduler-worker=3 dolphinscheduler-worker
```

### How to Deploy DolphinScheduler on Docker Swarm?

Assuming that the Docker Swarm cluster has been created (If there is no Docker Swarm cluster, please refer to [create-swarm](https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/)).

Start a stack named `dolphinscheduler`:

```
docker stack deploy -c docker-stack.yml dolphinscheduler
```

List the services in the stack named `dolphinscheduler`:

```
docker stack services dolphinscheduler
```

Stop and remove the stack named `dolphinscheduler`:

```
docker stack rm dolphinscheduler
```

Remove the volumes of the stack named `dolphinscheduler`:

```
docker volume rm -f $(docker volume ls --format "{{.Name}}" | grep -e "^dolphinscheduler")
```

### How to Scale Master and Worker on Docker Swarm?

Scale master of the stack named `dolphinscheduler` to 2 instances:

```
docker service scale dolphinscheduler_dolphinscheduler-master=2
```

Scale worker of the stack named `dolphinscheduler` to 3 instances:

```
docker service scale dolphinscheduler_dolphinscheduler-worker=3
```

### How to Build a Docker Image?

#### Build From the Source Code (Require Maven 3.3+ and JDK 1.8+)

In Unix-Like, execute in Terminal:

```bash
$ bash ./docker/build/hooks/build
```

In Windows, execute in cmd or PowerShell:

```bat
C:\dolphinscheduler-src>.\docker\build\hooks\build.bat
```

Please read `./docker/build/hooks/build` `./docker/build/hooks/build.bat` script files if you don't understand.

#### Build From the Binary Distribution (Not require Maven 3.3+ and JDK 1.8+)

Please download the binary distribution package `apache-dolphinscheduler-1.3.8-bin.tar.gz`, download address: [download address](/en-us/download/download.html). And put `apache-dolphinscheduler-1.3.8-bin.tar.gz` into the `apache-dolphinscheduler-1.3.8-src/docker/build` directory, execute in Terminal or PowerShell:

```
$ cd apache-dolphinscheduler-1.3.8-src/docker/build
$ docker build --build-arg VERSION=1.3.8 -t apache/dolphinscheduler:1.3.8 .
```

> PowerShell should use `cd apache-dolphinscheduler-1.3.8-src/docker/build`

#### Build Multi-Platform Images

Currently, support build images including `linux/amd64` and `linux/arm64` platform architecture, requirements:

1. Support [docker buildx](https://docs.docker.com/engine/reference/commandline/buildx/)
2. Own the push permission of `https://hub.docker.com/r/apache/dolphinscheduler` (**Be cautious**: The build command will automatically push the multi-platform architecture images to the docker hub of `apache/dolphinscheduler` by default)

Execute:

```bash
$ docker login # login to push apache/dolphinscheduler
$ bash ./docker/build/hooks/build x
```

### How to Add an Environment Variable for Docker?

If you would like to do additional initialization or add environment variables when compiling or execution, you can add one or more environment variables in the script `/root/start-init-conf.sh`. If involves configuration modification, modify the script `/opt/dolphinscheduler/conf/*.tpl`.

For example, add an environment variable `SECURITY_AUTHENTICATION_TYPE` in `/root/start-init-conf.sh`:

```
export SECURITY_AUTHENTICATION_TYPE=PASSWORD
```

Add the `SECURITY_AUTHENTICATION_TYPE` to the template file `application-api.properties.tpl`:

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

### How to Use MySQL as the DolphinScheduler's Database Instead of PostgreSQL?

> Because of the commercial license, we cannot directly use the driver of MySQL.
>
> If you want to use MySQL, you can build a new image based on the `apache/dolphinscheduler` image follow the following instructions:

1. Download the MySQL driver [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar).

2. Create a new `Dockerfile` to add MySQL driver:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/lib
```

3. Build a new docker image including MySQL driver:

```
docker build -t apache/dolphinscheduler:mysql-driver .
```

4. Modify all the `image` fields to `apache/dolphinscheduler:mysql-driver` in `docker-compose.yml`.

> If you want to deploy DolphinScheduler on Docker Swarm, you need to modify `docker-stack.yml`

5. Comment the `dolphinscheduler-postgresql` block in `docker-compose.yml`.

6. Add `dolphinscheduler-mysql` service in `docker-compose.yml` (**Optional**, you can directly use an external MySQL database).

7. Modify DATABASE environment variables in `config.env.sh`:

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

8. Run the DolphinScheduler (See **How to use this docker image**)

### How to Support MySQL Datasource in `Datasource manage`?

> Because of the commercial license, we cannot directly use the driver of MySQL.
>
> If you want to add MySQL datasource, you can build a new image based on the `apache/dolphinscheduler` image follow the following instructions:

1. Download the MySQL driver [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar).

2. Create a new `Dockerfile` to add MySQL driver:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/lib
```

3. Build a new docker image including MySQL driver:

```
docker build -t apache/dolphinscheduler:mysql-driver .
```

4. Modify all `image` fields to `apache/dolphinscheduler:mysql-driver` in `docker-compose.yml`.

> If you want to deploy DolphinScheduler on Docker Swarm, you need to modify `docker-stack.yml`.

5. Run the DolphinScheduler (See **How to use this docker image**).

6. Add a MySQL datasource in `Datasource manage`.

### How to Support Oracle Datasource in `Datasource manage`?

> Because of the commercial license, we cannot directly use the driver of Oracle.
>
> If you want to add Oracle datasource, you can build a new image based on the `apache/dolphinscheduler` image follow the following instructions:

1. Download the Oracle driver [ojdbc8.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/) (such as `ojdbc8-19.9.0.0.jar`).

2. Create a new `Dockerfile` to add Oracle driver:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY ojdbc8-19.9.0.0.jar /opt/dolphinscheduler/lib
```

3. Build a new docker image including Oracle driver:

```
docker build -t apache/dolphinscheduler:oracle-driver .
```

4. Modify all `image` fields to `apache/dolphinscheduler:oracle-driver` in `docker-compose.yml`.

> If you want to deploy DolphinScheduler on Docker Swarm, you need to modify `docker-stack.yml`.

5. Run the DolphinScheduler (See **How to use this docker image**).

6. Add an Oracle datasource in `Datasource manage`.

### How to Support Python 2 pip and Custom requirements.txt?

1. Create a new `Dockerfile` to install pip:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY requirements.txt /tmp
RUN apt-get update && \
    apt-get install -y --no-install-recommends python-pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    rm -rf /var/lib/apt/lists/*
```

The command will install the default **pip 18.1**. If you need to upgrade the pip, just add one more line.

```
    pip install --no-cache-dir -U pip && \
```

2. Build a new docker image including pip:

```
docker build -t apache/dolphinscheduler:pip .
```

3. Modify all `image` fields to `apache/dolphinscheduler:pip` in `docker-compose.yml`.

> If you want to deploy DolphinScheduler on Docker Swarm, you need to modify `docker-stack.yml`.

4. Run the DolphinScheduler (See **How to use this docker image**).

5. Verify pip under a new Python task.

### How to Support Python 3?

1. Create a new `Dockerfile` to install Python 3:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 && \
    rm -rf /var/lib/apt/lists/*
```

The command will install the default **Python 3.7.3**. If you also want to install **pip3**, just replace `python3` with `python3-pip`.

```
    apt-get install -y --no-install-recommends python3-pip && \
```

2. Build a new docker image including Python 3:

```
docker build -t apache/dolphinscheduler:python3 .
```

3. Modify all `image` fields to `apache/dolphinscheduler:python3` in `docker-compose.yml`.

> If you want to deploy DolphinScheduler on Docker Swarm, you need to modify `docker-stack.yml`.

4. Modify `PYTHON_HOME` to `/usr/bin/python3` in `config.env.sh`.

5. Run the DolphinScheduler (See **How to use this docker image**).

6. Verify Python 3 under a new Python task.

### How to Support Hadoop, Spark, Flink, Hive or DataX?

Take Spark 2.4.7 as an example:

1. Download the Spark 2.4.7 release binary `spark-2.4.7-bin-hadoop2.7.tgz`.

2. Run the DolphinScheduler (See **How to use this docker image**).

3. Copy the Spark 2.4.7 release binary into the Docker container.

```bash
docker cp spark-2.4.7-bin-hadoop2.7.tgz docker-swarm_dolphinscheduler-worker_1:/opt/soft
```

Because the volume `dolphinscheduler-shared-local` is mounted on `/opt/soft`, all files in `/opt/soft` will not be lost.

4. Attach the container and ensure that `SPARK_HOME2` exists.

```bash
docker exec -it docker-swarm_dolphinscheduler-worker_1 bash
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # or just mv
$SPARK_HOME2/bin/spark-submit --version
```

The last command will print the Spark version if everything goes well.

5. Verify Spark under a Shell task.

```
$SPARK_HOME2/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME2/examples/jars/spark-examples_2.11-2.4.7.jar
```

Check whether the task log contains the output like `Pi is roughly 3.146015`.

6. Verify Spark under a Spark task.

The file `spark-examples_2.11-2.4.7.jar` needs to be uploaded to the resources first, and then create a Spark task with:

- Spark Version: `SPARK2`
- Main Class: `org.apache.spark.examples.SparkPi`
- Main Package: `spark-examples_2.11-2.4.7.jar`
- Deploy Mode: `local`

Similarly, check whether the task log contains the output like `Pi is roughly 3.146015`.

7. Verify Spark on YARN.

Spark on YARN (Deploy Mode is `cluster` or `client`) requires Hadoop support. Similar to Spark support, the operation of supporting Hadoop is almost the same as the previous steps.

Ensure that `$HADOOP_HOME` and `$HADOOP_CONF_DIR` exists.

### How to Support Spark 3?

In fact, the way to submit applications with `spark-submit` is the same, regardless of Spark 1, 2 or 3. In other words, the semantics of `SPARK_HOME2` is the second `SPARK_HOME` instead of `SPARK2`'s `HOME`, so just set `SPARK_HOME2=/path/to/spark3`.

Take Spark 3.1.1 as an example:

1. Download the Spark 3.1.1 release binary `spark-3.1.1-bin-hadoop2.7.tgz`.

2. Run the DolphinScheduler (See **How to use this docker image**).

3. Copy the Spark 3.1.1 release binary into the Docker container.

```bash
docker cp spark-3.1.1-bin-hadoop2.7.tgz docker-swarm_dolphinscheduler-worker_1:/opt/soft
```

4. Attach the container and ensure that `SPARK_HOME2` exists.

```bash
docker exec -it docker-swarm_dolphinscheduler-worker_1 bash
cd /opt/soft
tar zxf spark-3.1.1-bin-hadoop2.7.tgz
rm -f spark-3.1.1-bin-hadoop2.7.tgz
ln -s spark-3.1.1-bin-hadoop2.7 spark2 # or just mv
$SPARK_HOME2/bin/spark-submit --version
```

The last command will print the Spark version if everything goes well.

5. Verify Spark under a Shell task.

```
$SPARK_HOME2/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME2/examples/jars/spark-examples_2.12-3.1.1.jar
```

Check whether the task log contains the output like `Pi is roughly 3.146015`.

### How to Support Shared Storage between Master, Worker and API server?

> **Note**: If it is deployed on a single machine by `docker-compose`, step 1 and 2 can be skipped directly, and execute the command like `docker cp hadoop-3.2.2.tar.gz docker-swarm_dolphinscheduler-worker_1:/opt/soft` to put Hadoop into the shared directory `/opt/soft` in the container.

For example, Master, Worker and API servers may use Hadoop at the same time.

1. Modify the volume `dolphinscheduler-shared-local` to support NFS in `docker-compose.yml`.

> If you want to deploy DolphinScheduler on Docker Swarm, you need to modify `docker-stack.yml`.

```yaml
volumes:
  dolphinscheduler-shared-local:
    driver_opts:
      type: "nfs"
      o: "addr=10.40.0.199,nolock,soft,rw"
      device: ":/path/to/shared/dir"
```

2. Put the Hadoop into the NFS.

3. Ensure that `$HADOOP_HOME` and `$HADOOP_CONF_DIR` are correct.

### How to Support Local File Resource Storage Instead of HDFS and S3?

> **Note**: If it is deployed on a single machine by `docker-compose`, step 2 can be skipped directly.

1. Modify the following environment variables in `config.env.sh`:

```
RESOURCE_STORAGE_TYPE=HDFS
FS_DEFAULT_FS=file:///
```

2. Modify the volume `dolphinscheduler-resource-local` to support NFS in `docker-compose.yml`.

> If you want to deploy DolphinScheduler on Docker Swarm, you need to modify `docker-stack.yml`.

```yaml
volumes:
  dolphinscheduler-resource-local:
    driver_opts:
      type: "nfs"
      o: "addr=10.40.0.199,nolock,soft,rw"
      device: ":/path/to/resource/dir"
```

### How to Support S3 Resource Storage Like MinIO?

Take MinIO as an example: modify the following environment variables in `config.env.sh`.

```
RESOURCE_STORAGE_TYPE=S3
RESOURCE_UPLOAD_PATH=/dolphinscheduler
FS_DEFAULT_FS=s3a://BUCKET_NAME
FS_S3A_ENDPOINT=http://MINIO_IP:9000
FS_S3A_ACCESS_KEY=MINIO_ACCESS_KEY
FS_S3A_SECRET_KEY=MINIO_SECRET_KEY
```

Modify `BUCKET_NAME`, `MINIO_IP`, `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` to actual values.

> **Note**: `MINIO_IP` can only use IP instead of the domain name, because DolphinScheduler currently doesn't support S3 path style access.

### How to Configure SkyWalking?

Modify SkyWalking environment variables in `config.env.sh`:

```
SKYWALKING_ENABLE=true
SW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800
SW_GRPC_LOG_SERVER_HOST=127.0.0.1
SW_GRPC_LOG_SERVER_PORT=11800
```

## Appendix-Environment Variables

### Database

**`DATABASE_TYPE`**

This environment variable sets the `TYPE` for the `database`. The default value is `postgresql`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_DRIVER`**

This environment variable sets the `DRIVER` for the `database`. The default value is `org.postgresql.Driver`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_HOST`**

This environment variable sets the `HOST` for the `database`. The default value is `127.0.0.1`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_PORT`**

This environment variable sets the `PORT` for the `database`. The default value is `5432`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_USERNAME`**

This environment variable sets the `USERNAME` for the `database`. The default value is `root`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_PASSWORD`**

This environment variable sets the `PASSWORD` for the `database`. The default value is `root`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_DATABASE`**

This environment variable sets the `DATABASE` for the `database`. The default value is `dolphinscheduler`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

**`DATABASE_PARAMS`**

This environment variable sets the `PARAMS` for the `database`. The default value is `characterEncoding=utf8`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`, `alert-server`.

### ZooKeeper

**`ZOOKEEPER_QUORUM`**

This environment variable sets ZooKeeper quorum. The default value is `127.0.0.1:2181`.

**Note**: You must specify it when starting a standalone DolphinScheduler server. Like `master-server`, `worker-server`, `api-server`.

**`ZOOKEEPER_ROOT`**

This environment variable sets the ZooKeeper root directory for DolphinScheduler. The default value is `/dolphinscheduler`.

### Common

**`DOLPHINSCHEDULER_OPTS`**

This environment variable sets JVM options for DolphinScheduler, suitable for `master-server`, `worker-server`, `api-server`, `alert-server`. The default value is empty.

**`DATA_BASEDIR_PATH`**

This environment variable sets user data directory, customized configuration, please make sure the directory exists and have read-write permissions. The default value is `/tmp/dolphinscheduler`

**`RESOURCE_STORAGE_TYPE`**

This environment variable sets resource storage types for DolphinScheduler like `HDFS`, `S3`, `NONE`. The default value is `HDFS`.

**`RESOURCE_UPLOAD_PATH`**

This environment variable sets resource store path on `HDFS/S3` for resource storage. The default value is `/dolphinscheduler`.

**`FS_DEFAULT_FS`**

This environment variable sets `fs.defaultFS` for resource storage like `file:///`, `hdfs://mycluster:8020` or `s3a://dolphinscheduler`. The default value is `file:///`.

**`FS_S3A_ENDPOINT`**

This environment variable sets `s3` endpoint for resource storage. The default value is `s3.xxx.amazonaws.com`.

**`FS_S3A_ACCESS_KEY`**

This environment variable sets `s3` access key for resource storage. The default value is `xxxxxxx`.

**`FS_S3A_SECRET_KEY`**

This environment variable sets `s3` secret key for resource storage. The default value is `xxxxxxx`.

**`HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE`**

This environment variable sets whether to startup Kerberos. The default value is `false`.

**`JAVA_SECURITY_KRB5_CONF_PATH`**

This environment variable sets `java.security.krb5.conf` path. The default value is `/opt/krb5.conf`.

**`LOGIN_USER_KEYTAB_USERNAME`**

This environment variable sets the `keytab` username for the login user. The default value is `hdfs@HADOOP.COM`.

**`LOGIN_USER_KEYTAB_PATH`**

This environment variable sets the `keytab` path for the login user. The default value is `/opt/hdfs.keytab`.

**`KERBEROS_EXPIRE_TIME`**

This environment variable sets Kerberos expiration time, use hour as unit. The default value is `2`.

**`HDFS_ROOT_USER`**

This environment variable sets HDFS root user when `resource.storage.type=HDFS`. The default value is `hdfs`.

**`RESOURCE_MANAGER_HTTPADDRESS_PORT`**

This environment variable sets resource manager HTTP address port. The default value is `8088`.

**`YARN_RESOURCEMANAGER_HA_RM_IDS`**

This environment variable sets yarn `resourcemanager` ha rm ids. The default value is empty.

**`YARN_APPLICATION_STATUS_ADDRESS`**

This environment variable sets yarn application status address. The default value is `http://ds1:%s/ws/v1/cluster/apps/%s`.

**`SKYWALKING_ENABLE`**

This environment variable sets whether to enable SkyWalking. The default value is `false`.

**`SW_AGENT_COLLECTOR_BACKEND_SERVICES`**

This environment variable sets agent collector backend services for SkyWalking. The default value is `127.0.0.1:11800`.

**`SW_GRPC_LOG_SERVER_HOST`**

This environment variable sets `gRPC` log server host for SkyWalking. The default value is `127.0.0.1`.

**`SW_GRPC_LOG_SERVER_PORT`**

This environment variable sets `gRPC` log server port for SkyWalking. The default value is `11800`.

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

This environment variable sets JVM options for `master-server`. The default value is `-Xms1g -Xmx1g -Xmn512m`.

**`MASTER_EXEC_THREADS`**

This environment variable sets execute thread number for `master-server`. The default value is `100`.

**`MASTER_EXEC_TASK_NUM`**

This environment variable sets execute task number for `master-server`. The default value is `20`.

**`MASTER_DISPATCH_TASK_NUM`**

This environment variable sets dispatch task number for `master-server`. The default value is `3`.

**`MASTER_HOST_SELECTOR`**

This environment variable sets host selector for `master-server`. Optional values include `Random`, `RoundRobin` and `LowerWeight`. The default value is `LowerWeight`.

**`MASTER_HEARTBEAT_INTERVAL`**

This environment variable sets heartbeat intervals for `master-server`. The default value is `10`.

**`MASTER_TASK_COMMIT_RETRYTIMES`**

This environment variable sets task commit retry times for `master-server`. The default value is `5`.

**`MASTER_TASK_COMMIT_INTERVAL`**

This environment variable sets task commit interval for `master-server`. The default value is `1`.

**`MASTER_MAX_CPULOAD_AVG`**

This environment variable sets max CPU load avg for `master-server`. The default value is `-1`.

**`MASTER_RESERVED_MEMORY`**

This environment variable sets reserved memory for `master-server`, the unit is G. The default value is `0.3`.

### Worker Server

**`WORKER_SERVER_OPTS`**

This environment variable sets JVM options for `worker-server`. The default value is `-Xms1g -Xmx1g -Xmn512m`.

**`WORKER_EXEC_THREADS`**

This environment variable sets execute thread number for `worker-server`. The default value is `100`.

**`WORKER_HEARTBEAT_INTERVAL`**

This environment variable sets heartbeat interval for `worker-server`. The default value is `10`.

**`WORKER_MAX_CPULOAD_AVG`**

This environment variable sets max CPU load avg for `worker-server`. The default value is `-1`.

**`WORKER_RESERVED_MEMORY`**

This environment variable sets reserved memory for `worker-server`, the unit is G. The default value is `0.3`.

**`WORKER_GROUPS`**

This environment variable sets groups for `worker-server`. The default value is `default`.

### Alert Server

**`ALERT_SERVER_OPTS`**

This environment variable sets JVM options for `alert-server`. The default value is `-Xms512m -Xmx512m -Xmn256m`.

**`XLS_FILE_PATH`**

This environment variable sets `xls` file path for `alert-server`. The default value is `/tmp/xls`.

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

This environment variable sets SMTP `tls` for `alert-server`. The default value is `true`.

**`MAIL_SMTP_SSL_ENABLE`**

This environment variable sets SMTP `ssl` for `alert-server`. The default value is `false`.

**`MAIL_SMTP_SSL_TRUST`**

This environment variable sets SMTP `ssl` trust for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_ENABLE`**

This environment variable sets enterprise WeChat enables for `alert-server`. The default value is `false`.

**`ENTERPRISE_WECHAT_CORP_ID`**

This environment variable sets enterprise WeChat corp id for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_SECRET`**

This environment variable sets enterprise WeChat secret for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_AGENT_ID`**

This environment variable sets enterprise WeChat agent id for `alert-server`. The default value is empty.

**`ENTERPRISE_WECHAT_USERS`**

This environment variable sets enterprise WeChat users for `alert-server`. The default value is empty.

### API Server

**`API_SERVER_OPTS`**

This environment variable sets JVM options for `api-server`. The default value is `-Xms512m -Xmx512m -Xmn256m`.
