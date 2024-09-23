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
set -xeo pipefail

DOCKER_HUB=$1
DOCKER_TAG=$2
DOCKER_REPO_BASE=dolphinscheduler

CURRENT_HOME=$(dirname $(readlink -f "$0"))

docker buildx build --load --no-cache -t $DOCKER_HUB/$DOCKER_REPO_BASE-api:$DOCKER_TAG -t $DOCKER_HUB/$DOCKER_REPO_BASE-api:latest -f ${CURRENT_HOME}/api-server.dockerfile .
docker buildx build --load -t $DOCKER_HUB/$DOCKER_REPO_BASE-master:$DOCKER_TAG -t $DOCKER_HUB/$DOCKER_REPO_BASE-master:latest -f ${CURRENT_HOME}/master-server.dockerfile .
docker buildx build --load -t $DOCKER_HUB/$DOCKER_REPO_BASE-worker:$DOCKER_TAG -t $DOCKER_HUB/$DOCKER_REPO_BASE-worker:latest -f ${CURRENT_HOME}/worker-server.dockerfile .
docker buildx build --load -t $DOCKER_HUB/$DOCKER_REPO_BASE-alert-server:$DOCKER_TAG -t $DOCKER_HUB/$DOCKER_REPO_BASE-alert-server:latest -f ${CURRENT_HOME}/alert-server.dockerfile .
docker buildx build --load -t $DOCKER_HUB/$DOCKER_REPO_BASE-standalone-server:$DOCKER_TAG -t $DOCKER_HUB/$DOCKER_REPO_BASE-standalone-server:latest -f ${CURRENT_HOME}/standalone-server.dockerfile .
docker buildx build --load -t $DOCKER_HUB/$DOCKER_REPO_BASE-tools:$DOCKER_TAG -t $DOCKER_HUB/$DOCKER_REPO_BASE-tools:latest -f ${CURRENT_HOME}/tools.dockerfile .
