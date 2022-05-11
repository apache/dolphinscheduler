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
set -euox pipefail


DOLPHINSCHEDULER_HOME=/xxx

#Download mysql jar
MYSQL_URL="https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar"
MYSQL_DRIVER="mysql-connector-java-8.0.16.jar"
wget -O $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $MYSQL_URL
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/api-server/libs/$MYSQL_DRIVER
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/master-server/libs/$MYSQL_DRIVER
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/worker-server/libs/$MYSQL_DRIVER
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/tools/libs/$MYSQL_DRIVER

#Create database
mysql -uroot -p123456 -e "CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;"