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

#============================================================================
# Database
#============================================================================
# postgresql
DATABASE_TYPE=postgresql
DATABASE_DRIVER=org.postgresql.Driver
DATABASE_HOST=dolphinscheduler-postgresql
DATABASE_PORT=5432
DATABASE_USERNAME=root
DATABASE_PASSWORD=root
DATABASE_DATABASE=dolphinscheduler
DATABASE_PARAMS=characterEncoding=utf8
# mysql
# DATABASE_TYPE=mysql
# DATABASE_DRIVER=com.mysql.jdbc.Driver
# DATABASE_HOST=dolphinscheduler-mysql
# DATABASE_PORT=3306
# DATABASE_USERNAME=root
# DATABASE_PASSWORD=root
# DATABASE_DATABASE=dolphinscheduler
# DATABASE_PARAMS=useUnicode=true&characterEncoding=UTF-8

#============================================================================
# ZooKeeper
#============================================================================
ZOOKEEPER_QUORUM=dolphinscheduler-zookeeper:2181
ZOOKEEPER_ROOT=/dolphinscheduler

#============================================================================
# Common
#============================================================================
# common opts
DOLPHINSCHEDULER_OPTS=
# common env
DATA_BASEDIR_PATH=/tmp/dolphinscheduler
RESOURCE_STORAGE_TYPE=HDFS
RESOURCE_UPLOAD_PATH=/dolphinscheduler
FS_DEFAULT_FS=file:///
FS_S3A_ENDPOINT=s3.xxx.amazonaws.com
FS_S3A_ACCESS_KEY=xxxxxxx
FS_S3A_SECRET_KEY=xxxxxxx
HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE=false
JAVA_SECURITY_KRB5_CONF_PATH=/opt/krb5.conf
LOGIN_USER_KEYTAB_USERNAME=hdfs@HADOOP.COM
LOGIN_USER_KEYTAB_PATH=/opt/hdfs.keytab
KERBEROS_EXPIRE_TIME=2
HDFS_ROOT_USER=hdfs
YARN_RESOURCEMANAGER_HA_RM_IDS=
YARN_APPLICATION_STATUS_ADDRESS=http://ds1:8088/ws/v1/cluster/apps/%s
# skywalking
SKYWALKING_ENABLE=false
SW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800
SW_GRPC_LOG_SERVER_HOST=127.0.0.1
SW_GRPC_LOG_SERVER_PORT=11800
# dolphinscheduler env
HADOOP_HOME=/opt/soft/hadoop
HADOOP_CONF_DIR=/opt/soft/hadoop/etc/hadoop
SPARK_HOME1=/opt/soft/spark1
SPARK_HOME2=/opt/soft/spark2
PYTHON_HOME=/usr/bin/python
JAVA_HOME=/usr/local/openjdk-8
HIVE_HOME=/opt/soft/hive
FLINK_HOME=/opt/soft/flink
DATAX_HOME=/opt/soft/datax

#============================================================================
# Master Server
#============================================================================
MASTER_SERVER_OPTS=-Xms1g -Xmx1g -Xmn512m
MASTER_EXEC_THREADS=100
MASTER_EXEC_TASK_NUM=20
MASTER_DISPATCH_TASK_NUM=3
MASTER_HOST_SELECTOR=LowerWeight
MASTER_HEARTBEAT_INTERVAL=10
MASTER_TASK_COMMIT_RETRYTIMES=5
MASTER_TASK_COMMIT_INTERVAL=1000
MASTER_MAX_CPULOAD_AVG=-1
MASTER_RESERVED_MEMORY=0.3

#============================================================================
# Worker Server
#============================================================================
WORKER_SERVER_OPTS=-Xms1g -Xmx1g -Xmn512m
WORKER_EXEC_THREADS=100
WORKER_HEARTBEAT_INTERVAL=10
WORKER_MAX_CPULOAD_AVG=-1
WORKER_RESERVED_MEMORY=0.3
WORKER_GROUPS=default

#============================================================================
# Alert Server
#============================================================================
ALERT_SERVER_OPTS=-Xms512m -Xmx512m -Xmn256m
# xls file
XLS_FILE_PATH=/tmp/xls
# mail
MAIL_SERVER_HOST=
MAIL_SERVER_PORT=
MAIL_SENDER=
MAIL_USER=
MAIL_PASSWD=
MAIL_SMTP_STARTTLS_ENABLE=true
MAIL_SMTP_SSL_ENABLE=false
MAIL_SMTP_SSL_TRUST=
# wechat
ENTERPRISE_WECHAT_ENABLE=false
ENTERPRISE_WECHAT_CORP_ID=
ENTERPRISE_WECHAT_SECRET=
ENTERPRISE_WECHAT_AGENT_ID=
ENTERPRISE_WECHAT_USERS=

#============================================================================
# Api Server
#============================================================================
API_SERVER_OPTS=-Xms512m -Xmx512m -Xmn256m

#============================================================================
# Logger Server
#============================================================================
LOGGER_SERVER_OPTS=-Xms512m -Xmx512m -Xmn256m
