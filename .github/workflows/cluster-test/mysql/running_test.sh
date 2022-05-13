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


TIME_OUT=10
MASTER_PORT_COMMAND="docker exec -u root ds bash -c \"nc -zv localhost 5678\""
WORKER_PORT_COMMAND="docker exec -u root ds bash -c \"nc -zv localhost 1234\""
ALERT_PORT_COMMAND="docker exec -u root ds bash -c \"nc -zv localhost 50052\""
API_PORT_COMMAND="docker exec -u root ds bash -c \"nc -zv localhost 12345\""

docker logs ds

#Cluster start health check
sleep $TIME_OUT
eval "$MASTER_PORT_COMMAND"
if [[ $? -eq 0 ]];then
  echo "master start health check success"
else
  echo "master start health check failed"
  exit 2
fi

eval "$WORKER_PORT_COMMAND"
if [[ $? -eq 0 ]];then
  echo "worker start health check success"
else
  echo "worker start health check failed"
  exit 2
fi

eval "$ALERT_PORT_COMMAND"
if [[ $? -eq 0 ]];then
  echo "alert start health check success"
else
  echo "alert start health check failed"
  exit 2
fi

eval "$API_PORT_COMMAND"
if [[ $? -eq 0 ]];then
  echo "api start health check success"
else
  echo "api start health check failed"
  exit 2
fi

#Stop Cluster
docker exec -u root ds bash -c "/root/apache-dolphinscheduler-dev-SNAPSHOT-bin/bin/stop-all.sh"

#Cluster stop health check
sleep $TIME_OUT
eval "$MASTER_PORT_COMMAND"
if [[ $? -ne 0 ]];then
  echo "master stop health check success"
else
  echo "master stop health check failed"
  exit 3
fi

eval "$WORKER_PORT_COMMAND"
if [[ $? -ne 0 ]];then
  echo "worker stop health check success"
else
  echo "worker stop health check failed"
  exit 3
fi

eval "$ALERT_PORT_COMMAND"
if [[ $? -ne 0 ]];then
  echo "alert stop health check success"
else
  echo "alert stop health check failed"
  exit 3
fi

eval "$API_PORT_COMMAND"
if [[ $? -ne 0 ]];then
  echo "api stop health check success"
else
  echo "api stop health check failed"
  exit 3
fi
