# DolphinScheduler

[DolphinScheduler](https://dolphinscheduler.apache.org) is a distributed and easy-to-expand visual DAG workflow scheduling system, dedicated to solving the complex dependencies in data processing, making the scheduling system out of the box for data processing.

## Introduction
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

Otherwise, you need to execute port-forward command like:

```bash
$ kubectl port-forward --address 0.0.0.0 svc/dolphinscheduler-api 12345:12345
$ kubectl port-forward --address 0.0.0.0 -n test svc/dolphinscheduler-api 12345:12345 # with test namespace
```

> **Tip**: If the error of `unable to do port forwarding: socat not found` appears, you need to install `socat` at first

And then access the web: http://192.168.xx.xx:12345/dolphinscheduler

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

## Configuration

The Configuration file is `values.yaml`, and the following tables lists the configurable parameters of the DolphinScheduler chart and their default values.

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
| `postgresql.persistence.size`                                                     | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
| `postgresql.persistence.storageClass`                                             | PostgreSQL data Persistent Volume Storage Class. If set to "-", storageClassName: "", which disables dynamic provisioning      | `-`                                                   |
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
| `zookeeper.service.port`                                                          | ZooKeeper port                                                                                                                 | `2181`                                                |
| `zookeeper.persistence.enabled`                                                   | Set `zookeeper.persistence.enabled` to `true` to mount a new volume for internal Zookeeper                                     | `false`                                               |
| `zookeeper.persistence.size`                                                      | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
| `zookeeper.persistence.storageClass`                                              | Zookeeper data Persistent Volume Storage Class. If set to "-", storageClassName: "", which disables dynamic provisioning       | `-`                                                   |
| `zookeeper.zookeeperRoot`                                                         | Specify dolphinscheduler root directory in Zookeeper                                                                           | `/dolphinscheduler`                                   |
| `externalZookeeper.zookeeperQuorum`                                               | If exists external Zookeeper, and set `zookeeper.enabled` value to false. Specify Zookeeper quorum                             | `127.0.0.1:2181`                                      |
| `externalZookeeper.zookeeperRoot`                                                 | If exists external Zookeeper, and set `zookeeper.enabled` value to false. Specify dolphinscheduler root directory in Zookeeper | `/dolphinscheduler`                                   |
|                                                                                   |                                                                                                                                |                                                       |
| `common.configmap.DOLPHINSCHEDULER_ENV`                                           | System env path, self configuration, please read `values.yaml`                                                                 | `[]`                                                  |
| `common.configmap.DOLPHINSCHEDULER_DATA_BASEDIR_PATH`                             | User data directory path, self configuration, please make sure the directory exists and have read write permissions            | `/tmp/dolphinscheduler`                               |
| `common.configmap.RESOURCE_STORAGE_TYPE`                                          | Resource storage type: HDFS, S3, NONE                                                                                          | `HDFS`                                                |
| `common.configmap.RESOURCE_UPLOAD_PATH`                                           | Resource store on HDFS/S3 path, please make sure the directory exists on hdfs and have read write permissions                  | `/dolphinscheduler`                                   |
| `common.configmap.FS_DEFAULT_FS`                                                  | Resource storage file system like `file:///`, `hdfs://mycluster:8020` or `s3a://dolphinscheduler`                              | `file:///`                                            |
| `common.configmap.FS_S3A_ENDPOINT`                                                | S3 endpoint when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                       | `s3.xxx.amazonaws.com`                                |
| `common.configmap.FS_S3A_ACCESS_KEY`                                              | S3 access key when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                     | `xxxxxxx`                                             |
| `common.configmap.FS_S3A_SECRET_KEY`                                              | S3 secret key when `common.configmap.RESOURCE_STORAGE_TYPE` is set to `S3`                                                     | `xxxxxxx`                                             |
| `common.fsFileResourcePersistence.enabled`                                        | Set `common.fsFileResourcePersistence.enabled` to `true` to mount a new file resource volume for `api` and `worker`            | `false`                                               |
| `common.fsFileResourcePersistence.accessModes`                                    | `PersistentVolumeClaim` Access Modes, must be `ReadWriteMany`                                                                  | `[ReadWriteMany]`                                     |
| `common.fsFileResourcePersistence.storageClassName`                               | Resource Persistent Volume Storage Class, must support the access mode: ReadWriteMany                                          | `-`                                                   |
| `common.fsFileResourcePersistence.storage`                                        | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
|                                                                                   |                                                                                                                                |                                                       |
| `master.podManagementPolicy`                                                      | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down  | `Parallel`                                            |
| `master.replicas`                                                                 | Replicas is the desired number of replicas of the given Template                                                               | `3`                                                   |
| `master.annotations`                                                              | The `annotations` for master server                                                                                            | `{}`                                                  |
| `master.affinity`                                                                 | If specified, the pod's scheduling constraints                                                                                 | `{}`                                                  |
| `master.nodeSelector`                                                             | NodeSelector is a selector which must be true for the pod to fit on a node                                                     | `{}`                                                  |
| `master.tolerations`                                                              | If specified, the pod's tolerations                                                                                            | `{}`                                                  |
| `master.resources`                                                                | The `resource` limit and request config for master server                                                                      | `{}`                                                  |
| `master.configmap.DOLPHINSCHEDULER_OPTS`                                          | The java options for master server                                                                                             | `""`                                                  |
| `master.configmap.MASTER_EXEC_THREADS`                                            | Master execute thread number                                                                                                   | `100`                                                 |
| `master.configmap.MASTER_EXEC_TASK_NUM`                                           | Master execute task number in parallel                                                                                         | `20`                                                  |
| `master.configmap.MASTER_HEARTBEAT_INTERVAL`                                      | Master heartbeat interval                                                                                                      | `10`                                                  |
| `master.configmap.MASTER_TASK_COMMIT_RETRYTIMES`                                  | Master commit task retry times                                                                                                 | `5`                                                   |
| `master.configmap.MASTER_TASK_COMMIT_INTERVAL`                                    | Master commit task interval                                                                                                    | `1000`                                                |
| `master.configmap.MASTER_MAX_CPULOAD_AVG`                                         | Only less than cpu avg load, master server can work. default value : the number of cpu cores * 2                               | `100`                                                 |
| `master.configmap.MASTER_RESERVED_MEMORY`                                         | Only larger than reserved memory, master server can work. default value : physical memory * 1/10, unit is G                    | `0.1`                                                 |
| `master.configmap.MASTER_LISTEN_PORT`                                             | Master listen port                                                                                                             | `5678`                                                |
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
| `master.persistentVolumeClaim.accessModes`                                        | `PersistentVolumeClaim` Access Modes                                                                                           | `[ReadWriteOnce]`                                     |
| `master.persistentVolumeClaim.storageClassName`                                   | `Master` logs data Persistent Volume Storage Class. If set to "-", storageClassName: "", which disables dynamic provisioning   | `-`                                                   |
| `master.persistentVolumeClaim.storage`                                            | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
|                                                                                   |                                                                                                                                |                                                       |
| `worker.podManagementPolicy`                                                      | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down  | `Parallel`                                            |
| `worker.replicas`                                                                 | Replicas is the desired number of replicas of the given Template                                                               | `3`                                                   |
| `worker.annotations`                                                              | The `annotations` for worker server                                                                                            | `{}`                                                  |
| `worker.affinity`                                                                 | If specified, the pod's scheduling constraints                                                                                 | `{}`                                                  |
| `worker.nodeSelector`                                                             | NodeSelector is a selector which must be true for the pod to fit on a node                                                     | `{}`                                                  |
| `worker.tolerations`                                                              | If specified, the pod's tolerations                                                                                            | `{}`                                                  |
| `worker.resources`                                                                | The `resource` limit and request config for worker server                                                                      | `{}`                                                  |
| `worker.configmap.DOLPHINSCHEDULER_OPTS`                                          | The java options for worker server                                                                                             | `""`                                                  |
| `worker.configmap.WORKER_EXEC_THREADS`                                            | Worker execute thread number                                                                                                   | `100`                                                 |
| `worker.configmap.WORKER_HEARTBEAT_INTERVAL`                                      | Worker heartbeat interval                                                                                                      | `10`                                                  |
| `worker.configmap.WORKER_MAX_CPULOAD_AVG`                                         | Only less than cpu avg load, worker server can work. default value : the number of cpu cores * 2                               | `100`                                                 |
| `worker.configmap.WORKER_RESERVED_MEMORY`                                         | Only larger than reserved memory, worker server can work. default value : physical memory * 1/10, unit is G                    | `0.1`                                                 |
| `worker.configmap.WORKER_LISTEN_PORT`                                             | Worker listen port                                                                                                             | `1234`                                                |
| `worker.configmap.WORKER_GROUPS`                                                  | Worker groups                                                                                                                  | `default`                                             |
| `worker.configmap.WORKER_HOST_WEIGHT`                                             | Worker host weight                                                                                                             | `100`                                                 |
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
| `worker.persistentVolumeClaim.dataPersistentVolume.accessModes`                   | `PersistentVolumeClaim` Access Modes                                                                                           | `[ReadWriteOnce]`                                     |
| `worker.persistentVolumeClaim.dataPersistentVolume.storageClassName`              | `Worker` data Persistent Volume Storage Class. If set to "-", storageClassName: "", which disables dynamic provisioning        | `-`                                                   |
| `worker.persistentVolumeClaim.dataPersistentVolume.storage`                       | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
| `worker.persistentVolumeClaim.logsPersistentVolume.enabled`                       | Set `worker.persistentVolumeClaim.logsPersistentVolume.enabled` to `true` to mount a logs volume for `worker`                  | `false`                                               |
| `worker.persistentVolumeClaim.logsPersistentVolume.accessModes`                   | `PersistentVolumeClaim` Access Modes                                                                                           | `[ReadWriteOnce]`                                     |
| `worker.persistentVolumeClaim.logsPersistentVolume.storageClassName`              | `Worker` logs data Persistent Volume Storage Class. If set to "-", storageClassName: "", which disables dynamic provisioning   | `-`                                                   |
| `worker.persistentVolumeClaim.logsPersistentVolume.storage`                       | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
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
| `alert.configmap.DOLPHINSCHEDULER_OPTS`                                           | The java options for alert server                                                                                              | `""`                                                  |
| `alert.configmap.ALERT_PLUGIN_DIR`                                                | Alert plugin directory                                                                                                         | `lib/plugin/alert`                                    |
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
| `alert.persistentVolumeClaim.accessModes`                                         | `PersistentVolumeClaim` Access Modes                                                                                           | `[ReadWriteOnce]`                                     |
| `alert.persistentVolumeClaim.storageClassName`                                    | `Alert` logs data Persistent Volume Storage Class. If set to "-", storageClassName: "", which disables dynamic provisioning    | `-`                                                   |
| `alert.persistentVolumeClaim.storage`                                             | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
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
| `api.configmap.DOLPHINSCHEDULER_OPTS`                                             | The java options for api server                                                                                                | `""`                                                  |
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
| `api.persistentVolumeClaim.accessModes`                                           | `PersistentVolumeClaim` Access Modes                                                                                           | `[ReadWriteOnce]`                                     |
| `api.persistentVolumeClaim.storageClassName`                                      | `api` logs data Persistent Volume Storage Class. If set to "-", storageClassName: "", which disables dynamic provisioning      | `-`                                                   |
| `api.persistentVolumeClaim.storage`                                               | `PersistentVolumeClaim` Size                                                                                                   | `20Gi`                                                |
|                                                                                   |                                                                                                                                |                                                       |
| `ingress.enabled`                                                                 | Enable ingress                                                                                                                 | `false`                                               |
| `ingress.host`                                                                    | Ingress host                                                                                                                   | `dolphinscheduler.org`                                |
| `ingress.path`                                                                    | Ingress path                                                                                                                   | `/dolphinscheduler`                                   |
| `ingress.tls.enabled`                                                             | Enable ingress tls                                                                                                             | `false`                                               |
| `ingress.tls.secretName`                                                          | Ingress tls secret name                                                                                                        | `dolphinscheduler-tls`                                |

## FAQ

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

4. Push the docker image `apache/dolphinscheduler:mysql` to a docker registry

5. Modify image `repository` and update `tag` to `mysql` in `values.yaml`

6. Modify postgresql `enabled` to `false`

7. Modify externalDatabase (especially modify `host`, `username` and `password`):

```
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

For more information please refer to the [incubator-dolphinscheduler](https://github.com/apache/incubator-dolphinscheduler.git) documentation.
