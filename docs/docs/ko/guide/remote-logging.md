# 원격 로깅

Apache DolphinScheduler는 원격 저장소에 작업 로그 쓰기를 지원합니다.원격 로깅이 활성화되면 DolphinScheduler는 작업이 끝난 후 지정된 원격 저장소에 작업 로그를 비동기적으로 보냅니다.또한 사용자가 작업 로그를 보거나 다운로드할 때 해당 로그 파일이 로컬에 없으면 DolphinScheduler는 해당 로그 파일을 원격 저장소에서 로컬 파일 시스템으로 다운로드합니다.

## 원격 로깅 활성화

DolphinScheduler를 `Cluster` 또는 `Pseudo-Cluster` 모드로 배포하는 경우 `api-server/conf/common.properties`, `master-server/conf/common.properties` 및 `worker-server/conf/common.properties`를 구성해야 합니다.
DolphinScheduler를 '독립형' 모드로 배포하는 경우 다음과 같이 'standalone-server/conf/common.properties'만 구성하면 됩니다.```properties
# Whether to enable remote logging
remote.logging.enable=false
# if remote.logging.enable = true, set the target of remote logging, currently support OSS, S3, GCS, ABS
remote.logging.target=OSS
# if remote.logging.enable = true, set the log base directory
remote.logging.base.dir=logs
# if remote.logging.enable = true, set the number of threads to send logs to remote storage
remote.logging.thread.pool.size=10
````

## [Aliyun Object Storage Service(OSS)](https://www.aliyun.com/product/oss)에 작업 로그 쓰기

다음과 같이 `common.properties`를 구성합니다.```properties
# oss access key id, required if you set remote.logging.target=OSS
remote.logging.oss.access.key.id=<access.key.id>
# oss access key secret, required if you set remote.logging.target=OSS
remote.logging.oss.access.key.secret=<access.key.secret>
# oss bucket name, required if you set remote.logging.target=OSS
remote.logging.oss.bucket.name=<bucket.name>
# oss endpoint, required if you set remote.logging.target=OSS
remote.logging.oss.endpoint=<endpoint>
````

## [Amazon S3](https://aws.amazon.com/cn/s3/)에 작업 로그 쓰기

다음과 같이 `common.properties`를 구성합니다.```properties
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
````

## [Google Cloud Storage(GCS)](https://cloud.google.com/storage)에 작업 로그 쓰기

다음과 같이 `common.properties`를 구성합니다.```properties
# the location of the google cloud credential, required if you set remote.logging.target=GCS
remote.logging.google.cloud.storage.credential=/path/to/credential
# gcs bucket name, required if you set remote.logging.target=GCS
remote.logging.google.cloud.storage.bucket.name=<your-bucket>
````

## [Azure Blob Storage(ABS)](https://azure.microsoft.com/en-us/products/storage/blobs)에 작업 로그 쓰기

다음과 같이 `common.properties`를 구성합니다.```properties
# abs account name, required if you set resource.storage.type=ABS
remote.logging.abs.account.name=<your-account-name>
# abs account key, required if you set resource.storage.type=ABS
remote.logging.abs.account.key=<your-account-key>
# abs container name, required if you set resource.storage.type=ABS
remote.logging.abs.container.name=<your-container-name>
````

### 공지사항

Azure Blob Storage는 빈 디렉터리의 존재를 지원하지 않으므로 리소스 디렉터리 아래에 '<이름 없음>' 빈 파일이 있습니다.그러나 Dolphinscheduler 리소스 센터의 파일 표시에는 영향을 미치지 않습니다.