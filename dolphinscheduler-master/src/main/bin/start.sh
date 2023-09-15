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
DOLPHINSCHEDULER_HOME=${DOLPHINSCHEDULER_HOME:-$(cd $BIN_DIR/..; pwd)}

source "$DOLPHINSCHEDULER_HOME/conf/dolphinscheduler_env.sh"

JVM_ARGS_ENV_FILE=${BIN_DIR}/jvm_args_env.sh
JVM_ARGS="-server"

if [ -f $JVM_ARGS_ENV_FILE ]; then
  while read line
  do
      if [[ "$line" == -* ]]; then
            JVM_ARGS="${JVM_ARGS} $line"
      fi
  done < $JVM_ARGS_ENV_FILE
fi

JAVA_OPTS=${JAVA_OPTS:-"${JVM_ARGS}"}

if [[ "$DOCKER" == "true" ]]; then
  JAVA_OPTS="${JAVA_OPTS} -XX:-UseContainerSupport"
fi

echo "JAVA_HOME=${JAVA_HOME}"
echo "JAVA_OPTS=${JAVA_OPTS}"

$JAVA_HOME/bin/java $JAVA_OPTS \
  -cp "$DOLPHINSCHEDULER_HOME/conf":"$DOLPHINSCHEDULER_HOME/libs/*" \
  org.apache.dolphinscheduler.server.master.MasterServer
