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

#Start base service containers
docker-compose -f .github/workflows/cluster-test/mysql/docker-compose-base.yaml up -d

#Build ds mysql cluster image
docker build -t jdk8:ds_mysql_cluster -f .github/workflows/cluster-test/mysql/Dockerfile .

#Start ds mysql cluster container
docker-compose -f .github/workflows/cluster-test/mysql/docker-compose-cluster.yaml up -d

#Running tests
/bin/bash .github/workflows/cluster-test/mysql/running_test.sh

#Cleanup
docker rm -f $(docker ps -aq)
