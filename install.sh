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

source ${workDir}/conf/config/install_config.conf

# 1.create directory
echo "1.create directory"

if [ ! -d $installPath ];then
  sudo mkdir -p $installPath
  sudo chown -R $deployUser:$deployUser $installPath
fi

# 2.scp resources
echo "2.scp resources"
sh ${workDir}/script/scp-hosts.sh
if [ $? -eq 0 ]
then
	echo 'scp copy completed'
else
	echo 'scp copy failed to exit'
	exit 1
fi


# 3.stop server
echo "3.stop server"
sh ${workDir}/script/stop-all.sh


# 4.delete zk node
echo "4.delete zk node"

sh ${workDir}/script/remove-zk-node.sh $zkRoot


# 5.startup
echo "5.startup"
sh ${workDir}/script/start-all.sh
