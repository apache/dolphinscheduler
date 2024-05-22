# 资源中心配置详情

- 资源中心通常用于上传文件、UDF 函数，以及任务组管理等操作。
- 资源中心可以对接分布式的文件存储系统，如[Hadoop](https://hadoop.apache.org/docs/r2.7.0/)（2.6+）或者[MinIO](https://github.com/minio/minio)集群，也可以对接远端的对象存储，如[AWS S3](https://aws.amazon.com/s3/)或者[阿里云 OSS](https://www.aliyun.com/product/oss)，[华为云 OBS](https://support.huaweicloud.com/obs/index.html) 等。
- 资源中心也可以直接对接本地文件系统。在单机模式下，您无需依赖`Hadoop`或`S3`一类的外部存储系统，可以方便地对接本地文件系统进行体验。
- 除此之外，对于集群模式下的部署，您可以通过使用[S3FS-FUSE](https://github.com/s3fs-fuse/s3fs-fuse)将`S3`挂载到本地，或者使用[JINDO-FUSE](https://help.aliyun.com/document_detail/187410.html)将`OSS`挂载到本地等，再用资源中心对接本地文件系统方式来操作远端对象存储中的文件。

## 对接本地文件系统

### 配置 `common.properties` 文件

Dolphinscheduler 资源中心使用本地系统默认是开启的，不需要用户做任何额外的配置，但是当用户需要对默认配置做修改时，请确保同时完成下面的修改。

- 如果您以 `集群` 模式或者 `伪集群` 模式部署DolphinScheduler，您需要对以下路径的文件进行配置：`api-server/conf/common.properties` 和 `worker-server/conf/common.properties`；
- 若您以 `单机` 模式部署DolphinScheduler，您只需要配置 `standalone-server/conf/common.properties`，具体配置如下：

您可能需要涉及如下的修改：

- 将 `resource.storage.upload.base.path` 改为本地存储路径，请确保部署 DolphinScheduler 的用户拥有读写权限，例如：`resource.storage.upload.base.path=/tmp/dolphinscheduler`。当路径不存在时会自动创建文件夹

> **注意**
> 1. LOCAL模式不支持分布式模式读写，意味着上传的资源只能在一台机器上使用，除非使用共享文件挂载点
> 2. 如果您不想用默认值作为资源中心的基础路径，请修改`resource.storage.upload.base.path`的值。
> 3. 当配置 `resource.storage.type=LOCAL`，其实您配置了两个配置项，分别是 `resource.storage.type=HDFS` 和 `resource.hdfs.fs.defaultFS=file:///` ，我们单独配置 `resource.storage.type=LOCAL` 这个值是为了
> 方便用户，并且能使得本地资源中心默认开启

## 对接AWS S3

如果需要使用到资源中心的 S3 上传资源，我们需要对以下路径的进行配置：`api-server/conf/common.properties`, `api-server/conf/aws.yaml` 和 `worker-server/conf/common.properties`, `worker-server/conf/aws.yaml`。可参考如下：

配置以下字段

```properties

resource.storage.type=S3
```

```yaml
aws:
    s3:
        # The AWS credentials provider type. support: AWSStaticCredentialsProvider, InstanceProfileCredentialsProvider
        # AWSStaticCredentialsProvider: use the access key and secret key to authenticate
        # InstanceProfileCredentialsProvider: use the IAM role to authenticate
        credentials.provider.type: AWSStaticCredentialsProvider
        access.key.id: <access.key.id>
        access.key.secret: <access.key.secret>
        region: <region>
        bucket.name: <bucket.name>
        endpoint: <endpoint>

```

## 对接阿里云 OSS

如果需要使用到资源中心的 OSS 上传资源，我们需要对以下路径的进行配置：`api-server/conf/common.properties` 和 `worker-server/conf/common.properties`。可参考如下：

```properties
# alibaba cloud access key id, required if you set resource.storage.type=OSS 
resource.alibaba.cloud.access.key.id=<your-access-key-id>
# alibaba cloud access key secret, required if you set resource.storage.type=OSS
resource.alibaba.cloud.access.key.secret=<your-access-key-secret>
# alibaba cloud region, required if you set resource.storage.type=OSS
resource.alibaba.cloud.region=cn-hangzhou
# oss bucket name, required if you set resource.storage.type=OSS
resource.alibaba.cloud.oss.bucket.name=dolphinscheduler
# oss bucket endpoint, required if you set resource.storage.type=OSS
resource.alibaba.cloud.oss.endpoint=https://oss-cn-hangzhou.aliyuncs.com

```

## 对接华为云 OBS

如果需要使用到资源中心的 OBS 上传资源，我们需要对以下路径的进行配置：`api-server/conf/common.properties` 和 `worker-server/conf/common.properties`。可参考如下：

```properties
# access key id, required if you set resource.storage.type=OBS
resource.huawei.cloud.access.key.id=<your-access-key-id>
# access key secret, required if you set resource.storage.type=OBS
resource.huawei.cloud.access.key.secret=<your-access-key-secret>
# oss bucket name, required if you set resource.storage.type=OBS
resource.huawei.cloud.obs.bucket.name=dolphinscheduler
# oss bucket endpoint, required if you set resource.storage.type=OBS
resource.huawei.cloud.obs.endpoint=obs.cn-southwest-2.huaweicloud.com

```

> **注意**：
>
> * 如果只配置了 `api-server/conf/common.properties` 的文件，则只是开启了资源上传的操作，并不能满足正常使用。如果想要在工作流中执行相关文件则需要额外配置 `worker-server/conf/common.properties`。
> * 如果用到资源上传的功能，那么[安装部署](../installation/standalone.md)中，部署用户需要有这部分的操作权限。
> * 如果 Hadoop 集群的 NameNode 配置了 HA 的话，需要开启 HDFS 类型的资源上传，同时需要将 Hadoop 集群下的 `core-site.xml` 和 `hdfs-site.xml` 复制到 `worker-server/conf` 以及 `api-server/conf`，非 NameNode HA 跳过此步骤。

