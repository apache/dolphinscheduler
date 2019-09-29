#!/bin/sh

usage="Usage: escheduler-daemon.sh (start|stop) <command> "

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
ESCHEDULER_HOME=$BIN_DIR/..

export JAVA_HOME=$JAVA_HOME
#export JAVA_HOME=/opt/soft/jdk
export HOSTNAME=`hostname`

export ESCHEDULER_PID_DIR=/tmp/
export ESCHEDULER_LOG_DIR=$ESCHEDULER_HOME/logs
export ESCHEDULER_CONF_DIR=$ESCHEDULER_HOME/conf
export ESCHEDULER_LIB_JARS=$ESCHEDULER_HOME/lib/*

export ESCHEDULER_OPTS="-server -Xmx16g -Xms4g -Xss512k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70"
export STOP_TIMEOUT=5

if [ ! -d "$ESCHEDULER_LOG_DIR" ]; then
  mkdir $ESCHEDULER_LOG_DIR
fi

log=$ESCHEDULER_LOG_DIR/escheduler-$command-$HOSTNAME.out
pid=$ESCHEDULER_LOG_DIR/escheduler-$command.pid

cd $ESCHEDULER_HOME

if [ "$command" = "api-server" ]; then
  LOG_FILE="-Dlogging.config=conf/apiserver_logback.xml"
  CLASS=cn.escheduler.api.ApiApplicationServer
elif [ "$command" = "master-server" ]; then
  LOG_FILE="-Dspring.config.location=conf/application_master.properties -Ddruid.mysql.usePingMethod=false"
  CLASS=cn.escheduler.server.master.MasterServer
elif [ "$command" = "worker-server" ]; then
  LOG_FILE="-Dspring.config.location=conf/application_worker.properties -Ddruid.mysql.usePingMethod=false"
  CLASS=cn.escheduler.server.worker.WorkerServer
elif [ "$command" = "alert-server" ]; then
  LOG_FILE="-Dspring.config.location=conf/application_alert.properties -Ddruid.mysql.usePingMethod=false"
  CLASS=cn.escheduler.alert.AlertServer
elif [ "$command" = "logger-server" ]; then
  CLASS=cn.escheduler.server.rpc.LoggerServer
else
  echo "Error: No command named \`$command' was found."
  exit 1
fi

case $startStop in
  (start)
    [ -w "$ESCHEDULER_PID_DIR" ] ||  mkdir -p "$ESCHEDULER_PID_DIR"

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    echo starting $command, logging to $log

    exec_command="$LOG_FILE $ESCHEDULER_OPTS -classpath $ESCHEDULER_CONF_DIR:$ESCHEDULER_LIB_JARS $CLASS"

    echo "nohup $JAVA_HOME/bin/java $exec_command > $log 2>&1 < /dev/null &"
    nohup $JAVA_HOME/bin/java $exec_command > $log 2>&1 < /dev/null &
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