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

workDir=`dirname $0`
workDir=`cd ${workDir};pwd`

source ${workDir}/env/install_env.sh

# install_env.sh info
echo -e '\n'
echo "====================== dolphinscheduler server config ============================="
echo -e "1.dolphinscheduler server node config hosts:[ \033[1;32m ${ips} \033[0m ]"
echo -e "2.master server node config hosts:[ \033[1;32m ${masters} \033[0m ]"
echo -e "3.worker server node config hosts:[ \033[1;32m ${workers} \033[0m ]"
echo -e "4.alert server node config hosts:[ \033[1;32m ${alertServer} \033[0m ]"
echo -e "5.api server node config hosts:[ \033[1;32m ${apiServers} \033[0m ]"

# all server check state
echo -e '\n'
echo "====================== dolphinscheduler server status ============================="
firstColumn="node  server  state"
echo $firstColumn
echo -e '\n'

workersGroup=(${workers//,/ })
for workerGroup in ${workersGroup[@]}
do
  worker=`echo $workerGroup|awk -F':' '{print $1}'`
  workerNames+=($worker)
done

StateRunning="Running"
# 1.master server check state
mastersHost=(${masters//,/ })
for master in ${mastersHost[@]}
do
  masterState=`ssh -o StrictHostKeyChecking=no -p $sshPort $master  "cd $installPath/; bash bin/dolphinscheduler-daemon.sh status master-server;"`
  echo "$master  $masterState"
done

# 2.worker server check state
for worker in ${workerNames[@]}
do
  workerState=`ssh -o StrictHostKeyChecking=no -p $sshPort $worker  "cd $installPath/; bash bin/dolphinscheduler-daemon.sh status worker-server;"`
  echo "$worker  $workerState"
done

# 3.alter server check state
alertState=`ssh -o StrictHostKeyChecking=no -p $sshPort $alertServer  "cd $installPath/; bash bin/dolphinscheduler-daemon.sh status alert-server;"`
echo "$alertServer  $alertState"

# 4.api server check state
apiServersHost=(${apiServers//,/ })
for apiServer in ${apiServersHost[@]}
do
  apiState=`ssh -o StrictHostKeyChecking=no -p $sshPort $apiServer  "cd $installPath/; bash bin/dolphinscheduler-daemon.sh status api-server;"`
  echo "$apiServer  $apiState"
done
