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
source ${workDir}/env/dolphinscheduler_env.sh

echo "1.create directory"

# If install Path equal to "/" or related path is "/" or is empty, will cause directory "/bin" be overwrite or file adding,
# so we should check its value. Here use command `realpath` to get the related path, and it will skip if your shell env
# without command `realpath`.
if [ ! -d $installPath ];then
  sudo mkdir -p $installPath
  sudo chown -R $deployUser:$deployUser $installPath
elif [[ -z "${installPath// }" || "${installPath// }" == "/" || ( $(command -v realpath) && $(realpath -s "${installPath}") == "/" ) ]]; then
  echo "Parameter installPath can not be empty, use in root path or related path of root path, currently use ${installPath}"
  exit 1
fi

echo "2.scp resources"
bash ${workDir}/scp-hosts.sh
if [ $? -eq 0 ];then
	echo 'scp copy completed'
else
	echo 'scp copy failed to exit'
	exit 1
fi

echo "3.stop server"
bash ${workDir}/stop-all.sh

echo "4.delete zk node"
bash ${workDir}/remove-zk-node.sh $zkRoot

echo "5.startup"
bash ${workDir}/start-all.sh
