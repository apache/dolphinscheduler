# QuickStart in Kubernetes

Kubernetes deployment is DolphinScheduler deployment in a Kubernetes cluster, which can schedule massive tasks and can be used in production.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

> **Tip**: You can also try [DolphinScheduler K8S Operator](https://github.com/apache/dolphinscheduler-operator)，which is current on alpha1 stage

## Prerequisites

- [Helm](https://helm.sh/) version 3.1.0+
- [Kubernetes](https://kubernetes.io/) version 1.12+
- PV provisioner support in the underlying infrastructure

## Install DolphinScheduler

Please download the source code package `apache-dolphinscheduler-3.2.1-src.tar.gz`, download address: [download address](https://dolphinscheduler.apache.org/en-us/download)

To publish the release name `dolphinscheduler` version, please execute the following commands:

```
$ tar -zxvf apache-dolphinscheduler-3.2.1-src.tar.gz
$ cd apache-dolphinscheduler-3.2.1-src/deploy/kubernetes/dolphinscheduler
$ helm repo add bitnami https://charts.bitnami.com/bitnami
$ helm dependency update .
$ helm install dolphinscheduler . --set image.tag=3.2.1
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
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:3.2.1
# For example
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-tools:3.2.1

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
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:3.2.1
# For example
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:3.2.1

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
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:3.2.1
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
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:3.2.1
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

### How to deploy specific components separately?

Modify the `api.enabled`, `alert.enabled`, `master.enabled`, or `worker.enabled` configuration items in the `values.yaml` file.

For example, if you need to deploy worker to both CPU and GPU servers in a cluster, and the worker uses different images, you can do the following:

```bash
# Install master, api-server, alert-server, and other default components, but do not install worker
helm install dolphinscheduler . --set worker.enabled=false
# Disable the installation of other components, only install worker, use the self-built CPU image, deploy to CPU servers with the `x86` label through nodeselector, and use zookeeper as the external registry center
helm install dolphinscheduler-cpu-worker . \
     --set minio.enabled=false --set postgresql.enabled=false --set zookeeper.enabled=false \
     --set master.enabled=false  --set api.enabled=false --set alert.enabled=false \
     --set worker.enabled=true --set image.tag=latest-cpu --set worker.nodeSelector.cpu="x86" \
     --set externalRegistry.registryPluginName=zookeeper --set externalRegistry.registryServers=dolphinscheduler-zookeeper:2181
# Disable the installation of other components, only install worker, use the self-built GPU image, deploy to GPU servers with the `a100` label through nodeselector, and use zookeeper as the external registry center
helm install dolphinscheduler-gpu-worker . \
     --set minio.enabled=false --set postgresql.enabled=false --set zookeeper.enabled=false \
     --set master.enabled=false  --set api.enabled=false --set alert.enabled=false \
     --set worker.enabled=true --set image.tag=latest-gpu --set worker.nodeSelector.gpu="a100" \
     --set externalRegistry.registryPluginName=zookeeper --set externalRegistry.registryServers=dolphinscheduler-zookeeper:2181
```

> **Note**: the above steps are for reference only, and specific operations need to be adjusted according to the actual situation.

## Appendix-Configuration

Ref: [DolphinScheduler Helm Charts](https://github.com/apache/dolphinscheduler/blob/dev/deploy/kubernetes/dolphinscheduler/README.md)
