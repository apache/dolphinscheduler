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

usage="Usage: dolphinscheduler-daemon.sh (start|stop|status) <api-server|master-server|worker-server|alert-server|standalone-server> "

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

startStop=$1
shift
command=$1
shift

echo "Begin $startStop $command......"

BIN_DIR=`dirname $0`
BIN_DIR=`cd "$BIN_DIR"; pwd`
DOLPHINSCHEDULER_HOME=`cd "$BIN_DIR/.."; pwd`
BIN_ENV_FILE="${DOLPHINSCHEDULER_HOME}/bin/env/dolphinscheduler_env.sh"

# Overwrite server dolphinscheduler_env.sh in path `<server>/conf/dolphinscheduler_env.sh` when exists
# `bin/env/dolphinscheduler_env.sh` file. User could only change `bin/env/dolphinscheduler_env.sh` instead
# of each server's dolphinscheduler_env.sh when they want to start the server
function overwrite_server_env() {
  local server=$1
  local server_env_file="${DOLPHINSCHEDULER_HOME}/${server}/conf/dolphinscheduler_env.sh"
  if [ -f "${BIN_ENV_FILE}" ]; then
    echo "Overwrite ${server}/conf/dolphinscheduler_env.sh using bin/env/dolphinscheduler_env.sh."
    cp "${BIN_ENV_FILE}" "${server_env_file}"
  else
    echo "Start server ${server} using env config path ${server_env_file}, because file ${BIN_ENV_FILE} not exists."
  fi
}

export HOSTNAME=`hostname`

export DOLPHINSCHEDULER_LOG_DIR=$DOLPHINSCHEDULER_HOME/$command/logs

export STOP_TIMEOUT=5

if [ ! -d "$DOLPHINSCHEDULER_LOG_DIR" ]; then
  mkdir $DOLPHINSCHEDULER_LOG_DIR
fi

pid=$DOLPHINSCHEDULER_HOME/$command/pid

cd $DOLPHINSCHEDULER_HOME/$command

if [ "$command" = "api-server" ]; then
  log=$DOLPHINSCHEDULER_HOME/api-server/logs/$command-$HOSTNAME.out
elif [ "$command" = "master-server" ]; then
  log=$DOLPHINSCHEDULER_HOME/master-server/logs/$command-$HOSTNAME.out
elif [ "$command" = "worker-server" ]; then
  log=$DOLPHINSCHEDULER_HOME/worker-server/logs/$command-$HOSTNAME.out
elif [ "$command" = "alert-server" ]; then
  log=$DOLPHINSCHEDULER_HOME/alert-server/logs/$command-$HOSTNAME.out
elif [ "$command" = "standalone-server" ]; then
  log=$DOLPHINSCHEDULER_HOME/standalone-server/logs/$command-$HOSTNAME.out
else
  echo "Error: No command named '$command' was found."
  exit 1
fi

state=""
function get_server_running_status() {
  state="STOP"
  if [ -f $pid ]; then
    TARGET_PID=`cat $pid`
    if [[ $(ps -p "$TARGET_PID" -o comm=) =~ "bash" ]]; then
      state="RUNNING"
    fi
  fi
}

case $startStop in
  (start)
    # if server is already started, cancel this launch
    get_server_running_status
    if [[ $state == "RUNNING" ]]; then
      echo "$command running as process $TARGET_PID.  Stop it first."
      exit 1
    fi
    echo starting $command, logging to $DOLPHINSCHEDULER_LOG_DIR
    overwrite_server_env "${command}"
    nohup /bin/bash "$DOLPHINSCHEDULER_HOME/$command/bin/start.sh" > $log 2>&1 &
    echo $! > $pid
    ;;

  (stop)
      if [ -f $pid ]; then
        TARGET_PID=`cat $pid`
        if kill -0 $TARGET_PID > /dev/null 2>&1; then
          echo stopping $command
          pkill -P $TARGET_PID
          sleep $STOP_TIMEOUT
          if kill -0 $TARGET_PID > /dev/null 2>&1; then
            echo "$command did not stop gracefully after $STOP_TIMEOUT seconds: killing with kill -9"
            pkill -P -9 $TARGET_PID
          fi
        else
          echo no $command to stop
        fi
        rm -f $pid
      else
        echo no $command to stop
      fi
      ;;

  (status)
    get_server_running_status
    if [[ $state == "STOP" ]]; then
      #  font color - red
      state="[ \033[1;31m $state \033[0m ]"
    else
      # font color - green
      state="[ \033[1;32m $state \033[0m ]"
    fi
    echo -e "$command  $state"
    ;;

  (*)
    echo $usage
    exit 1
    ;;

esac

echo "End $startStop $command."
