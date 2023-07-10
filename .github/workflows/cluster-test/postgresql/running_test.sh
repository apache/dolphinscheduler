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


API_HEALTHCHECK_COMMAND="curl -I -m 10 -o /dev/null -s -w %{http_code} http://0.0.0.0:12345/dolphinscheduler/actuator/health"
MASTER_HEALTHCHECK_COMMAND="curl -I -m 10 -o /dev/null -s -w %{http_code} http://0.0.0.0:5679/actuator/health"
WORKER_HEALTHCHECK_COMMAND="curl -I -m 10 -o /dev/null -s -w %{http_code} http://0.0.0.0:1235/actuator/health"
ALERT_HEALTHCHECK_COMMAND="curl -I -m 10 -o /dev/null -s -w %{http_code} http://0.0.0.0:50053/actuator/health"

#Cluster start health check
TIMEOUT=180
START_HEALTHCHECK_EXITCODE=0

for ((i=1; i<=TIMEOUT; i++))
do
  MASTER_HTTP_STATUS=$(eval "$MASTER_HEALTHCHECK_COMMAND")
  WORKER_HTTP_STATUS=$(eval "$WORKER_HEALTHCHECK_COMMAND")
  API_HTTP_STATUS=$(eval "$API_HEALTHCHECK_COMMAND")
  ALERT_HTTP_STATUS=$(eval "$ALERT_HEALTHCHECK_COMMAND")
  if [[ $MASTER_HTTP_STATUS -eq 200 && $WORKER_HTTP_STATUS -eq 200 && $API_HTTP_STATUS -eq 200 && $ALERT_HTTP_STATUS -eq 200 ]];then
    START_HEALTHCHECK_EXITCODE=0
  else
    START_HEALTHCHECK_EXITCODE=2
  fi

  if [[ $START_HEALTHCHECK_EXITCODE -eq 0 ]];then
    echo "cluster start health check success"
    break
  fi

  if [[ $i -eq $TIMEOUT ]];then
      if [[ $MASTER_HTTP_STATUS -ne 200 ]];then
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/master-server/logs/dolphinscheduler-master.log"
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/master-server/logs/*.out"
        echo "master start health check failed"
      fi
      if [[ $WORKER_HTTP_STATUS -ne 200 ]]; then
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/worker-server/logs/dolphinscheduler-worker.log"
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/worker-server/logs/*.out"
        echo "worker start health check failed"
      fi
      if [[ $API_HTTP_STATUS -ne 200 ]]; then
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/api-server/logs/dolphinscheduler-api.log"
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/api-server/logs/*.out"
        echo "api start health check failed"
      fi
      if [[ $ALERT_HTTP_STATUS -ne 200 ]]; then
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/alert-server/logs/dolphinscheduler-alert.log"
        docker exec -u root ds bash -c "cat /root/apache-dolphinscheduler-*-SNAPSHOT-bin/alert-server/logs/*.out"
        echo "alert start health check failed"
      fi
      exit $START_HEALTHCHECK_EXITCODE
    fi

  sleep 1
done

#Stop Cluster
docker exec -u root ds bash -c "/root/apache-dolphinscheduler-*-SNAPSHOT-bin/bin/stop-all.sh"

#Cluster stop health check
sleep 5
MASTER_HTTP_STATUS=$(eval "$MASTER_HEALTHCHECK_COMMAND")
if [[ $MASTER_HTTP_STATUS -ne 200 ]];then
  echo "master stop health check success"
else
  echo "master stop health check failed"
  exit 3
fi

WORKER_HTTP_STATUS=$(eval "$WORKER_HEALTHCHECK_COMMAND")
if [[ $WORKER_HTTP_STATUS -ne 200 ]];then
  echo "worker stop health check success"
else
  echo "worker stop health check failed"
  exit 3
fi

API_HTTP_STATUS=$(eval "$API_HEALTHCHECK_COMMAND")
if [[ $API_HTTP_STATUS -ne 200 ]];then
  echo "api stop health check success"
else
  echo "api stop health check failed"
  exit 3
fi

ALERT_HTTP_STATUS=$(eval "$ALERT_HEALTHCHECK_COMMAND")
if [[ $ALERT_HTTP_STATUS -ne 200 ]];then
  echo "alert stop health check success"
else
  echo "alert stop health check failed"
  exit 3
fi
