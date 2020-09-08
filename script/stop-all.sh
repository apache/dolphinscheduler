#!/bin/sh
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

source $workDir/../conf/config/install_config.conf

declare -A workersGroupMap=()

workersGroup=(${workers//,/ })
for workerGroup in ${workersGroup[@]}
do
  echo $workerGroup;
  worker=`echo $workerGroup|awk -F':' '{print $1}'`
  groupName=`echo $workerGroup|awk -F':' '{print $2}'`
  workersGroupMap+=([$worker]=$groupName)
done

mastersHost=(${masters//,/ })
for master in ${mastersHost[@]}
do
  echo "$master master server is stopping"
	ssh -p $sshPort $master  "cd $installPath/; sh bin/dolphinscheduler-daemon.sh stop master-server;"

done

for worker in ${!workersGroupMap[*]}
do
  echo "$worker worker server is stopping"
  ssh -p $sshPort $worker  "cd $installPath/; sh bin/dolphinscheduler-daemon.sh stop worker-server;"
  ssh -p $sshPort $worker  "cd $installPath/; sh bin/dolphinscheduler-daemon.sh stop logger-server;"
done

ssh -p $sshPort $alertServer  "cd $installPath/; sh bin/dolphinscheduler-daemon.sh stop alert-server;"

apiServersHost=(${apiServers//,/ })
for apiServer in ${apiServersHost[@]}
do
  echo "$apiServer worker server is stopping"
  ssh -p $sshPort $apiServer  "cd $installPath/; sh bin/dolphinscheduler-daemon.sh stop api-server;"
done