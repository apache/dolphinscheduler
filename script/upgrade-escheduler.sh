#!/bin/bash

BIN_DIR=`dirname $0`
BIN_DIR=`cd "$BIN_DIR"; pwd`
ESCHEDULER_HOME=$BIN_DIR/..

export JAVA_HOME=$JAVA_HOME


export ESCHEDULER_CONF_DIR=$ESCHEDULER_HOME/conf
export ESCHEDULER_LIB_JARS=$ESCHEDULER_HOME/lib/*

export ESCHEDULER_OPTS="-server -Xmx1g -Xms1g -Xss512k -XX:+DisableExplicitGC -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=70"
export STOP_TIMEOUT=5

CLASS=cn.escheduler.dao.upgrade.shell.UpgradeDolphinScheduler

exec_command="$ESCHEDULER_OPTS -classpath $ESCHEDULER_CONF_DIR:$ESCHEDULER_LIB_JARS $CLASS"

cd $ESCHEDULER_HOME
$JAVA_HOME/bin/java $exec_command
