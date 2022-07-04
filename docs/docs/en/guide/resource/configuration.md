# Configuration

The Resource Center is usually used for operations such as uploading files, UDF functions, and task group management. You can appoint the local file directory as the upload directory for a single machine (this operation does not need to deploy Hadoop). Or you can also upload to a Hadoop or MinIO cluster, at this time, you need to have Hadoop (2.6+) or MinIO or other related environments.

## Local File Resource Configuration

For a single machine, you can choose to use local file directory as the upload directory (no need to deploy Hadoop) by making the following configuration.

### Configuring the `common.properties`

Configure the file in the following paths: `api-server/conf/common.properties` and `worker-server/conf/common.properties`.

- Change `data.basedir.path` to the local directory path. Please make sure the user who deploy dolphinscheduler have read and write permissions, such as: `data.basedir.path=/tmp/dolphinscheduler`. And the directory you configured will be auto-created if it does not exists.
- Modify the following two parameters, `resource.storage.type=HDFS` and `resource.hdfs.fs.defaultFS=file:///`.

## HDFS Resource Configuration

When it is necessary to use the Resource Center to create or upload relevant files, all files and resources will be stored on HDFS. Therefore the following configuration is required.

### Configuring the common.properties

After version 3.0.0-alpha, if you want to upload resources using HDFS or S3 from the Resource Center, the following paths need to be configured: `api-server/conf/common.properties` and `worker-server/conf/common.properties`. This can be found as follows.

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

> **_Note:_**
>
> *  If only the `api-server/conf/common.properties` file is configured, then resource uploading is enabled, but you can not use resources in task. If you want to use or execute the files in the workflow you need to configure `worker-server/conf/common.properties` too.
> * If you want to use the resource upload function, the deployment user in [installation and deployment](../installation/standalone.md) must have relevant operation authority.
> * If you using a Hadoop cluster with HA, you need to enable HDFS resource upload, and you need to copy the `core-site.xml` and `hdfs-site.xml` under the Hadoop cluster to `worker-server/conf` and `api-server/conf`, otherwise skip this copy step.