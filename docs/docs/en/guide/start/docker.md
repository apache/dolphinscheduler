# Quick Trial Docker Deployment

## Pre-conditions

- [Docker](https://docs.docker.com/engine/install/) 1.13.1+
- [Docker Compose](https://docs.docker.com/compose/) 1.11.0+

## How to use docker image?

There are 3 ways to quickly try DolphinScheduler.

### I. Start DolphinScheduler as docker-compose (recommended)

This method requires the installation of [docker-compose](https://docs.docker.com/compose/). The installation of docker-compose is widely available online, so please install it yourself.

For Windows 7-10 versions, you can install [Docker Toolbox](https://github.com/docker/toolbox/releases). For Windows 10 64-bit, you can install [Docker Desktop](https://docs.docker.com/docker-for-windows/install/) and note the [system requirements](https://docs.docker.com/ docker-for-windows/install/#system-requirements).

#### 0. Please allocate at least 4GB of memory

For Mac users, click on `Docker Desktop -> Preferences -> Resources -> Memory`.

For Windows Docker Toolbox users, there are two items that need to be configured.

- **Memory**: Open Oracle VirtualBox Manager and if you double click on Docker Quickstart Terminal and run Docker Toolbox successfully, you will see a virtual machine named `default`. Click on `Settings -> System -> Motherboard -> Memory Size`.
- **Port Forwarding**: Click `Settings -> Network -> Advanced -> Port Forwarding -> Add`. `Name`, fill in `12345` for both `Host Port` and `Subsystem Port`, leave out the `Host IP` and `Subsystem IP`.

For Windows Docker Desktop users.
- **Hyper-V Mode**: Click `Docker Desktop -> Settings -> Resources -> Memory`
- **WSL 2 Mode**: Reference [WSL 2 utility VM](https://docs.microsoft.com/zh-cn/windows/wsl/wsl-config#configure-global-options-with-wslconfig)

#### 1.Download the source code package

Please download the source package apache-dolphinscheduler-x.x.x-src.tar.gz from: [download](/en-us/download/download.html)

#### 2.Pull the image and start the service

> For Mac and Linux user, open **Terminal**
> For Windows Docker Toolbox user, open **Docker Quickstart Terminal**
> For Windows Docker Desktop user, open **Windows PowerShell**

```
$ tar -zxvf apache-dolphinscheduler-1.3.8-src.tar.gz
$ cd apache-dolphinscheduler-1.3.8-src/docker/docker-swarm
$ docker pull dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
$ docker tag apache/dolphinscheduler:1.3.8 apache/dolphinscheduler:latest
$ docker-compose up -d
```
> PowerShell should use `cd apache-dolphinscheduler-1.3.8-src\docker\docker-swarm`

**PostgreSQL** (user `root`, password `root`, database `dolphinscheduler`) and **ZooKeeper** services will be started by default

#### 3.Login system

To access the front-end page: http://localhost:12345/dolphinscheduler, please change to the corresponding IP address if necessary

The default user is `admin` and the default password is `dolphinscheduler123`.

![login](/img/new_ui/dev/quick-start/login.png)

Please refer to the user manual chapter [Quick Start] (... /start/quick-start.md) to see how to use DolphinScheduler.

### II. By specifying the existing PostgreSQL and ZooKeeper services

This method requires the installation of [docker](https://docs.docker.com/engine/install/). The installation of docker is well documented on the web, so please install it yourself.

#### 1.Basic software installation (please install it yourself)

- [PostgreSQL](https://www.postgresql.org/download/) (8.2.15+)
- [ZooKeeper](https://zookeeper.apache.org/releases.html) (3.4.6+)
- [Docker](https://docs.docker.com/engine/install/) (1.13.1+)

#### 2. Please login to the PostgreSQL database and create a database named `dolphinscheduler`.

#### 3. Initialize the database and import `sql/dolphinscheduler_postgre.sql` to create tables and import the base data

#### 4. Download the DolphinScheduler image

We have uploaded the DolphinScheduler images for users to the docker repository. Instead of building the image locally, users can pull the image from the docker repository by running the following command.

```
docker pull dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
```

#### 5. Run a DolphinScheduler instance

```
$ docker run -d --name dolphinscheduler \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:1.3.8 all
```

Note: The database user test and password test need to be replaced with the actual PostgreSQL user and password. 192.168.x.x needs to be replaced with the host IP of PostgreSQL and ZooKeeper.

#### 6. Login system

As above.

### III. Running a standalone service in DolphinScheduler

When the container is started, the following services are automatically started.

```
    MasterServer         ----- master service
    WorkerServer         ----- worker service
    ApiApplicationServer ----- api service
    AlertServer          ----- alert service
```

If you just want to run some of the services in dolphinscheduler. You can run some of the services in dolphinscheduler by executing the following command.

* Start a **master server**, as follows:

```
$ docker run -d --name dolphinscheduler-master \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:1.3.8 master-server
```

* Start a **worker server**, as follows:

```
$ docker run -d --name dolphinscheduler-worker \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
apache/dolphinscheduler:1.3.8 worker-server
```

* Start a **api server**, as follows:

```
$ docker run -d --name dolphinscheduler-api \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-p 12345:12345 \
apache/dolphinscheduler:1.3.8 api-server
```

* Start a **alter server**, as follows:

```
$ docker run -d --name dolphinscheduler-alert \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
apache/dolphinscheduler:1.3.8 alert-server
```

**NOTE**: When you run some of the services in dolphinscheduler, you must specify these environment variables `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_DATABASE`, `DATABASE_USERNAME`, `DATABASE_ PASSWORD`, `ZOOKEEPER_QUORUM`.

## Environment variables

Docker containers are configured via environment variables, appendix-environment-variables lists the configurable environment variables for DolphinScheduler and their default values <! -- markdown-link-check-disable-line -->

In particular, in Docker Compose and Docker Swarm, this can be configured via the environment variable configuration file `config.env.sh`.

## Support Matrix

| Type | Support | Notes |
| ------------------------------------------------------------ | ------- | --------------------- |
| Shell | Yes | Yes
| Python2 | Yes | Yes
| Python3 | Indirect support | See FAQ |
| Hadoop2 | Indirect support | See FAQ |
| Hadoop3 | Not yet determined | Not yet tested |
| Spark-Local(client) | Indirect support | See FAQ |
| Spark-YARN(cluster) | Indirect support | See FAQ |
| Spark-Standalone(cluster) | not yet | |
| Spark-Kubernetes(cluster) | not yet |
| Flink-Local(local>=1.11) | Not yet | Generic CLI mode is not yet supported
| Flink-YARN(yarn-cluster) | indirectly supported | see FAQ |
| Flink-YARN(yarn-session/yarn-per-job/yarn-application>=1.11) | Not yet | Generic CLI mode is not yet supported |
| Flink-Standalone(default) | not yet |
| Flink-Standalone(remote>=1.11) | Not yet | Generic CLI mode is not yet supported |
| Flink-Kubernetes(default) | not yet | | Flink-Kubernetes(default) | not yet |
| Flink-Kubernetes(remote>=1.11) | Not yet | Generic CLI mode is not yet supported |
| Flink-NativeKubernetes(kubernetes-session/application>=1.11) | Not yet | Generic CLI mode is not yet supported |
| MapReduce | Indirectly supported | See FAQ |
| Kerberos | Indirectly supported | See FAQ |
| HTTP | Yes | Yes
| DataX | Indirect support | See FAQ | Yes
| Sqoop | Indirect Support | See FAQ |
| SQL-MySQL | Indirect Support | See FAQ |
| SQL-PostgreSQL | Yes | Yes
| SQL-Hive | Indirect Support | See FAQ |
| SQL-Spark | Indirect support | See FAQ |
| SQL-ClickHouse | Indirect Support | See FAQ |
| SQL-Oracle | Indirect Support | See FAQ |
| SQL-SQLServer | Indirect Support | See FAQ |
| SQL-DB2 | Indirect Support | See FAQ |

## FAQ

### How to manage DolphinScheduler via docker-compose?

Start, restart, stop or list all containers:

```
docker-compose start
docker-compose restart
docker-compose stop
docker-compose ps
```

Stop all containers and remove all containers, networks:

```
docker-compose down
```

Stop all containers and remove all containers, networks and storage volumes:

```
docker-compose down -v
```

### How do I check the logs of a container?

Lists all running containers:

```
docker ps
docker ps --format "{{.Names}}" # 只打印名字
```

View the logs of the container named docker-swarm_dolphinscheduler-api_1:

```
docker logs docker-swarm_dolphinscheduler-api_1
docker logs -f docker-swarm_dolphinscheduler-api_1 # 跟随日志输出
docker logs --tail 10 docker-swarm_dolphinscheduler-api_1 # 显示倒数10行日志
```

### How to scale master and worker with docker-compose?

Scaling master to 2 instances:

```
docker-compose up -d --scale dolphinscheduler-master=2 dolphinscheduler-master
```

Scaling worker to 3 instances:

```
docker-compose up -d --scale dolphinscheduler-worker=3 dolphinscheduler-worker
```

### How to deploy DolphinScheduler on Docker Swarm?

Assuming the Docker Swarm cluster has been deployed (see [create-swarm](https://docs.docker.com/engine/swarm/swarm-tutorial/create-swarm/) if a Docker Swarm cluster has not been created yet).

Start a stack called dolphinscheduler:

```
docker stack deploy -c docker-stack.yml dolphinscheduler
```

List all services of the stack named dolphinscheduler:

```
docker stack services dolphinscheduler
```

Stop and remove the stack named dolphinscheduler:

```
docker stack rm dolphinscheduler
```

Remove all storage volumes from the stack named dolphinscheduler:

```
docker volume rm -f $(docker volume ls --format "{{.Name}}" | grep -e "^dolphinscheduler")
```

### How to scale up and down master and worker on Docker Swarm?

Scaling up the master of a stack named dolphinscheduler to 2 instances:

```
docker service scale dolphinscheduler_dolphinscheduler-master=2
```

Scaling up the workers of the stack named dolphinscheduler to 3 instances:

```
docker service scale dolphinscheduler_dolphinscheduler-worker=3
```

### How to build a Docker image?

#### Build from source (requires Maven 3.3+ & JDK 1.8+)

In a Unix-like system, execute in Terminal:

```bash
$ bash ./docker/build/hooks/build
```

In a Windows system, execute in cmd or PowerShell:

```bat
C:\dolphinscheduler-src>.\docker\build\hooks\build.bat
```

If you don't understand `. /docker/build/hooks/build` `. /docker/build/hooks/build.bat` these scripts, please read the contents.

#### Build from binary packages (Maven 3.3+ & JDK 1.8+ not required)

Please download the binary package apache-dolphinscheduler-1.3.8-bin.tar.gz from: [download](/zh-cn/download/download.html). Then put apache-dolphinscheduler-1.3.8-bin.tar.gz into the `apache-dolphinscheduler-1.3.8-src/docker/build` directory and execute it in Terminal or PowerShell:

```
$ cd apache-dolphinscheduler-1.3.8-src/docker/build
$ docker build --build-arg VERSION=1.3.8 -t apache/dolphinscheduler:1.3.8 .
```

> PowerShell should use `cd apache-dolphinscheduler-1.3.8-src/docker/build`

#### Building images for multi-platform architectures

Currently supports building images for `linux/amd64` and `linux/arm64` platform architectures, requiring

1. support for [docker buildx](https://docs.docker.com/engine/reference/commandline/buildx/)
2. have push permissions for https://hub.docker.com/r/apache/dolphinscheduler (**Be careful**: the build command automatically pushes multi-platform architecture images to the apache/dolphinscheduler docker hub by default)

Execute :

```bash
$ docker login # login, use to push apache/dolphinscheduler
$ bash ./docker/build/hooks/build x
```

### How to add an environment variable to Docker?

If you want to add additional actions and environment variables at compile time or run time, you can do so in the `/root/start-init-conf.sh` file, and if you need to change the configuration file, please change the corresponding configuration file in `/opt/dolphinscheduler/conf/*.tpl`. configuration file

For example, add an environment variable `SECURITY_AUTHENTICATION_TYPE` to `/root/start-init-conf.sh`.

```
export SECURITY_AUTHENTICATION_TYPE=PASSWORD
```

After adding the above environment variables, you should add this environment variable configuration to the corresponding template file `application-api.properties.tpl`:

```
security.authentication.type=${SECURITY_AUTHENTICATION_TYPE}
```

`/root/start-init-conf.sh` will dynamically generate a configuration file based on the template file.

```sh
echo "generate dolphinscheduler config"
ls ${DOLPHINSCHEDULER_HOME}/conf/ | grep ".tpl" | while read line; do
eval "cat << EOF
$(cat ${DOLPHINSCHEDULER_HOME}/conf/${line})
EOF
" > ${DOLPHINSCHEDULER_HOME}/conf/${line%.*}
done
```

### How to replace PostgreSQL with MySQL as the database for DolphinScheduler?

> Due to commercial licensing, we cannot use MySQL driver packages directly.
>
> If you want to use MySQL, you can build it based on the official image `apache/dolphinscheduler`.

1. Download the MySQL driver package [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)

2. Create a new `Dockerfile` to add the MySQL driver package:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/lib
```

3. Build a new image containing the MySQL driver package:

```
docker build -t apache/dolphinscheduler:mysql-driver
```

4. Modify all the image fields in the `docker-compose.yml` file to `apache/dolphinscheduler:mysql-driver`.

> If you want to deploy dolphinscheduler on Docker Swarm, you need to modify `docker-stack.yml`.

5. Comment out the `dolphinscheduler-postgresql` block in the `docker-compose.yml` file.

6. Add the `dolphinscheduler-mysql` service to the `docker-compose.yml` file (**optional**, you can use an external MySQL database directly).

7. Modify the DATABASE environment variable in the `config.env.sh` file.

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

> If you have already added the `dolphinscheduler-mysql` service, set `DATABASE_HOST` to `dolphinscheduler-mysql`.

8. Run dolphinscheduler (see **How to use a docker image** for details).

### How do I support MySQL data sources in the Data Source Centre?

> Due to commercial licensing, we cannot use MySQL's driver packages directly.
>
> If you want to add a MySQL datasource, you can build it based on the official image `apache/dolphinscheduler`.

1. Download MySQL driver package [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)

2. Create a new `Dockerfile` to add the MySQL driver package:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/lib
```

3. Build a new image containing the MySQL driver package:

```
docker build -t apache/dolphinscheduler:mysql-driver .
```

4. Change all `image` fields in the `docker-compose.yml` file to `apache/dolphinscheduler:mysql-driver`.

> If you want to deploy dolphinscheduler on Docker Swarm, you will need to modify `docker-stack.yml`.

5. Run dolphinscheduler (see **How to use a docker image** for details).

6. Add a MySQL data source to the data source centre.

### How to support Oracle data sources in the Data Source Centre?

> Due to commercial licensing, we cannot use Oracle's driver packages directly.
>
> If you want to add an Oracle datasource, you can build it based on the official image `apache/dolphinscheduler`.

1. Download the Oracle driver package [ojdbc8.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/) (such as `ojdbc8-19.9.0.0.jar`).

2. Create a new `Dockerfile` to add the Oracle driver package:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY ojdbc8-19.9.0.0.jar /opt/dolphinscheduler/lib
```

3. Build a new image containing the Oracle driver package:

```
docker build -t apache/dolphinscheduler:oracle-driver .
```

4. Change all `image` fields in the `docker-compose.yml` file to `apache/dolphinscheduler:oracle-driver`.

> If you want to deploy dolphinscheduler on Docker Swarm, you will need to modify `docker-stack.yml`.

5. Run dolphinscheduler (see **How to use a docker image** for details).

6. Add an Oracle data source to the data source centre.

### How to support Python 2 pip and custom requirements.txt?

1. Create a new `Dockerfile` for installing pip:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
COPY requirements.txt /tmp
RUN apt-get update && \
    apt-get install -y --no-install-recommends python-pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    rm -rf /var/lib/apt/lists/*
```

This command will install the default **pip 18.1**. If you want to upgrade pip, just add a line.


```
    pip install --no-cache-dir -U pip && \
```

2. Build a new image containing pip.

```
docker build -t apache/dolphinscheduler:pip .
```

3. Change all `image` fields in the `docker-compose.yml` file to `apache/dolphinscheduler:pip`.

> If you want to deploy dolphinscheduler on Docker Swarm, you will need to modify `docker-stack.yml`.

4. Run dolphinscheduler (see **How to use docker images** for details).

5. Verify pip under a new Python task.

### How do I support Python 3?

1. Create a new `Dockerfile` for installing Python 3:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler:1.3.8
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 && \
    rm -rf /var/lib/apt/lists/*
```

This command will install the default **Python 3.7.3**. If you also want to install **pip3**, replace `python3` with `python3-pip` and you're done.

```
    apt-get install -y --no-install-recommends python3-pip && \
```

2. Build a new image containing Python 3:

```
docker build -t apache/dolphinscheduler:python3 .
```

3. Change all `image` fields in the `docker-compose.yml` file to `apache/dolphinscheduler:python3`.

> If you want to deploy dolphinscheduler on Docker Swarm, you will need to modify `docker-stack.yml`.

4. Modify `PYTHON_HOME` to `/usr/bin/python3` in the `config.env.sh` file.

5. Run dolphinscheduler (see **How to use docker images** for details).

6. Verify Python 3 under a new Python task.

### How to support Hadoop, Spark, Flink, Hive or DataX?

Take Spark 2.4.7 as an example:

1. Download the Spark 2.4.7 release binary package `spark-2.4.7-bin-hadoop2.7.tgz`.

2. Run dolphinscheduler (see **How to use a docker image** for details).

3. Copy the Spark 2.4.7 binary package to the Docker container.

```bash
docker cp spark-2.4.7-bin-hadoop2.7.tgz docker-swarm_dolphinscheduler-worker_1:/opt/soft
```

Because the storage volume `dolphinscheduler-shared-local` is mounted to `/opt/soft`, all files in `/opt/soft` will not be lost.

4. Login to the container and make sure `SPARK_HOME2` exists.

```bash
docker exec -it docker-swarm_dolphinscheduler-worker_1 bash
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # or mv
$SPARK_HOME2/bin/spark-submit --version
```

If everything executes correctly, the last command will print the Spark version information.

5. Verify Spark in a shell task.

```
$SPARK_HOME2/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME2/examples/jars/spark-examples_2.11-2.4.7.jar
```

Check if the task log contains the output `Pi is roughly 3.146015`.

6. Verifying Spark in a Spark Task

The file `spark-examples_2.11-2.4.7.jar` needs to be uploaded to the Resource Center first, then a Spark task created and set up:

- Spark version: `SPARK2`
- Class of main function: `org.apache.spark.examples.SparkPi`
- Main package: `spark-examples_2.11-2.4.7.jar`
- Deployment method: `local`

Similarly, check if the task log contains output `Pi is roughly 3.146015`

7. Verifying Spark on YARN

Spark on YARN (deployed as a `cluster` or `client`) requires Hadoop support. Similar to Spark support, supporting Hadoop is almost identical to the previous steps.

Make sure `$HADOOP_HOME` and `$HADOOP_CONF_DIR` are present.

### How is Spark 3 supported?

In fact, submitting an application using `spark-submit` is the same, whether it is Spark 1, 2 or 3. In other words, the semantics of `SPARK_HOME2` is a second `SPARK_HOME`, not the `HOME` of `SPARK2`, so simply setting `SPARK_HOME2=/path/to/ spark3`.

Let's take Spark 3.1.1 as an example:

1. Download the Spark 3.1.1 release binary package `spark-3.1.1-bin-hadoop2.7.tgz`.

2. Run dolphinscheduler (see **How to use a docker image** for details).

3. Copy the Spark 3.1.1 binary package to the Docker container

```bash
docker cp spark-3.1.1-bin-hadoop2.7.tgz docker-swarm_dolphinscheduler-worker_1:/opt/soft
```

4. log in to the container and ensure that `SPARK_HOME2` exists

```bash
docker exec -it docker-swarm_dolphinscheduler-worker_1 bash
cd /opt/soft
tar zxf spark-3.1.1-bin-hadoop2.7.tgz
rm -f spark-3.1.1-bin-hadoop2.7.tgz
ln -s spark-3.1.1-bin-hadoop2.7 spark2 # or mv
$SPARK_HOME2/bin/spark-submit --version
```

If everything executes correctly, the last command will print the Spark version information.

5. Verify Spark in a shell task.

```
$SPARK_HOME2/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME2/examples/jars/spark-examples_2.12-3.1.1.jar
```

Check if the task log contains the output `Pi is roughly 3.146015`.

### How to support shared storage between Master, Worker and Api services?

> **Note**: If you are deploying via docker-compose on a single machine, steps 1 and 2 can be skipped and you can execute commands like `docker cp hadoop-3.2.2.tar.gz docker-swarm_dolphinscheduler-worker_1:/opt/soft ' Place Hadoop in the container under the shared directory /opt/soft.

For example, the Master, Worker and Api services may use Hadoop at the same time.

1. Modify the `dolphinscheduler-shared-local` storage volume in the `docker-compose.yml` file to support nfs.

> If you want to deploy dolphinscheduler on Docker Swarm, you need to modify `docker-stack.yml`.

```yaml
volumes:
  dolphinscheduler-shared-local:
    driver_opts:
      type: "nfs"
      o: "addr=10.40.0.199,nolock,soft,rw"
      device: ":/path/to/shared/dir"
```


2. Put Hadoop into nfs.

3. Make sure `$HADOOP_HOME` and `$HADOOP_CONF_DIR` are correct.

### How to support local file storage instead of HDFS and S3?

> **Note**: If you are deploying on a standalone machine via docker-compose, you can skip step 2.

1. modify the following environment variables in the `config.env.sh` file:

```
RESOURCE_STORAGE_TYPE=HDFS
FS_DEFAULT_FS=file:///
```

2. Modify the `dolphinscheduler-resource-local` storage volume in the `docker-compose.yml` file to support nfs.

> If you want to deploy dolphinscheduler on Docker Swarm, you need to modify `docker-stack.yml`.

```yaml
volumes:
  dolphinscheduler-resource-local:
    driver_opts:
      type: "nfs"
      o: "addr=10.40.0.199,nolock,soft,rw"
      device: ":/path/to/resource/dir"
```

### How do I support S3 resource stores such as MinIO?

Take MinIO as an example: Modify the following environment variables in the `config.env.sh` file.

```
RESOURCE_STORAGE_TYPE=S3
RESOURCE_UPLOAD_PATH=/dolphinscheduler
FS_DEFAULT_FS=s3a://BUCKET_NAME
FS_S3A_ENDPOINT=http://MINIO_IP:9000
FS_S3A_ACCESS_KEY=MINIO_ACCESS_KEY
FS_S3A_SECRET_KEY=MINIO_SECRET_KEY
```

`BUCKET_NAME`, `MINIO_IP`, `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` need to be changed to actual values.

> **NOTE**: `MINIO_IP` can only use IPs and not domain names, as DolphinScheduler does not yet support S3 path style access.

### How to configure SkyWalking?

Modify the SKYWALKING environment variables in the `config.env.sh` file.

```
SKYWALKING_ENABLE=true
SW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800
SW_GRPC_LOG_SERVER_HOST=127.0.0.1
SW_GRPC_LOG_SERVER_PORT=11800
```

## Appendix - Environment Variables

### Database

**`DATABASE_TYPE`**

Configure the `TYPE` of the `database`, default value `postgresql`.

**NOTE**: This environment variable must be specified when running the `master-server`, `worker-server`, `api-server`, and `alert-server` services in `dolphinscheduler`, so that you can build distributed services better.

**`DATABASE_DRIVER`**

Configure `DRIVER` for `database`, default value `org.postgresql.Driver`.

**NOTE**: This environment variable must be specified when running the `master-server`, `worker-server`, `api-server`, and `alert-server` services in `dolphinscheduler`, so that you can better build distributed services.

**`DATABASE_HOST`**

Configure the `HOST` of `database`, default value `127.0.0.1`.

**NOTE**: This environment variable must be specified when running `master-server`, `worker-server`, `api-server`, `alert-server` services in `dolphinscheduler` so that you can build distributed services better.

**`DATABASE_PORT`**

Configure `PORT` for `database`, default value `5432`.

**NOTE**: This environment variable must be specified when running `master-server`, `worker-server`, `api-server`, `alert-server` services in `dolphinscheduler` so that you can build distributed services better.

**`DATABASE_USERNAME`**

Configure the `USERNAME` of `database`, default value `root`.

**NOTE**: This environment variable must be specified when running `master-server`, `worker-server`, `api-server`, `alert-server` services in `dolphinscheduler`, so that you can build distributed services better.

**`DATABASE_PASSWORD`**

Configure `PASSWORD` for `database`, default value `root`.

**NOTE**: This environment variable must be specified when running `master-server`, `worker-server`, `api-server`, `alert-server` services in `dolphinscheduler`, so that you can better build distributed services.

**`DATABASE_DATABASE`**

Configure `DATABASE` for `database`, default value `dolphinscheduler`.

**NOTE**: This environment variable must be specified when running the `master-server`, `worker-server`, `api-server`, and `alert-server` services in `dolphinscheduler`, so that you can better build distributed services.

**`DATABASE_PARAMS`**

Configure `PARAMS` for `database`, default value `characterEncoding=utf8`.

**NOTE**: This environment variable must be specified when running `master-server`, `worker-server`, `api-server`, `alert-server` services in `dolphinscheduler`, so that you can build distributed services better.

### ZooKeeper

**`ZOOKEEPER_QUORUM`**

Configure the `Zookeeper` address for `dolphinscheduler`, default value `127.0.0.1:2181`.

**NOTE**: This environment variable must be specified when running the `master-server`, `worker-server`, `api-server` services in `dolphinscheduler`, so that you can build distributed services better.

**`ZOOKEEPER_ROOT`**

Configure `dolphinscheduler` as the root directory for data storage in `zookeeper`, default value `/dolphinscheduler`.

### General

**`DOLPHINSCHEDULER_OPTS`**

Configure `jvm options` for `dolphinscheduler`, for `master-server`, `worker-server`, `api-server`, `alert-server`, default `""`,

**`DATA_BASEDIR_PATH`**

User data directory, user configured, make sure it exists and user read/write access, default value `/tmp/dolphinscheduler`.

**`RESOURCE_STORAGE_TYPE`**

Configure the resource storage type for `dolphinscheduler`, options are `HDFS`, `S3`, `NONE`, default `HDFS`.

**`RESOURCE_UPLOAD_PATH`**

Configure the resource storage path on `HDFS/S3`, default value `/dolphinscheduler`.

**`FS_DEFAULT_FS`**

Configure the file system protocol for the resource store, e.g. `file:///`, `hdfs://mycluster:8020` or `s3a://dolphinscheduler`, default value `file:///`.

**`FS_S3A_ENDPOINT`**

When `RESOURCE_STORAGE_TYPE=S3`, the access path to `S3` needs to be configured, default value `s3.xxx.amazonaws.com`.

**`FS_S3A_ACCESS_KEY`**

When `RESOURCE_STORAGE_TYPE=S3`, you need to configure the `s3 access key` of `S3`, default value `xxxxxxx`.

**`FS_S3A_SECRET_KEY`**

When `RESOURCE_STORAGE_TYPE=S3`, you need to configure `s3 secret key` for `S3`, default value `xxxxxxx`.

**`HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE`**

Configure whether `dolphinscheduler` is kerberos enabled, default value `false`.

**`JAVA_SECURITY_KRB5_CONF_PATH`**

Configure the path to java.security.krb5.conf for `dolphinscheduler`, default value `/opt/krb5.conf`.

**`LOGIN_USER_KEYTAB_USERNAME`**

Configure the keytab username for the `dolphinscheduler` login user, default value `hdfs@HADOOP.COM`.

**`LOGIN_USER_KEYTAB_PATH`**

Configure the keytab path for the `dolphinscheduler` login user, default value `/opt/hdfs.keytab`.

**`KERBEROS_EXPIRE_TIME`**

Configure the kerberos expiration time for `dolphinscheduler`, in hours, default value `2`.

**`HDFS_ROOT_USER`**

Configure the root user name of hdfs for `dolphinscheduler` when `RESOURCE_STORAGE_TYPE=HDFS`, default value `hdfs`.

**`RESOURCE_MANAGER_HTTPADDRESS_PORT`**

Configure the resource manager httpaddress port for `dolphinscheduler`, default value `8088`.

**`YARN_RESOURCEMANAGER_HA_RM_IDS`**

Configure `dolphinscheduler`'s yarn resourcemanager ha rm ids, default value `null`.

**`YARN_APPLICATION_STATUS_ADDRESS`**

Configure the yarn application status address for `dolphinscheduler`, default value `http://ds1:%s/ws/v1/cluster/apps/%s`.

**`SKYWALKING_ENABLE`**

Configure whether `skywalking` is enabled or not. Default value `false`.

**`SW_AGENT_COLLECTOR_BACKEND_SERVICES`**

Configure the collector back-end address for `skywalking`. Default value `127.0.0.1:11800`.

**`SW_GRPC_LOG_SERVER_HOST`**

Configure the grpc service host or IP for `skywalking`. Default value `127.0.0.1`.

**`SW_GRPC_LOG_SERVER_PORT`**

Configure the grpc service port for `skywalking`. Default value `11800`.

**`HADOOP_HOME`**

Configure `HADOOP_HOME` for `dolphinscheduler`, default value `/opt/soft/hadoop`.

**`HADOOP_CONF_DIR`**

Configure `HADOOP_CONF_DIR` for `dolphinscheduler`, default value `/opt/soft/hadoop/etc/hadoop`.

**`SPARK_HOME1`**

Configure `SPARK_HOME1` for `dolphinscheduler`, default value `/opt/soft/spark1`.

**`SPARK_HOME2`**

Configure `SPARK_HOME2` for `dolphinscheduler`, default value `/opt/soft/spark2`.

**`PYTHON_HOME`**

Configure `PYTHON_HOME` for `dolphinscheduler`, default value `/usr/bin/python`.

**`JAVA_HOME`**

Configure `JAVA_HOME` for `dolphinscheduler`, default value `/usr/local/openjdk-8`.

**`HIVE_HOME`**

Configure `HIVE_HOME` for `dolphinscheduler`, default value `/opt/soft/hive`.

**`FLINK_HOME`**

Configure `FLINK_HOME` for `dolphinscheduler`, default value `/opt/soft/flink`.

**`DATAX_HOME`**

Configure `DATAX_HOME` for `dolphinscheduler`, default value `/opt/soft/datax`.

### Master Server

**`MASTER_SERVER_OPTS`**

Configure `jvm options` for `master-server`, default value `-Xms1g -Xmx1g -Xmn512m`.

**`MASTER_EXEC_THREADS`**

Configure the number of threads to be executed in `master-server`, default value `100`.

**`MASTER_EXEC_TASK_NUM`**

Configure the number of tasks to be executed in `master-server`, default value `20`.

**`MASTER_DISPATCH_TASK_NUM`**

Configure the number of tasks to be dispatched in `master-server`, default value `3`.

**`MASTER_HOST_SELECTOR`**

Configure the selector for the worker host when dispatching tasks in `master-server`, optional values are `Random`, `RoundRobin` and `LowerWeight`, default value `LowerWeight`.

**`MASTER_HEARTBEAT_INTERVAL'**

Configure the heartbeat interaction time in `master-server`, default value `10`.

**`MASTER_TASK_COMMIT_RETRYTIMES`**

Configure the number of task commit retries in `master-server`, default value `5`.

**`MASTER_TASK_COMMIT_INTERVAL`**

Configure the task commit interaction time in `master-server`, default value `1`.

**`MASTER_MAX_CPULOAD_AVG`**

Configure the `load average` value in the CPU in `master-server`, default value `-1`.

**`MASTER_RESERVED_MEMORY`**

Configure the reserved memory in G for `master-server`, default value `0.3`.

### Worker Server

**`WORKER_SERVER_OPTS`**

Configure `jvm options` for `worker-server`, default value `-Xms1g -Xmx1g -Xmn512m`.

**`WORKER_EXEC_THREADS`**

Configure the number of threads to be executed in `worker-server`, default value `100`.

**`WORKER_HEARTBEAT_INTERVAL`**

Configure the heartbeat interaction time in `worker-server`, default value `10`.

**`WORKER_MAX_CPULOAD_AVG`**

Configure the maximum `load average` value in the CPU in `worker-server`, default value `-1`.

**`WORKER_RESERVED_MEMORY`**

Configure the reserved memory in G for `worker-server`, default value `0.3`.

**`WORKER_GROUPS`**

Configure the grouping of `worker-server`, default value `default`.

### Alert Server

**`ALERT_SERVER_OPTS`**

Configure `jvm options` for `alert-server`, default value `-Xms512m -Xmx512m -Xmn256m`.

**`XLS_FILE_PATH`**

Configure the path to store `XLS` files for the `alert-server`, default value `/tmp/xls`.

**`MAIL_SERVER_HOST`**

Configure the mail service address for `alert-server`, default value `empty`.

**`MAIL_SERVER_PORT`**

Configure the mail service port for `alert-server`, default value `empty`.

**`MAIL_SENDER`**

Configure the mail sender for `alert-server`, default value `empty`.

**`MAIL_USER=`**

Configure the user name of the mail service for `alert-server`, default value `empty`.

**`MAIL_PASSWD`**

Configure the mail service user password for `alert-server`, default value `empty`.

**`MAIL_SMTP_STARTTLS_ENABLE`**

Configure whether TLS is enabled for `alert-server`'s mail service, default value `true`.

**`MAIL_SMTP_SSL_ENABLE`**

Configure whether the mail service of `alert-server` is SSL enabled or not, default value `false`.

**`MAIL_SMTP_SSL_TRUST`**

Configure the trusted address for SSL for `alert-server`'s mail service, default value `null`.

**`ENTERPRISE_WECHAT_ENABLE`**

Configure whether the mail service of `alert-server` has Enterprise Wechat enabled, default value `false`.

**`ENTERPRISE_WECHAT_CORP_ID`**

Configures the Enterprise Wechat `ID` of the mail service for `alert-server`, default value `null`.

**`ENTERPRISE_WECHAT_SECRET`**

Configure the mail service enterprise wechat `SECRET` for `alert-server`, default value `Empty`.

**`ENTERPRISE_WECHAT_AGENT_ID`**

Configure `AGENT_ID` of the mail service enterprise wechat for `alert-server`, default value `Empty`.

**`ENTERPRISE_WECHAT_USERS`**

Configure `USERS` for the mail service enterprise microsoft for `alert-server`, default value `empty`.

### Api Server

**`API_SERVER_OPTS`**

Configure `jvm options` for `api-server`, default value `-Xms512m -Xmx512m -Xmn256m`.
