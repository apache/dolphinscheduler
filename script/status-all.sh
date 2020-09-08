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

echo -e '\n'
echo "====================== dolphinscheduler install config============================="
echo -e "1.dolphinscheduler server node install hosts:[ \033[1;32m ${ips} \033[0m ]"
echo -e "2.master server node install hosts:[ \033[1;32m ${masters} \033[0m ]"
echo -e "3.worker server node install hosts:[ \033[1;32m ${workers} \033[0m ]"
echo -e "4.alert server node install hosts:[ \033[1;32m ${alertServer} \033[0m ]"
echo -e "5.api server node install hosts:[ \033[1;32m ${apiServers} \033[0m ]"

echo -e '\n'


ipsHost=(${ips//,/ })
for ip in ${ipsHost[@]}
do
  echo -e "====================== [ \033[1;32m ${ip} \033[0m ] node all servers =========================="
	ssh -p $sshPort $ip  "jps"
  echo -e '\n'
done

