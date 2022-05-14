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
    apt install -y curl wget default-mysql-client sudo openssh-server netcat-traditional ;

#COPY ./dolphinscheduler-dist/target/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz /root/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz
COPY ./apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz /root/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz
RUN tar -zxvf /root/apache-dolphinscheduler-dev-SNAPSHOT-bin.tar.gz -C ~

ENV DOLPHINSCHEDULER_HOME /root/apache-dolphinscheduler-dev-SNAPSHOT-bin

#Setting install.sh
COPY .github/workflows/cluster-test/mysql/install_env.sh $DOLPHINSCHEDULER_HOME/bin/env/install_env.sh

#Setting dolphinscheduler_env.sh
COPY .github/workflows/cluster-test/mysql/dolphinscheduler_env.sh $DOLPHINSCHEDULER_HOME/bin/env/dolphinscheduler_env.sh

#Download mysql jar
ENV MYSQL_URL "https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar"
ENV MYSQL_DRIVER "mysql-connector-java-8.0.16.jar"
RUN wget -O $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $MYSQL_URL ; \
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/api-server/libs/$MYSQL_DRIVER ; \
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/master-server/libs/$MYSQL_DRIVER ; \
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/worker-server/libs/$MYSQL_DRIVER ; \
cp $DOLPHINSCHEDULER_HOME/alert-server/libs/$MYSQL_DRIVER $DOLPHINSCHEDULER_HOME/tools/libs/$MYSQL_DRIVER

#Deploy
COPY .github/workflows/cluster-test/mysql/deploy.sh /root/deploy.sh

CMD [ "/bin/bash", "/root/deploy.sh" ]
