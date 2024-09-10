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
mkdir -p ds_schema_check_test/dev ds_schema_check_test/${DS_VERSION}
curl -sSf https://atlasgo.sh | sh

# Preparing the environment
if [ ! -f /tmp/${DS_VERSION}/apache-dolphinscheduler-${DS_VERSION}-bin.tar.gz ]; then
  wget https://archive.apache.org/dist/dolphinscheduler/${DS_VERSION}/apache-dolphinscheduler-${DS_VERSION}-bin.tar.gz -P ds_schema_check_test/${DS_VERSION}
else
  mv /tmp/${DS_VERSION}/apache-dolphinscheduler-${DS_VERSION}-bin.tar.gz ds_schema_check_test/${DS_VERSION}
fi
tar -xzf ds_schema_check_test/${DS_VERSION}/apache-dolphinscheduler-${DS_VERSION}-bin.tar.gz -C ds_schema_check_test/${DS_VERSION} --strip-components 1
tar -xzf ds_schema_check_test/dev/apache-dolphinscheduler-*-bin.tar.gz -C ds_schema_check_test/dev --strip-components 1

if [[ $DATABASE_VERSION -lt 300 ]]; then
  chmod +x ds_schema_check_test/dev/tools/bin/upgrade-schema.sh ds_schema_check_test/${DS_VERSION}/script/create-dolphinscheduler.sh
else
  chmod +x ds_schema_check_test/dev/tools/bin/upgrade-schema.sh ds_schema_check_test/${DS_VERSION}/tools/bin/upgrade-schema.sh
fi

docker compose -f .github/workflows/schema-check/postgresql/docker-compose-base.yaml up -d --wait
docker exec -i postgres psql -U postgres -c "create database dolphinscheduler_${DATABASE_VERSION}";

#Running schema check tests
/bin/bash .github/workflows/schema-check/postgresql/running-test.sh ${DS_VERSION} ${DATABASE_VERSION}

#Cleanup
docker compose -f .github/workflows/schema-check/postgresql/docker-compose-base.yaml down -v --remove-orphans
