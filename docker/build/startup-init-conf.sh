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

# Define parameters default value.
#============================================================================
# Database Source
#============================================================================
export DATABASE_HOST=${DATABASE_HOST:-"127.0.0.1"}
export DATABASE_PORT=${DATABASE_PORT:-"5432"}
export DATABASE_USERNAME=${DATABASE_USERNAME:-"root"}
export DATABASE_PASSWORD=${DATABASE_PASSWORD:-"root"}
export DATABASE_DATABASE=${DATABASE_DATABASE:-"dolphinscheduler"}
export DATABASE_TYPE=${DATABASE_TYPE:-"postgresql"}
export DATABASE_DRIVER=${DATABASE_DRIVER:-"org.postgresql.Driver"}
export DATABASE_PARAMS=${DATABASE_PARAMS:-"characterEncoding=utf8"}

#============================================================================
# System
#============================================================================
export DOLPHINSCHEDULER_ENV_PATH=${DOLPHINSCHEDULER_ENV_PATH:-"/opt/dolphinscheduler/conf/env/dolphinscheduler_env.sh"}
export DOLPHINSCHEDULER_DATA_BASEDIR_PATH=${DOLPHINSCHEDULER_DATA_BASEDIR_PATH:-"/tmp/dolphinscheduler"}
export DOLPHINSCHEDULER_RESOURCE_STORAGE_TYPE=${DOLPHINSCHEDULER_RESOURCE_STORAGE_TYPE:-"HDFS"}
export RESOURCE_UPLOAD_PATH=${RESOURCE_UPLOAD_PATH:-"/ds"}
export DOLPHINSCHEDULER_FS_DEFAULTFS=${DOLPHINSCHEDULER_FS_DEFAULTFS:-"file:///data/dolphinscheduler"}
export FS_S3A_ENDPOINT=${FS_S3A_ENDPOINT:-"s3.xxx.amazonaws.com"}
export FS_S3A_ACCESS_KEY=${FS_S3A_ACCESS_KEY:-"xxxxxxx"}
export FS_S3A_SECRET_KEY=${FS_S3A_SECRET_KEY:-"xxxxxxx"}

#============================================================================
# Zookeeper
#============================================================================
export ZOOKEEPER_QUORUM=${ZOOKEEPER_QUORUM:-"127.0.0.1:2181"}
export ZOOKEEPER_ROOT=${ZOOKEEPER_ROOT:-"/dolphinscheduler"}

#============================================================================
# Master Server
#============================================================================
export MASTER_EXEC_THREADS=${MASTER_EXEC_THREADS:-"100"}
export MASTER_EXEC_TASK_NUM=${MASTER_EXEC_TASK_NUM:-"20"}
export MASTER_HEARTBEAT_INTERVAL=${MASTER_HEARTBEAT_INTERVAL:-"10"}
export MASTER_TASK_COMMIT_RETRYTIMES=${MASTER_TASK_COMMIT_RETRYTIMES:-"5"}
export MASTER_TASK_COMMIT_INTERVAL=${MASTER_TASK_COMMIT_INTERVAL:-"1000"}
export MASTER_MAX_CPULOAD_AVG=${MASTER_MAX_CPULOAD_AVG:-"100"}
export MASTER_RESERVED_MEMORY=${MASTER_RESERVED_MEMORY:-"0.1"}
export MASTER_LISTEN_PORT=${MASTER_LISTEN_PORT:-"5678"}

#============================================================================
# Worker Server
#============================================================================
export WORKER_EXEC_THREADS=${WORKER_EXEC_THREADS:-"100"}
export WORKER_HEARTBEAT_INTERVAL=${WORKER_HEARTBEAT_INTERVAL:-"10"}
export WORKER_FETCH_TASK_NUM=${WORKER_FETCH_TASK_NUM:-"3"}
export WORKER_MAX_CPULOAD_AVG=${WORKER_MAX_CPULOAD_AVG:-"100"}
export WORKER_RESERVED_MEMORY=${WORKER_RESERVED_MEMORY:-"0.1"}
export WORKER_LISTEN_PORT=${WORKER_LISTEN_PORT:-"1234"}
export WORKER_GROUP=${WORKER_GROUP:-"default"}

#============================================================================
# Alert Server
#============================================================================
# XLS FILE
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
# Frontend
#============================================================================
export FRONTEND_API_SERVER_HOST=${FRONTEND_API_SERVER_HOST:-"127.0.0.1"}
export FRONTEND_API_SERVER_PORT=${FRONTEND_API_SERVER_PORT:-"12345"}

echo "generate app config"
ls ${DOLPHINSCHEDULER_HOME}/conf/ | grep ".tpl" | while read line; do
eval "cat << EOF
$(cat ${DOLPHINSCHEDULER_HOME}/conf/${line})
EOF
" > ${DOLPHINSCHEDULER_HOME}/conf/${line%.*}
done

echo "generate nginx config"
sed -i "s/FRONTEND_API_SERVER_HOST/${FRONTEND_API_SERVER_HOST}/g" /etc/nginx/conf.d/dolphinscheduler.conf
sed -i "s/FRONTEND_API_SERVER_PORT/${FRONTEND_API_SERVER_PORT}/g" /etc/nginx/conf.d/dolphinscheduler.conf