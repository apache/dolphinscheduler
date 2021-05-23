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

export DOLPHINSCHEDULER_BIN=${DOLPHINSCHEDULER_HOME}/bin
export MASTER_START_ENABLED=false
export WORKER_START_ENABLED=false
export API_START_ENABLED=false
export ALERT_START_ENABLED=false
export LOGGER_START_ENABLED=false

# wait database
waitDatabase() {
    echo "try to connect ${DATABASE_TYPE} ..."
    while ! nc -z ${DATABASE_HOST} ${DATABASE_PORT}; do
        local counter=$((counter+1))
        if [ $counter == 30 ]; then
            echo "Error: Couldn't connect to ${DATABASE_TYPE}."
            exit 1
        fi
        echo "Trying to connect to ${DATABASE_TYPE} at ${DATABASE_HOST}:${DATABASE_PORT}. Attempt $counter."
        sleep 5
    done
    echo "${DATABASE_TYPE} connection is ok"
}

# init database
initDatabase() {
    echo "import sql data"
    ${DOLPHINSCHEDULER_HOME}/script/create-dolphinscheduler.sh
}

# wait zk
waitZK() {
    echo "try to connect zookeeper ..."
    echo "${ZOOKEEPER_QUORUM}" | awk -F ',' 'BEGIN{ i=1 }{ while( i <= NF ){ print $i; i++ } }' | while read line; do
        while ! nc -z ${line%:*} ${line#*:}; do
            local counter=$((counter+1))
            if [ $counter == 30 ]; then
                echo "Error: Couldn't connect to zookeeper."
                exit 1
            fi
            echo "Trying to connect to zookeeper at ${line}. Attempt $counter."
            sleep 5
        done
    done
    echo "zookeeper connection is ok"
}

# print usage
printUsage() {
    echo -e "Dolphin Scheduler is a distributed and easy-to-expand visual DAG workflow scheduling system,"
    echo -e "dedicated to solving the complex dependencies in data processing, making the scheduling system out of the box for data processing.\n"
    echo -e "Usage: [ all | master-server | worker-server | api-server | alert-server ]\n"
    printf "%-13s:  %s\n" "all"           "Run master-server, worker-server, api-server and alert-server"
    printf "%-13s:  %s\n" "master-server" "MasterServer is mainly responsible for DAG task split, task submission monitoring."
    printf "%-13s:  %s\n" "worker-server" "WorkerServer is mainly responsible for task execution and providing log services."
    printf "%-13s:  %s\n" "api-server"    "ApiServer is mainly responsible for processing requests and providing the front-end UI layer."
    printf "%-13s:  %s\n" "alert-server"  "AlertServer mainly include Alarms."
}

# init config file
source /root/startup-init-conf.sh

case "$1" in
    (all)
        waitZK
        waitDatabase
        initDatabase
        export MASTER_START_ENABLED=true
        export WORKER_START_ENABLED=true
        export API_START_ENABLED=true
        export ALERT_START_ENABLED=true
        export LOGGER_START_ENABLED=true
    ;;
    (master-server)
        waitZK
        waitDatabase
        export MASTER_START_ENABLED=true
    ;;
    (worker-server)
        waitZK
        waitDatabase
        export WORKER_START_ENABLED=true
        export LOGGER_START_ENABLED=true
    ;;
    (api-server)
        waitZK
        waitDatabase
        initDatabase
        export API_START_ENABLED=true
    ;;
    (alert-server)
        waitDatabase
        export ALERT_START_ENABLED=true
    ;;
    (help)
        printUsage
        exit 1
    ;;
    (*)
        printUsage
        exit 1
    ;;
esac

# init directories
mkdir -p ${DOLPHINSCHEDULER_HOME}/logs

# start supervisord
supervisord -n -u root
