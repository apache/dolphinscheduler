## About DolphinScheduler for Kubernetes

Dolphin Scheduler is a distributed and easy-to-expand visual DAG workflow scheduling system, dedicated to solving the complex dependencies in data processing, making the scheduling system out of the box for data processing.

This chart bootstraps all the components needed to run Apache DolphinScheduler on a Kubernetes Cluster using [Helm](https://helm.sh).

## QuickStart in Kubernetes

Please refer to the [Quick Start in Kubernetes](../../../docs/docs/en/guide/installation/kubernetes.md)

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| alert.affinity | object | `{}` | Affinity is a group of affinity scheduling rules. If specified, the pod's scheduling constraints. More info: [node-affinity](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#node-affinity) |
| alert.annotations | object | `{}` | You can use annotations to attach arbitrary non-identifying metadata to objects. Clients such as tools and libraries can retrieve this metadata. |
| alert.customizedConfig | object | `{}` | configure aligned with https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-alert/dolphinscheduler-alert-server/src/main/resources/application.yaml |
| alert.enableCustomizedConfig | bool | `false` | enable configure custom config |
| alert.enabled | bool | `true` | Enable or disable the Alert-Server component |
| alert.env.JAVA_OPTS | string | `"-Xms512m -Xmx512m -Xmn256m"` | The jvm options for alert server |
| alert.livenessProbe | object | `{"enabled":true,"failureThreshold":"3","initialDelaySeconds":"30","periodSeconds":"30","successThreshold":"1","timeoutSeconds":"5"}` | Periodic probe of container liveness. Container will be restarted if the probe fails. More info: [container-probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) |
| alert.livenessProbe.enabled | bool | `true` | Turn on and off liveness probe |
| alert.livenessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| alert.livenessProbe.initialDelaySeconds | string | `"30"` | Delay before liveness probe is initiated |
| alert.livenessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| alert.livenessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| alert.livenessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| alert.nodeSelector | object | `{}` | NodeSelector is a selector which must be true for the pod to fit on a node. Selector which must match a node's labels for the pod to be scheduled on that node. More info: [assign-pod-node](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/) |
| alert.persistentVolumeClaim | object | `{"accessModes":["ReadWriteOnce"],"enabled":false,"storage":"20Gi","storageClassName":"-"}` | PersistentVolumeClaim represents a reference to a PersistentVolumeClaim in the same namespace. More info: [persistentvolumeclaims](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims) |
| alert.persistentVolumeClaim.accessModes | list | `["ReadWriteOnce"]` | `PersistentVolumeClaim` access modes |
| alert.persistentVolumeClaim.enabled | bool | `false` | Set `alert.persistentVolumeClaim.enabled` to `true` to mount a new volume for `alert` |
| alert.persistentVolumeClaim.storage | string | `"20Gi"` | `PersistentVolumeClaim` size |
| alert.persistentVolumeClaim.storageClassName | string | `"-"` | `Alert` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| alert.readinessProbe | object | `{"enabled":true,"failureThreshold":"3","initialDelaySeconds":"30","periodSeconds":"30","successThreshold":"1","timeoutSeconds":"5"}` | Periodic probe of container service readiness. Container will be removed from service endpoints if the probe fails. More info: [container-probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) |
| alert.readinessProbe.enabled | bool | `true` | Turn on and off readiness probe |
| alert.readinessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| alert.readinessProbe.initialDelaySeconds | string | `"30"` | Delay before readiness probe is initiated |
| alert.readinessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| alert.readinessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| alert.readinessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| alert.replicas | int | `1` | Number of desired pods. This is a pointer to distinguish between explicit zero and not specified. Defaults to 1. |
| alert.resources | object | `{}` | Compute Resources required by this container. More info: [manage-resources-containers](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/) |
| alert.service.annotations | object | `{}` | annotations may need to be set when want to scrapy metrics by prometheus but not install prometheus operator |
| alert.service.serviceMonitor | object | `{"annotations":{},"enabled":false,"interval":"15s","labels":{},"path":"/actuator/prometheus"}` | serviceMonitor for prometheus operator |
| alert.service.serviceMonitor.annotations | object | `{}` | serviceMonitor.annotations ServiceMonitor annotations |
| alert.service.serviceMonitor.enabled | bool | `false` | Enable or disable alert-server serviceMonitor |
| alert.service.serviceMonitor.interval | string | `"15s"` | serviceMonitor.interval interval at which metrics should be scraped |
| alert.service.serviceMonitor.labels | object | `{}` | serviceMonitor.labels ServiceMonitor extra labels |
| alert.service.serviceMonitor.path | string | `"/actuator/prometheus"` | serviceMonitor.path path of the metrics endpoint |
| alert.strategy | object | `{"rollingUpdate":{"maxSurge":"25%","maxUnavailable":"25%"},"type":"RollingUpdate"}` | The deployment strategy to use to replace existing pods with new ones. |
| alert.strategy.rollingUpdate.maxSurge | string | `"25%"` | The maximum number of pods that can be scheduled above the desired number of pods |
| alert.strategy.rollingUpdate.maxUnavailable | string | `"25%"` | The maximum number of pods that can be unavailable during the update |
| alert.strategy.type | string | `"RollingUpdate"` | Type of deployment. Can be "Recreate" or "RollingUpdate" |
| alert.tolerations | list | `[]` | Tolerations are appended (excluding duplicates) to pods running with this RuntimeClass during admission, effectively unioning the set of nodes tolerated by the pod and the RuntimeClass. |
| api.affinity | object | `{}` | Affinity is a group of affinity scheduling rules. If specified, the pod's scheduling constraints. More info: [node-affinity](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#node-affinity) |
| api.annotations | object | `{}` | You can use annotations to attach arbitrary non-identifying metadata to objects. Clients such as tools and libraries can retrieve this metadata. |
| api.customizedConfig | object | `{}` | configure aligned with https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-api/src/main/resources/application.yaml |
| api.enableCustomizedConfig | bool | `false` | enable configure custom config |
| api.enabled | bool | `true` | Enable or disable the API-Server component |
| api.env.JAVA_OPTS | string | `"-Xms512m -Xmx512m -Xmn256m"` | The jvm options for api server |
| api.livenessProbe | object | `{"enabled":true,"failureThreshold":"3","initialDelaySeconds":"30","periodSeconds":"30","successThreshold":"1","timeoutSeconds":"5"}` | Periodic probe of container liveness. Container will be restarted if the probe fails. More info: [container-probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) |
| api.livenessProbe.enabled | bool | `true` | Turn on and off liveness probe |
| api.livenessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| api.livenessProbe.initialDelaySeconds | string | `"30"` | Delay before liveness probe is initiated |
| api.livenessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| api.livenessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| api.livenessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| api.nodeSelector | object | `{}` | NodeSelector is a selector which must be true for the pod to fit on a node. Selector which must match a node's labels for the pod to be scheduled on that node. More info: [assign-pod-node](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/) |
| api.persistentVolumeClaim | object | `{"accessModes":["ReadWriteOnce"],"enabled":false,"storage":"20Gi","storageClassName":"-"}` | PersistentVolumeClaim represents a reference to a PersistentVolumeClaim in the same namespace. More info: [persistentvolumeclaims](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#persistentvolumeclaims) |
| api.persistentVolumeClaim.accessModes | list | `["ReadWriteOnce"]` | `PersistentVolumeClaim` access modes |
| api.persistentVolumeClaim.enabled | bool | `false` | Set `api.persistentVolumeClaim.enabled` to `true` to mount a new volume for `api` |
| api.persistentVolumeClaim.storage | string | `"20Gi"` | `PersistentVolumeClaim` size |
| api.persistentVolumeClaim.storageClassName | string | `"-"` | `api` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| api.readinessProbe | object | `{"enabled":true,"failureThreshold":"3","initialDelaySeconds":"30","periodSeconds":"30","successThreshold":"1","timeoutSeconds":"5"}` | Periodic probe of container service readiness. Container will be removed from service endpoints if the probe fails. More info: [container-probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) |
| api.readinessProbe.enabled | bool | `true` | Turn on and off readiness probe |
| api.readinessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| api.readinessProbe.initialDelaySeconds | string | `"30"` | Delay before readiness probe is initiated |
| api.readinessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| api.readinessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| api.readinessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| api.replicas | string | `"1"` | Number of desired pods. This is a pointer to distinguish between explicit zero and not specified. Defaults to 1. |
| api.resources | object | `{}` | Compute Resources required by this container. More info: [manage-resources-containers](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/) |
| api.service.annotations | object | `{}` | annotations may need to be set when service.type is LoadBalancer service.beta.kubernetes.io/aws-load-balancer-ssl-cert: arn:aws:acm:us-east-1:EXAMPLE_CERT |
| api.service.clusterIP | string | `""` | clusterIP is the IP address of the service and is usually assigned randomly by the master |
| api.service.externalIPs | list | `[]` | externalIPs is a list of IP addresses for which nodes in the cluster will also accept traffic for this service |
| api.service.externalName | string | `""` | externalName is the external reference that kubedns or equivalent will return as a CNAME record for this service, requires Type to be ExternalName |
| api.service.loadBalancerIP | string | `""` | loadBalancerIP when service.type is LoadBalancer. LoadBalancer will get created with the IP specified in this field |
| api.service.nodePort | string | `""` | nodePort is the port on each node on which this api service is exposed when type=NodePort |
| api.service.pythonNodePort | string | `""` | pythonNodePort is the port on each node on which this python api service is exposed when type=NodePort |
| api.service.serviceMonitor | object | `{"annotations":{},"enabled":false,"interval":"15s","labels":{},"path":"/dolphinscheduler/actuator/prometheus"}` | serviceMonitor for prometheus operator |
| api.service.serviceMonitor.annotations | object | `{}` | serviceMonitor.annotations ServiceMonitor annotations |
| api.service.serviceMonitor.enabled | bool | `false` | Enable or disable api-server serviceMonitor |
| api.service.serviceMonitor.interval | string | `"15s"` | serviceMonitor.interval interval at which metrics should be scraped |
| api.service.serviceMonitor.labels | object | `{}` | serviceMonitor.labels ServiceMonitor extra labels |
| api.service.serviceMonitor.path | string | `"/dolphinscheduler/actuator/prometheus"` | serviceMonitor.path path of the metrics endpoint |
| api.service.type | string | `"ClusterIP"` | type determines how the Service is exposed. Defaults to ClusterIP. Valid options are ExternalName, ClusterIP, NodePort, and LoadBalancer |
| api.strategy | object | `{"rollingUpdate":{"maxSurge":"25%","maxUnavailable":"25%"},"type":"RollingUpdate"}` | The deployment strategy to use to replace existing pods with new ones. |
| api.strategy.rollingUpdate.maxSurge | string | `"25%"` | The maximum number of pods that can be scheduled above the desired number of pods |
| api.strategy.rollingUpdate.maxUnavailable | string | `"25%"` | The maximum number of pods that can be unavailable during the update |
| api.strategy.type | string | `"RollingUpdate"` | Type of deployment. Can be "Recreate" or "RollingUpdate" |
| api.taskTypeFilter.enabled | bool | `false` | Enable or disable the task type filter. If set to true, the API-Server will return tasks of a specific type set in api.taskTypeFilter.task Note: This feature only filters tasks to return a specific type on the WebUI. However, you can still create any task that DolphinScheduler supports via the API. |
| api.taskTypeFilter.task | object | `{}` | ref: [task-type-config.yaml](https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-api/src/main/resources/task-type-config.yaml) |
| api.tolerations | list | `[]` | Tolerations are appended (excluding duplicates) to pods running with this RuntimeClass during admission, effectively unioning the set of nodes tolerated by the pod and the RuntimeClass. |
| common.configmap.DATAX_LAUNCHER | string | `"/opt/soft/datax/bin/datax.py"` | Set `DATAX_LAUNCHER` for DolphinScheduler's task environment |
| common.configmap.DATA_BASEDIR_PATH | string | `"/tmp/dolphinscheduler"` | User data directory path, self configuration, please make sure the directory exists and have read write permissions |
| common.configmap.DOLPHINSCHEDULER_OPTS | string | `""` | The jvm options for dolphinscheduler, suitable for all servers |
| common.configmap.FLINK_HOME | string | `"/opt/soft/flink"` | Set `FLINK_HOME` for DolphinScheduler's task environment |
| common.configmap.HADOOP_CONF_DIR | string | `"/opt/soft/hadoop/etc/hadoop"` | Set `HADOOP_CONF_DIR` for DolphinScheduler's task environment |
| common.configmap.HADOOP_HOME | string | `"/opt/soft/hadoop"` | Set `HADOOP_HOME` for DolphinScheduler's task environment |
| common.configmap.HIVE_HOME | string | `"/opt/soft/hive"` | Set `HIVE_HOME` for DolphinScheduler's task environment |
| common.configmap.JAVA_HOME | string | `"/opt/java/openjdk"` | Set `JAVA_HOME` for DolphinScheduler's task environment |
| common.configmap.PYTHON_LAUNCHER | string | `"/usr/bin/python/bin/python3"` | Set `PYTHON_LAUNCHER` for DolphinScheduler's task environment |
| common.configmap.RESOURCE_UPLOAD_PATH | string | `"/dolphinscheduler"` | Resource store on HDFS/S3 path, please make sure the directory exists on hdfs and have read write permissions |
| common.configmap.SPARK_HOME | string | `"/opt/soft/spark"` | Set `SPARK_HOME` for DolphinScheduler's task environment |
| common.fsFileResourcePersistence.accessModes | list | `["ReadWriteMany"]` | `PersistentVolumeClaim` access modes, must be `ReadWriteMany` |
| common.fsFileResourcePersistence.enabled | bool | `false` | Set `common.fsFileResourcePersistence.enabled` to `true` to mount a new file resource volume for `api` and `worker` |
| common.fsFileResourcePersistence.storage | string | `"20Gi"` | `PersistentVolumeClaim` size |
| common.fsFileResourcePersistence.storageClassName | string | `"-"` | Resource persistent volume storage class, must support the access mode: `ReadWriteMany` |
| common.sharedStoragePersistence.accessModes | list | `["ReadWriteMany"]` | `PersistentVolumeClaim` access modes, must be `ReadWriteMany` |
| common.sharedStoragePersistence.enabled | bool | `false` | Set `common.sharedStoragePersistence.enabled` to `true` to mount a shared storage volume for Hadoop, Spark binary and etc |
| common.sharedStoragePersistence.mountPath | string | `"/opt/soft"` | The mount path for the shared storage volume |
| common.sharedStoragePersistence.storage | string | `"20Gi"` | `PersistentVolumeClaim` size |
| common.sharedStoragePersistence.storageClassName | string | `"-"` | Shared Storage persistent volume storage class, must support the access mode: ReadWriteMany |
| conf.auto | bool | `false` | auto restart, if true, all components will be restarted automatically after the common configuration is updated. if false, you need to restart the components manually. default is false |
| conf.common."appId.collect" | string | `"log"` | way to collect applicationId: log, aop |
| conf.common."aws.credentials.provider.type" | string | `"AWSStaticCredentialsProvider"` |  |
| conf.common."aws.s3.access.key.id" | string | `"minioadmin"` | The AWS access key. if resource.storage.type=S3, and credentials.provider.type is AWSStaticCredentialsProvider. This configuration is required |
| conf.common."aws.s3.access.key.secret" | string | `"minioadmin"` | The AWS secret access key. if resource.storage.type=S3, and credentials.provider.type is AWSStaticCredentialsProvider. This configuration is required |
| conf.common."aws.s3.bucket.name" | string | `"dolphinscheduler"` | The name of the bucket. You need to create them by yourself. Otherwise, the system cannot start. All buckets in Amazon S3 share a single namespace; ensure the bucket is given a unique name. |
| conf.common."aws.s3.endpoint" | string | `"http://minio:9000"` | You need to set this parameter when private cloud s3. If S3 uses public cloud, you only need to set resource.aws.region or set to the endpoint of a public cloud such as S3.cn-north-1.amazonaws.com.cn |
| conf.common."aws.s3.region" | string | `"ca-central-1"` | The AWS Region to use. if resource.storage.type=S3, This configuration is required |
| conf.common."conda.path" | string | `"/opt/anaconda3/etc/profile.d/conda.sh"` | set path of conda.sh |
| conf.common."data.basedir.path" | string | `"/tmp/dolphinscheduler"` | user data local directory path, please make sure the directory exists and have read write permissions |
| conf.common."datasource.encryption.enable" | bool | `false` | datasource encryption enable |
| conf.common."datasource.encryption.salt" | string | `"!@#$%^&*"` | datasource encryption salt |
| conf.common."development.state" | bool | `false` | development state |
| conf.common."hadoop.security.authentication.startup.state" | bool | `false` | whether to startup kerberos |
| conf.common."java.security.krb5.conf.path" | string | `"/opt/krb5.conf"` | java.security.krb5.conf path |
| conf.common."kerberos.expire.time" | int | `2` | kerberos expire time, the unit is hour |
| conf.common."login.user.keytab.path" | string | `"/opt/hdfs.headless.keytab"` | login user from keytab path |
| conf.common."login.user.keytab.username" | string | `"hdfs-mycluster@ESZ.COM"` | login user from keytab username |
| conf.common."ml.mlflow.preset_repository" | string | `"https://github.com/apache/dolphinscheduler-mlflow"` | mlflow task plugin preset repository |
| conf.common."ml.mlflow.preset_repository_version" | string | `"main"` | mlflow task plugin preset repository version |
| conf.common."resource.alibaba.cloud.access.key.id" | string | `"<your-access-key-id>"` | alibaba cloud access key id, required if you set resource.storage.type=OSS |
| conf.common."resource.alibaba.cloud.access.key.secret" | string | `"<your-access-key-secret>"` | alibaba cloud access key secret, required if you set resource.storage.type=OSS |
| conf.common."resource.alibaba.cloud.oss.bucket.name" | string | `"dolphinscheduler"` | oss bucket name, required if you set resource.storage.type=OSS |
| conf.common."resource.alibaba.cloud.oss.endpoint" | string | `"https://oss-cn-hangzhou.aliyuncs.com"` | oss bucket endpoint, required if you set resource.storage.type=OSS |
| conf.common."resource.alibaba.cloud.region" | string | `"cn-hangzhou"` | alibaba cloud region, required if you set resource.storage.type=OSS |
| conf.common."resource.azure.client.id" | string | `"minioadmin"` | azure storage account name, required if you set resource.storage.type=ABS |
| conf.common."resource.azure.client.secret" | string | `"minioadmin"` | azure storage account key, required if you set resource.storage.type=ABS |
| conf.common."resource.azure.subId" | string | `"minioadmin"` | azure storage subId, required if you set resource.storage.type=ABS |
| conf.common."resource.azure.tenant.id" | string | `"minioadmin"` | azure storage tenantId, required if you set resource.storage.type=ABS |
| conf.common."resource.hdfs.fs.defaultFS" | string | `"hdfs://mycluster:8020"` | if resource.storage.type=S3, the value like: s3a://dolphinscheduler; if resource.storage.type=HDFS and namenode HA is enabled, you need to copy core-site.xml and hdfs-site.xml to conf dir |
| conf.common."resource.hdfs.root.user" | string | `"hdfs"` | if resource.storage.type=HDFS, the user must have the permission to create directories under the HDFS root path |
| conf.common."resource.manager.httpaddress.port" | int | `8088` | resourcemanager port, the default value is 8088 if not specified |
| conf.common."resource.storage.type" | string | `"S3"` | resource storage type: HDFS, S3, OSS, GCS, ABS, NONE |
| conf.common."resource.storage.upload.base.path" | string | `"/dolphinscheduler"` | resource store on HDFS/S3 path, resource file will store to this base path, self configuration, please make sure the directory exists on hdfs and have read write permissions. "/dolphinscheduler" is recommended |
| conf.common."sudo.enable" | bool | `true` | use sudo or not, if set true, executing user is tenant user and deploy user needs sudo permissions; if set false, executing user is the deploy user and doesn't need sudo permissions |
| conf.common."support.hive.oneSession" | bool | `false` | Whether hive SQL is executed in the same session |
| conf.common."task.resource.limit.state" | bool | `false` | Task resource limit state |
| conf.common."yarn.application.status.address" | string | `"http://ds1:%s/ws/v1/cluster/apps/%s"` | if resourcemanager HA is enabled or not use resourcemanager, please keep the default value; If resourcemanager is single, you only need to replace ds1 to actual resourcemanager hostname |
| conf.common."yarn.job.history.status.address" | string | `"http://ds1:19888/ws/v1/history/mapreduce/jobs/%s"` | job history status url when application number threshold is reached(default 10000, maybe it was set to 1000) |
| conf.common."yarn.resourcemanager.ha.rm.ids" | string | `"192.168.xx.xx,192.168.xx.xx"` | if resourcemanager HA is enabled, please set the HA IPs; if resourcemanager is single, keep this value empty |
| datasource.profile | string | `"postgresql"` | The profile of datasource |
| externalDatabase.database | string | `"dolphinscheduler"` | The database of external database |
| externalDatabase.driverClassName | string | `"org.postgresql.Driver"` | The driverClassName of external database |
| externalDatabase.enabled | bool | `false` | If exists external database, and set postgresql.enable value to false. external database will be used, otherwise Dolphinscheduler's internal database will be used. |
| externalDatabase.host | string | `"localhost"` | The host of external database |
| externalDatabase.params | string | `"characterEncoding=utf8"` | The params of external database |
| externalDatabase.password | string | `"root"` | The password of external database |
| externalDatabase.port | string | `"5432"` | The port of external database |
| externalDatabase.type | string | `"postgresql"` | The type of external database, supported types: postgresql, mysql |
| externalDatabase.username | string | `"root"` | The username of external database |
| externalRegistry.registryPluginName | string | `"zookeeper"` | If exists external registry and set `zookeeper.enable` && `registryEtcd.enabled` && `registryJdbc.enabled` to false, specify the external registry plugin name |
| externalRegistry.registryServers | string | `"127.0.0.1:2181"` | If exists external registry and set `zookeeper.enable` && `registryEtcd.enabled` && `registryJdbc.enabled` to false, specify the external registry servers |
| image.alert | string | `"dolphinscheduler-alert-server"` | alert-server image |
| image.api | string | `"dolphinscheduler-api"` | api-server image |
| image.master | string | `"dolphinscheduler-master"` | master image |
| image.pullPolicy | string | `"IfNotPresent"` | Image pull policy. Options: Always, Never, IfNotPresent |
| image.pullSecret | string | `""` | Specify a imagePullSecrets |
| image.registry | string | `"apache"` | Docker image repository for the DolphinScheduler |
| image.tag | string | `"latest"` | Docker image version for the DolphinScheduler |
| image.tools | string | `"dolphinscheduler-tools"` | tools image |
| image.worker | string | `"dolphinscheduler-worker"` | worker image |
| ingress.annotations | object | `{}` | Ingress annotations |
| ingress.enabled | bool | `false` | Enable ingress |
| ingress.host | string | `"dolphinscheduler.org"` | Ingress host |
| ingress.path | string | `"/dolphinscheduler"` | Ingress path |
| ingress.tls.enabled | bool | `false` | Enable ingress tls |
| ingress.tls.secretName | string | `"dolphinscheduler-tls"` | Ingress tls secret name |
| initImage | object | `{"busybox":"busybox:1.30.1","pullPolicy":"IfNotPresent"}` | Used to detect whether dolphinscheduler dependent services such as database are ready |
| initImage.busybox | string | `"busybox:1.30.1"` | Specify initImage repository |
| initImage.pullPolicy | string | `"IfNotPresent"` | Image pull policy. Options: Always, Never, IfNotPresent |
| master.affinity | object | `{}` | Affinity is a group of affinity scheduling rules. If specified, the pod's scheduling constraints. More info: [node-affinity](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#node-affinity) |
| master.annotations | object | `{}` | You can use annotations to attach arbitrary non-identifying metadata to objects. Clients such as tools and libraries can retrieve this metadata. |
| master.customizedConfig | object | `{}` | configure aligned with https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-master/src/main/resources/application.yaml |
| master.enableCustomizedConfig | bool | `false` | enable configure custom config |
| master.enabled | bool | `true` | Enable or disable the Master component |
| master.env.JAVA_OPTS | string | `"-Xms1g -Xmx1g -Xmn512m"` | The jvm options for master server |
| master.env.MASTER_DISPATCH_TASK_NUM | string | `"3"` | Master dispatch task number per batch |
| master.env.MASTER_EXEC_TASK_NUM | string | `"20"` | Master execute task number in parallel per process instance |
| master.env.MASTER_EXEC_THREADS | string | `"100"` | Master execute thread number to limit process instances |
| master.env.MASTER_FAILOVER_INTERVAL | string | `"10m"` | Master failover interval, the unit is minute |
| master.env.MASTER_HEARTBEAT_ERROR_THRESHOLD | string | `"5"` | Master heartbeat error threshold |
| master.env.MASTER_HOST_SELECTOR | string | `"LowerWeight"` | Master host selector to select a suitable worker, optional values include Random, RoundRobin, LowerWeight |
| master.env.MASTER_KILL_APPLICATION_WHEN_HANDLE_FAILOVER | string | `"true"` | Master kill application when handle failover |
| master.env.MASTER_MAX_HEARTBEAT_INTERVAL | string | `"10s"` | Master max heartbeat interval |
| master.env.MASTER_SERVER_LOAD_PROTECTION_ENABLED | bool | `false` | If set true, will open master overload protection |
| master.env.MASTER_SERVER_LOAD_PROTECTION_MAX_DISK_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Master max disk usage , when the master's disk usage is smaller then this value, master server can execute workflow. |
| master.env.MASTER_SERVER_LOAD_PROTECTION_MAX_JVM_CPU_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Master max jvm cpu usage, when the master's jvm cpu usage is smaller then this value, master server can execute workflow. |
| master.env.MASTER_SERVER_LOAD_PROTECTION_MAX_SYSTEM_CPU_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Master max system cpu usage, when the master's system cpu usage is smaller then this value, master server can execute workflow. |
| master.env.MASTER_SERVER_LOAD_PROTECTION_MAX_SYSTEM_MEMORY_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Master max System memory usage , when the master's system memory usage is smaller then this value, master server can execute workflow. |
| master.env.MASTER_STATE_WHEEL_INTERVAL | string | `"5s"` | master state wheel interval, the unit is second |
| master.env.MASTER_TASK_COMMIT_INTERVAL | string | `"1s"` | master commit task interval, the unit is second |
| master.env.MASTER_TASK_COMMIT_RETRYTIMES | string | `"5"` | Master commit task retry times |
| master.livenessProbe | object | `{"enabled":true,"failureThreshold":"3","initialDelaySeconds":"30","periodSeconds":"30","successThreshold":"1","timeoutSeconds":"5"}` | Periodic probe of container liveness. Container will be restarted if the probe fails. More info: [container-probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) |
| master.livenessProbe.enabled | bool | `true` | Turn on and off liveness probe |
| master.livenessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| master.livenessProbe.initialDelaySeconds | string | `"30"` | Delay before liveness probe is initiated |
| master.livenessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| master.livenessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| master.livenessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| master.nodeSelector | object | `{}` | NodeSelector is a selector which must be true for the pod to fit on a node. Selector which must match a node's labels for the pod to be scheduled on that node. More info: [assign-pod-node](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/) |
| master.persistentVolumeClaim | object | `{"accessModes":["ReadWriteOnce"],"enabled":false,"storage":"20Gi","storageClassName":"-"}` | PersistentVolumeClaim represents a reference to a PersistentVolumeClaim in the same namespace. The StatefulSet controller is responsible for mapping network identities to claims in a way that maintains the identity of a pod. Every claim in this list must have at least one matching (by name) volumeMount in one container in the template. A claim in this list takes precedence over any volumes in the template, with the same name. |
| master.persistentVolumeClaim.accessModes | list | `["ReadWriteOnce"]` | `PersistentVolumeClaim` access modes |
| master.persistentVolumeClaim.enabled | bool | `false` | Set `master.persistentVolumeClaim.enabled` to `true` to mount a new volume for `master` |
| master.persistentVolumeClaim.storage | string | `"20Gi"` | `PersistentVolumeClaim` size |
| master.persistentVolumeClaim.storageClassName | string | `"-"` | `Master` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| master.podManagementPolicy | string | `"Parallel"` | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down. |
| master.readinessProbe | object | `{"enabled":true,"failureThreshold":"3","initialDelaySeconds":"30","periodSeconds":"30","successThreshold":"1","timeoutSeconds":"5"}` | Periodic probe of container service readiness. Container will be removed from service endpoints if the probe fails. More info: [container-probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) |
| master.readinessProbe.enabled | bool | `true` | Turn on and off readiness probe |
| master.readinessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| master.readinessProbe.initialDelaySeconds | string | `"30"` | Delay before readiness probe is initiated |
| master.readinessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| master.readinessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| master.readinessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| master.replicas | string | `"3"` | Replicas is the desired number of replicas of the given Template. |
| master.resources | object | `{}` | Compute Resources required by this container. More info: [manage-resources-containers](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/) |
| master.service.annotations | object | `{}` | annotations may need to be set when want to scrapy metrics by prometheus but not install prometheus operator |
| master.service.serviceMonitor | object | `{"annotations":{},"enabled":false,"interval":"15s","labels":{},"path":"/actuator/prometheus"}` | serviceMonitor for prometheus operator |
| master.service.serviceMonitor.annotations | object | `{}` | serviceMonitor.annotations ServiceMonitor annotations |
| master.service.serviceMonitor.enabled | bool | `false` | Enable or disable master serviceMonitor |
| master.service.serviceMonitor.interval | string | `"15s"` | serviceMonitor.interval interval at which metrics should be scraped |
| master.service.serviceMonitor.labels | object | `{}` | serviceMonitor.labels ServiceMonitor extra labels |
| master.service.serviceMonitor.path | string | `"/actuator/prometheus"` | serviceMonitor.path path of the metrics endpoint |
| master.tolerations | list | `[]` | Tolerations are appended (excluding duplicates) to pods running with this RuntimeClass during admission, effectively unioning the set of nodes tolerated by the pod and the RuntimeClass. |
| master.updateStrategy | object | `{"type":"RollingUpdate"}` | Update strategy ref: https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/#update-strategies  |
| minio.auth.rootPassword | string | `"minioadmin"` | minio password |
| minio.auth.rootUser | string | `"minioadmin"` | minio username |
| minio.defaultBuckets | string | `"dolphinscheduler"` | minio default buckets |
| minio.enabled | bool | `true` | Deploy minio and configure it as the default storage for DolphinScheduler, note this is for demo only, not for production. |
| minio.persistence.enabled | bool | `false` | Set minio.persistence.enabled to true to mount a new volume for internal minio |
| mysql.auth.database | string | `"dolphinscheduler"` | mysql database |
| mysql.auth.params | string | `"characterEncoding=utf8"` | mysql params |
| mysql.auth.password | string | `"ds"` | mysql password |
| mysql.auth.username | string | `"ds"` | mysql username |
| mysql.driverClassName | string | `"com.mysql.cj.jdbc.Driver"` | mysql driverClassName |
| mysql.enabled | bool | `false` | If not exists external MySQL, by default, the DolphinScheduler will use a internal MySQL |
| mysql.primary.persistence.enabled | bool | `false` | Set mysql.primary.persistence.enabled to true to mount a new volume for internal MySQL |
| mysql.primary.persistence.size | string | `"20Gi"` | `PersistentVolumeClaim` size |
| mysql.primary.persistence.storageClass | string | `"-"` | MySQL data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| postgresql.driverClassName | string | `"org.postgresql.Driver"` | The driverClassName for internal PostgreSQL |
| postgresql.enabled | bool | `true` | If not exists external PostgreSQL, by default, the DolphinScheduler will use a internal PostgreSQL |
| postgresql.params | string | `"characterEncoding=utf8"` | The params for internal PostgreSQL |
| postgresql.persistence.enabled | bool | `false` | Set postgresql.persistence.enabled to true to mount a new volume for internal PostgreSQL |
| postgresql.persistence.size | string | `"20Gi"` | `PersistentVolumeClaim` size |
| postgresql.persistence.storageClass | string | `"-"` | PostgreSQL data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| postgresql.postgresqlDatabase | string | `"dolphinscheduler"` | The database for internal PostgreSQL |
| postgresql.postgresqlPassword | string | `"root"` | The password for internal PostgreSQL |
| postgresql.postgresqlUsername | string | `"root"` | The username for internal PostgreSQL |
| registryEtcd.authority | string | `""` | Etcd authority |
| registryEtcd.enabled | bool | `false` | If you want to use Etcd for your registry center, change this value to true. And set zookeeper.enabled to false |
| registryEtcd.endpoints | string | `""` | Etcd endpoints |
| registryEtcd.namespace | string | `"dolphinscheduler"` | Etcd namespace |
| registryEtcd.passWord | string | `""` | Etcd passWord |
| registryEtcd.ssl.certFile | string | `"etcd-certs/ca.crt"` | CertFile file path |
| registryEtcd.ssl.enabled | bool | `false` | If your Etcd server has configured with ssl, change this value to true. About certification files you can see [here](https://github.com/etcd-io/jetcd/blob/main/docs/SslConfig.md) for how to convert. |
| registryEtcd.ssl.keyCertChainFile | string | `"etcd-certs/client.crt"` | keyCertChainFile file path |
| registryEtcd.ssl.keyFile | string | `"etcd-certs/client.pem"` | keyFile file path |
| registryEtcd.user | string | `""` | Etcd user |
| registryJdbc.enabled | bool | `false` | If you want to use JDbc for your registry center, change this value to true. And set zookeeper.enabled and registryEtcd.enabled to false |
| registryJdbc.hikariConfig.driverClassName | string | `"com.mysql.cj.jdbc.Driver"` | Default use same Dolphinscheduler's database if you don't change this value. If you set this value, Registry jdbc's database type will use it |
| registryJdbc.hikariConfig.enabled | bool | `false` | Default use same Dolphinscheduler's database, if you want to use other database please change `enabled` to `true` and change other configs |
| registryJdbc.hikariConfig.jdbcurl | string | `"jdbc:mysql://"` | Default use same Dolphinscheduler's database if you don't change this value. If you set this value, Registry jdbc's database type will use it |
| registryJdbc.hikariConfig.password | string | `""` | Default use same Dolphinscheduler's database if you don't change this value. If you set this value, Registry jdbc's database type will use it |
| registryJdbc.hikariConfig.username | string | `""` | Default use same Dolphinscheduler's database if you don't change this value. If you set this value, Registry jdbc's database type will use it |
| registryJdbc.termExpireTimes | int | `3` | Used to calculate the expire time |
| registryJdbc.termRefreshInterval | string | `"2s"` | Used to schedule refresh the ephemeral data/ lock |
| security.authentication.ldap.basedn | string | `"dc=example,dc=com"` | LDAP base dn |
| security.authentication.ldap.password | string | `"password"` | LDAP password |
| security.authentication.ldap.ssl.enable | bool | `false` | LDAP ssl switch |
| security.authentication.ldap.ssl.jksbase64content | string | `""` | LDAP jks file base64 content. If you use macOS, please run `base64 -b 0 -i /path/to/your.jks`. If you use Linux, please run `base64 -w 0 /path/to/your.jks`. If you use Windows, please run `certutil -f -encode /path/to/your.jks`. Then copy the base64 content to below field in one line |
| security.authentication.ldap.ssl.truststore | string | `"/opt/ldapkeystore.jks"` | LDAP jks file absolute path, do not change this value |
| security.authentication.ldap.ssl.truststorepassword | string | `""` | LDAP jks password |
| security.authentication.ldap.urls | string | `"ldap://ldap.forumsys.com:389/"` | LDAP urls |
| security.authentication.ldap.user.admin | string | `"read-only-admin"` | Admin user account when you log-in with LDAP |
| security.authentication.ldap.user.emailattribute | string | `"mail"` | LDAP user email attribute |
| security.authentication.ldap.user.identityattribute | string | `"uid"` | LDAP user identity attribute |
| security.authentication.ldap.user.notexistaction | string | `"CREATE"` | action when ldap user is not exist,default value: CREATE. Optional values include(CREATE,DENY) |
| security.authentication.ldap.username | string | `"cn=read-only-admin,dc=example,dc=com"` | LDAP username |
| security.authentication.type | string | `"PASSWORD"` | Authentication types (supported types: PASSWORD,LDAP,CASDOOR_SSO) |
| timezone | string | `"Asia/Shanghai"` | World time and date for cities in all time zones |
| worker.affinity | object | `{}` | Affinity is a group of affinity scheduling rules. If specified, the pod's scheduling constraints. More info: [node-affinity](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/#node-affinity) |
| worker.annotations | object | `{}` | You can use annotations to attach arbitrary non-identifying metadata to objects. Clients such as tools and libraries can retrieve this metadata. |
| worker.customizedConfig | object | `{}` | configure aligned with https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-worker/src/main/resources/application.yaml |
| worker.enableCustomizedConfig | bool | `false` | enable configure custom config |
| worker.enabled | bool | `true` | Enable or disable the Worker component |
| worker.env.WORKER_EXEC_THREADS | string | `"100"` | Worker execute thread number to limit task instances |
| worker.env.WORKER_HOST_WEIGHT | string | `"100"` | Worker host weight to dispatch tasks |
| worker.env.WORKER_MAX_HEARTBEAT_INTERVAL | string | `"10s"` | Worker heartbeat interval |
| worker.env.WORKER_SERVER_LOAD_PROTECTION_ENABLED | bool | `false` | If set true, will open worker overload protection |
| worker.env.WORKER_SERVER_LOAD_PROTECTION_MAX_DISK_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Worker max disk usage , when the worker's disk usage is smaller then this value, worker server can be dispatched tasks. |
| worker.env.WORKER_SERVER_LOAD_PROTECTION_MAX_JVM_CPU_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Worker max jvm cpu usage, when the worker's jvm cpu usage is smaller then this value, worker server can be dispatched tasks. |
| worker.env.WORKER_SERVER_LOAD_PROTECTION_MAX_SYSTEM_CPU_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Worker max system cpu usage, when the worker's system cpu usage is smaller then this value, worker server can be dispatched tasks. |
| worker.env.WORKER_SERVER_LOAD_PROTECTION_MAX_SYSTEM_MEMORY_USAGE_PERCENTAGE_THRESHOLDS | float | `0.7` | Worker max memory usage , when the worker's memory usage is smaller then this value, worker server can be dispatched tasks. |
| worker.env.WORKER_TENANT_CONFIG_AUTO_CREATE_TENANT_ENABLED | bool | `true` | tenant corresponds to the user of the system, which is used by the worker to submit the job. If system does not have this user, it will be automatically created after the parameter worker.tenant.auto.create is true. |
| worker.env.WORKER_TENANT_CONFIG_DEFAULT_TENANT_ENABLED | bool | `false` | If set true, will use worker bootstrap user as the tenant to execute task when the tenant is `default`; |
| worker.keda.advanced | object | `{}` | Specify HPA related options |
| worker.keda.cooldownPeriod | int | `30` | How many seconds KEDA will wait before scaling to zero. Note that HPA has a separate cooldown period for scale-downs |
| worker.keda.enabled | bool | `false` | Enable or disable the Keda component |
| worker.keda.maxReplicaCount | int | `3` | Maximum number of workers created by keda |
| worker.keda.minReplicaCount | int | `0` | Minimum number of workers created by keda |
| worker.keda.namespaceLabels | object | `{}` | Keda namespace labels |
| worker.keda.pollingInterval | int | `5` | How often KEDA polls the DolphinScheduler DB to report new scale requests to the HPA |
| worker.livenessProbe.enabled | bool | `true` | Turn on and off liveness probe |
| worker.livenessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| worker.livenessProbe.initialDelaySeconds | string | `"30"` | Delay before liveness probe is initiated |
| worker.livenessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| worker.livenessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| worker.livenessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| worker.nodeSelector | object | `{}` | NodeSelector is a selector which must be true for the pod to fit on a node. Selector which must match a node's labels for the pod to be scheduled on that node. More info: [assign-pod-node](https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/) |
| worker.persistentVolumeClaim | object | `{"dataPersistentVolume":{"accessModes":["ReadWriteOnce"],"enabled":false,"storage":"20Gi","storageClassName":"-"},"enabled":false,"logsPersistentVolume":{"accessModes":["ReadWriteOnce"],"enabled":false,"storage":"20Gi","storageClassName":"-"}}` | PersistentVolumeClaim represents a reference to a PersistentVolumeClaim in the same namespace. The StatefulSet controller is responsible for mapping network identities to claims in a way that maintains the identity of a pod. Every claim in this list must have at least one matching (by name) volumeMount in one container in the template. A claim in this list takes precedence over any volumes in the template, with the same name. |
| worker.persistentVolumeClaim.dataPersistentVolume.accessModes | list | `["ReadWriteOnce"]` | `PersistentVolumeClaim` access modes |
| worker.persistentVolumeClaim.dataPersistentVolume.enabled | bool | `false` | Set `worker.persistentVolumeClaim.dataPersistentVolume.enabled` to `true` to mount a data volume for `worker` |
| worker.persistentVolumeClaim.dataPersistentVolume.storage | string | `"20Gi"` | `PersistentVolumeClaim` size |
| worker.persistentVolumeClaim.dataPersistentVolume.storageClassName | string | `"-"` | `Worker` data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| worker.persistentVolumeClaim.enabled | bool | `false` | Set `worker.persistentVolumeClaim.enabled` to `true` to enable `persistentVolumeClaim` for `worker` |
| worker.persistentVolumeClaim.logsPersistentVolume.accessModes | list | `["ReadWriteOnce"]` | `PersistentVolumeClaim` access modes |
| worker.persistentVolumeClaim.logsPersistentVolume.enabled | bool | `false` | Set `worker.persistentVolumeClaim.logsPersistentVolume.enabled` to `true` to mount a logs volume for `worker` |
| worker.persistentVolumeClaim.logsPersistentVolume.storage | string | `"20Gi"` | `PersistentVolumeClaim` size |
| worker.persistentVolumeClaim.logsPersistentVolume.storageClassName | string | `"-"` | `Worker` logs data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| worker.podManagementPolicy | string | `"Parallel"` | PodManagementPolicy controls how pods are created during initial scale up, when replacing pods on nodes, or when scaling down. |
| worker.readinessProbe | object | `{"enabled":true,"failureThreshold":"3","initialDelaySeconds":"30","periodSeconds":"30","successThreshold":"1","timeoutSeconds":"5"}` | Periodic probe of container service readiness. Container will be removed from service endpoints if the probe fails. More info: [container-probes](https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#container-probes) |
| worker.readinessProbe.enabled | bool | `true` | Turn on and off readiness probe |
| worker.readinessProbe.failureThreshold | string | `"3"` | Minimum consecutive failures for the probe |
| worker.readinessProbe.initialDelaySeconds | string | `"30"` | Delay before readiness probe is initiated |
| worker.readinessProbe.periodSeconds | string | `"30"` | How often to perform the probe |
| worker.readinessProbe.successThreshold | string | `"1"` | Minimum consecutive successes for the probe |
| worker.readinessProbe.timeoutSeconds | string | `"5"` | When the probe times out |
| worker.replicas | string | `"3"` | Replicas is the desired number of replicas of the given Template. |
| worker.resources | object | `{}` | Compute Resources required by this container. More info: [manage-resources-containers](https://kubernetes.io/docs/concepts/configuration/manage-resources-containers/) |
| worker.service.annotations | object | `{}` | annotations may need to be set when want to scrapy metrics by prometheus but not install prometheus operator |
| worker.service.serviceMonitor | object | `{"annotations":{},"enabled":false,"interval":"15s","labels":{},"path":"/actuator/prometheus"}` | serviceMonitor for prometheus operator |
| worker.service.serviceMonitor.annotations | object | `{}` | serviceMonitor.annotations ServiceMonitor annotations |
| worker.service.serviceMonitor.enabled | bool | `false` | Enable or disable worker serviceMonitor |
| worker.service.serviceMonitor.interval | string | `"15s"` | serviceMonitor.interval interval at which metrics should be scraped |
| worker.service.serviceMonitor.labels | object | `{}` | serviceMonitor.labels ServiceMonitor extra labels |
| worker.service.serviceMonitor.path | string | `"/actuator/prometheus"` | serviceMonitor.path path of the metrics endpoint |
| worker.tolerations | list | `[]` | Tolerations are appended (excluding duplicates) to pods running with this RuntimeClass during admission, effectively unioning the set of nodes tolerated by the pod and the RuntimeClass. |
| worker.updateStrategy | object | `{"type":"RollingUpdate"}` | Update strategy ref: https://kubernetes.io/docs/concepts/workloads/controllers/statefulset/#update-strategies  |
| zookeeper.enabled | bool | `true` | If not exists external registry, the zookeeper registry will be used by default. |
| zookeeper.fourlwCommandsWhitelist | string | `"srvr,ruok,wchs,cons"` | A list of comma separated Four Letter Words commands to use |
| zookeeper.persistence.enabled | bool | `false` | Set `zookeeper.persistence.enabled` to true to mount a new volume for internal ZooKeeper |
| zookeeper.persistence.size | string | `"20Gi"` | PersistentVolumeClaim size |
| zookeeper.persistence.storageClass | string | `"-"` | ZooKeeper data persistent volume storage class. If set to "-", storageClassName: "", which disables dynamic provisioning |
| zookeeper.service.port | int | `2181` | The port of zookeeper |
