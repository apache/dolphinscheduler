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

usage="Usage: dolphinscheduler-daemon.sh (start|stop|status) <api-server|master-server|worker-server|alert-server> "

# if no args specified, show usage
if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

startStop=$1
shift
command=$1
shift


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
  HEAP_NEW_GENERATION_SIZE=512m
  LOG_FILE="-Dlogging.config=classpath:logback-api.xml -Dspring.profiles.active=api"
  CLASS=org.apache.dolphinscheduler.api.ApiApplicationServer
elif [ "$command" = "master-server" ]; then
  HEAP_INITIAL_SIZE=4g
  HEAP_MAX_SIZE=4g
  HEAP_NEW_GENERATION_SIZE=2g
  LOG_FILE="-Dlogging.config=classpath:logback-master.xml -Ddruid.mysql.usePingMethod=false"
  CLASS=org.apache.dolphinscheduler.server.master.MasterServer
elif [ "$command" = "worker-server" ]; then
  HEAP_INITIAL_SIZE=2g
  HEAP_MAX_SIZE=2g
  HEAP_NEW_GENERATION_SIZE=1g
  LOG_FILE="-Dlogging.config=classpath:logback-worker.xml -Ddruid.mysql.usePingMethod=false"
  CLASS=org.apache.dolphinscheduler.server.worker.WorkerServer
elif [ "$command" = "alert-server" ]; then
  HEAP_INITIAL_SIZE=1g
  HEAP_MAX_SIZE=1g
  HEAP_NEW_GENERATION_SIZE=512m
  LOG_FILE="-Dlogback.configurationFile=conf/logback-alert.xml"
  CLASS=org.apache.dolphinscheduler.alert.AlertServer
elif [ "$command" = "logger-server" ]; then
  HEAP_INITIAL_SIZE=1g
  HEAP_MAX_SIZE=1g
  HEAP_NEW_GENERATION_SIZE=512m
  CLASS=org.apache.dolphinscheduler.server.log.LoggerServer
elif [ "$command" = "zookeeper-server" ]; then
  #note: this command just for getting a quick experience，not recommended for production. this operation will start a standalone zookeeper server
  LOG_FILE="-Dlogback.configurationFile=classpath:logback-zookeeper.xml"
  CLASS=org.apache.dolphinscheduler.service.zk.ZKServer
else
  echo "Error: No command named \`$command' was found."
  exit 1
fi

export DOLPHINSCHEDULER_OPTS="-server -Xms$HEAP_INITIAL_SIZE -Xmx$HEAP_MAX_SIZE -Xmn$HEAP_NEW_GENERATION_SIZE -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -Xss512k -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70 -XX:+PrintGCDetails -Xloggc:$DOLPHINSCHEDULER_LOG_DIR/gc.log -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=dump.hprof $DOLPHINSCHEDULER_OPTS"

case $startStop in
  (start)
    exec_command="$LOG_FILE $DOLPHINSCHEDULER_OPTS -classpath $DOLPHINSCHEDULER_CONF_DIR:$DOLPHINSCHEDULER_LIB_JARS $CLASS"
    if [ "$DOCKER" = "true" ]; then
      echo "start in docker"
      $JAVA_HOME/bin/java $exec_command
    else
      [ -w "$DOLPHINSCHEDULER_PID_DIR" ] || mkdir -p "$DOLPHINSCHEDULER_PID_DIR"

      if [ -f $pid ]; then
        if kill -0 `cat $pid` > /dev/null 2>&1; then
          echo $command running as process `cat $pid`.  Stop it first.
          exit 1
        fi
      fi

      echo starting $command, logging to $log
      echo "nohup $JAVA_HOME/bin/java $exec_command > $log 2>&1 &"
      nohup $JAVA_HOME/bin/java $exec_command > $log 2>&1 &
      echo $! > $pid
    fi
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

  (status)
    # more details about the status can be added later
    serverCount=`ps -ef |grep "$CLASS" |grep -v "grep" |wc -l`
    state="STOP"
    #  font color - red
    state="[ \033[1;31m $state \033[0m ]"
    if [[ $serverCount -gt 0 ]];then
      state="RUNNING"
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