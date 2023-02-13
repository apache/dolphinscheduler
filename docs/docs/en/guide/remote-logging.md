# Remote Logging

Apache DolphinScheduler supports writing task logs to remote storage. When remote logging is enabled, DolphinScheduler will send the task logs to the specified remote storage asynchronously after the task ends. In addition, when the user views or downloads the task log, if the log file does not exist locally, DolphinScheduler will download the corresponding log file from the remote storage to the local file system.

## Enabling remote logging

If you deploy DolphinScheduler in `Cluster` or `Pseudo-Cluster` mode, you need to configure `api-server/conf/common.properties`, `master-server/conf/common.properties` and `worker-server/conf/common.properties`.
If you deploy DolphinScheduler in `Standalone` mode, you only need to configure `standalone-server/conf/common.properties` as follows:

```properties
# Whether to enable remote logging
remote.logging.enable=false
# if remote.logging.enable = true, set the target of remote logging
remote.logging.target=OSS
# if remote.logging.enable = true, set the log base directory
remote.logging.base.dir=logs
# if remote.logging.enable = true, set the number of threads to send logs to remote storage
remote.logging.thread.pool.size=10
```

## Writing task logs to [Aliyun Object Storage Service (OSS)](https://www.aliyun.com/product/oss)

Configure `common.properties` as follows:

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

