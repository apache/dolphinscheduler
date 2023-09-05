# QuickStart in Kubernetes

Kubernetes deployment is DolphinScheduler deployment in a Kubernetes cluster, which can schedule massive tasks and can be used in production.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Prerequisites

- [Helm](https://helm.sh/) version 3.1.0+
- [Kubernetes](https://kubernetes.io/) version 1.12+
- PV provisioner support in the underlying infrastructure

## Install DolphinScheduler

Please download the source code package `apache-dolphinscheduler-<version>-src.tar.gz`, download address: [download address](https://dolphinscheduler.apache.org/en-us/download)

To publish the release name `dolphinscheduler` version, please execute the following commands:

```
$ tar -zxvf apache-dolphinscheduler-<version>-src.tar.gz
$ cd apache-dolphinscheduler-<version>-src/deploy/kubernetes/dolphinscheduler
$ helm repo add bitnami https://charts.bitnami.com/bitnami
$ helm dependency update .
$ helm install dolphinscheduler . --set image.tag=<version>
```

To publish the release name `dolphinscheduler` version to `test` namespace:

```bash
$ helm install dolphinscheduler . -n test
```

> **Tip**: If a namespace named `test` is used, the optional parameter `-n test` needs to be added to the `helm` and `kubectl` commands.

These commands are used to deploy DolphinScheduler on the Kubernetes cluster by default. The [Appendix-Configuration](#appendix-configuration) section lists the parameters that can be configured during installation.

> **Tip**: List all releases using `helm list`

The **PostgreSQL** (with username `root`, password `root` and database `dolphinscheduler`) and **ZooKeeper** services will start by default.

## Access DolphinScheduler UI

If `ingress.enabled` in `values.yaml` is set to `true`, you could access `http://${ingress.host}/dolphinscheduler` in browser.

> **Tip**: If there is a problem with ingress access, please contact the Kubernetes administrator and refer to the [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/).

Otherwise, when `api.service.type=ClusterIP` you need to execute `port-forward` commands:

```bash
$ kubectl port-forward --address 0.0.0.0 svc/dolphinscheduler-api 12345:12345
$ kubectl port-forward --address 0.0.0.0 -n test svc/dolphinscheduler-api 12345:12345 # with test namespace
```

> **Tip**: If the error of `unable to do port forwarding: socat not found` appears, you need to install `socat` first.

Access the web: `http://localhost:12345/dolphinscheduler/ui` (Modify the IP address if needed).

Or when `api.service.type=NodePort` you need to execute the command:

```bash
NODE_IP=$(kubectl get no -n {{ .Release.Namespace }} -o jsonpath="{.items[0].status.addresses[0].address}")
NODE_PORT=$(kubectl get svc {{ template "dolphinscheduler.fullname" . }}-api -n {{ .Release.Namespace }} -o jsonpath="{.spec.ports[0].nodePort}")
echo http://$NODE_IP:$NODE_PORT/dolphinscheduler
```

Access the web: `http://$NODE_IP:$NODE_PORT/dolphinscheduler`.

The default username is `admin` and the default password is `dolphinscheduler123`.

Please refer to the `Quick Start` in the chapter [Quick Start](../start/quick-start.md) to explore how to use DolphinScheduler.

## Uninstall the Chart

To uninstall or delete the `dolphinscheduler` deployment:

```bash
$ helm uninstall dolphinscheduler
```

The command removes all the Kubernetes components (except PVC) associated with the `dolphinscheduler` and deletes the release.

Run the command below to delete the PVC's associated with `dolphinscheduler`:

```bash
$ kubectl delete pvc -l app.kubernetes.io/instance=dolphinscheduler
```

> **Note**: Deleting the PVC's will delete all data as well. Please be cautious before doing it.

## [Experimental] Worker Autoscaling

> **Warning**: Currently this is an experimental feature and may not be suitable for production!

`DolphinScheduler` uses [KEDA](https://github.com/kedacore/keda) for worker autoscaling. However, `DolphinScheduler` disables
this feature by default. To turn on worker autoscaling:

Firstly, you need to create a namespace for `KEDA` and install it with `helm`:

```bash
helm repo add kedacore https://kedacore.github.io/charts

helm repo update

kubectl create namespace keda

helm install keda kedacore/keda \
    --namespace keda \
    --version "v2.0.0"
```

Secondly, you need to set `worker.keda.enabled` to `true` in `values.yaml` or install the chart by:

```bash
helm install dolphinscheduler . --set worker.keda.enabled=true -n <your-namespace-to-deploy-dolphinscheduler>
```

Once autoscaling enabled, the number of workers will scale between `minReplicaCount` and `maxReplicaCount` based on the states
of your tasks. For example, when there is no tasks running in your `DolphinScheduler` instance, there will be no workers,
which will significantly save the resources.

Worker autoscaling feature is compatible with `postgresql` and `mysql` shipped with `DolphinScheduler official helm chart`. If you
use external database, worker autoscaling feature only supports external `mysql` and `postgresql` databases.

If you need to change the value of worker `WORKER_EXEC_THREADS` when using autoscaling feature,
please change `worker.env.WORKER_EXEC_THREADS` in `values.yaml` instead of through `configmap`.

## Configuration

The configuration file is `values.yaml`, and the [Appendix-Configuration](#appendix-configuration) tables lists the configurable parameters of the DolphinScheduler and their default values.

## Support Matrix

|                             Type                             |   Support    |                 Notes                 |
|--------------------------------------------------------------|--------------|---------------------------------------|
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

### How to View the Logs of a Pod Container?

List all pods (aka `po`):

```
kubectl get po
kubectl get po -n test # with test namespace
```

View the logs of a pod container named `dolphinscheduler-master-0`:

```
kubectl logs dolphinscheduler-master-0
kubectl logs -f dolphinscheduler-master-0 # follow log output
kubectl logs --tail 10 dolphinscheduler-master-0 -n test # show last 10 lines from the end of the logs
```

### How to Scale API, master and worker on Kubernetes?

List all deployments (aka `deploy`):

```
kubectl get deploy
kubectl get deploy -n test # with test namespace
```

Scale api to 3 replicas:

```
kubectl scale --replicas=3 deploy dolphinscheduler-api
kubectl scale --replicas=3 deploy dolphinscheduler-api -n test # with test namespace
```

List all stateful sets (aka `sts`):

```
kubectl get sts
kubectl get sts -n test # with test namespace
```

Scale master to 2 replicas:

```
kubectl scale --replicas=2 sts dolphinscheduler-master
kubectl scale --replicas=2 sts dolphinscheduler-master -n test # with test namespace
```

Scale worker to 6 replicas:

```
kubectl scale --replicas=6 sts dolphinscheduler-worker
kubectl scale --replicas=6 sts dolphinscheduler-worker -n test # with test namespace
```

### How to Use MySQL as the DolphinScheduler's Database Instead of PostgreSQL?

> Because of the commercial license, we cannot directly use the driver of MySQL.
>
> If you want to use MySQL, you can build a new image based on the `apache/dolphinscheduler-<service>` image follow the following instructions:
>
> Since version 3.0.0, dolphinscheduler has been microserviced and the change of metadata storage requires replacing all services with MySQL driver, which including dolphinscheduler-tools, dolphinscheduler-master, dolphinscheduler-worker, dolphinscheduler-api, dolphinscheduler-alert-server

1. Download the MySQL driver [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar).

2. Create a new `Dockerfile` to add MySQL driver:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:<version>
# For example
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-tools:<version>

# Attention Please, If the build is dolphinscheduler-tools image
# You need to change the following line to: COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/tools/libs
# The other services don't need any changes
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs
```

3. Build a new docker image including MySQL driver:

```
docker build -t apache/dolphinscheduler-<service>:mysql-driver .
```

4. Push the docker image `apache/dolphinscheduler-<service>:mysql-driver` to a docker registry.

5. Modify image `repository` and update `tag` to `mysql-driver` in `values.yaml`.

6. Modify postgresql `enabled` to `false` in `values.yaml`.

7. Modify externalDatabase (especially modify `host`, `username` and `password`) in `values.yaml`:

```yaml
externalDatabase:
  type: "mysql"
  host: "localhost"
  port: "3306"
  username: "root"
  password: "root"
  database: "dolphinscheduler"
  params: "useUnicode=true&characterEncoding=UTF-8"
```

8. Run a DolphinScheduler release in Kubernetes (See **Install DolphinScheduler**).

### How to Support MySQL or Oracle Datasource in `Datasource manage`?

> Because of the commercial license, we cannot directly use the driver of MySQL or Oracle.
>
> If you want to add MySQL or Oracle datasource, you can build a new image based on the `apache/dolphinscheduler-<service>` image follow the following instructions:
>
> You need to change the two service images including dolphinscheduler-worker, dolphinscheduler-api.

1. Download the MySQL driver [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar).
   or download the Oracle driver [ojdbc8.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/) (such as `ojdbc8-19.9.0.0.jar`)

2. Create a new `Dockerfile` to add MySQL or Oracle driver:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:<version>
# For example
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>

# If you want to support MySQL Datasource
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs

# If you want to support Oracle Datasource
COPY ojdbc8-19.9.0.0.jar /opt/dolphinscheduler/libs
```

3. Build a new docker image including MySQL or Oracle driver:

```
docker build -t apache/dolphinscheduler-<service>:new-driver .
```

4. Push the docker image `apache/dolphinscheduler-<service>:new-driver` to a docker registry.

5. Modify image `repository` and update `tag` to `new-driver` in `values.yaml`.

6. Run a DolphinScheduler release in Kubernetes (See **Install DolphinScheduler**).

7. Add a MySQL or Oracle datasource in `Datasource manage`.

### How to Support Python 2 pip and Custom requirements.txt?

> Just change the image of the dolphinscheduler-worker service.

1. Create a new `Dockerfile` to install pip:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>
COPY requirements.txt /tmp
RUN apt-get update && \
    apt-get install -y --no-install-recommends python-pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    rm -rf /var/lib/apt/lists/*
```

The command will install the default **pip 18.1**. If you upgrade the pip, just add the following command.

```
pip install --no-cache-dir -U pip && \
```

2. Build a new docker image including pip:

```
docker build -t apache/dolphinscheduler-worker:pip .
```

3. Push the docker image `apache/dolphinscheduler-worker:pip` to a docker registry.

4. Modify image `repository` and update `tag` to `pip` in `values.yaml`.

5. Run a DolphinScheduler release in Kubernetes (See **Install DolphinScheduler**).

6. Verify pip under a new Python task.

### How to Support Python 3?

> Just change the image of the dolphinscheduler-worker service.

1. Create a new `Dockerfile` to install Python 3:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 && \
    rm -rf /var/lib/apt/lists/*
```

The command will install the default **Python 3.7.3**. If you also want to install **pip3**, just replace `python3` with `python3-pip` like:

```
apt-get install -y --no-install-recommends python3-pip && \
```

2. Build a new docker image including Python 3:

```
docker build -t apache/dolphinscheduler-worker:python3 .
```

3. Push the docker image `apache/dolphinscheduler-worker:python3` to a docker registry.

4. Modify image `repository` and update `tag` to `python3` in `values.yaml`.

5. Modify `PYTHON_LAUNCHER` to `/usr/bin/python3` in `values.yaml`.

6. Run a DolphinScheduler release in Kubernetes (See **Install DolphinScheduler**).

7. Verify Python 3 under a new Python task.

### How to Support Hadoop, Spark, Flink, Hive or DataX?

Take Spark 2.4.7 as an example:

1. Download the Spark 2.4.7 release binary `spark-2.4.7-bin-hadoop2.7.tgz`.

2. Ensure that `common.sharedStoragePersistence.enabled` is turned on.

3. Run a DolphinScheduler release in Kubernetes (See **Install DolphinScheduler**).

4. Copy the Spark 2.4.7 release binary into the Docker container.

```bash
kubectl cp spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft
kubectl cp -n test spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft # with test namespace
```

Because the volume `sharedStoragePersistence` is mounted on `/opt/soft`, all files in `/opt/soft` will not be lost.

5. Attach the container and ensure that `SPARK_HOME` exists.

```bash
kubectl exec -it dolphinscheduler-worker-0 bash
kubectl exec -n test -it dolphinscheduler-worker-0 bash # with test namespace
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # or just mv
$SPARK_HOME/bin/spark-submit --version
```

The last command will print the Spark version if everything goes well.

6. Verify Spark under a Shell task.

```
$SPARK_HOME/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME/examples/jars/spark-examples_2.11-2.4.7.jar
```

Check whether the task log contains the output like `Pi is roughly 3.146015`.

7. Verify Spark under a Spark task.

The file `spark-examples_2.11-2.4.7.jar` needs to be uploaded to the resources first, and then create a Spark task with:

- Main Class: `org.apache.spark.examples.SparkPi`
- Main Package: `spark-examples_2.11-2.4.7.jar`
- Deploy Mode: `local`

Similarly, check whether the task log contains the output like `Pi is roughly 3.146015`.

8. Verify Spark on YARN.

Spark on YARN (Deploy Mode is `cluster` or `client`) requires Hadoop support. Similar to Spark support, the operation of supporting Hadoop is almost the same as the previous steps.

Ensure that `$HADOOP_HOME` and `$HADOOP_CONF_DIR` exists.

### How to Support Shared Storage Between Master, Worker and Api Server?

For example, Master, Worker and API server may use Hadoop at the same time.

1. Modify the following configurations in `values.yaml`

```yaml
common:
  sharedStoragePersistence:
    enabled: false
    mountPath: "/opt/soft"
    accessModes:
      - "ReadWriteMany"
    storageClassName: "-"
    storage: "20Gi"
```

Modify `storageClassName` and `storage` to actual environment values.

> **Note**: `storageClassName` must support the access mode: `ReadWriteMany`.

2. Copy the Hadoop into the directory `/opt/soft`.

3. Ensure that `$HADOOP_HOME` and `$HADOOP_CONF_DIR` are correct.

### How to Support Local File Resource Storage Instead of HDFS and S3?

Modify the following configurations in `values.yaml`:

```yaml
common:
  configmap:
    RESOURCE_STORAGE_TYPE: "HDFS"
    RESOURCE_UPLOAD_PATH: "/dolphinscheduler"
    FS_DEFAULT_FS: "file:///"
  fsFileResourcePersistence:
    enabled: true
    accessModes:
      - "ReadWriteMany"
    storageClassName: "-"
    storage: "20Gi"
```

Modify `storageClassName` and `storage` to actual environment values.

> **Note**: `storageClassName` must support the access mode: `ReadWriteMany`.

### How to Support S3 Resource Storage Like MinIO?

Take MinIO as an example: Modify the following configurations in `values.yaml`:

```yaml
common:
  configmap:
    RESOURCE_STORAGE_TYPE: "S3"
    RESOURCE_UPLOAD_PATH: "/dolphinscheduler"
    FS_DEFAULT_FS: "s3a://BUCKET_NAME"
    FS_S3A_ENDPOINT: "http://MINIO_IP:9000"
    FS_S3A_ACCESS_KEY: "MINIO_ACCESS_KEY"
    FS_S3A_SECRET_KEY: "MINIO_SECRET_KEY"
```

Modify `BUCKET_NAME`, `MINIO_IP`, `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` to actual environment values.

> **Note**: `MINIO_IP` can only use IP instead of the domain name, because DolphinScheduler currently doesn't support S3 path style access.

### How to Configure SkyWalking?

Modify SkyWalking configurations in `values.yaml`:

```yaml
common:
  configmap:
    SKYWALKING_ENABLE: "true"
    SW_AGENT_COLLECTOR_BACKEND_SERVICES: "127.0.0.1:11800"
    SW_GRPC_LOG_SERVER_HOST: "127.0.0.1"
    SW_GRPC_LOG_SERVER_PORT: "11800"
```

## Appendix-Configuration

|                              Parameter                               |                                                          Description                                                          |                Default                |
|----------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------|---------------------------------------|
| `timezone`                                                           | World time and date for cities in all time zones                                                                              | `Asia/Shanghai`                       |
|                                                                      |                                                                                                                               |                                       |
| `image.repository`                                                   | Docker image repository for the DolphinScheduler                                                                              | `apache/dolphinscheduler`             |
| `image.tag`                                                          | Docker image version for the DolphinScheduler                                                                                 | `latest`                              |
| `image.pullPolicy`                                                   | Image pull policy. Options: Always, Never, IfNotPresent                                                                       | `IfNotPresent`                        |
| `image.pullSecret`                                                   | Image pull secret. An optional reference to secret in the same namespace to use for pulling any of the images                 | `nil`                                 |
|                                                                      |                                                                                                                               |                                       |
| `postgresql.enabled`                                                 | If not exists external PostgreSQL, by default, the DolphinScheduler will use a internal PostgreSQL                            | `true`                                |
| `postgresql.postgresqlUsername`                                      | The username for internal PostgreSQL                                                                                          | `root`                                |
| `postgresql.postgresqlPassword`                                      | The password for internal PostgreSQL                                                                                          | `root`                                |
| `postgresql.postgresqlDatabase`                                      | The database for internal PostgreSQL                                                                                          | `dolphinscheduler`                    |
| `postgresql.persistence.enabled`                                     | Set `postgresql.persistence.enabled` to `true` to mount a new volume for internal PostgreSQL                                  | `false`                               |
| `postgresql.persistence.size`                                        | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
| `postgresql.persistence.storageClass`                                | PostgreSQL data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning     | `-`                                   |
| `minio.enabled`                                                      | Deploy minio and configure it as the default storage for DolphinScheduler, note this is for demo only, not for production.    | `false`                               |
| `externalDatabase.type`                                              | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database type will use it      | `postgresql`                          |
| `externalDatabase.driver`                                            | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database driver will use it    | `org.postgresql.Driver`               |
| `externalDatabase.host`                                              | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database host will use it      | `localhost`                           |
| `externalDatabase.port`                                              | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database port will use it      | `5432`                                |
| `externalDatabase.username`                                          | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database username will use it  | `root`                                |
| `externalDatabase.password`                                          | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database password will use it  | `root`                                |
| `externalDatabase.database`                                          | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database database will use it  | `dolphinscheduler`                    |
| `externalDatabase.params`                                            | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database params will use it    | `characterEncoding=utf8`              |
|                                                                      |                                                                                                                               |                                       |
| `zookeeper.enabled`                                                  | If not exists external ZooKeeper, by default, the DolphinScheduler will use a internal ZooKeeper                              | `true`                                |
| `zookeeper.service.port`                                             | The port of zookeeper                                                                                                         | `2181`                                |
| `zookeeper.fourlwCommandsWhitelist`                                  | A list of comma separated Four Letter Words commands to use                                                                   | `srvr,ruok,wchs,cons`                 |
| `zookeeper.persistence.enabled`                                      | Set `zookeeper.persistence.enabled` to `true` to mount a new volume for internal ZooKeeper                                    | `false`                               |
| `zookeeper.persistence.size`                                         | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
| `zookeeper.persistence.storageClass`                                 | ZooKeeper data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning      | `-`                                   |
| `externalRegistry.registryPluginName`                                | If exists external registry and set `zookeeper.enable` to `false`, specify the external registry plugin name                  | `zookeeper`                           |
| `externalRegistry.registryServers`                                   | If exists external registry and set `zookeeper.enable` to `false`, specify the external registry servers                      | `127.0.0.1:2181`                      |
|                                                                      |                                                                                                                               |                                       |
| `common.configmap.DOLPHINSCHEDULER_OPTS`                             | The jvm options for dolphinscheduler, suitable for all servers                                                                | `""`                                  |
| `common.configmap.DATA_BASEDIR_PATH`                                 | User data directory path, self configuration, please make sure the directory exists and have read write permissions           | `/tmp/dolphinscheduler`               |
| `common.configmap.RESOURCE_STORAGE_TYPE`                             | Resource storage type: HDFS, S3, OSS, GCS, ABS, NONE                                                                          | `HDFS`                                |
| `common.configmap.RESOURCE_UPLOAD_PATH`                              | Resource store on HDFS/S3 path, please make sure the directory exists on hdfs and have read write permissions                 | `/dolphinscheduler`                   |
| `common.configmap.FS_DEFAULT_FS`                                     | Resource storage file system like `file:///`, `hdfs://mycluster:8020` or `s3a://dolphinscheduler`                             | `file:///`                            |
| `common.configmap.FS_S3A_ENDPOINT`                                   | S3 endpoint when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                      | `s3.xxx.amazonaws.com`                |
| `common.configmap.FS_S3A_ACCESS_KEY`                                 | S3 access key when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                    | `xxxxxxx`                             |
| `common.configmap.FS_S3A_SECRET_KEY`                                 | S3 secret key when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                    | `xxxxxxx`                             |
| `common.configmap.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE`      | Whether to startup kerberos                                                                                                   | `false`                               |
| `common.configmap.JAVA_SECURITY_KRB5_CONF_PATH`                      | The java.security.krb5.conf path                                                                                              | `/opt/krb5.conf`                      |
| `common.configmap.LOGIN_USER_KEYTAB_USERNAME`                        | The login user from keytab username                                                                                           | `hdfs@HADOOP.COM`                     |
| `common.configmap.LOGIN_USER_KEYTAB_PATH`                            | The login user from keytab path                                                                                               | `/opt/hdfs.keytab`                    |
| `common.configmap.KERBEROS_EXPIRE_TIME`                              | The kerberos expire time, the unit is hour                                                                                    | `2`                                   |
| `common.configmap.HDFS_ROOT_USER`                                    | The HDFS root user who must have the permission to create directories under the HDFS root path                                | `hdfs`                                |
| `common.configmap.RESOURCE_MANAGER_HTTPADDRESS_PORT`                 | Set resource manager httpaddress port for yarn                                                                                | `8088`                                |
| `common.configmap.YARN_RESOURCEMANAGER_HA_RM_IDS`                    | If resourcemanager HA is enabled, please set the HA IPs                                                                       | `nil`                                 |
| `common.configmap.YARN_APPLICATION_STATUS_ADDRESS`                   | If resourcemanager is single, you only need to replace ds1 to actual resourcemanager hostname, otherwise keep default         | `http://ds1:%s/ws/v1/cluster/apps/%s` |
| `common.configmap.SKYWALKING_ENABLE`                                 | Set whether to enable skywalking                                                                                              | `false`                               |
| `common.configmap.SW_AGENT_COLLECTOR_BACKEND_SERVICES`               | Set agent collector backend services for skywalking                                                                           | `127.0.0.1:11800`                     |
| `common.configmap.SW_GRPC_LOG_SERVER_HOST`                           | Set grpc log server host for skywalking                                                                                       | `127.0.0.1`                           |
| `common.configmap.SW_GRPC_LOG_SERVER_PORT`                           | Set grpc log server port for skywalking                                                                                       | `11800`                               |
| `common.configmap.HADOOP_HOME`                                       | Set `HADOOP_HOME` for DolphinScheduler's task environment                                                                     | `/opt/soft/hadoop`                    |
| `common.configmap.HADOOP_CONF_DIR`                                   | Set `HADOOP_CONF_DIR` for DolphinScheduler's task environment                                                                 | `/opt/soft/hadoop/etc/hadoop`         |
| `common.configmap.SPARK_HOME`                                        | Set `SPARK_HOME` for DolphinScheduler's task environment                                                                      | `/opt/soft/spark`                     |
| `common.configmap.PYTHON_LAUNCHER`                                   | Set `PYTHON_LAUNCHER` for DolphinScheduler's task environment                                                                 | `/usr/bin/python`                     |
| `common.configmap.JAVA_HOME`                                         | Set `JAVA_HOME` for DolphinScheduler's task environment                                                                       | `/opt/java/openjdk`                   |
| `common.configmap.HIVE_HOME`                                         | Set `HIVE_HOME` for DolphinScheduler's task environment                                                                       | `/opt/soft/hive`                      |
| `common.configmap.FLINK_HOME`                                        | Set `FLINK_HOME` for DolphinScheduler's task environment                                                                      | `/opt/soft/flink`                     |
| `common.configmap.DATAX_LAUNCHER`                                    | Set `DATAX_LAUNCHER` for DolphinScheduler's task environment                                                                  | `/opt/soft/datax`                     |
| `common.sharedStoragePersistence.enabled`                            | Set `common.sharedStoragePersistence.enabled` to `true` to mount a shared storage volume for Hadoop, Spark binary and etc     | `false`                               |
| `common.sharedStoragePersistence.mountPath`                          | The mount path for the shared storage volume                                                                                  | `/opt/soft`                           |
| `common.sharedStoragePersistence.accessModes`                        | `PersistentVolumeClaim` access modes, must be `ReadWriteMany`                                                                 | `[ReadWriteMany]`                     |
| `common.sharedStoragePersistence.storageClassName`                   | Shared Storage persistent volume storage class, must support the access mode: ReadWriteMany                                   | `-`                                   |
| `common.sharedStoragePersistence.storage`                            | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
| `common.fsFileResourcePersistence.enabled`                           | Set `common.fsFileResourcePersistence.enabled` to `true` to mount a new file resource volume for `api` and `worker`           | `false`                               |
| `common.fsFileResourcePersistence.accessModes`                       | `PersistentVolumeClaim` access modes, must be `ReadWriteMany`                                                                 | `[ReadWriteMany]`                     |
| `common.fsFileResourcePersistence.storageClassName`                  | Resource persistent volume storage class, must support the access mode: ReadWriteMany                                         | `-`                                   |
| `common.fsFileResourcePersistence.storage`                           | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
|                                                                      |                                                                                                                               |                                       |
| `master.podManagementPolicy`                                         | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down | `Parallel`                            |
| `master.replicas`                                                    | Replicas is the desired number of replicas of the given Template                                                              | `3`                                   |
| `master.annotations`                                                 | The `annotations` for master server                                                                                           | `{}`                                  |
| `master.affinity`                                                    | If specified, the pod's scheduling constraints                                                                                | `{}`                                  |
| `master.nodeSelector`                                                | NodeSelector is a selector which must be true for the pod to fit on a node                                                    | `{}`                                  |
| `master.tolerations`                                                 | If specified, the pod's tolerations                                                                                           | `{}`                                  |
| `master.resources`                                                   | The `resource` limit and request config for master server                                                                     | `{}`                                  |
| `master.env.JAVA_OPTS`                                               | The jvm options for master server                                                                                             | `-Xms1g -Xmx1g -Xmn512m`              |
| `master.env.MASTER_EXEC_THREADS`                                     | Master execute thread number to limit process instances                                                                       | `100`                                 |
| `master.env.MASTER_EXEC_TASK_NUM`                                    | Master execute task number in parallel per process instance                                                                   | `20`                                  |
| `master.env.MASTER_DISPATCH_TASK_NUM`                                | Master dispatch task number per batch                                                                                         | `3`                                   |
| `master.env.MASTER_HOST_SELECTOR`                                    | Master host selector to select a suitable worker, optional values include Random, RoundRobin, LowerWeight                     | `LowerWeight`                         |
| `master.env.MASTER_HEARTBEAT_INTERVAL`                               | Master heartbeat interval, the unit is second                                                                                 | `10s`                                 |
| `master.env.MASTER_TASK_COMMIT_RETRYTIMES`                           | Master commit task retry times                                                                                                | `5`                                   |
| `master.env.MASTER_TASK_COMMIT_INTERVAL`                             | master commit task interval, the unit is second                                                                               | `1s`                                  |
| `master.env.MASTER_MAX_CPULOAD_AVG`                                  | Master max cpuload avg, only higher than the system cpu load average, master server can schedule                              | `-1` (`the number of cpu cores * 2`)  |
| `master.env.MASTER_RESERVED_MEMORY`                                  | Master reserved memory, only lower than system available memory, master server can schedule, the unit is G                    | `0.3`                                 |
| `master.livenessProbe.enabled`                                       | Turn on and off liveness probe                                                                                                | `true`                                |
| `master.livenessProbe.initialDelaySeconds`                           | Delay before liveness probe is initiated                                                                                      | `30`                                  |
| `master.livenessProbe.periodSeconds`                                 | How often to perform the probe                                                                                                | `30`                                  |
| `master.livenessProbe.timeoutSeconds`                                | When the probe times out                                                                                                      | `5`                                   |
| `master.livenessProbe.failureThreshold`                              | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `master.livenessProbe.successThreshold`                              | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `master.readinessProbe.enabled`                                      | Turn on and off readiness probe                                                                                               | `true`                                |
| `master.readinessProbe.initialDelaySeconds`                          | Delay before readiness probe is initiated                                                                                     | `30`                                  |
| `master.readinessProbe.periodSeconds`                                | How often to perform the probe                                                                                                | `30`                                  |
| `master.readinessProbe.timeoutSeconds`                               | When the probe times out                                                                                                      | `5`                                   |
| `master.readinessProbe.failureThreshold`                             | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `master.readinessProbe.successThreshold`                             | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `master.persistentVolumeClaim.enabled`                               | Set `master.persistentVolumeClaim.enabled` to `true` to mount a new volume for `master`                                       | `false`                               |
| `master.persistentVolumeClaim.accessModes`                           | `PersistentVolumeClaim` access modes                                                                                          | `[ReadWriteOnce]`                     |
| `master.persistentVolumeClaim.storageClassName`                      | `Master` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning  | `-`                                   |
| `master.persistentVolumeClaim.storage`                               | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
|                                                                      |                                                                                                                               |                                       |
| `worker.podManagementPolicy`                                         | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down | `Parallel`                            |
| `worker.replicas`                                                    | Replicas is the desired number of replicas of the given Template                                                              | `3`                                   |
| `worker.annotations`                                                 | The `annotations` for worker server                                                                                           | `{}`                                  |
| `worker.affinity`                                                    | If specified, the pod's scheduling constraints                                                                                | `{}`                                  |
| `worker.nodeSelector`                                                | NodeSelector is a selector which must be true for the pod to fit on a node                                                    | `{}`                                  |
| `worker.tolerations`                                                 | If specified, the pod's tolerations                                                                                           | `{}`                                  |
| `worker.resources`                                                   | The `resource` limit and request config for worker server                                                                     | `{}`                                  |
| `worker.env.WORKER_EXEC_THREADS`                                     | Worker execute thread number to limit task instances                                                                          | `100`                                 |
| `worker.env.WORKER_HEARTBEAT_INTERVAL`                               | Worker heartbeat interval, the unit is second                                                                                 | `10s`                                 |
| `worker.env.WORKER_MAX_CPU_LOAD_AVG`                                 | Worker max cpu load avg, only higher than the system cpu load average, worker server can be dispatched tasks                  | `-1` (`the number of cpu cores * 2`)  |
| `worker.env.WORKER_RESERVED_MEMORY`                                  | Worker reserved memory, only lower than system available memory, worker server can be dispatched tasks, the unit is G         | `0.3`                                 |
| `worker.env.HOST_WEIGHT`                                             | Worker host weight to dispatch tasks                                                                                          | `100`                                 |
| `worker.livenessProbe.enabled`                                       | Turn on and off liveness probe                                                                                                | `true`                                |
| `worker.livenessProbe.initialDelaySeconds`                           | Delay before liveness probe is initiated                                                                                      | `30`                                  |
| `worker.livenessProbe.periodSeconds`                                 | How often to perform the probe                                                                                                | `30`                                  |
| `worker.livenessProbe.timeoutSeconds`                                | When the probe times out                                                                                                      | `5`                                   |
| `worker.livenessProbe.failureThreshold`                              | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `worker.livenessProbe.successThreshold`                              | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `worker.readinessProbe.enabled`                                      | Turn on and off readiness probe                                                                                               | `true`                                |
| `worker.readinessProbe.initialDelaySeconds`                          | Delay before readiness probe is initiated                                                                                     | `30`                                  |
| `worker.readinessProbe.periodSeconds`                                | How often to perform the probe                                                                                                | `30`                                  |
| `worker.readinessProbe.timeoutSeconds`                               | When the probe times out                                                                                                      | `5`                                   |
| `worker.readinessProbe.failureThreshold`                             | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `worker.readinessProbe.successThreshold`                             | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `worker.persistentVolumeClaim.enabled`                               | Set `worker.persistentVolumeClaim.enabled` to `true` to enable `persistentVolumeClaim` for `worker`                           | `false`                               |
| `worker.persistentVolumeClaim.dataPersistentVolume.enabled`          | Set `worker.persistentVolumeClaim.dataPersistentVolume.enabled` to `true` to mount a data volume for `worker`                 | `false`                               |
| `worker.persistentVolumeClaim.dataPersistentVolume.accessModes`      | `PersistentVolumeClaim` access modes                                                                                          | `[ReadWriteOnce]`                     |
| `worker.persistentVolumeClaim.dataPersistentVolume.storageClassName` | `Worker` data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning       | `-`                                   |
| `worker.persistentVolumeClaim.dataPersistentVolume.storage`          | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
| `worker.persistentVolumeClaim.logsPersistentVolume.enabled`          | Set `worker.persistentVolumeClaim.logsPersistentVolume.enabled` to `true` to mount a logs volume for `worker`                 | `false`                               |
| `worker.persistentVolumeClaim.logsPersistentVolume.accessModes`      | `PersistentVolumeClaim` access modes                                                                                          | `[ReadWriteOnce]`                     |
| `worker.persistentVolumeClaim.logsPersistentVolume.storageClassName` | `Worker` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning  | `-`                                   |
| `worker.persistentVolumeClaim.logsPersistentVolume.storage`          | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
|                                                                      |                                                                                                                               |                                       |
| `alert.replicas`                                                     | Replicas is the desired number of replicas of the given Template                                                              | `1`                                   |
| `alert.strategy.type`                                                | Type of deployment. Can be "Recreate" or "RollingUpdate"                                                                      | `RollingUpdate`                       |
| `alert.strategy.rollingUpdate.maxSurge`                              | The maximum number of pods that can be scheduled above the desired number of pods                                             | `25%`                                 |
| `alert.strategy.rollingUpdate.maxUnavailable`                        | The maximum number of pods that can be unavailable during the update                                                          | `25%`                                 |
| `alert.annotations`                                                  | The `annotations` for alert server                                                                                            | `{}`                                  |
| `alert.affinity`                                                     | If specified, the pod's scheduling constraints                                                                                | `{}`                                  |
| `alert.nodeSelector`                                                 | NodeSelector is a selector which must be true for the pod to fit on a node                                                    | `{}`                                  |
| `alert.tolerations`                                                  | If specified, the pod's tolerations                                                                                           | `{}`                                  |
| `alert.resources`                                                    | The `resource` limit and request config for alert server                                                                      | `{}`                                  |
| `alert.configmap.ALERT_SERVER_OPTS`                                  | The jvm options for alert server                                                                                              | `-Xms512m -Xmx512m -Xmn256m`          |
| `alert.configmap.XLS_FILE_PATH`                                      | XLS file path                                                                                                                 | `/tmp/xls`                            |
| `alert.configmap.MAIL_SERVER_HOST`                                   | Mail `SERVER HOST `                                                                                                           | `nil`                                 |
| `alert.configmap.MAIL_SERVER_PORT`                                   | Mail `SERVER PORT`                                                                                                            | `nil`                                 |
| `alert.configmap.MAIL_SENDER`                                        | Mail `SENDER`                                                                                                                 | `nil`                                 |
| `alert.configmap.MAIL_USER`                                          | Mail `USER`                                                                                                                   | `nil`                                 |
| `alert.configmap.MAIL_PASSWD`                                        | Mail `PASSWORD`                                                                                                               | `nil`                                 |
| `alert.configmap.MAIL_SMTP_STARTTLS_ENABLE`                          | Mail `SMTP STARTTLS` enable                                                                                                   | `false`                               |
| `alert.configmap.MAIL_SMTP_SSL_ENABLE`                               | Mail `SMTP SSL` enable                                                                                                        | `false`                               |
| `alert.configmap.MAIL_SMTP_SSL_TRUST`                                | Mail `SMTP SSL TRUST`                                                                                                         | `nil`                                 |
| `alert.configmap.ENTERPRISE_WECHAT_ENABLE`                           | `Enterprise Wechat` enable                                                                                                    | `false`                               |
| `alert.configmap.ENTERPRISE_WECHAT_CORP_ID`                          | `Enterprise Wechat` corp id                                                                                                   | `nil`                                 |
| `alert.configmap.ENTERPRISE_WECHAT_SECRET`                           | `Enterprise Wechat` secret                                                                                                    | `nil`                                 |
| `alert.configmap.ENTERPRISE_WECHAT_AGENT_ID`                         | `Enterprise Wechat` agent id                                                                                                  | `nil`                                 |
| `alert.configmap.ENTERPRISE_WECHAT_USERS`                            | `Enterprise Wechat` users                                                                                                     | `nil`                                 |
| `alert.livenessProbe.enabled`                                        | Turn on and off liveness probe                                                                                                | `true`                                |
| `alert.livenessProbe.initialDelaySeconds`                            | Delay before liveness probe is initiated                                                                                      | `30`                                  |
| `alert.livenessProbe.periodSeconds`                                  | How often to perform the probe                                                                                                | `30`                                  |
| `alert.livenessProbe.timeoutSeconds`                                 | When the probe times out                                                                                                      | `5`                                   |
| `alert.livenessProbe.failureThreshold`                               | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `alert.livenessProbe.successThreshold`                               | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `alert.readinessProbe.enabled`                                       | Turn on and off readiness probe                                                                                               | `true`                                |
| `alert.readinessProbe.initialDelaySeconds`                           | Delay before readiness probe is initiated                                                                                     | `30`                                  |
| `alert.readinessProbe.periodSeconds`                                 | How often to perform the probe                                                                                                | `30`                                  |
| `alert.readinessProbe.timeoutSeconds`                                | When the probe times out                                                                                                      | `5`                                   |
| `alert.readinessProbe.failureThreshold`                              | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `alert.readinessProbe.successThreshold`                              | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `alert.persistentVolumeClaim.enabled`                                | Set `alert.persistentVolumeClaim.enabled` to `true` to mount a new volume for `alert`                                         | `false`                               |
| `alert.persistentVolumeClaim.accessModes`                            | `PersistentVolumeClaim` access modes                                                                                          | `[ReadWriteOnce]`                     |
| `alert.persistentVolumeClaim.storageClassName`                       | `Alert` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning   | `-`                                   |
| `alert.persistentVolumeClaim.storage`                                | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
|                                                                      |                                                                                                                               |                                       |
| `api.replicas`                                                       | Replicas is the desired number of replicas of the given Template                                                              | `1`                                   |
| `api.strategy.type`                                                  | Type of deployment. Can be "Recreate" or "RollingUpdate"                                                                      | `RollingUpdate`                       |
| `api.strategy.rollingUpdate.maxSurge`                                | The maximum number of pods that can be scheduled above the desired number of pods                                             | `25%`                                 |
| `api.strategy.rollingUpdate.maxUnavailable`                          | The maximum number of pods that can be unavailable during the update                                                          | `25%`                                 |
| `api.annotations`                                                    | The `annotations` for api server                                                                                              | `{}`                                  |
| `api.affinity`                                                       | If specified, the pod's scheduling constraints                                                                                | `{}`                                  |
| `api.nodeSelector`                                                   | NodeSelector is a selector which must be true for the pod to fit on a node                                                    | `{}`                                  |
| `api.tolerations`                                                    | If specified, the pod's tolerations                                                                                           | `{}`                                  |
| `api.resources`                                                      | The `resource` limit and request config for api server                                                                        | `{}`                                  |
| `api.configmap.API_SERVER_OPTS`                                      | The jvm options for api server                                                                                                | `-Xms512m -Xmx512m -Xmn256m`          |
| `api.livenessProbe.enabled`                                          | Turn on and off liveness probe                                                                                                | `true`                                |
| `api.livenessProbe.initialDelaySeconds`                              | Delay before liveness probe is initiated                                                                                      | `30`                                  |
| `api.livenessProbe.periodSeconds`                                    | How often to perform the probe                                                                                                | `30`                                  |
| `api.livenessProbe.timeoutSeconds`                                   | When the probe times out                                                                                                      | `5`                                   |
| `api.livenessProbe.failureThreshold`                                 | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `api.livenessProbe.successThreshold`                                 | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `api.readinessProbe.enabled`                                         | Turn on and off readiness probe                                                                                               | `true`                                |
| `api.readinessProbe.initialDelaySeconds`                             | Delay before readiness probe is initiated                                                                                     | `30`                                  |
| `api.readinessProbe.periodSeconds`                                   | How often to perform the probe                                                                                                | `30`                                  |
| `api.readinessProbe.timeoutSeconds`                                  | When the probe times out                                                                                                      | `5`                                   |
| `api.readinessProbe.failureThreshold`                                | Minimum consecutive successes for the probe                                                                                   | `3`                                   |
| `api.readinessProbe.successThreshold`                                | Minimum consecutive failures for the probe                                                                                    | `1`                                   |
| `api.persistentVolumeClaim.enabled`                                  | Set `api.persistentVolumeClaim.enabled` to `true` to mount a new volume for `api`                                             | `false`                               |
| `api.persistentVolumeClaim.accessModes`                              | `PersistentVolumeClaim` access modes                                                                                          | `[ReadWriteOnce]`                     |
| `api.persistentVolumeClaim.storageClassName`                         | `api` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning     | `-`                                   |
| `api.persistentVolumeClaim.storage`                                  | `PersistentVolumeClaim` size                                                                                                  | `20Gi`                                |
| `api.service.type`                                                   | `type` determines how the Service is exposed. Valid options are ExternalName, ClusterIP, NodePort, and LoadBalancer           | `ClusterIP`                           |
| `api.service.clusterIP`                                              | `clusterIP` is the IP address of the service and is usually assigned randomly by the master                                   | `nil`                                 |
| `api.service.nodePort`                                               | `nodePort` is the port on each node on which this service is exposed when type=NodePort                                       | `nil`                                 |
| `api.service.externalIPs`                                            | `externalIPs` is a list of IP addresses for which nodes in the cluster will also accept traffic for this service              | `[]`                                  |
| `api.service.externalName`                                           | `externalName` is the external reference that kubedns or equivalent will return as a CNAME record for this service            | `nil`                                 |
| `api.service.loadBalancerIP`                                         | `loadBalancerIP` when service.type is LoadBalancer. LoadBalancer will get created with the IP specified in this field         | `nil`                                 |
| `api.service.annotations`                                            | `annotations` may need to be set when service.type is LoadBalancer                                                            | `{}`                                  |
|                                                                      |                                                                                                                               |                                       |
| `ingress.enabled`                                                    | Enable ingress                                                                                                                | `false`                               |
| `ingress.host`                                                       | Ingress host                                                                                                                  | `dolphinscheduler.org`                |
| `ingress.path`                                                       | Ingress path                                                                                                                  | `/dolphinscheduler`                   |
| `ingress.tls.enabled`                                                | Enable ingress tls                                                                                                            | `false`                               |
| `ingress.tls.secretName`                                             | Ingress tls secret name                                                                                                       | `dolphinscheduler-tls`                |

