# 远程日志存储（Remote Logging）

Apache DolphinScheduler支持将任务日志传输到远端存储上。当配置开启远程日志存储后，DolphinScheduler将在任务结束后，将对应的任务日志异步地发送到指定的远端存储上。此外，用户在查看或下载任务日志时，若本地没有该日志文件，DolphinScheduler将从远端存储上下载对应的日志文件到本地文件系统。

## 开启远程日志存储

如果您以 `集群` 模式或者 `伪集群` 模式部署DolphinScheduler，您需要对以下路径的文件进行配置：`api-server/conf/common.properties`，`master-server/conf/common.properties`和 `worker-server/conf/common.properties`；
若您以 `单机` 模式部署DolphinScheduler，您只需要配置 `standalone-server/conf/common.properties`，具体配置如下：

```properties
# 是否开启远程日志存储
remote.logging.enable=true
# 任务日志写入的远端存储，目前支持OSS, S3, GCS
remote.logging.target=OSS
# 任务日志在远端存储上的目录
remote.logging.base.dir=logs
# 设置向远端存储异步发送日志的线程池大小
remote.logging.thread.pool.size=10
```

## 将任务日志写入[阿里云对象存储（OSS）](https://www.aliyun.com/product/oss)

配置`common.propertis`如下：

```properties
# oss access key id, required if you set remote.logging.target=OSS
remote.logging.oss.access.key.id=<access.key.id>
# oss access key secret, required if you set remote.logging.target=OSS
remote.logging.oss.access.key.secret=<access.key.secret>
# oss bucket name, required if you set remote.logging.target=OSS
remote.logging.oss.bucket.name=<bucket.name>
# oss endpoint, required if you set remote.logging.target=OSS
remote.logging.oss.endpoint=<endpoint>
```

## 将任务日志写入[Amazon S3](https://aws.amazon.com/cn/s3/)

配置`common.propertis`如下：

```properties
# s3 access key id, required if you set remote.logging.target=S3
remote.logging.s3.access.key.id=<access.key.id>
# s3 access key secret, required if you set remote.logging.target=S3
remote.logging.s3.access.key.secret=<access.key.secret>
# s3 bucket name, required if you set remote.logging.target=S3
remote.logging.s3.bucket.name=<bucket.name>
# s3 endpoint, required if you set remote.logging.target=S3
remote.logging.s3.endpoint=<endpoint>
# s3 region, required if you set remote.logging.target=S3
remote.logging.s3.region=<region>
```

## 将任务日志写入[Google Cloud Storage (GCS)](https://cloud.google.com/storage)

配置`common.propertis`如下：

```properties
# the location of the google cloud credential, required if you set remote.logging.target=GCS
remote.logging.google.cloud.storage.credential=/path/to/credential
# gcs bucket name, required if you set remote.logging.target=GCS
remote.logging.google.cloud.storage.bucket.name=<your-bucket>
```

## 将任务日志写入[Azure Blob Storage (ABS)](https://azure.microsoft.com/en-us/products/storage/blobs)

配置`common.propertis`如下：

```properties
# abs container name, required if you set resource.storage.type=ABS
resource.azure.blob.storage.container.name=<your-container>
# abs account name, required if you set resource.storage.type=ABS
resource.azure.blob.storage.account.name=<your-account-name>
# abs connection string, required if you set resource.storage.type=ABS
resource.azure.blob.storage.connection.string=<your-connection-string>
```

### 注意事项

由于Azure Blob Storage不支持空目录单独存在，因此资源目录下会有空文件`<no name>`。但是并不影响Dolphinscheduler资源中心上的文件展示。
