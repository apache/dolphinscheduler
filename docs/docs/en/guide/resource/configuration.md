# Resource Center Configuration

- You could use `Resource Center` to upload text files, UDFs and other task-related files.
- You could configure `Resource Center` to use distributed file system like [Hadoop](https://hadoop.apache.org/docs/r2.7.0/) (2.6+), [MinIO](https://github.com/minio/minio) cluster or remote storage products like [AWS S3](https://aws.amazon.com/s3/), [Alibaba Cloud OSS](https://www.aliyun.com/product/oss), [Huawei Cloud OBS](https://support.huaweicloud.com/obs/index.html) etc.
- You could configure `Resource Center` to use local file system. If you deploy `DolphinScheduler` in `Standalone` mode, you could configure it to use local file system for `Resource Center` without the need of an external `HDFS` system or `S3`.
- Furthermore, if you deploy `DolphinScheduler` in `Cluster` mode, you could use [S3FS-FUSE](https://github.com/s3fs-fuse/s3fs-fuse) to mount `S3` or [JINDO-FUSE](https://help.aliyun.com/document_detail/187410.html) to mount `OSS` to your machines and use the local file system for `Resource Center`. In this way, you could operate remote files as if on your local machines.

## Use Local File System

### Configure `common.properties`

DolphinScheduler Resource Center uses local file system by default, and does not require any additional configuration.
But please make sure to change the following configuration at the same time when you need to modify the default value.

- If you deploy DolphinScheduler in `Cluster` or `Pseudo-Cluster` mode, you need to configure `api-server/conf/common.properties` and `worker-server/conf/common.properties`.
- If you deploy DolphinScheduler in `Standalone` mode, you only need to configure `standalone-server/conf/common.properties` as follows:

The configuration you may need to change:

- Change `resource.storage.upload.base.path` to your local directory path. Please make sure the `tenant resource.hdfs.root.user` has read and write permissions for `resource.storage.upload.base.path`, e,g. `/tmp/dolphinscheduler`. `DolphinScheduler` will create the directory you configure if it does not exist.

> NOTE:
> 1. LOCAL mode does not support reading and writing in distributed mode, which mean you can only use your resource in one machine, unless use shared file mount point
> 2. Please modify the value of `resource.storage.upload.base.path` if you do not want to use the default value as the base path.
> 3. The local config is `resource.storage.type=LOCAL` it has actually configured two setting, `resource.storage.type=HDFS`
> and `resource.hdfs.fs.defaultFS=file:///`, The configuration of `resource.storage.type=LOCAL` is for user-friendly, and enables
> the local resource center to be enabled by default

## connect AWS S3

if you want to upload resources to `Resource Center` connected to `S3`, you need to configure `api-server/conf/common.properties`, `api-server/conf/aws.yaml` and `worker-server/conf/common.properties`, `worker-server/conf/aws.yaml`. You can refer to the following:

config the following fields

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

## connect OSS S3

if you want to upload resources to `Resource Center` connected to `OSS`, you need to configure `api-server/conf/common.properties` and `worker-server/conf/common.properties`. You can refer to the following:

config the following fields

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

## connect OBS S3

if you want to upload resources to `Resource Center` connected to `OBS`, you need to configure `api-server/conf/common.properties` and `worker-server/conf/common.properties`. You can refer to the following:

config the following fields

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

> **Note:**
>
> * If only the `api-server/conf/common.properties` file is configured, then resource uploading is enabled, but you can not use resources in task. If you want to use or execute the files in the workflow you need to configure `worker-server/conf/common.properties` too.
> * If you want to use the resource upload function, the deployment user in [installation and deployment](../installation/standalone.md) must have relevant operation authority.
> * If you using a Hadoop cluster with HA, you need to enable HDFS resource upload, and you need to copy the `core-site.xml` and `hdfs-site.xml` under the Hadoop cluster to `worker-server/conf` and `api-server/conf`, otherwise skip this copy step.

