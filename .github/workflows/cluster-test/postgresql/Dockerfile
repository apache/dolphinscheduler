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

FROM eclipse-temurin:8-jre

RUN apt update ; \
    apt install -y wget sudo openssh-server netcat-traditional ;

COPY ./apache-dolphinscheduler-*-SNAPSHOT-bin.tar.gz /root
RUN tar -zxvf /root/apache-dolphinscheduler-*-SNAPSHOT-bin.tar.gz -C ~

RUN mv /root/apache-dolphinscheduler-*-SNAPSHOT-bin /root/apache-dolphinscheduler-test-SNAPSHOT-bin

ENV DOLPHINSCHEDULER_HOME /root/apache-dolphinscheduler-test-SNAPSHOT-bin

#Setting install.sh
COPY .github/workflows/cluster-test/postgresql/install_env.sh $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh

#Setting dolphinscheduler_env.sh
COPY .github/workflows/cluster-test/postgresql/dolphinscheduler_env.sh $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh

#Deploy
COPY .github/workflows/cluster-test/postgresql/deploy.sh /root/deploy.sh

CMD [ "/bin/bash", "/root/deploy.sh" ]
