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

FROM openjdk:8-jre-slim-buster

RUN apt update ; \
    apt install -y curl wget sudo openssh-server netcat-traditional ;

#COPY ./dolphinscheduler-dist/target/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz /root/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz
COPY ./apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz /root/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz
RUN tar -zxvf /root/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz -C ~

ENV DOLPHINSCHEDULER_HOME /root/apache-dolphinscheduler-dev-SNAPSHOT-bin

#Setting install.sh
COPY .github/workflows/cluster-test/postgresql/install_env.sh $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh

#Setting dolphinscheduler_env.sh
COPY .github/workflows/cluster-test/postgresql/dolphinscheduler_env.sh $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh

#Deploy
COPY .github/workflows/cluster-test/postgresql/deploy.sh /root/deploy.sh

CMD [ "/bin/bash", "/root/deploy.sh" ]
