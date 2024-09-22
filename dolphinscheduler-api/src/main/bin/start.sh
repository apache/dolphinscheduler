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
set -eo pipefail

BIN_DIR=$(dirname $(readlink -f "$0"))
DOLPHINSCHEDULER_HOME=$(cd ${BIN_DIR}/../..;pwd)
API_HOME=$(cd ${BIN_DIR}/..;pwd)

source "$API_HOME/conf/dolphinscheduler_env.sh"

JVM_ARGS_ENV_FILE=${API_HOME}/bin/jvm_args_env.sh
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

CP=""
for jar in $(find $DOLPHINSCHEDULER_HOME/libs/* -name "*.jar"); do
  CP=$CP:"$jar"
done

for jar in $(find $DOLPHINSCHEDULER_HOME/plugins/* -name "*.jar" | | grep -v "alert-plugins"); do
  CP=$CP:"$jar"
done

$JAVA_HOME/bin/java $JAVA_OPTS \
  -cp "$API_HOME/conf":"$CP" \
  org.apache.dolphinscheduler.api.ApiApplicationServer
