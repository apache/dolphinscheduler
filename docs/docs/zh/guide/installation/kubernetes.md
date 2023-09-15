# 快速试用 Kubernetes 部署

Kubernetes 部署目的是在 Kubernetes 集群中部署 DolphinScheduler 服务，能调度大量任务，可用于在生产中部署。

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用[Standalone](standalone.md)方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用[伪集群部署](pseudo-cluster.md)。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

## 先决条件

- [Helm](https://helm.sh/) 3.1.0+
- [Kubernetes](https://kubernetes.io/) 1.12+
- PV 供应(需要基础设施支持)

## 安装 dolphinscheduler

请下载源码包 apache-dolphinscheduler-<version>-src.tar.gz，下载地址: [下载](https://dolphinscheduler.apache.org/zh-cn/download)

发布一个名为 `dolphinscheduler` 的版本(release)，请执行以下命令：

```
$ tar -zxvf apache-dolphinscheduler-<version>-src.tar.gz
$ cd apache-dolphinscheduler-<version>-src/deploy/kubernetes/dolphinscheduler
$ helm repo add bitnami https://charts.bitnami.com/bitnami
$ helm dependency update .
$ helm install dolphinscheduler . --set image.tag=<version>
```

将名为 `dolphinscheduler` 的版本(release) 发布到 `test` 的命名空间中：

```bash
$ helm install dolphinscheduler . -n test
```

> **提示**: 如果名为 `test` 的命名空间被使用, 选项参数 `-n test` 需要添加到 `helm` 和 `kubectl` 命令中

这些命令以默认配置在 Kubernetes 集群上部署 DolphinScheduler，[附录-配置](#appendix-configuration)部分列出了可以在安装过程中配置的参数 <!-- markdown-link-check-disable-line -->

> **提示**: 列出所有已发布的版本，使用 `helm list`

**PostgreSQL** (用户 `root`, 密码 `root`, 数据库 `dolphinscheduler`) 和 **ZooKeeper** 服务将会默认启动

## 访问 DolphinScheduler 前端页面

如果 `values.yaml` 文件中的 `ingress.enabled` 被设置为 `true`, 在浏览器中访问 `http://${ingress.host}/dolphinscheduler` 即可

> **提示**: 如果 ingress 访问遇到问题，请联系 Kubernetes 管理员并查看 [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)

否则，当 `api.service.type=ClusterIP` 时，你需要执行 port-forward 端口转发命令：

```bash
$ kubectl port-forward --address 0.0.0.0 svc/dolphinscheduler-api 12345:12345
$ kubectl port-forward --address 0.0.0.0 -n test svc/dolphinscheduler-api 12345:12345 # 使用 test 命名空间
```

> **提示**: 如果出现 `unable to do port forwarding: socat not found` 错误, 需要先安装 `socat`

访问前端页面：http://localhost:12345/dolphinscheduler/ui，如果有需要请修改成对应的 IP 地址

或者当 `api.service.type=NodePort` 时，你需要执行命令：

```bash
NODE_IP=$(kubectl get no -n {{ .Release.Namespace }} -o jsonpath="{.items[0].status.addresses[0].address}")
NODE_PORT=$(kubectl get svc {{ template "dolphinscheduler.fullname" . }}-api -n {{ .Release.Namespace }} -o jsonpath="{.spec.ports[0].nodePort}")
echo http://$NODE_IP:$NODE_PORT/dolphinscheduler
```

然后访问前端页面: http://localhost:12345/dolphinscheduler/ui

默认的用户是`admin`，默认的密码是`dolphinscheduler123`

请参考用户手册章节的[快速上手](../start/quick-start.md)查看如何使用 DolphinScheduler

## 卸载 dolphinscheduler

卸载名为 `dolphinscheduler` 的版本(release)，请执行：

```bash
$ helm uninstall dolphinscheduler
```

该命令将删除与 `dolphinscheduler` 相关的所有 Kubernetes 组件（但 PVC 除外），并删除版本(release)

要删除与 `dolphinscheduler` 相关的 PVC，请执行：

```bash
$ kubectl delete pvc -l app.kubernetes.io/instance=dolphinscheduler
```

> **注意**: 删除 PVC 也会删除所有数据，请谨慎操作！

## [试验性] worker 自动扩缩容

> **警告**: 目前此功能尚在试验阶段，不建议在生产环境使用！

`DolphinScheduler` 使用 [KEDA](https://github.com/kedacore/keda) 对 worker 进行自动扩缩容。但是 `DolphinScheduler` 默认是不启用该功能的。
您需要做下列配置来启用该功能：

首先您需要创建一个单独的命名空间并使用 `helm` 安装 `KEDA`：

```bash
helm repo add kedacore https://kedacore.github.io/charts

helm repo update

kubectl create namespace keda

helm install keda kedacore/keda \
    --namespace keda \
    --version "v2.0.0"
```

其次，您需要将 `values.yaml` 中的 `worker.keda.enabled` 配置设置成 `true`，或者您可以通过以下命令安装 chart：

```bash
helm install dolphinscheduler . --set worker.keda.enabled=true -n <your-namespace-to-deploy-dolphinscheduler>
```

一旦自动扩缩容功能启用，worker的数量将基于任务状态在 `minReplicaCount` 和 `maxReplicaCount` 之间弹性扩缩。
举例来说，当您的 `DolphinScheduler` 实例中没有任务在运行时，将不会有 worker。因此，这个功能会显著节约资源，降低您的使用成本。

自动扩缩容功能目前支持 `DolphinScheduler 官方 helm chart` 中自带的 `postgresql` and `mysql`。
如果您要使用外部的数据库，自动扩缩容功能目前只支持 `mysql` 和 `postgresql` 类型的外部数据库。

如果您在使用自动扩缩容时需要改变 worker `WORKER_EXEC_THREADS` 的值，请直接在 `values.yaml` 中修改 `worker.env.WORKER_EXEC_THREADS` 的值，
而不要通过 `configmap` 来更新。

## 配置

配置文件为 `values.yaml`，[附录-配置](#appendix-configuration) 表格列出了 DolphinScheduler 的可配置参数及其默认值 <!-- markdown-link-check-disable-line -->

## 支持矩阵

|                             Type                             |  支持  |         备注         |
|--------------------------------------------------------------|------|--------------------|
| Shell                                                        | 是    |                    |
| Python2                                                      | 是    |                    |
| Python3                                                      | 间接支持 | 详见 FAQ             |
| Hadoop2                                                      | 间接支持 | 详见 FAQ             |
| Hadoop3                                                      | 尚未确定 | 尚未测试               |
| Spark-Local(client)                                          | 间接支持 | 详见 FAQ             |
| Spark-YARN(cluster)                                          | 间接支持 | 详见 FAQ             |
| Spark-Standalone(cluster)                                    | 尚不   |                    |
| Spark-Kubernetes(cluster)                                    | 尚不   |                    |
| Flink-Local(local>=1.11)                                     | 尚不   | Generic CLI 模式尚未支持 |
| Flink-YARN(yarn-cluster)                                     | 间接支持 | 详见 FAQ             |
| Flink-YARN(yarn-session/yarn-per-job/yarn-application>=1.11) | 尚不   | Generic CLI 模式尚未支持 |
| Flink-Standalone(default)                                    | 尚不   |                    |
| Flink-Standalone(remote>=1.11)                               | 尚不   | Generic CLI 模式尚未支持 |
| Flink-Kubernetes(default)                                    | 尚不   |                    |
| Flink-Kubernetes(remote>=1.11)                               | 尚不   | Generic CLI 模式尚未支持 |
| Flink-NativeKubernetes(kubernetes-session/application>=1.11) | 尚不   | Generic CLI 模式尚未支持 |
| MapReduce                                                    | 间接支持 | 详见 FAQ             |
| Kerberos                                                     | 间接支持 | 详见 FAQ             |
| HTTP                                                         | 是    |                    |
| DataX                                                        | 间接支持 | 详见 FAQ             |
| Sqoop                                                        | 间接支持 | 详见 FAQ             |
| SQL-MySQL                                                    | 间接支持 | 详见 FAQ             |
| SQL-PostgreSQL                                               | 是    |                    |
| SQL-Hive                                                     | 间接支持 | 详见 FAQ             |
| SQL-Spark                                                    | 间接支持 | 详见 FAQ             |
| SQL-ClickHouse                                               | 间接支持 | 详见 FAQ             |
| SQL-Oracle                                                   | 间接支持 | 详见 FAQ             |
| SQL-SQLServer                                                | 间接支持 | 详见 FAQ             |
| SQL-DB2                                                      | 间接支持 | 详见 FAQ             |

## FAQ

### 如何查看一个 pod 容器的日志？

列出所有 pods (别名 `po`):

```
kubectl get po
kubectl get po -n test # with test namespace
```

查看名为 dolphinscheduler-master-0 的 pod 容器的日志:

```
kubectl logs dolphinscheduler-master-0
kubectl logs -f dolphinscheduler-master-0 # 跟随日志输出
kubectl logs --tail 10 dolphinscheduler-master-0 -n test # 显示倒数10行日志
```

### 如何在 Kubernetes 上扩缩容 api, master 和 worker？

列出所有 deployments (别名 `deploy`):

```
kubectl get deploy
kubectl get deploy -n test # with test namespace
```

扩缩容 api 至 3 个副本:

```
kubectl scale --replicas=3 deploy dolphinscheduler-api
kubectl scale --replicas=3 deploy dolphinscheduler-api -n test # with test namespace
```

列出所有 statefulsets (别名 `sts`):

```
kubectl get sts
kubectl get sts -n test # with test namespace
```

扩缩容 master 至 2 个副本:

```
kubectl scale --replicas=2 sts dolphinscheduler-master
kubectl scale --replicas=2 sts dolphinscheduler-master -n test # with test namespace
```

扩缩容 worker 至 6 个副本:

```
kubectl scale --replicas=6 sts dolphinscheduler-worker
kubectl scale --replicas=6 sts dolphinscheduler-worker -n test # with test namespace
```

### 如何用 MySQL 替代 PostgreSQL 作为 DolphinScheduler 的数据库？

> 由于商业许可证的原因，我们不能直接使用 MySQL 的驱动包.
>
> 如果你要使用 MySQL, 你可以基于官方镜像 `apache/dolphinscheduler-<service>` 进行构建.
>
> 从 3.0.0 版本起，dolphinscheduler 已经微服务化，更改元数据存储需要对把所有的服务都替换为 MySQL 驱动包，包括 dolphinscheduler-tools, dolphinscheduler-master, dolphinscheduler-worker, dolphinscheduler-api, dolphinscheduler-alert-server .

1. 下载 MySQL 驱动包 [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)

2. 创建一个新的 `Dockerfile`，用于添加 MySQL 的驱动包:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:<version>
# 例如
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-tools:<version>

# 注意，如果构建的是dolphinscheduler-tools镜像
# 需要将下面一行修改为COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/tools/libs
# 其他服务保持不变即可
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs
```

3. 构建一个包含 MySQL 驱动包的新镜像:

```
docker build -t apache/dolphinscheduler-<service>:mysql-driver .
```

4. 推送 docker 镜像 `apache/dolphinscheduler-<service>:mysql-driver` 到一个 docker registry 中

5. 修改 `values.yaml` 文件中 image 的 `repository` 字段，并更新 `tag` 为 `mysql-driver`

6. 修改 `values.yaml` 文件中 postgresql 的 `enabled` 为 `false`

7. 修改 `values.yaml` 文件中的 externalDatabase 配置 (尤其修改 `host`, `username` 和 `password`)

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

8. 部署 dolphinscheduler (详见**安装 dolphinscheduler**)

### 如何在数据源中心支持 MySQL 或者 Oracle 数据源？

> 由于商业许可证的原因，我们不能直接使用 MySQL 或者 Oracle 的驱动包.
>
> 如果你要添加 MySQL 或者 Oracle, 你可以基于官方镜像 `apache/dolphinscheduler-<service>` 进行构建.
>
> 需要更改 dolphinscheduler-worker, dolphinscheduler-api 两个服务的镜像.

1. 下载 MySQL 驱动包 [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)
   或者 Oracle 驱动包 [ojdbc8.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/) (例如 `ojdbc8-19.9.0.0.jar`)

2. 创建一个新的 `Dockerfile`，用于添加 MySQL 或者 Oracle 驱动包:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:<version>
# 例如
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>

# 如果你想支持 MySQL 数据源
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs

# 如果你想支持 Oracle 数据源
COPY ojdbc8-19.9.0.0.jar /opt/dolphinscheduler/libs
```

3. 构建一个包含 MySQL 或者 Oracle 驱动包的新镜像:

```
docker build -t apache/dolphinscheduler-<service>:new-driver .
```

4. 推送 docker 镜像 `apache/dolphinscheduler-<service>:new-driver` 到一个 docker registry 中

5. 修改 `values.yaml` 文件中 image 的 `repository` 字段，并更新 `tag` 为 `new-driver`

6. 部署 dolphinscheduler (详见**安装 dolphinscheduler**)

7. 在数据源中心添加一个 MySQL 或者 Oracle 数据源

### 如何支持 Python 2 pip 以及自定义 requirements.txt？

> 只需要更改 dolphinscheduler-worker 服务的镜像.

1. 创建一个新的 `Dockerfile`，用于安装 pip:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>
COPY requirements.txt /tmp
RUN apt-get update && \
    apt-get install -y --no-install-recommends python-pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    rm -rf /var/lib/apt/lists/*
```

这个命令会安装默认的 **pip 18.1**. 如果你想升级 pip, 只需添加一行

```
pip install --no-cache-dir -U pip && \
```

2. 构建一个包含 pip 的新镜像:

```
docker build -t apache/dolphinscheduler-worker:pip .
```

3. 推送 docker 镜像 `apache/dolphinscheduler-worker:pip` 到一个 docker registry 中

4. 修改 `values.yaml` 文件中 image 的 `repository` 字段，并更新 `tag` 为 `pip`

5. 部署 dolphinscheduler (详见**安装 dolphinscheduler**)

6. 在一个新 Python 任务下验证 pip

### 如何支持 Python 3？

> 只需要更改 dolphinscheduler-worker 服务的镜像.

1. 创建一个新的 `Dockerfile`，用于安装 Python 3:

```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 && \
    rm -rf /var/lib/apt/lists/*
```

这个命令会安装默认的 **Python 3.7.3**. 如果你也想安装 **pip3**, 将 `python3` 替换为 `python3-pip` 即可

```
apt-get install -y --no-install-recommends python3-pip && \
```

2. 构建一个包含 Python 3 的新镜像:

```
docker build -t apache/dolphinscheduler-worker:python3 .
```

3. 推送 docker 镜像 `apache/dolphinscheduler-worker:python3` 到一个 docker registry 中

4. 修改 `values.yaml` 文件中 image 的 `repository` 字段，并更新 `tag` 为 `python3`

5. 修改 `values.yaml` 文件中的 `PYTHON_LAUNCHER` 为 `/usr/bin/python3`

6. 部署 dolphinscheduler (详见**安装 dolphinscheduler**)

7. 在一个新 Python 任务下验证 Python 3

### 如何支持 Hadoop, Spark, Flink, Hive 或 DataX？

以 Spark 2.4.7 为例:

1. 下载 Spark 2.4.7 发布的二进制包 `spark-2.4.7-bin-hadoop2.7.tgz`

2. 确保 `common.sharedStoragePersistence.enabled` 开启

3. 部署 dolphinscheduler (详见**安装 dolphinscheduler**)

4. 复制 Spark 3.1.1 二进制包到 Docker 容器中

```bash
kubectl cp spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft
kubectl cp -n test spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft # with test namespace
```

因为存储卷 `sharedStoragePersistence` 被挂载到 `/opt/soft`, 因此 `/opt/soft` 中的所有文件都不会丢失

5. 登录到容器并确保 `SPARK_HOME` 存在

```bash
kubectl exec -it dolphinscheduler-worker-0 bash
kubectl exec -n test -it dolphinscheduler-worker-0 bash # with test namespace
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # or just mv
$SPARK_HOME/bin/spark-submit --version
```

如果一切执行正常，最后一条命令将会打印 Spark 版本信息

6. 在一个 Shell 任务下验证 Spark

```
$SPARK_HOME/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME/examples/jars/spark-examples_2.11-2.4.7.jar
```

检查任务日志是否包含输出 `Pi is roughly 3.146015`

7. 在一个 Spark 任务下验证 Spark

文件 `spark-examples_2.11-2.4.7.jar` 需要先被上传到资源中心，然后创建一个 Spark 任务并设置:

- 主函数的 Class: `org.apache.spark.examples.SparkPi`
- 主程序包: `spark-examples_2.11-2.4.7.jar`
- 部署方式: `local`

同样地, 检查任务日志是否包含输出 `Pi is roughly 3.146015`

8. 验证 Spark on YARN

Spark on YARN (部署方式为 `cluster` 或 `client`) 需要 Hadoop 支持. 类似于 Spark 支持, 支持 Hadoop 的操作几乎和前面的步骤相同

确保 `$HADOOP_HOME` 和 `$HADOOP_CONF_DIR` 存在

### 如何在 Master、Worker 和 Api 服务之间支持共享存储？

例如, Master、Worker 和 Api 服务可能同时使用 Hadoop

1. 修改 `values.yaml` 文件中下面的配置项

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

`storageClassName` 和 `storage` 需要被修改为实际值

> **注意**: `storageClassName` 必须支持访问模式: `ReadWriteMany`

2. 将 Hadoop 复制到目录 `/opt/soft`

3. 确保 `$HADOOP_HOME` 和 `$HADOOP_CONF_DIR` 正确

### 如何支持本地文件存储而非 HDFS 和 S3？

修改 `values.yaml` 文件中下面的配置项

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

`storageClassName` 和 `storage` 需要被修改为实际值

> **注意**: `storageClassName` 必须支持访问模式: `ReadWriteMany`

### 如何支持 S3 资源存储，例如 MinIO？

以 MinIO 为例: 修改 `values.yaml` 文件中下面的配置项

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

`BUCKET_NAME`, `MINIO_IP`, `MINIO_ACCESS_KEY` 和 `MINIO_SECRET_KEY` 需要被修改为实际值

> **注意**: `MINIO_IP` 只能使用 IP 而非域名, 因为 DolphinScheduler 尚不支持 S3 路径风格访问 (S3 path style access)

### 如何配置 SkyWalking？

修改 `values.yaml` 文件中的 SKYWALKING 配置项

```yaml
common:
  configmap:
    SKYWALKING_ENABLE: "true"
    SW_AGENT_COLLECTOR_BACKEND_SERVICES: "127.0.0.1:11800"
    SW_GRPC_LOG_SERVER_HOST: "127.0.0.1"
    SW_GRPC_LOG_SERVER_PORT: "11800"
```

## 附录-配置

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

