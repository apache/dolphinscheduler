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

DS_VERSION=$1
DATABASE_VERSION=${VERSION//\./}

# Install Atlas and Create Dir
mkdir -p dolphinscheduler/dev dolphinscheduler/${DS_VERSION}
curl -sSf https://atlasgo.sh | sh

# Preparing the environment
wget https://archive.apache.org/dist/dolphinscheduler/${DS_VERSION}/apache-dolphinscheduler-${DS_VERSION}-bin.tar.gz -P dolphinscheduler/${DS_VERSION}
tar -xzf dolphinscheduler/${DS_VERSION}/apache-dolphinscheduler-${DS_VERSION}-bin.tar.gz -C dolphinscheduler/${DS_VERSION} --strip-components 1
tar -xzf dolphinscheduler/dev/apache-dolphinscheduler-*-bin.tar.gz -C dolphinscheduler/dev --strip-components 1

if [[ $DATABASE_VERSION -lt 300 ]]; then
  chmod +x dolphinscheduler/dev/tools/bin/upgrade-schema.sh dolphinscheduler/${DS_VERSION}/script/create-dolphinscheduler.sh
else
  chmod +x dolphinscheduler/dev/tools/bin/upgrade-schema.sh dolphinscheduler/${DS_VERSION}/tools/bin/upgrade-schema.sh
fi

MYSQL_JDBC_URL="https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar"
MYSQL_JDBC_JAR="mysql-connector-java-8.0.16.jar"
wget ${MYSQL_JDBC_URL} -O /tmp/${MYSQL_JDBC_JAR}
for base_dir in dolphinscheduler/dev dolphinscheduler/${DS_VERSION}; do
  if [[ $base_dir == *"dolphinscheduler/2"* ]]; then
    cp /tmp/${MYSQL_JDBC_JAR} ${base_dir}/lib
  else
    for d in alert-server api-server master-server worker-server tools; do
      cp /tmp/${MYSQL_JDBC_JAR} ${base_dir}/${d}/libs
    done
  fi
done
docker compose -f .github/workflows/schema-check/mysql/docker-compose-base.yaml up -d
docker exec -i mysql mysql -uroot -pmysql -e "create database dolphinscheduler_${DATABASE_VERSION}";

#Running schema check tests
/bin/bash .github/workflows/schema-check/mysql/running-test.sh ${DS_VERSION} ${DATABASE_VERSION}

#Cleanup
docker compose -f .github/workflows/cluster-test/mysql_with_mysql_registry/docker-compose-cluster.yaml down -v --remove-orphans
