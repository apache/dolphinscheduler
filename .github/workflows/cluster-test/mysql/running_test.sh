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
set -x

BASE_COMMAND="docker exec -u root ds bash -c"
MASTER_PROCESS_COMMAND="$BASE_COMMAND \"ps -ef | grep -v grep | grep -c MasterServer\""
WORKER_PROCESS_COMMAND="$BASE_COMMAND \"ps -ef | grep -v grep | grep -c WorkerServer\""
ALERT_PROCESS_COMMAND="$BASE_COMMAND \"ps -ef | grep -v grep | grep -c AlertServer\""
API_PROCESS_COMMAND="$BASE_COMMAND \"ps -ef | grep -v grep | grep -c ApiApplicationServer\""

#Cluster start health check
MASTER_PROCESS_NUM=$(eval "$MASTER_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $MASTER_PROCESS_NUM -gt 0 ]];then
  echo "master health check success"
else
  echo "master health check failed"
  exit 2
fi

WORKER_PROCESS_NUM=$(eval "$WORKER_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $WORKER_PROCESS_NUM -gt 0 ]];then
  echo "worker health check success"
else
  echo "worker health check failed"
  exit 2
fi

ALERT_PROCESS_NUM=$(eval "$ALERT_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $ALERT_PROCESS_NUM -gt 0 ]];then
  echo "alert health check success"
else
  echo "alert health check failed"
  exit 2
fi

API_PROCESS_NUM=$(eval "$API_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $API_PROCESS_NUM -gt 0 ]];then
  echo "api health check success"
else
  echo "api health check failed"
  exit 2
fi

#Stop Cluster
$BASE_COMMAND "/root/apache-dolphinscheduler-dev-SNAPSHOT-bin/bin/stop-all.sh"

#Cluster stop health check
sleep 5
MASTER_PROCESS_NUM=$(eval "$MASTER_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $MASTER_PROCESS_NUM -eq 0 ]];then
  echo "master health check success"
else
  echo "master health check failed"
  exit 10
fi

WORKER_PROCESS_NUM=$(eval "$WORKER_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $WORKER_PROCESS_NUM -eq 0 ]];then
  echo "worker health check success"
else
  echo "worker health check failed"
  exit 1
fi

ALERT_PROCESS_NUM=$(eval "$ALERT_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $ALERT_PROCESS_NUM -eq 0 ]];then
  echo "alert health check success"
else
  echo "alert health check failed"
  exit 1
fi

API_PROCESS_NUM=$(eval "$API_PROCESS_COMMAND" | tr -cd "[0-9]")
if [[ $API_PROCESS_NUM -eq 0 ]];then
  echo "api health check success"
else
  echo "api health check failed"
  exit 1
fi