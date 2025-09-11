#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
set -eo pipefail

# This script is used to download the plugins required during the running process.
# All are downloaded by default. You can also choose what you need.
# You only need to configure the plug-in name in config/plugin_config.

# get ds home
DOLPHINSCHEDULER_HOME=$(cd $(dirname $0);cd ../;pwd)

# plugins default version is dev-SNAPSHOT, you can also choose a custom version. eg: dev-SNAPSHOT: bash install-plugins.sh dev-SNAPSHOT
version=dev-SNAPSHOT

if [ -n "$1" ]; then
  version="$1"
fi

echo "Install Dolphinscheduler plugins, usage version is ${version}"

# create the plugins directory
if [ ! -d ${DOLPHINSCHEDULER_HOME}/plugins ]; then
      mkdir -p ${DOLPHINSCHEDULER_HOME}/plugins
fi

plugin_dir=""
while read line; do
  if [ -z "$line" ]; then
    continue
  fi

  start_char=$(echo "$line" | cut -c 1)

  if [ "$start_char" == "-" ]; then
    plugin_dir=$(echo ${line//--/})
    if [ "$plugin_dir" != "end" ]; then
      mkdir -p ${DOLPHINSCHEDULER_HOME}/plugins/${plugin_dir}
    fi
  fi

  if [ "$start_char" != "-" ] && [ "$start_char" != "#" ]; then
      echo "installing plugin: " $line
      ${DOLPHINSCHEDULER_HOME}/mvnw dependency:get -DgroupId=org.apache.dolphinscheduler -DartifactId=${line} -Dversion=${version} -Dclassifier=shade -Ddest=${DOLPHINSCHEDULER_HOME}/plugins/${plugin_dir}
  fi

done < ${DOLPHINSCHEDULER_HOME}/conf/plugins_config

