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
DATABASE_VERSION=${DS_VERSION//\./}

# Install Atlas and Create Dir
mkdir -p ds_schema_check_test/dev
curl -sSf https://atlasgo.sh | sh

# Preparing the environment
docker pull apache/dolphinscheduler-tools:${DS_VERSION}
tar -xzf ds_schema_check_test/dev/apache-dolphinscheduler-*-bin.tar.gz -C ds_schema_check_test/dev --strip-components 1

chmod +x ds_schema_check_test/dev/tools/bin/upgrade-schema.sh

MYSQL_JDBC_URL="https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar"
MYSQL_JDBC_JAR="mysql-connector-java-8.0.16.jar"
wget ${MYSQL_JDBC_URL} -O ds_schema_check_test/${MYSQL_JDBC_JAR}
for base_dir in ds_schema_check_test/dev; do
    for d in alert-server api-server master-server worker-server tools; do
      cp ds_schema_check_test/${MYSQL_JDBC_JAR} ${base_dir}/${d}/libs
    done
done
docker compose -f .github/workflows/schema-check/mysql/docker-compose-base.yaml up -d --wait
docker exec -i mysql mysql -uroot -pmysql -e "create database dolphinscheduler_${DATABASE_VERSION}";

#Running schema check tests
/bin/bash .github/workflows/schema-check/mysql/running-test.sh ${DS_VERSION} ${DATABASE_VERSION}

#Cleanup
docker compose -f .github/workflows/schema-check/mysql/docker-compose-base.yaml down -v --remove-orphans
