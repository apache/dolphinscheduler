# 리소스 센터 구성

- '리소스 센터'를 사용하여 텍스트 파일 및 기타 작업 관련 파일을 업로드할 수 있습니다.
- [Hadoop](https://hadoop.apache.org/docs/r2.7.0/)(2.6+), [MinIO](https://github.com/minio/minio) 클러스터와 같은 분산 파일 시스템이나 [AWS S3](https://aws.amazon.com/s3/), [Alibaba Cloud와 같은 원격 스토리지 제품을 사용하도록 `리소스 센터`를 구성할 수 있습니다.OSS](https://www.aliyun.com/product/oss), [Huawei Cloud OBS](https://support.huaweicloud.com/obs/index.html), [Tencent Cloud COS](https://cloud.tencent.com/product/cos) 등
- 로컬 파일 시스템을 사용하도록 `리소스 센터`를 구성할 수 있습니다.'DolphinScheduler'를 '독립 실행형' 모드로 배포하는 경우 외부 'HDFS' 시스템이나 'S3' 없이도 '리소스 센터'에 로컬 파일 시스템을 사용하도록 구성할 수 있습니다.
- 또한 `DolphinScheduler`를 `Cluster` 모드로 배포하는 경우 [S3FS-FUSE](https://github.com/s3fs-fuse/s3fs-fuse)를 사용하여 `S3`을 마운트하거나 [JINDO-FUSE](https://help.aliyun.com/document_detail/187410.html)를 사용하여 `OSS`를 머신에 마운트하고 로컬 파일 시스템을 사용할 수 있습니다.'리소스 센터'.이러한 방식으로 로컬 시스템에서처럼 원격 파일을 작동할 수 있습니다.

## 로컬 파일 시스템 사용

### `common.properties` 구성

DolphinScheduler Resource Center는 기본적으로 로컬 파일 시스템을 사용하므로 추가 구성이 필요하지 않습니다.
하지만 기본값을 수정해야 하는 경우에는 반드시 다음 구성도 함께 변경하시기 바랍니다.

- DolphinScheduler를 `Cluster` 또는 `Pseudo-Cluster` 모드로 배포하는 경우 `api-server/conf/common.properties` 및 `worker-server/conf/common.properties`를 구성해야 합니다.
- '독립형' 모드로 DolphinScheduler를 배포하는 경우 다음과 같이 'standalone-server/conf/common.properties'만 구성하면 됩니다.

변경해야 할 구성은 다음과 같습니다.

- `resource.storage.upload.base.path`를 로컬 디렉터리 경로로 변경합니다.`tenant resources.hdfs.root.user`에 `resource.storage.upload.base.path`에 대한 읽기 및 쓰기 권한이 있는지 확인하세요.`/tmp/dolphinscheduler`.`DolphinScheduler`는 구성한 디렉토리가 존재하지 않는 경우 이를 생성합니다.

> 참고:
> 1. LOCAL 모드는 분산 모드에서 읽기 및 쓰기를 지원하지 않습니다. 즉, 공유 파일 마운트 지점을 사용하지 않는 한 하나의 시스템에서만 리소스를 사용할 수 있습니다.
> 2. 기본값을 기본 경로로 사용하지 않으려면 `resource.storage.upload.base.path` 값을 수정하세요.
> 3. 로컬 구성은 `resource.storage.type=LOCAL`이며 실제로 `resource.storage.type=HDFS`라는 두 가지 설정을 구성했습니다.
> 및 `resource.hdfs.fs.defaultFS=file:///`, `resource.storage.type=LOCAL` 구성은 사용자 친화적이며
> 기본적으로 활성화되는 로컬 리소스 센터

## AWS S3 연결

`S3`에 연결된 `Resource Center`에 리소스를 업로드하려면 `api-server/conf/common.properties`, `api-server/conf/aws.yaml` 및 `worker-server/conf/common.properties`, `worker-server/conf/aws.yaml`을 구성해야 합니다.다음을 참조할 수 있습니다.

다음 필드를 구성하십시오```properties

resource.storage.type=S3
```````yaml
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

````

## OSS S3 연결

`OSS`에 연결된 `Resource Center`에 리소스를 업로드하려면 `api-server/conf/common.properties` 및 `worker-server/conf/common.properties`를 구성해야 합니다.다음을 참조할 수 있습니다.

다음 필드를 구성하십시오```properties
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

````

## OBS S3 연결

`OBS`에 연결된 `Resource Center`에 리소스를 업로드하려면 `api-server/conf/common.properties` 및 `worker-server/conf/common.properties`를 구성해야 합니다.다음을 참조할 수 있습니다.

다음 필드를 구성하십시오```properties
# access key id, required if you set resource.storage.type=OBS
resource.huawei.cloud.access.key.id=<your-access-key-id>
# access key secret, required if you set resource.storage.type=OBS
resource.huawei.cloud.access.key.secret=<your-access-key-secret>
# oss bucket name, required if you set resource.storage.type=OBS
resource.huawei.cloud.obs.bucket.name=dolphinscheduler
# oss bucket endpoint, required if you set resource.storage.type=OBS
resource.huawei.cloud.obs.endpoint=obs.cn-southwest-2.huaweicloud.com

````

> **참고:**
>
> * `api-server/conf/common.properties` 파일만 구성한 경우 리소스 업로드는 활성화되지만 작업에서는 리소스를 사용할 수 없습니다.워크플로에서 파일을 사용하거나 실행하려면 `worker-server/conf/common.properties`도 구성해야 합니다.
> * 리소스 업로드 기능을 사용하려면 [설치 및 배포](../installation/standalone.md)의 배포 사용자에게 해당 작업 권한이 있어야 합니다.
> * HA가 포함된 Hadoop 클러스터를 사용하는 경우 HDFS 리소스 업로드를 활성화해야 하며 Hadoop 클러스터 아래의 `core-site.xml` 및 `hdfs-site.xml`을 `worker-server/conf` 및 `api-server/conf`에 복사해야 하며, 그렇지 않으면 이 복사 단계를 건너뜁니다.

## COS 연결

`COS`에 연결된 `Resource Center`에 리소스를 업로드하려면 `api-server/conf/resource-center.yaml` 및 `worker-server/conf/resource-center.yaml`을 구성해야 합니다.다음을 참조할 수 있습니다.

다음 필드를 구성하십시오```yaml
resource:
  # Tencent Cloud Storage (COS) setup, required if you set resource.storage.type=COS
  tencent:
    cloud:
      access:
        key:
          id: <your-access-key-id>
          secret: <your-access-key-secret>
      cos:
        # predefined region code: https://cloud.tencent.com/document/product/436/6224
        region: ap-nanjing
        bucket:
          name: dolphinscheduler

````

COS를 활성화하려면 'api-server/conf/common.properties' 및 'worker-server/conf/common.properties'도 구성해야 합니다.다음을 참조할 수 있습니다.```properties
resource.storage.type=COS
````