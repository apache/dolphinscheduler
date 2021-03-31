DolphinScheduler
=================

* [Introduction](#introduction)
* [Prerequisites](#prerequisites)
* [Installing the Chart](#installing-the-chart)
* [Access DolphinScheduler UI](#access-dolphinscheduler-ui)
* [Uninstalling the Chart](#uninstalling-the-chart)
* [Support Matrix](#support-matrix)
* [Configuration](#configuration)
* [FAQ](#faq)
  * [How to use MySQL as the DolphinScheduler's database instead of PostgreSQL?](#how-to-use-mysql-as-the-dolphinschedulers-database-instead-of-postgresql)
  * [How to support MySQL datasource in Datasource manage?](#how-to-support-mysql-datasource-in-datasource-manage)
  * [How to support Oracle datasource in Datasource manage?](#how-to-support-oracle-datasource-in-datasource-manage)
  * [How to support Python 2 pip and custom requirements\.txt?](#how-to-support-python-2-pip-and-custom-requirementstxt)
  * [How to support Python 3?](#how-to-support-python-3)
  * [How to support Hadoop, Spark, Flink, Hive or DataX?](#how-to-support-hadoop-spark-flink-hive-or-datax)
  * [How to support shared storage between Master, Worker and Api server?](#how-to-support-shared-storage-between-master-worker-and-api-server)
  * [How to support local file resource storage instead of HDFS and S3?](#how-to-support-local-file-resource-storage-instead-of-hdfs-and-s3)
  * [How to support S3 resource storage like MinIO?](#how-to-support-s3-resource-storage-like-minio)
  * [How to configure SkyWalking?](#how-to-configure-skywalking)

## Introduction

[DolphinScheduler](https://dolphinscheduler.apache.org) is a distributed and easy-to-expand visual DAG workflow scheduling system, dedicated to solving the complex dependencies in data processing, making the scheduling system out of the box for data processing.

This chart bootstraps a [DolphinScheduler](https://dolphinscheduler.apache.org) distributed deployment on a [Kubernetes](http://kubernetes.io) cluster using the [Helm](https://helm.sh) package manager.

## Prerequisites

- [Helm](https://helm.sh/) 3.1.0+
- [Kubernetes](https://kubernetes.io/) 1.12+
- PV provisioner support in the underlying infrastructure

## Installing the Chart

To install the chart with the release name `dolphinscheduler`:

```bash
$ git clone https://github.com/apache/incubator-dolphinscheduler.git
$ cd incubator-dolphinscheduler/docker/kubernetes/dolphinscheduler
$ helm repo add bitnami https://charts.bitnami.com/bitnami
$ helm dependency update .
$ helm install dolphinscheduler .
```

To install the chart with a namespace named `test`:

```bash
$ helm install dolphinscheduler . -n test
```

> **Tip**: If a namespace named `test` is used, the option `-n test` needs to be added to the `helm` and `kubectl` command

These commands deploy DolphinScheduler on the Kubernetes cluster in the default configuration. The [configuration](#configuration) section lists the parameters that can be configured during installation.

> **Tip**: List all releases using `helm list`

## Access DolphinScheduler UI

If `ingress.enabled` in `values.yaml` is set to `true`, you just access `http://${ingress.host}/dolphinscheduler` in browser.

> **Tip**: If there is a problem with ingress access, please contact the Kubernetes administrator and refer to the [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)

Otherwise, when `api.service.type=ClusterIP` you need to execute port-forward command like:

```bash
$ kubectl port-forward --address 0.0.0.0 svc/dolphinscheduler-api 12345:12345
$ kubectl port-forward --address 0.0.0.0 -n test svc/dolphinscheduler-api 12345:12345 # with test namespace
```

> **Tip**: If the error of `unable to do port forwarding: socat not found` appears, you need to install `socat` at first

And then access the web: http://192.168.xx.xx:12345/dolphinscheduler

Or when `api.service.type=NodePort` you need to execute the command:

```bash
NODE_IP=$(kubectl get no -n {{ .Release.Namespace }} -o jsonpath="{.items[0].status.addresses[0].address}")
NODE_PORT=$(kubectl get svc {{ template "dolphinscheduler.fullname" . }}-api -n {{ .Release.Namespace }} -o jsonpath="{.spec.ports[0].nodePort}")
echo http://$NODE_IP:$NODE_PORT/dolphinscheduler
```

And then access the web: http://$NODE_IP:$NODE_PORT/dolphinscheduler

The default username is `admin` and the default password is `dolphinscheduler123`

> **Tip**: For quick start in docker, you can create a tenant named `ds` and associate the user `admin` with the tenant `ds`

## Uninstalling the Chart

To uninstall/delete the `dolphinscheduler` deployment:

```bash
$ helm uninstall dolphinscheduler
```

The command removes all the Kubernetes components but PVC's associated with the chart and deletes the release.

To delete the PVC's associated with `dolphinscheduler`:

```bash
$ kubectl delete pvc -l app.kubernetes.io/instance=dolphinscheduler
```

> **Note**: Deleting the PVC's will delete all data as well. Please be cautious before doing it.

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
| Spark-Mesos(cluster)                                         | Not Yet      |                                       |
| Spark-Standalone(cluster)                                    | Not Yet      |                                       |
| Spark-Kubernetes(cluster)                                    | Not Yet      |                                       |
| Flink-Local(local>=1.11)                                     | Not Yet      | Generic CLI mode is not yet supported |
| Flink-YARN(yarn-cluster)                                     | Indirect Yes | Refer to FAQ                          |
| Flink-YARN(yarn-session/yarn-per-job/yarn-application>=1.11) | Not Yet      | Generic CLI mode is not yet supported |
| Flink-Mesos(default)                                         | Not Yet      |                                       |
| Flink-Mesos(remote>=1.11)                                    | Not Yet      | Generic CLI mode is not yet supported |
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

## Configuration

The configuration file is `values.yaml`, and the following tables lists the configurable parameters of the DolphinScheduler chart and their default values.

| Parameter                                                                         | Description                                                                                                                    | Default                                               |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------- |
| `timezone`                                                                        | World time and date for cities in all time zones                                                                               | `Asia/Shanghai`                                       |
|                                                                                   |                                                                                                                                |                                                       |
| `image.repository`                                                                | Docker image repository for the DolphinScheduler                                                                               | `apache/dolphinscheduler`                             |
| `image.tag`                                                                       | Docker image version for the DolphinScheduler                                                                                  | `latest`                                              |
| `image.pullPolicy`                                                                | Image pull policy. One of Always, Never, IfNotPresent                                                                          | `IfNotPresent`                                        |
| `image.pullSecret`                                                                | Image pull secret. An optional reference to secret in the same namespace to use for pulling any of the images                  | `nil`                                                 |
|                                                                                   |                                                                                                                                |                                                       |
| `postgresql.enabled`                                                              | If not exists external PostgreSQL, by default, the DolphinScheduler will use a internal PostgreSQL                             | `true`                                                |
| `postgresql.postgresqlUsername`                                                   | The username for internal PostgreSQL                                                                                           | `root`                                                |
| `postgresql.postgresqlPassword`                                                   | The password for internal PostgreSQL                                                                                           | `root`                                                |
| `postgresql.postgresqlDatabase`                                                   | The database for internal PostgreSQL                                                                                           | `dolphinscheduler`                                    |
| `postgresql.persistence.enabled`                                                  | Set `postgresql.persistence.enabled` to `true` to mount a new volume for internal PostgreSQL                                   | `false`                                               |
| `postgresql.persistence.size`                                                     | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
| `postgresql.persistence.storageClass`                                             | PostgreSQL data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning      | `-`                                                   |
| `externalDatabase.type`                                                           | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database type will use it       | `postgresql`                                          |
| `externalDatabase.driver`                                                         | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database driver will use it     | `org.postgresql.Driver`                               |
| `externalDatabase.host`                                                           | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database host will use it       | `localhost`                                           |
| `externalDatabase.port`                                                           | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database port will use it       | `5432`                                                |
| `externalDatabase.username`                                                       | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database username will use it   | `root`                                                |
| `externalDatabase.password`                                                       | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database password will use it   | `root`                                                |
| `externalDatabase.database`                                                       | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database database will use it   | `dolphinscheduler`                                    |
| `externalDatabase.params`                                                         | If exists external PostgreSQL, and set `postgresql.enabled` value to false. DolphinScheduler's database params will use it     | `characterEncoding=utf8`                              |
|                                                                                   |                                                                                                                                |                                                       |
| `zookeeper.enabled`                                                               | If not exists external Zookeeper, by default, the DolphinScheduler will use a internal Zookeeper                               | `true`                                                |
| `zookeeper.fourlwCommandsWhitelist`                                               | A list of comma separated Four Letter Words commands to use                                                                    | `srvr,ruok,wchs,cons`                                 |
| `zookeeper.persistence.enabled`                                                   | Set `zookeeper.persistence.enabled` to `true` to mount a new volume for internal Zookeeper                                     | `false`                                               |
| `zookeeper.persistence.size`                                                      | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
| `zookeeper.persistence.storageClass`                                              | Zookeeper data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning       | `-`                                                   |
| `zookeeper.zookeeperRoot`                                                         | Specify dolphinscheduler root directory in Zookeeper                                                                           | `/dolphinscheduler`                                   |
| `externalZookeeper.zookeeperQuorum`                                               | If exists external Zookeeper, and set `zookeeper.enabled` value to false. Specify Zookeeper quorum                             | `127.0.0.1:2181`                                      |
| `externalZookeeper.zookeeperRoot`                                                 | If exists external Zookeeper, and set `zookeeper.enabled` value to false. Specify dolphinscheduler root directory in Zookeeper | `/dolphinscheduler`                                   |
|                                                                                   |                                                                                                                                |                                                       |
| `common.configmap.DOLPHINSCHEDULER_OPTS`                                          | The jvm options for dolphinscheduler, suitable for all servers                                                                 | `""`                                                  |
| `common.configmap.DATA_BASEDIR_PATH`                                              | User data directory path, self configuration, please make sure the directory exists and have read write permissions            | `/tmp/dolphinscheduler`                               |
| `common.configmap.RESOURCE_STORAGE_TYPE`                                          | Resource storage type: HDFS, S3, NONE                                                                                          | `HDFS`                                                |
| `common.configmap.RESOURCE_UPLOAD_PATH`                                           | Resource store on HDFS/S3 path, please make sure the directory exists on hdfs and have read write permissions                  | `/dolphinscheduler`                                   |
| `common.configmap.FS_DEFAULT_FS`                                                  | Resource storage file system like `file:///`, `hdfs://mycluster:8020` or `s3a://dolphinscheduler`                              | `file:///`                                            |
| `common.configmap.FS_S3A_ENDPOINT`                                                | S3 endpoint when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                       | `s3.xxx.amazonaws.com`                                |
| `common.configmap.FS_S3A_ACCESS_KEY`                                              | S3 access key when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                     | `xxxxxxx`                                             |
| `common.configmap.FS_S3A_SECRET_KEY`                                              | S3 secret key when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                     | `xxxxxxx`                                             |
| `common.configmap.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE`                   | Whether to startup kerberos                                                                                                    | `false`                                               |
| `common.configmap.JAVA_SECURITY_KRB5_CONF_PATH`                                   | The java.security.krb5.conf path                                                                                               | `/opt/krb5.conf`                                      |
| `common.configmap.LOGIN_USER_KEYTAB_USERNAME`                                     | The login user from keytab username                                                                                            | `hdfs@HADOOP.COM`                                     |
| `common.configmap.LOGIN_USER_KEYTAB_PATH`                                         | The login user from keytab path                                                                                                | `/opt/hdfs.keytab`                                    |
| `common.configmap.KERBEROS_EXPIRE_TIME`                                           | The kerberos expire time, the unit is hour                                                                                     | `2`                                                   |
| `common.configmap.HDFS_ROOT_USER`                                                 | The HDFS root user who must have the permission to create directories under the HDFS root path                                 | `hdfs`                                                |
| `common.configmap.YARN_RESOURCEMANAGER_HA_RM_IDS`                                 | If resourcemanager HA is enabled, please set the HA IPs                                                                        | `nil`                                                 |
| `common.configmap.YARN_APPLICATION_STATUS_ADDRESS`                                | If resourcemanager is single, you only need to replace ds1 to actual resourcemanager hostname, otherwise keep default          | `http://ds1:8088/ws/v1/cluster/apps/%s`               |
| `common.configmap.SKYWALKING_ENABLE`                                              | Set whether to enable skywalking                                                                                               | `false`                                               |
| `common.configmap.SW_AGENT_COLLECTOR_BACKEND_SERVICES`                            | Set agent collector backend services for skywalking                                                                            | `127.0.0.1:11800`                                     |
| `common.configmap.SW_GRPC_LOG_SERVER_HOST`                                        | Set grpc log server host for skywalking                                                                                        | `127.0.0.1`                                           |
| `common.configmap.SW_GRPC_LOG_SERVER_PORT`                                        | Set grpc log server port for skywalking                                                                                        | `11800`                                               |
| `common.configmap.HADOOP_HOME`                                                    | Set `HADOOP_HOME` for DolphinScheduler's task environment                                                                      | `/opt/soft/hadoop`                                    |
| `common.configmap.HADOOP_CONF_DIR`                                                | Set `HADOOP_CONF_DIR` for DolphinScheduler's task environment                                                                  | `/opt/soft/hadoop/etc/hadoop`                         |
| `common.configmap.SPARK_HOME1`                                                    | Set `SPARK_HOME1` for DolphinScheduler's task environment                                                                      | `/opt/soft/spark1`                                    |
| `common.configmap.SPARK_HOME2`                                                    | Set `SPARK_HOME2` for DolphinScheduler's task environment                                                                      | `/opt/soft/spark2`                                    |
| `common.configmap.PYTHON_HOME`                                                    | Set `PYTHON_HOME` for DolphinScheduler's task environment                                                                      | `/usr/bin/python`                                     |
| `common.configmap.JAVA_HOME`                                                      | Set `JAVA_HOME` for DolphinScheduler's task environment                                                                        | `/usr/local/openjdk-8`                                |
| `common.configmap.HIVE_HOME`                                                      | Set `HIVE_HOME` for DolphinScheduler's task environment                                                                        | `/opt/soft/hive`                                      |
| `common.configmap.FLINK_HOME`                                                     | Set `FLINK_HOME` for DolphinScheduler's task environment                                                                       | `/opt/soft/flink`                                     |
| `common.configmap.DATAX_HOME`                                                     | Set `DATAX_HOME` for DolphinScheduler's task environment                                                                       | `/opt/soft/datax`                                     |
| `common.sharedStoragePersistence.enabled`                                         | Set `common.sharedStoragePersistence.enabled` to `true` to mount a shared storage volume for Hadoop, Spark binary and etc      | `false`                                               |
| `common.sharedStoragePersistence.mountPath`                                       | The mount path for the shared storage volume                                                                                   | `/opt/soft`                                           |
| `common.sharedStoragePersistence.accessModes`                                     | `PersistentVolumeClaim` access modes, must be `ReadWriteMany`                                                                  | `[ReadWriteMany]`                                     |
| `common.sharedStoragePersistence.storageClassName`                                | Shared Storage persistent volume storage class, must support the access mode: ReadWriteMany                                    | `-`                                                   |
| `common.sharedStoragePersistence.storage`                                         | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
| `common.fsFileResourcePersistence.enabled`                                        | Set `common.fsFileResourcePersistence.enabled` to `true` to mount a new file resource volume for `api` and `worker`            | `false`                                               |
| `common.fsFileResourcePersistence.accessModes`                                    | `PersistentVolumeClaim` access modes, must be `ReadWriteMany`                                                                  | `[ReadWriteMany]`                                     |
| `common.fsFileResourcePersistence.storageClassName`                               | Resource persistent volume storage class, must support the access mode: ReadWriteMany                                          | `-`                                                   |
| `common.fsFileResourcePersistence.storage`                                        | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
|                                                                                   |                                                                                                                                |                                                       |
| `master.podManagementPolicy`                                                      | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down  | `Parallel`                                            |
| `master.replicas`                                                                 | Replicas is the desired number of replicas of the given Template                                                               | `3`                                                   |
| `master.annotations`                                                              | The `annotations` for master server                                                                                            | `{}`                                                  |
| `master.affinity`                                                                 | If specified, the pod's scheduling constraints                                                                                 | `{}`                                                  |
| `master.nodeSelector`                                                             | NodeSelector is a selector which must be true for the pod to fit on a node                                                     | `{}`                                                  |
| `master.tolerations`                                                              | If specified, the pod's tolerations                                                                                            | `{}`                                                  |
| `master.resources`                                                                | The `resource` limit and request config for master server                                                                      | `{}`                                                  |
| `master.configmap.MASTER_SERVER_OPTS`                                             | The jvm options for master server                                                                                              | `-Xms1g -Xmx1g -Xmn512m`                              |
| `master.configmap.MASTER_EXEC_THREADS`                                            | Master execute thread number                                                                                                   | `100`                                                 |
| `master.configmap.MASTER_EXEC_TASK_NUM`                                           | Master execute task number in parallel                                                                                         | `20`                                                  |
| `master.configmap.MASTER_DISPATCH_TASK_NUM`                                       | Master dispatch task number                                                                                                    | `3`                                                   |
| `master.configmap.MASTER_HOST_SELECTOR`                                           | Master host selector to select a suitable worker, optional values include Random, RoundRobin, LowerWeight                      | `LowerWeight`                                         |
| `master.configmap.MASTER_HEARTBEAT_INTERVAL`                                      | Master heartbeat interval                                                                                                      | `10`                                                  |
| `master.configmap.MASTER_TASK_COMMIT_RETRYTIMES`                                  | Master commit task retry times                                                                                                 | `5`                                                   |
| `master.configmap.MASTER_TASK_COMMIT_INTERVAL`                                    | Master commit task interval                                                                                                    | `1000`                                                |
| `master.configmap.MASTER_MAX_CPULOAD_AVG`                                         | Only less than cpu avg load, master server can work. default value : the number of cpu cores * 2                               | `-1`                                                  |
| `master.configmap.MASTER_RESERVED_MEMORY`                                         | Only larger than reserved memory, master server can work. default value : physical memory * 1/10, unit is G                    | `0.3`                                                 |
| `master.livenessProbe.enabled`                                                    | Turn on and off liveness probe                                                                                                 | `true`                                                |
| `master.livenessProbe.initialDelaySeconds`                                        | Delay before liveness probe is initiated                                                                                       | `30`                                                  |
| `master.livenessProbe.periodSeconds`                                              | How often to perform the probe                                                                                                 | `30`                                                  |
| `master.livenessProbe.timeoutSeconds`                                             | When the probe times out                                                                                                       | `5`                                                   |
| `master.livenessProbe.failureThreshold`                                           | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `master.livenessProbe.successThreshold`                                           | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `master.readinessProbe.enabled`                                                   | Turn on and off readiness probe                                                                                                | `true`                                                |
| `master.readinessProbe.initialDelaySeconds`                                       | Delay before readiness probe is initiated                                                                                      | `30`                                                  |
| `master.readinessProbe.periodSeconds`                                             | How often to perform the probe                                                                                                 | `30`                                                  |
| `master.readinessProbe.timeoutSeconds`                                            | When the probe times out                                                                                                       | `5`                                                   |
| `master.readinessProbe.failureThreshold`                                          | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `master.readinessProbe.successThreshold`                                          | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `master.persistentVolumeClaim.enabled`                                            | Set `master.persistentVolumeClaim.enabled` to `true` to mount a new volume for `master`                                        | `false`                                               |
| `master.persistentVolumeClaim.accessModes`                                        | `PersistentVolumeClaim` access modes                                                                                           | `[ReadWriteOnce]`                                     |
| `master.persistentVolumeClaim.storageClassName`                                   | `Master` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning   | `-`                                                   |
| `master.persistentVolumeClaim.storage`                                            | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
|                                                                                   |                                                                                                                                |                                                       |
| `worker.podManagementPolicy`                                                      | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down  | `Parallel`                                            |
| `worker.replicas`                                                                 | Replicas is the desired number of replicas of the given Template                                                               | `3`                                                   |
| `worker.annotations`                                                              | The `annotations` for worker server                                                                                            | `{}`                                                  |
| `worker.affinity`                                                                 | If specified, the pod's scheduling constraints                                                                                 | `{}`                                                  |
| `worker.nodeSelector`                                                             | NodeSelector is a selector which must be true for the pod to fit on a node                                                     | `{}`                                                  |
| `worker.tolerations`                                                              | If specified, the pod's tolerations                                                                                            | `{}`                                                  |
| `worker.resources`                                                                | The `resource` limit and request config for worker server                                                                      | `{}`                                                  |
| `worker.configmap.LOGGER_SERVER_OPTS`                                             | The jvm options for logger server                                                                                              | `-Xms512m -Xmx512m -Xmn256m`                          |
| `worker.configmap.WORKER_SERVER_OPTS`                                             | The jvm options for worker server                                                                                              | `-Xms1g -Xmx1g -Xmn512m`                              |
| `worker.configmap.WORKER_EXEC_THREADS`                                            | Worker execute thread number                                                                                                   | `100`                                                 |
| `worker.configmap.WORKER_HEARTBEAT_INTERVAL`                                      | Worker heartbeat interval                                                                                                      | `10`                                                  |
| `worker.configmap.WORKER_MAX_CPULOAD_AVG`                                         | Only less than cpu avg load, worker server can work. default value : the number of cpu cores * 2                               | `-1`                                                  |
| `worker.configmap.WORKER_RESERVED_MEMORY`                                         | Only larger than reserved memory, worker server can work. default value : physical memory * 1/10, unit is G                    | `0.3`                                                 |
| `worker.configmap.WORKER_GROUPS`                                                  | Worker groups                                                                                                                  | `default`                                             |
| `worker.livenessProbe.enabled`                                                    | Turn on and off liveness probe                                                                                                 | `true`                                                |
| `worker.livenessProbe.initialDelaySeconds`                                        | Delay before liveness probe is initiated                                                                                       | `30`                                                  |
| `worker.livenessProbe.periodSeconds`                                              | How often to perform the probe                                                                                                 | `30`                                                  |
| `worker.livenessProbe.timeoutSeconds`                                             | When the probe times out                                                                                                       | `5`                                                   |
| `worker.livenessProbe.failureThreshold`                                           | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `worker.livenessProbe.successThreshold`                                           | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `worker.readinessProbe.enabled`                                                   | Turn on and off readiness probe                                                                                                | `true`                                                |
| `worker.readinessProbe.initialDelaySeconds`                                       | Delay before readiness probe is initiated                                                                                      | `30`                                                  |
| `worker.readinessProbe.periodSeconds`                                             | How often to perform the probe                                                                                                 | `30`                                                  |
| `worker.readinessProbe.timeoutSeconds`                                            | When the probe times out                                                                                                       | `5`                                                   |
| `worker.readinessProbe.failureThreshold`                                          | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `worker.readinessProbe.successThreshold`                                          | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `worker.persistentVolumeClaim.enabled`                                            | Set `worker.persistentVolumeClaim.enabled` to `true` to enable `persistentVolumeClaim` for `worker`                            | `false`                                               |
| `worker.persistentVolumeClaim.dataPersistentVolume.enabled`                       | Set `worker.persistentVolumeClaim.dataPersistentVolume.enabled` to `true` to mount a data volume for `worker`                  | `false`                                               |
| `worker.persistentVolumeClaim.dataPersistentVolume.accessModes`                   | `PersistentVolumeClaim` access modes                                                                                           | `[ReadWriteOnce]`                                     |
| `worker.persistentVolumeClaim.dataPersistentVolume.storageClassName`              | `Worker` data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning        | `-`                                                   |
| `worker.persistentVolumeClaim.dataPersistentVolume.storage`                       | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
| `worker.persistentVolumeClaim.logsPersistentVolume.enabled`                       | Set `worker.persistentVolumeClaim.logsPersistentVolume.enabled` to `true` to mount a logs volume for `worker`                  | `false`                                               |
| `worker.persistentVolumeClaim.logsPersistentVolume.accessModes`                   | `PersistentVolumeClaim` access modes                                                                                           | `[ReadWriteOnce]`                                     |
| `worker.persistentVolumeClaim.logsPersistentVolume.storageClassName`              | `Worker` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning   | `-`                                                   |
| `worker.persistentVolumeClaim.logsPersistentVolume.storage`                       | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
|                                                                                   |                                                                                                                                |                                                       |
| `alert.replicas`                                                                  | Replicas is the desired number of replicas of the given Template                                                               | `1`                                                   |
| `alert.strategy.type`                                                             | Type of deployment. Can be "Recreate" or "RollingUpdate"                                                                       | `RollingUpdate`                                       |
| `alert.strategy.rollingUpdate.maxSurge`                                           | The maximum number of pods that can be scheduled above the desired number of pods                                              | `25%`                                                 |
| `alert.strategy.rollingUpdate.maxUnavailable`                                     | The maximum number of pods that can be unavailable during the update                                                           | `25%`                                                 |
| `alert.annotations`                                                               | The `annotations` for alert server                                                                                             | `{}`                                                  |
| `alert.affinity`                                                                  | If specified, the pod's scheduling constraints                                                                                 | `{}`                                                  |
| `alert.nodeSelector`                                                              | NodeSelector is a selector which must be true for the pod to fit on a node                                                     | `{}`                                                  |
| `alert.tolerations`                                                               | If specified, the pod's tolerations                                                                                            | `{}`                                                  |
| `alert.resources`                                                                 | The `resource` limit and request config for alert server                                                                       | `{}`                                                  |
| `alert.configmap.ALERT_SERVER_OPTS`                                               | The jvm options for alert server                                                                                               | `-Xms512m -Xmx512m -Xmn256m`                          |
| `alert.configmap.XLS_FILE_PATH`                                                   | XLS file path                                                                                                                  | `/tmp/xls`                                            |
| `alert.configmap.MAIL_SERVER_HOST`                                                | Mail `SERVER HOST `                                                                                                            | `nil`                                                 |
| `alert.configmap.MAIL_SERVER_PORT`                                                | Mail `SERVER PORT`                                                                                                             | `nil`                                                 |
| `alert.configmap.MAIL_SENDER`                                                     | Mail `SENDER`                                                                                                                  | `nil`                                                 |
| `alert.configmap.MAIL_USER`                                                       | Mail `USER`                                                                                                                    | `nil`                                                 |
| `alert.configmap.MAIL_PASSWD`                                                     | Mail `PASSWORD`                                                                                                                | `nil`                                                 |
| `alert.configmap.MAIL_SMTP_STARTTLS_ENABLE`                                       | Mail `SMTP STARTTLS` enable                                                                                                    | `false`                                               |
| `alert.configmap.MAIL_SMTP_SSL_ENABLE`                                            | Mail `SMTP SSL` enable                                                                                                         | `false`                                               |
| `alert.configmap.MAIL_SMTP_SSL_TRUST`                                             | Mail `SMTP SSL TRUST`                                                                                                          | `nil`                                                 |
| `alert.configmap.ENTERPRISE_WECHAT_ENABLE`                                        | `Enterprise Wechat` enable                                                                                                     | `false`                                               |
| `alert.configmap.ENTERPRISE_WECHAT_CORP_ID`                                       | `Enterprise Wechat` corp id                                                                                                    | `nil`                                                 |
| `alert.configmap.ENTERPRISE_WECHAT_SECRET`                                        | `Enterprise Wechat` secret                                                                                                     | `nil`                                                 |
| `alert.configmap.ENTERPRISE_WECHAT_AGENT_ID`                                      | `Enterprise Wechat` agent id                                                                                                   | `nil`                                                 |
| `alert.configmap.ENTERPRISE_WECHAT_USERS`                                         | `Enterprise Wechat` users                                                                                                      | `nil`                                                 |
| `alert.livenessProbe.enabled`                                                     | Turn on and off liveness probe                                                                                                 | `true`                                                |
| `alert.livenessProbe.initialDelaySeconds`                                         | Delay before liveness probe is initiated                                                                                       | `30`                                                  |
| `alert.livenessProbe.periodSeconds`                                               | How often to perform the probe                                                                                                 | `30`                                                  |
| `alert.livenessProbe.timeoutSeconds`                                              | When the probe times out                                                                                                       | `5`                                                   |
| `alert.livenessProbe.failureThreshold`                                            | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `alert.livenessProbe.successThreshold`                                            | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `alert.readinessProbe.enabled`                                                    | Turn on and off readiness probe                                                                                                | `true`                                                |
| `alert.readinessProbe.initialDelaySeconds`                                        | Delay before readiness probe is initiated                                                                                      | `30`                                                  |
| `alert.readinessProbe.periodSeconds`                                              | How often to perform the probe                                                                                                 | `30`                                                  |
| `alert.readinessProbe.timeoutSeconds`                                             | When the probe times out                                                                                                       | `5`                                                   |
| `alert.readinessProbe.failureThreshold`                                           | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `alert.readinessProbe.successThreshold`                                           | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `alert.persistentVolumeClaim.enabled`                                             | Set `alert.persistentVolumeClaim.enabled` to `true` to mount a new volume for `alert`                                          | `false`                                               |
| `alert.persistentVolumeClaim.accessModes`                                         | `PersistentVolumeClaim` access modes                                                                                           | `[ReadWriteOnce]`                                     |
| `alert.persistentVolumeClaim.storageClassName`                                    | `Alert` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning    | `-`                                                   |
| `alert.persistentVolumeClaim.storage`                                             | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
|                                                                                   |                                                                                                                                |                                                       |
| `api.replicas`                                                                    | Replicas is the desired number of replicas of the given Template                                                               | `1`                                                   |
| `api.strategy.type`                                                               | Type of deployment. Can be "Recreate" or "RollingUpdate"                                                                       | `RollingUpdate`                                       |
| `api.strategy.rollingUpdate.maxSurge`                                             | The maximum number of pods that can be scheduled above the desired number of pods                                              | `25%`                                                 |
| `api.strategy.rollingUpdate.maxUnavailable`                                       | The maximum number of pods that can be unavailable during the update                                                           | `25%`                                                 |
| `api.annotations`                                                                 | The `annotations` for api server                                                                                               | `{}`                                                  |
| `api.affinity`                                                                    | If specified, the pod's scheduling constraints                                                                                 | `{}`                                                  |
| `api.nodeSelector`                                                                | NodeSelector is a selector which must be true for the pod to fit on a node                                                     | `{}`                                                  |
| `api.tolerations`                                                                 | If specified, the pod's tolerations                                                                                            | `{}`                                                  |
| `api.resources`                                                                   | The `resource` limit and request config for api server                                                                         | `{}`                                                  |
| `api.configmap.API_SERVER_OPTS`                                                   | The jvm options for api server                                                                                                 | `-Xms512m -Xmx512m -Xmn256m`                          |
| `api.livenessProbe.enabled`                                                       | Turn on and off liveness probe                                                                                                 | `true`                                                |
| `api.livenessProbe.initialDelaySeconds`                                           | Delay before liveness probe is initiated                                                                                       | `30`                                                  |
| `api.livenessProbe.periodSeconds`                                                 | How often to perform the probe                                                                                                 | `30`                                                  |
| `api.livenessProbe.timeoutSeconds`                                                | When the probe times out                                                                                                       | `5`                                                   |
| `api.livenessProbe.failureThreshold`                                              | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `api.livenessProbe.successThreshold`                                              | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `api.readinessProbe.enabled`                                                      | Turn on and off readiness probe                                                                                                | `true`                                                |
| `api.readinessProbe.initialDelaySeconds`                                          | Delay before readiness probe is initiated                                                                                      | `30`                                                  |
| `api.readinessProbe.periodSeconds`                                                | How often to perform the probe                                                                                                 | `30`                                                  |
| `api.readinessProbe.timeoutSeconds`                                               | When the probe times out                                                                                                       | `5`                                                   |
| `api.readinessProbe.failureThreshold`                                             | Minimum consecutive successes for the probe                                                                                    | `3`                                                   |
| `api.readinessProbe.successThreshold`                                             | Minimum consecutive failures for the probe                                                                                     | `1`                                                   |
| `api.persistentVolumeClaim.enabled`                                               | Set `api.persistentVolumeClaim.enabled` to `true` to mount a new volume for `api`                                              | `false`                                               |
| `api.persistentVolumeClaim.accessModes`                                           | `PersistentVolumeClaim` access modes                                                                                           | `[ReadWriteOnce]`                                     |
| `api.persistentVolumeClaim.storageClassName`                                      | `api` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning      | `-`                                                   |
| `api.persistentVolumeClaim.storage`                                               | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
| `api.service.type`                                                                | `type` determines how the Service is exposed. Valid options are ExternalName, ClusterIP, NodePort, and LoadBalancer            | `ClusterIP`                                           |
| `api.service.clusterIP`                                                           | `clusterIP` is the IP address of the service and is usually assigned randomly by the master                                    | `nil`                                                 |
| `api.service.nodePort`                                                            | `nodePort` is the port on each node on which this service is exposed when type=NodePort                                        | `nil`                                                 |
| `api.service.externalIPs`                                                         | `externalIPs` is a list of IP addresses for which nodes in the cluster will also accept traffic for this service               | `[]`                                                  |
| `api.service.externalName`                                                        | `externalName` is the external reference that kubedns or equivalent will return as a CNAME record for this service             | `nil`                                                 |
| `api.service.loadBalancerIP`                                                      | `loadBalancerIP` when service.type is LoadBalancer. LoadBalancer will get created with the IP specified in this field          | `nil`                                                 |
| `api.service.annotations`                                                         | `annotations` may need to be set when service.type is LoadBalancer                                                             | `{}`                                                  |
|                                                                                   |                                                                                                                                |                                                       |
| `ingress.enabled`                                                                 | Enable ingress                                                                                                                 | `false`                                               |
| `ingress.host`                                                                    | Ingress host                                                                                                                   | `dolphinscheduler.org`                                |
| `ingress.path`                                                                    | Ingress path                                                                                                                   | `/dolphinscheduler`                                   |
| `ingress.tls.enabled`                                                             | Enable ingress tls                                                                                                             | `false`                                               |
| `ingress.tls.secretName`                                                          | Ingress tls secret name                                                                                                        | `dolphinscheduler-tls`                                |

## FAQ

### How to use MySQL as the DolphinScheduler's database instead of PostgreSQL?

> Because of the commercial license, we cannot directly use the driver of MySQL.
>
> If you want to use MySQL, you can build a new image based on the `apache/dolphinscheduler` image as follows.

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

4. Push the docker image `apache/dolphinscheduler:mysql-driver` to a docker registry

5. Modify image `repository` and update `tag` to `mysql-driver` in `values.yaml`

6. Modify postgresql `enabled` to `false` in `values.yaml`

7. Modify externalDatabase (especially modify `host`, `username` and `password`) in `values.yaml`:

```yaml
externalDatabase:
  type: "mysql"
  driver: "com.mysql.jdbc.Driver"
  host: "localhost"
  port: "3306"
  username: "root"
  password: "root"
  database: "dolphinscheduler"
  params: "useUnicode=true&characterEncoding=UTF-8"
```

8. Run a DolphinScheduler release in Kubernetes (See **Installing the Chart**)

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

4. Push the docker image `apache/dolphinscheduler:mysql-driver` to a docker registry

5. Modify image `repository` and update `tag` to `mysql-driver` in `values.yaml`

6. Run a DolphinScheduler release in Kubernetes (See **Installing the Chart**)

7. Add a MySQL datasource in `Datasource manage`

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

4. Push the docker image `apache/dolphinscheduler:oracle-driver` to a docker registry

5. Modify image `repository` and update `tag` to `oracle-driver` in `values.yaml`

6. Run a DolphinScheduler release in Kubernetes (See **Installing the Chart**)

7. Add a Oracle datasource in `Datasource manage`

### How to support Python 2 pip and custom requirements.txt?

1. Create a new `Dockerfile` to install pip:

```
FROM apache/dolphinscheduler:latest
COPY requirements.txt /tmp
RUN apt-get update && \
    apt-get install -y --no-install-recommends python-pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    rm -rf /var/lib/apt/lists/*
```

The command will install the default **pip 18.1**. If you upgrade the pip, just add one line

```
    pip install --no-cache-dir -U pip && \
```

2. Build a new docker image including pip:

```
docker build -t apache/dolphinscheduler:pip .
```

3. Push the docker image `apache/dolphinscheduler:pip` to a docker registry

4. Modify image `repository` and update `tag` to `pip` in `values.yaml`

5. Run a DolphinScheduler release in Kubernetes (See **Installing the Chart**)

6. Verify pip under a new Python task

### How to support Python 3?

1. Create a new `Dockerfile` to install Python 3:

```
FROM apache/dolphinscheduler:latest
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 && \
    rm -rf /var/lib/apt/lists/*
```

The command will install the default **Python 3.7.3**. If you also want to install **pip3**, just replace `python3` with `python3-pip` like

```
    apt-get install -y --no-install-recommends python3-pip && \
```

2. Build a new docker image including Python 3:

```
docker build -t apache/dolphinscheduler:python3 .
```

3. Push the docker image `apache/dolphinscheduler:python3` to a docker registry

4. Modify image `repository` and update `tag` to `python3` in `values.yaml`

5. Modify `PYTHON_HOME` to `/usr/bin/python3` in `values.yaml`

6. Run a DolphinScheduler release in Kubernetes (See **Installing the Chart**)

7. Verify Python 3 under a new Python task

### How to support Hadoop, Spark, Flink, Hive or DataX?

Take Spark 2.4.7 as an example:

1. Download the Spark 2.4.7 release binary `spark-2.4.7-bin-hadoop2.7.tgz`

2. Ensure that `common.sharedStoragePersistence.enabled` is turned on

3. Run a DolphinScheduler release in Kubernetes (See **Installing the Chart**)

4. Copy the Spark 2.4.7 release binary into Docker container

```bash
kubectl cp spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft
kubectl cp -n test spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft # with test namespace
```

Because the volume `sharedStoragePersistence` is mounted on `/opt/soft`, all files in `/opt/soft` will not be lost

5. Attach the container and ensure that `SPARK_HOME2` exists

```bash
kubectl exec -it dolphinscheduler-worker-0 bash
kubectl exec -n test -it dolphinscheduler-worker-0 bash # with test namespace
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # or just mv
$SPARK_HOME2/bin/spark-submit --version
```

The last command will print Spark version if everything goes well

6. Verify Spark under a Shell task

```
$SPARK_HOME2/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME2/examples/jars/spark-examples_2.11-2.4.7.jar
```

Check whether the task log contains the output like `Pi is roughly 3.146015`

7. Verify Spark under a Spark task

The file `spark-examples_2.11-2.4.7.jar` needs to be uploaded to the resources first, and then create a Spark task with:

- Spark Version: `SPARK2`
- Main Class: `org.apache.spark.examples.SparkPi`
- Main Package: `spark-examples_2.11-2.4.7.jar`
- Deploy Mode: `local`

Similarly, check whether the task log contains the output like `Pi is roughly 3.146015`

8. Verify Spark on YARN

Spark on YARN (Deploy Mode is `cluster` or `client`) requires Hadoop support. Similar to Spark support, the operation of supporting Hadoop is almost the same as the previous steps

Ensure that `$HADOOP_HOME` and `$HADOOP_CONF_DIR` exists

### How to support shared storage between Master, Worker and Api server?

For example, Master, Worker and Api server may use Hadoop at the same time

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

`storageClassName` and `storage` need to be modified to actual values

> **Note**: `storageClassName` must support the access mode: `ReadWriteMany`

2. Put the Hadoop into the nfs

3. Ensure that `$HADOOP_HOME` and `$HADOOP_CONF_DIR` are correct

### How to support local file resource storage instead of HDFS and S3?

Modify the following configurations in `values.yaml`

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

`storageClassName` and `storage` need to be modified to actual values

> **Note**: `storageClassName` must support the access mode: `ReadWriteMany`

### How to support S3 resource storage like MinIO?

Take MinIO as an example: Modify the following configurations in `values.yaml`

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

`BUCKET_NAME`, `MINIO_IP`, `MINIO_ACCESS_KEY` and `MINIO_SECRET_KEY` need to be modified to actual values

> **Note**: `MINIO_IP` can only use IP instead of domain name, because DolphinScheduler currently doesn't support S3 path style access

### How to configure SkyWalking?

Modify SKYWALKING configurations in `values.yaml`:

```yaml
common:
  configmap:
    SKYWALKING_ENABLE: "true"
    SW_AGENT_COLLECTOR_BACKEND_SERVICES: "127.0.0.1:11800"
    SW_GRPC_LOG_SERVER_HOST: "127.0.0.1"
    SW_GRPC_LOG_SERVER_PORT: "11800"
```

For more information please refer to the [incubator-dolphinscheduler](https://github.com/apache/incubator-dolphinscheduler.git) documentation.
