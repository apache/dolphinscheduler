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
source ${workDir}/env/dolphinscheduler_env.sh

echo "1.create directory"

if [ ! -d $installPath ];then
  sudo mkdir -p $installPath
  sudo chown -R $deployUser:$deployUser $installPath
fi

echo "2.scp resources"
sh ${workDir}/scp-hosts.sh
if [ $? -eq 0 ];then
	echo 'scp copy completed'
else
	echo 'scp copy failed to exit'
	exit 1
fi

echo "3.stop server"
sh ${workDir}/stop-all.sh

echo "4.delete zk node"
sh ${workDir}/remove-zk-node.sh $zkRoot

echo "5.startup"
sh ${workDir}/start-all.sh
