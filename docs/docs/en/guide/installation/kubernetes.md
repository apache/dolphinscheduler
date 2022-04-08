# QuickStart in Kubernetes

Kubernetes deployment is DolphinScheduler deployment in a Kubernetes cluster, which can schedule massive tasks and can be used in production.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Prerequisites

 - [Helm](https://helm.sh/) version 3.1.0+
 - [Kubernetes](https://kubernetes.io/) version 1.12+
 - PV provisioner support in the underlying infrastructure

## Install DolphinScheduler

Please download the source code package `apache-dolphinscheduler-1.3.8-src.tar.gz`, download address: [download address](/en-us/download/download.html)

To publish the release name `dolphinscheduler` version, please execute the following commands:

```
$ tar -zxvf apache-dolphinscheduler-1.3.8-src.tar.gz
$ cd apache-dolphinscheduler-1.3.8-src/docker/kubernetes/dolphinscheduler
$ helm repo add bitnami https://charts.bitnami.com/bitnami
$ helm dependency update .
$ helm install dolphinscheduler . --set image.tag=1.3.8
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

Access the web: `http://localhost:12345/dolphinscheduler` (Modify the IP address if needed).

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

## Configuration

The configuration file is `values.yaml`, and the [Appendix-Configuration](#appendix-configuration) tables lists the configurable parameters of the DolphinScheduler and their default values.

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



## Appendix-Configuration

| Parameter                                                                         | Description                                                                                                                    | Default                                               |
| --------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------- |
| `timezone`                                                                        | World time and date for cities in all time zones                                                                               | `Asia/Shanghai`                                       |
|                                                                                   |                                                                                                                                |                                                       |
| `image.repository`                                                                | Docker image repository for the DolphinScheduler                                                                               | `apache/dolphinscheduler`                             |
| `image.tag`                                                                       | Docker image version for the DolphinScheduler                                                                                  | `latest`                                              |
| `image.pullPolicy`                                                                | Image pull policy. Options: Always, Never, IfNotPresent                                                                          | `IfNotPresent`                                        |
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
| `zookeeper.enabled`                                                               | If not exists external ZooKeeper, by default, the DolphinScheduler will use a internal ZooKeeper                               | `true`                                                |
| `zookeeper.fourlwCommandsWhitelist`                                               | A list of comma separated Four Letter Words commands to use                                                                    | `srvr,ruok,wchs,cons`                                 |
| `zookeeper.persistence.enabled`                                                   | Set `zookeeper.persistence.enabled` to `true` to mount a new volume for internal ZooKeeper                                     | `false`                                               |
| `zookeeper.persistence.size`                                                      | `PersistentVolumeClaim` size                                                                                                   | `20Gi`                                                |
| `zookeeper.persistence.storageClass`                                              | ZooKeeper data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning       | `-`                                                   |
| `zookeeper.zookeeperRoot`                                                         | Specify dolphinscheduler root directory in ZooKeeper                                                                           | `/dolphinscheduler`                                   |
| `externalZookeeper.zookeeperQuorum`                                               | If exists external ZooKeeper, and set `zookeeper.enabled` value to false. Specify Zookeeper quorum                             | `127.0.0.1:2181`                                      |
| `externalZookeeper.zookeeperRoot`                                                 | If exists external ZooKeeper, and set `zookeeper.enabled` value to false. Specify dolphinscheduler root directory in Zookeeper | `/dolphinscheduler`                                   |
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
| `common.configmap.RESOURCE_MANAGER_HTTPADDRESS_PORT`                              | Set resource manager httpaddress port for yarn                                                                                 | `8088`                                                |
| `common.configmap.YARN_RESOURCEMANAGER_HA_RM_IDS`                                 | If resourcemanager HA is enabled, please set the HA IPs                                                                        | `nil`                                                 |
| `common.configmap.YARN_APPLICATION_STATUS_ADDRESS`                                | If resourcemanager is single, you only need to replace ds1 to actual resourcemanager hostname, otherwise keep default          | `http://ds1:%s/ws/v1/cluster/apps/%s`               |
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
| `master.configmap.MASTER_EXEC_THREADS`                                            | Master execute thread number to limit process instances                                                                        | `100`                                                 |
| `master.configmap.MASTER_EXEC_TASK_NUM`                                           | Master execute task number in parallel per process instance                                                                    | `20`                                                  |
| `master.configmap.MASTER_DISPATCH_TASK_NUM`                                       | Master dispatch task number per batch                                                                                          | `3`                                                   |
| `master.configmap.MASTER_HOST_SELECTOR`                                           | Master host selector to select a suitable worker, optional values include Random, RoundRobin, LowerWeight                      | `LowerWeight`                                         |
| `master.configmap.MASTER_HEARTBEAT_INTERVAL`                                      | Master heartbeat interval, the unit is second                                                                                  | `10`                                                  |
| `master.configmap.MASTER_TASK_COMMIT_RETRYTIMES`                                  | Master commit task retry times                                                                                                 | `5`                                                   |
| `master.configmap.MASTER_TASK_COMMIT_INTERVAL`                                    | master commit task interval, the unit is second                                                                                | `1`                                                   |
| `master.configmap.MASTER_MAX_CPULOAD_AVG`                                         | Master max cpuload avg, only higher than the system cpu load average, master server can schedule                               | `-1` (`the number of cpu cores * 2`)                  |
| `master.configmap.MASTER_RESERVED_MEMORY`                                         | Master reserved memory, only lower than system available memory, master server can schedule, the unit is G                     | `0.3`                                                 |
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
| `worker.configmap.WORKER_SERVER_OPTS`                                             | The jvm options for worker server                                                                                              | `-Xms1g -Xmx1g -Xmn512m`                              |
| `worker.configmap.WORKER_EXEC_THREADS`                                            | Worker execute thread number to limit task instances                                                                           | `100`                                                 |
| `worker.configmap.WORKER_HEARTBEAT_INTERVAL`                                      | Worker heartbeat interval, the unit is second                                                                                  | `10`                                                  |
| `worker.configmap.WORKER_MAX_CPULOAD_AVG`                                         | Worker max cpuload avg, only higher than the system cpu load average, worker server can be dispatched tasks                    | `-1` (`the number of cpu cores * 2`)                  |
| `worker.configmap.WORKER_RESERVED_MEMORY`                                         | Worker reserved memory, only lower than system available memory, worker server can be dispatched tasks, the unit is G          | `0.3`                                                 |
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
