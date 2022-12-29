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

workersGroup=(${workers//,/ })
for workerGroup in ${workersGroup[@]}
do
  echo $workerGroup;
  worker=`echo $workerGroup|awk -F':' '{print $1}'`
  group=`echo $workerGroup|awk -F':' '{print $2}'`
  workerNames+=($worker)
  groupNames+=(${group:-default})
done

hostsArr=(${ips//,/ })
for host in ${hostsArr[@]}
do

  if ! ssh -o StrictHostKeyChecking=no -p $sshPort $host test -e $installPath; then
    ssh -o StrictHostKeyChecking=no -p $sshPort $host "sudo mkdir -p $installPath; sudo chown -R $deployUser:$deployUser $installPath"
  fi

  echo "scp dirs to $host/$installPath starting"
  for i in ${!workerNames[@]}; do
    if [[ ${workerNames[$i]} == $host ]]; then
      workerIndex=$i
      break
    fi
  done
  # set worker groups in application.yaml
  [[ -n ${workerIndex} ]] && sed -i "s/- default/- ${groupNames[$workerIndex]}/" $workDir/../worker-server/conf/application.yaml

  for dsDir in bin master-server worker-server alert-server api-server ui tools
  do
    echo "start to scp $dsDir to $host/$installPath"
    # Use quiet mode to reduce command line output
    scp -q -P $sshPort -r $workDir/../$dsDir  $host:$installPath
  done
  # restore worker groups to default
  [[ -n ${workerIndex} ]] && sed -i "s/- ${groupNames[$workerIndex]}/- default/" $workDir/../worker-server/conf/application.yaml

  echo "scp dirs to $host/$installPath complete"
done
