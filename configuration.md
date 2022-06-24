HDFS resource configuration
============================

When you need to use the resource center to create or upload related files, all files and resources will be stored on HDFS. So, the following configuration is required:

## Configure the common.properties file

After the 3.0.0-alpha version, if we need to use HDFS or S3 to upload resources to the resource center, we need to configure the following paths: `api-server/conf/common.properties`and `worker-server/conf/common.properties`. You can refer to the following:

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
resource.upload.path=/tmp/dolphinscheduler

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
# if resource.storage.type=HDFS, the user must have the permission to create directories under the HDFS root path
hdfs.root.user=root
# if resource.storage.type=S3, the value like: s3a://dolphinscheduler;
# if resource.storage.type=HDFS and namenode HA is enabled, you need to copy core-site.xml and hdfs-site.xml to conf dir
fs.defaultFS=hdfs://localhost:8020
aws.access.key.id=minioadmin
aws.secret.access.key=minioadmin
aws.region=us-east-1
aws.endpoint=http://localhost:9000
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

> **Note** :
> 
> *   If only `api-server/conf/common.properties` the , it only enables the operation of resource uploading, which cannot meet the normal use. Additional configuration is required if you want to execute related files in the workflow `worker-server/conf/common.properties`.
> *   If the resource upload function is used , the deployment user needs to have the operation permission for this part of the [installation and deployment.](https://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/guide/installation/standalone.html)
> *   If the NameNode of the Hadoop cluster is configured with HA, you need to enable the upload of HDFS type resources, and you need `core-site.xml` to `hdfs-site.xml`copy the and under the Hadoop cluster to, and `/opt/dolphinscheduler/conf`skip the next step for non-NameNode HA.