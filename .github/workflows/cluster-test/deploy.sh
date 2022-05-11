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


DOLPHINSCHEDULER_HOME=/tmp/apache-dolphinscheduler-dev-SNAPSHOT-bin

#Docker
apt install -y docker docker-compose
docker-compose -f .github/workflows/cluster-test/docker-compose.yaml up -d

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
mysql -uroot -p123456 -e "source ${DOLPHINSCHEDULER_HOME}/tools/sql/sql/dolphinscheduler_mysql.sql"

#Setting install.sh
sudo sed -i '$aroot  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sodu sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers
sudo sed -i 's|ips=.*|ips=${ips:-"localhost"}|g' /$DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sudo sed -i 's|masters=.*|masters=${masters:-"localhost"}|g' /$DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sudo sed -i 's|workers=.*|workers=${workers:-"localhost:default"}|g' /$DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sudo sed -i 's|alertServer=.*|alertServer=${alertServer:-"localhost"}|g' /$DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sudo sed -i 's|apiServers=.*|apiServers=${apiServers:-"localhost"}|g' /$DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sudo sed -i "s|installPath=.*|installPath=$DOLPHINSCHEDULER_HOME}|g" /$DOLPHINSCHEDULER_HOME/bin/env/install_env.sh

#Setting dolphinscheduler_env.sh
sudo sed -i 's|export DATABASE=.*|export DATABASE=mysql|g' /$DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sudo sed -i 's|export SPRING_DATASOURCE_DRIVER_CLASS_NAME=.*|export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver|g' /$DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sudo sed -i 's|export SPRING_DATASOURCE_URL=.*|export SPRING_DATASOURCE_URL="jdbc:mysql://0.0.0.0:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"|g' /$DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sudo sed -i 's|export SPRING_DATASOURCE_USERNAME=.*|export SPRING_DATASOURCE_USERNAME=root|g' /$DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sudo sed -i 's|export SPRING_DATASOURCE_PASSWORD=.*|export SPRING_DATASOURCE_PASSWORD=123456|g' /$DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh

#Start Cluster
sudo $DOLPHINSCHEDULER_HOME/bin/start-all.sh

#Healthcheck
