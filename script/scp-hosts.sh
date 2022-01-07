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

source ${workDir}/env/install_env.sh

txt=""
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    txt="''"
fi

workersGroupMap=()

workersGroup=(${workers//,/ })
for workerGroup in ${workersGroup[@]}
do
  echo $workerGroup;
  worker=`echo $workerGroup|awk -F':' '{print $1}'`
  groupsName=`echo $workerGroup|awk -F':' '{print $2}'`
  workersGroupMap+=([$worker]=$groupsName)
done

hostsArr=(${ips//,/ })
for host in ${hostsArr[@]}
do

  if ! ssh -p $sshPort $host test -e $installPath; then
    ssh -p $sshPort $host "sudo mkdir -p $installPath; sudo chown -R $deployUser:$deployUser $installPath"
  fi

  echo "scp dirs to $host/$installPath starting"
	ssh -p $sshPort $host  "cd $installPath/; rm -rf bin/ conf/ lib/ script/ sql/ ui/"

  for dsDir in bin master-server worker-server alert-server api-server ui
  do
    # if worker in workersGroupMap
    if [[ "${workersGroupMap[${host}]}" ]]; then
      echo "export WORKER_GROUPS_0=${workersGroupMap[${host}]}" >> worker-server/bin/dolphinscheduler_env.sh
    fi

    echo "start to scp $dsDir to $host/$installPath"
    # Use quiet mode to reduce command line output
    scp -q -P $sshPort -r $workDir/../$dsDir  $host:$installPath
  done

  echo "scp dirs to $host/$installPath complete"
done
