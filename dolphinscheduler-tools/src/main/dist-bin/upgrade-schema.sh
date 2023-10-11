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

BIN_DIR=$(dirname $0)
DOLPHINSCHEDULER_HOME=${DOLPHINSCHEDULER_HOME:-$(cd $BIN_DIR/../..; pwd)}

if [ "$DOCKER" != "true" ]; then
  source "$DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh"
fi

JAVA_OPTS=${JAVA_OPTS:-"-server -Duser.timezone=${SPRING_JACKSON_TIME_ZONE} -Xms1g -Xmx1g -Xmn512m -XX:+PrintGCDetails -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof"}

# TODO temp solution to may our tarball small enough to pass ASF release policy, for more detail see: https://lists.apache.org/thread/rmp7fghlj0n7h9y2v3p8gkw9f9qbo6qt
CP=$DOLPHINSCHEDULER_HOME/tools/libs/*
for d in api-server; do
  for f in $DOLPHINSCHEDULER_HOME/$d/libs/*.jar; do
    JAR_FILE_NAME=${f##*/}
    if [[ ! $CP =~ $JAR_FILE_NAME ]] && [[ ! $JAR_FILE_NAME =~ "dolphinscheduler-" ]] && [[ ! $JAR_FILE_NAME == "spring"* ]]; then
      CP=$CP:$f
    fi
  done
done

$JAVA_HOME/bin/java $JAVA_OPTS \
  -cp "$DOLPHINSCHEDULER_HOME/tools/conf":"$DOLPHINSCHEDULER_HOME/tools/sql":"$CP" \
  -Dspring.profiles.active=upgrade,${DATABASE} \
  org.apache.dolphinscheduler.tools.datasource.UpgradeDolphinScheduler
