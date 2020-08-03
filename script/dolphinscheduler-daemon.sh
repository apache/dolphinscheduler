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

usage="Usage: dolphinscheduler-daemon.sh (start|stop) <command> "

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
DOLPHINSCHEDULER_HOME=$BIN_DIR/..

source /etc/profile

export JAVA_HOME=$JAVA_HOME
#export JAVA_HOME=/opt/soft/jdk
export HOSTNAME=`hostname`

export DOLPHINSCHEDULER_PID_DIR=$DOLPHINSCHEDULER_HOME/pid
export DOLPHINSCHEDULER_LOG_DIR=$DOLPHINSCHEDULER_HOME/logs
export DOLPHINSCHEDULER_CONF_DIR=$DOLPHINSCHEDULER_HOME/conf
export DOLPHINSCHEDULER_LIB_JARS=$DOLPHINSCHEDULER_HOME/lib/*

export STOP_TIMEOUT=5

if [ ! -d "$DOLPHINSCHEDULER_LOG_DIR" ]; then
  mkdir $DOLPHINSCHEDULER_LOG_DIR
fi

log=$DOLPHINSCHEDULER_LOG_DIR/dolphinscheduler-$command-$HOSTNAME.out
pid=$DOLPHINSCHEDULER_PID_DIR/dolphinscheduler-$command.pid

cd $DOLPHINSCHEDULER_HOME

if [ "$command" = "api-server" ]; then
  HEAP_INITIAL_SIZE=1g
  HEAP_MAX_SIZE=1g
  HEAP_NEW_GENERATION__SIZE=500m
  LOG_FILE="-Dlogging.config=classpath:logback-api.xml -Dspring.profiles.active=api"
  CLASS=org.apache.dolphinscheduler.api.ApiApplicationServer
elif [ "$command" = "master-server" ]; then
  HEAP_INITIAL_SIZE=4g
  HEAP_MAX_SIZE=4g
  HEAP_NEW_GENERATION__SIZE=2g
  LOG_FILE="-Dlogging.config=classpath:logback-master.xml -Ddruid.mysql.usePingMethod=false"
  CLASS=org.apache.dolphinscheduler.server.master.MasterServer
elif [ "$command" = "worker-server" ]; then
  HEAP_INITIAL_SIZE=2g
  HEAP_MAX_SIZE=2g
  HEAP_NEW_GENERATION__SIZE=1g
  LOG_FILE="-Dlogging.config=classpath:logback-worker.xml -Ddruid.mysql.usePingMethod=false"
  CLASS=org.apache.dolphinscheduler.server.worker.WorkerServer
elif [ "$command" = "alert-server" ]; then
  HEAP_INITIAL_SIZE=1g
  HEAP_MAX_SIZE=1g
  HEAP_NEW_GENERATION__SIZE=500m
  LOG_FILE="-Dlogback.configurationFile=conf/logback-alert.xml"
  CLASS=org.apache.dolphinscheduler.alert.AlertServer
elif [ "$command" = "logger-server" ]; then
  HEAP_INITIAL_SIZE=1g
  HEAP_MAX_SIZE=1g
  HEAP_NEW_GENERATION__SIZE=500m
  CLASS=org.apache.dolphinscheduler.server.log.LoggerServer
else
  echo "Error: No command named \`$command' was found."
  exit 1
fi

export DOLPHINSCHEDULER_OPTS="-server -Xms$HEAP_INITIAL_SIZE -Xmx$HEAP_MAX_SIZE -Xmn$HEAP_NEW_GENERATION__SIZE -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m  -Xss512k -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -Xloggc:gc.log -XX:+HeapDumpOnOutOfMemoryError  -XX:HeapDumpPath=dump.hprof"

case $startStop in
  (start)
    [ -w "$DOLPHINSCHEDULER_PID_DIR" ] ||  mkdir -p "$DOLPHINSCHEDULER_PID_DIR"

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    echo starting $command, logging to $log

    exec_command="$LOG_FILE $DOLPHINSCHEDULER_OPTS -classpath $DOLPHINSCHEDULER_CONF_DIR:$DOLPHINSCHEDULER_LIB_JARS $CLASS"

    echo "nohup $JAVA_HOME/bin/java $exec_command > $log 2>&1 &"
    nohup $JAVA_HOME/bin/java $exec_command > $log 2>&1 &
    echo $! > $pid
    ;;

  (stop)

      if [ -f $pid ]; then
        TARGET_PID=`cat $pid`
        if kill -0 $TARGET_PID > /dev/null 2>&1; then
          echo stopping $command
          kill $TARGET_PID
          sleep $STOP_TIMEOUT
          if kill -0 $TARGET_PID > /dev/null 2>&1; then
            echo "$command did not stop gracefully after $STOP_TIMEOUT seconds: killing with kill -9"
            kill -9 $TARGET_PID
          fi
        else
          echo no $command to stop
        fi
        rm -f $pid
      else
        echo no $command to stop
      fi
      ;;

  (*)
    echo $usage
    exit 1
    ;;

esac

echo "End $startStop $command."