#!/bin/bash
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

set -e

echo "init env variables"

# Define parameters default value

#============================================================================
# Database
#============================================================================
export DATABASE_TYPE=${DATABASE_TYPE:-"postgresql"}
export DATABASE_DRIVER=${DATABASE_DRIVER:-"org.postgresql.Driver"}
export DATABASE_HOST=${DATABASE_HOST:-"127.0.0.1"}
export DATABASE_PORT=${DATABASE_PORT:-"5432"}
export DATABASE_USERNAME=${DATABASE_USERNAME:-"root"}
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-"root"}
export DATABASE_DATABASE=${DATABASE_DATABASE:-"dolphinscheduler"}
export DATABASE_PARAMS=${DATABASE_PARAMS:-"characterEncoding=utf8"}

#============================================================================
# ZooKeeper
#============================================================================
export ZOOKEEPER_QUORUM=${ZOOKEEPER_QUORUM:-"127.0.0.1:2181"}
export ZOOKEEPER_ROOT=${ZOOKEEPER_ROOT:-"/dolphinscheduler"}

#============================================================================
# Common
#============================================================================
# common opts
export DOLPHINSCHEDULER_OPTS=${DOLPHINSCHEDULER_OPTS:-""}
# common env
export DATA_BASEDIR_PATH=${DATA_BASEDIR_PATH:-"/tmp/dolphinscheduler"}
export RESOURCE_STORAGE_TYPE=${RESOURCE_STORAGE_TYPE:-"HDFS"}
export RESOURCE_UPLOAD_PATH=${RESOURCE_UPLOAD_PATH:-"/dolphinscheduler"}
export FS_DEFAULT_FS=${FS_DEFAULT_FS:-"file:///"}
export FS_S3A_ENDPOINT=${FS_S3A_ENDPOINT:-"s3.xxx.amazonaws.com"}
export FS_S3A_ACCESS_KEY=${FS_S3A_ACCESS_KEY:-"xxxxxxx"}
export FS_S3A_SECRET_KEY=${FS_S3A_SECRET_KEY:-"xxxxxxx"}
export HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE=${HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE:-"false"}
export JAVA_SECURITY_KRB5_CONF_PATH=${JAVA_SECURITY_KRB5_CONF_PATH:-"/opt/krb5.conf"}
export LOGIN_USER_KEYTAB_USERNAME=${LOGIN_USER_KEYTAB_USERNAME:-"hdfs@HADOOP.COM"}
export LOGIN_USER_KEYTAB_PATH=${LOGIN_USER_KEYTAB_PATH:-"/opt/hdfs.keytab"}
export KERBEROS_EXPIRE_TIME=${KERBEROS_EXPIRE_TIME:-"2"}
export HDFS_ROOT_USER=${HDFS_ROOT_USER:-"hdfs"}
export YARN_RESOURCEMANAGER_HA_RM_IDS=${YARN_RESOURCEMANAGER_HA_RM_IDS:-""}
export YARN_APPLICATION_STATUS_ADDRESS=${YARN_APPLICATION_STATUS_ADDRESS:-"http://ds1:8088/ws/v1/cluster/apps/%s"}
# skywalking
export SKYWALKING_ENABLE=${SKYWALKING_ENABLE:-"false"}
export SW_AGENT_COLLECTOR_BACKEND_SERVICES=${SW_AGENT_COLLECTOR_BACKEND_SERVICES:-"127.0.0.1:11800"}
export SW_GRPC_LOG_SERVER_HOST=${SW_GRPC_LOG_SERVER_HOST:-"127.0.0.1"}
export SW_GRPC_LOG_SERVER_PORT=${SW_GRPC_LOG_SERVER_PORT:-"11800"}
# dolphinscheduler env
export HADOOP_HOME=${HADOOP_HOME:-"/opt/soft/hadoop"}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-"/opt/soft/hadoop/etc/hadoop"}
export SPARK_HOME1=${SPARK_HOME1:-"/opt/soft/spark1"}
export SPARK_HOME2=${SPARK_HOME2:-"/opt/soft/spark2"}
export PYTHON_HOME=${PYTHON_HOME:-"/usr/bin/python"}
export JAVA_HOME=${JAVA_HOME:-"/usr/local/openjdk-8"}
export HIVE_HOME=${HIVE_HOME:-"/opt/soft/hive"}
export FLINK_HOME=${FLINK_HOME:-"/opt/soft/flink"}
export DATAX_HOME=${DATAX_HOME:-"/opt/soft/datax"}

#============================================================================
# Master Server
#============================================================================
export MASTER_SERVER_OPTS=${MASTER_SERVER_OPTS:-"-Xms1g -Xmx1g -Xmn512m"}
export MASTER_EXEC_THREADS=${MASTER_EXEC_THREADS:-"100"}
export MASTER_EXEC_TASK_NUM=${MASTER_EXEC_TASK_NUM:-"20"}
export MASTER_DISPATCH_TASK_NUM=${MASTER_DISPATCH_TASK_NUM:-"3"}
export MASTER_HOST_SELECTOR=${MASTER_HOST_SELECTOR:-"LowerWeight"}
export MASTER_HEARTBEAT_INTERVAL=${MASTER_HEARTBEAT_INTERVAL:-"10"}
export MASTER_TASK_COMMIT_RETRYTIMES=${MASTER_TASK_COMMIT_RETRYTIMES:-"5"}
export MASTER_TASK_COMMIT_INTERVAL=${MASTER_TASK_COMMIT_INTERVAL:-"1000"}
export MASTER_MAX_CPULOAD_AVG=${MASTER_MAX_CPULOAD_AVG:-"-1"}
export MASTER_RESERVED_MEMORY=${MASTER_RESERVED_MEMORY:-"0.3"}

#============================================================================
# Worker Server
#============================================================================
export WORKER_SERVER_OPTS=${WORKER_SERVER_OPTS:-"-Xms1g -Xmx1g -Xmn512m"}
export WORKER_EXEC_THREADS=${WORKER_EXEC_THREADS:-"100"}
export WORKER_HEARTBEAT_INTERVAL=${WORKER_HEARTBEAT_INTERVAL:-"10"}
export WORKER_MAX_CPULOAD_AVG=${WORKER_MAX_CPULOAD_AVG:-"-1"}
export WORKER_RESERVED_MEMORY=${WORKER_RESERVED_MEMORY:-"0.3"}
export WORKER_GROUPS=${WORKER_GROUPS:-"default"}

#============================================================================
# Alert Server
#============================================================================
export ALERT_SERVER_OPTS=${ALERT_SERVER_OPTS:-"-Xms512m -Xmx512m -Xmn256m"}
# xls file
export XLS_FILE_PATH=${XLS_FILE_PATH:-"/tmp/xls"}
# mail
export MAIL_SERVER_HOST=${MAIL_SERVER_HOST:-""}
export MAIL_SERVER_PORT=${MAIL_SERVER_PORT:-""}
export MAIL_SENDER=${MAIL_SENDER:-""}
export MAIL_USER=${MAIL_USER:-""}
export MAIL_PASSWD=${MAIL_PASSWD:-""}
export MAIL_SMTP_STARTTLS_ENABLE=${MAIL_SMTP_STARTTLS_ENABLE:-"true"}
export MAIL_SMTP_SSL_ENABLE=${MAIL_SMTP_SSL_ENABLE:-"false"}
export MAIL_SMTP_SSL_TRUST=${MAIL_SMTP_SSL_TRUST:-""}
# wechat
export ENTERPRISE_WECHAT_ENABLE=${ENTERPRISE_WECHAT_ENABLE:-"false"}
export ENTERPRISE_WECHAT_CORP_ID=${ENTERPRISE_WECHAT_CORP_ID:-""}
export ENTERPRISE_WECHAT_SECRET=${ENTERPRISE_WECHAT_SECRET:-""}
export ENTERPRISE_WECHAT_AGENT_ID=${ENTERPRISE_WECHAT_AGENT_ID:-""}
export ENTERPRISE_WECHAT_USERS=${ENTERPRISE_WECHAT_USERS:-""}

#============================================================================
# Api Server
#============================================================================
export API_SERVER_OPTS=${API_SERVER_OPTS:-"-Xms512m -Xmx512m -Xmn256m"}

#============================================================================
# Logger Server
#============================================================================
export LOGGER_SERVER_OPTS=${LOGGER_SERVER_OPTS:-"-Xms512m -Xmx512m -Xmn256m"}

echo "generate dolphinscheduler config"
ls ${DOLPHINSCHEDULER_HOME}/conf/ | grep ".tpl" | while read line; do
eval "cat << EOF
$(cat ${DOLPHINSCHEDULER_HOME}/conf/${line})
EOF
" > ${DOLPHINSCHEDULER_HOME}/conf/${line%.*}
done

# generate dolphinscheduler env
DOLPHINSCHEDULER_ENV_PATH=${DOLPHINSCHEDULER_HOME}/conf/env/dolphinscheduler_env.sh
if [ -r "${DOLPHINSCHEDULER_ENV_PATH}.tpl" ]; then
eval "cat << EOF
$(cat ${DOLPHINSCHEDULER_ENV_PATH}.tpl)
EOF
" > ${DOLPHINSCHEDULER_ENV_PATH}
chmod +x ${DOLPHINSCHEDULER_ENV_PATH}
fi
