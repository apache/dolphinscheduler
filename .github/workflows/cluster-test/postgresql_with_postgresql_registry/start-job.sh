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

#Start base service containers
docker-compose -f .github/workflows/cluster-test/postgresql_with_postgresql_registry/docker-compose-base.yaml up -d

#Build ds postgresql cluster image
docker build -t jdk8:ds_postgresql_cluster -f .github/workflows/cluster-test/postgresql_with_postgresql_registry/Dockerfile .

#Start ds postgresql cluster container
docker-compose -f .github/workflows/cluster-test/postgresql_with_postgresql_registry/docker-compose-cluster.yaml up -d

#Running tests
/bin/bash .github/workflows/cluster-test/postgresql_with_postgresql_registry/running_test.sh

#Cleanup
docker-compose -f .github/workflows/cluster-test/postgresql_with_postgresql_registry/docker-compose-cluster.yaml down -v --remove-orphans
docker-compose -f .github/workflows/cluster-test/postgresql_with_postgresql_registry/docker-compose-base.yaml down -v --remove-orphans
