# 资源中心配置详情

资源中心通常用于上传文件、 UDF 函数，以及任务组管理等操作。针对单机环境可以选择本地文件目录作为上传文件夹（此操作不需要部署 Hadoop）。当然也可以选择上传到 Hadoop or MinIO 集群上，此时则需要有 Hadoop（2.6+）或者 MinIOn 等相关环境。

## 本地资源配置

在单机环境下，可以选择使用本地文件目录作为上传文件夹（无需部署Hadoop），此时需要进行如下配置：

### 配置 `common.properties` 文件

对以下路径的文件进行配置：`api-server/conf/common.properties` 和 `worker-server/conf/common.properties`

- 将 `data.basedir.path` 改为本地存储路径，请确保部署 DolphinScheduler 的用户拥有读写权限，例如：`data.basedir.path=/tmp/dolphinscheduler`。当路径不存在时会自动创建文件夹
- 修改下列两个参数，分别是 `resource.storage.type=HDFS` 和 `resource.hdfs.fs.defaultFS=file:///`。

## HDFS 资源配置

当需要使用资源中心进行相关文件的创建或者上传操作时，所有的文件和资源都会被存储在 HDFS 上。所以需要进行以下配置：

### 配置 common.properties 文件

在 3.0.0-alpha 版本之后，如果需要使用到资源中心的 HDFS 或 S3 上传资源，我们需要对以下路径的进行配置：`api-server/conf/common.properties` 和 `worker-server/conf/common.properties`。可参考如下：

```properties
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# user data local directory path, please make sure the directory exists and have read write permissions
data.basedir.path=/tmp/dolphinscheduler

# resource storage type: HDFS, S3, NONE
resource.storage.type=HDFS

# resource store on HDFS/S3 path, resource file will store to this hadoop hdfs path, self configuration,
# please make sure the directory exists on hdfs and have read write permissions. "/dolphinscheduler" is recommended
resource.storage.upload.base.path=/tmp/dolphinscheduler

# The AWS access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.access.key.id=minioadmin
# The AWS secret access key. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.secret.access.key=minioadmin
# The AWS Region to use. if resource.storage.type=S3 or use EMR-Task, This configuration is required
resource.aws.region=cn-north-1
# The name of the bucket. You need to create them by yourself. Otherwise, the system cannot start. All buckets in Amazon S3 share a single namespace; ensure the bucket is given a unique name.
resource.aws.s3.bucket.name=dolphinscheduler
# You need to set this parameter when private cloud s3. If S3 uses public cloud, you only need to set resource.aws.region or set to the endpoint of a public cloud such as S3.cn-north-1.amazonaws.com.cn
resource.aws.s3.endpoint=http://localhost:9000

# if resource.storage.type=HDFS, the user must have the permission to create directories under the HDFS root path
resource.hdfs.root.user=root
# if resource.storage.type=S3, the value like: s3a://dolphinscheduler;
# if resource.storage.type=HDFS and namenode HA is enabled, you need to copy core-site.xml and hdfs-site.xml to conf dir
resource.hdfs.fs.defaultFS=hdfs://localhost:8020

# whether to startup kerberos
hadoop.security.authentication.startup.state=false

# java.security.krb5.conf path
java.security.krb5.conf.path=/opt/krb5.conf

# login user from keytab username
login.user.keytab.username=hdfs-mycluster@ESZ.COM

# login user from keytab path
login.user.keytab.path=/opt/hdfs.headless.keytab

# kerberos expire time, the unit is hour
kerberos.expire.time=2
# resource view suffixs
#resource.view.suffixs=txt,log,sh,bat,conf,cfg,py,java,sql,xml,hql,properties,json,yml,yaml,ini,js

# resourcemanager port, the default value is 8088 if not specified
resource.manager.httpaddress.port=8088
# if resourcemanager HA is enabled, please set the HA IPs; if resourcemanager is single, keep this value empty
yarn.resourcemanager.ha.rm.ids=192.168.xx.xx,192.168.xx.xx
# if resourcemanager HA is enabled or not use resourcemanager, please keep the default value;
# If resourcemanager is single, you only need to replace ds1 to actual resourcemanager hostname
yarn.application.status.address=http://localhost:%s/ds/v1/cluster/apps/%s
# job history status url when application number threshold is reached(default 10000, maybe it was set to 1000)
yarn.job.history.status.address=http://localhost:19888/ds/v1/history/mapreduce/jobs/%s

# datasource encryption enable
datasource.encryption.enable=false

# datasource encryption salt
datasource.encryption.salt=!@#$%^&*

# data quality option
data-quality.jar.name=dolphinscheduler-data-quality-dev-SNAPSHOT.jar

#data-quality.error.output.path=/tmp/data-quality-error-data

# Network IP gets priority, default inner outer

# Whether hive SQL is executed in the same session
support.hive.oneSession=false

# use sudo or not, if set true, executing user is tenant user and deploy user needs sudo permissions;
# if set false, executing user is the deploy user and doesn't need sudo permissions
sudo.enable=true

# network interface preferred like eth0, default: empty
#dolphin.scheduler.network.interface.preferred=

# network IP gets priority, default: inner outer
#dolphin.scheduler.network.priority.strategy=default

# system env path
#dolphinscheduler.env.path=env/dolphinscheduler_env.sh

# development state
development.state=false

# rpc port
alert.rpc.port=50052
```

>    **注意**：
>
>    * 如果只配置了 `api-server/conf/common.properties` 的文件，则只是开启了资源上传的操作，并不能满足正常使用。如果想要在工作流中执行相关文件则需要额外配置 `worker-server/conf/common.properties`。
>    * 如果用到资源上传的功能，那么[安装部署](../installation/standalone.md)中，部署用户需要有这部分的操作权限。
>    * 如果 Hadoop 集群的 NameNode 配置了 HA 的话，需要开启 HDFS 类型的资源上传，同时需要将 Hadoop 集群下的 `core-site.xml` 和 `hdfs-site.xml` 复制到 `worker-server/conf` 以及 `api-server/conf`，非 NameNode HA 跳过次步骤。
