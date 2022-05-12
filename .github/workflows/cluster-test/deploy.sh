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


USER=$(whoami)
DOLPHINSCHEDULER_HOME=/home/$USER/apache-dolphinscheduler-dev-SNAPSHOT-bin

#Docker
#sudo apt install -y docker docker-compose
sudo docker-compose -f .github/workflows/cluster-test/docker-compose.yaml up -d

#Download mysql jar
MYSQL_URL="https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar"
MYSQL_DRIVER="mysql-connector-java-8.0.16.jar"
wget -O $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $MYSQL_URL
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/api-server/libs/$MYSQL_DRIVER
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/master-server/libs/$MYSQL_DRIVER
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/worker-server/libs/$MYSQL_DRIVER
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/tools/libs/$MYSQL_DRIVER

#Create database
sudo apt install -y mycli
sleep 10
mysql -h0.0.0.0 -P3306 -uroot -p123456 -e "CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;"

#Sudo
sudo sed -i '$a'$USER'  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sudo sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers

#SSH
ssh-keygen -t rsa -P '' -f ~/.ssh/id_rsa
cat ~/.ssh/id_rsa.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

#Setting install.sh
sed -i 's|ips=.*|ips=${ips:-"localhost"}|g' $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sed -i 's|masters=.*|masters=${masters:-"localhost"}|g' $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sed -i 's|workers=.*|workers=${workers:-"localhost:default"}|g' $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sed -i 's|alertServer=.*|alertServer=${alertServer:-"localhost"}|g' $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sed -i 's|apiServers=.*|apiServers=${apiServers:-"localhost"}|g' $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh
sed -i "s|installPath=.*|installPath=$DOLPHINSCHEDULER_HOME}|g" $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh

#Setting dolphinscheduler_env.sh
sed -i 's|export DATABASE=.*|export DATABASE=mysql|g' $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sed -i 's|export SPRING_DATASOURCE_DRIVER_CLASS_NAME.*|export SPRING_DATASOURCE_DRIVER_CLASS_NAME=com.mysql.cj.jdbc.Driver|g' $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sed -i 's|export SPRING_DATASOURCE_URL.*|export SPRING_DATASOURCE_URL="jdbc:mysql://0.0.0.0:3306/dolphinscheduler?useUnicode=true\&characterEncoding=UTF-8\&useSSL=false"|g' $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sed -i 's|export SPRING_DATASOURCE_USERNAME.*|export SPRING_DATASOURCE_USERNAME=root|g' $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh
sed -i 's|export SPRING_DATASOURCE_PASSWORD.*|export SPRING_DATASOURCE_PASSWORD=123456|g' $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh

#Init schema
/bin/bash $DOLPHINSCHEDULER_HOME/tools/bin/create-schema.sh

#Start Cluster
$DOLPHINSCHEDULER_HOME/bin/start-all.sh

#Cluster start health check
MASTER_PROCESS_NUM=$(ps -ef | grep -c MasterServer)
if [[ $MASTER_PROCESS_NUM -gt 0 ]];then
  echo "master health check success"
else
  echo "master health check failed"
  exit 1
fi

WORKER_PROCESS_NUM=$(ps -ef | grep -c WorkerServer)
if [[ $WORKER_PROCESS_NUM -gt 0 ]];then
  echo "worker health check success"
else
  echo "worker health check failed"
  exit 1
fi

ALERT_PROCESS_NUM=$(ps -ef | grep -c AlertServer)
if [[ $ALERT_PROCESS_NUM -gt 0 ]];then
  echo "alert health check success"
else
  echo "alert health check failed"
  exit 1
fi

API_PROCESS_NUM=$(ps -ef | grep -c ApiApplicationServer)
if [[ $API_PROCESS_NUM -gt 0 ]];then
  echo "api health check success"
else
  echo "api health check failed"
  exit 1
fi

#Stop Cluster
sudo $DOLPHINSCHEDULER_HOME/bin/stop-all.sh

#Cluster stop health check
MASTER_PROCESS_NUM=$(ps -ef | grep -c MasterServer)
if [[ $MASTER_PROCESS_NUM -eq 0 ]];then
  echo "master health check success"
else
  echo "master health check failed"
  exit 1
fi

WORKER_PROCESS_NUM=$(ps -ef | grep -c WorkerServer)
if [[ $WORKER_PROCESS_NUM -eq 0 ]];then
  echo "worker health check success"
else
  echo "worker health check failed"
  exit 1
fi

ALERT_PROCESS_NUM=$(ps -ef | grep -c AlertServer)
if [[ $ALERT_PROCESS_NUM -eq 0 ]];then
  echo "alert health check success"
else
  echo "alert health check failed"
  exit 1
fi

API_PROCESS_NUM=$(ps -ef | grep -c ApiApplicationServer)
if [[ $API_PROCESS_NUM -eq 0 ]];then
  echo "api health check success"
else
  echo "api health check failed"
  exit 1
fi
