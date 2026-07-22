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
DATABASE_VERSION=$2

# Install dev schema
export DATABASE="mysql"
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver"
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler_dev?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false"
export SPRING_DATASOURCE_USERNAME="root"
export SPRING_DATASOURCE_PASSWORD="mysql"
bash ds_schema_check_test/dev/tools/bin/upgrade-schema.sh

# Install the target version schema and upgrade it
docker run -v "./ds_schema_check_test/mysql-connector-java-8.0.16.jar:/opt/dolphinscheduler/tools/libs/mysql-connector-java-8.0.16.jar" \
--network schema-test apache/dolphinscheduler-tools:${DS_VERSION} -c \
'export DATABASE="mysql"; \
export SPRING_DATASOURCE_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver"; \
export SPRING_DATASOURCE_USERNAME="root"; \
export SPRING_DATASOURCE_PASSWORD="mysql"; \
export SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/dolphinscheduler_${DATABASE_VERSION}?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false'; \
bash tools/bin/upgrade-schema.sh'
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler_${DATABASE_VERSION}?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true&useSSL=false"
bash ds_schema_check_test/dev/tools/bin/upgrade-schema.sh

# Compare the schema
set +x
atlas_result=$(atlas schema diff \
  --from "mysql://root:mysql@127.0.0.1:3306/dolphinscheduler_${DATABASE_VERSION}" \
  --to "mysql://root:mysql@127.0.0.1:3306/dolphinscheduler_dev")
if [[ ${atlas_result} != *"Schemas are synced"* ]]; then
  echo "================================================================================================"
  echo "                                !!!!! For Contributors !!!!!"
  echo "================================================================================================"
  echo "Database schema not sync, please add below change in the latest version of dolphinscheduler-dao/src/main/resources/sql/upgrade directory"
  echo "${atlas_result}"
  exit 1
else
  echo "================================================================================================"
  echo "                                !!!!! For Contributors !!!!!"
  echo "================================================================================================"
  echo "Database schema sync successfully"
  exit 0
fi
