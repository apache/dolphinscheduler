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

FROM ubuntu:18.04

ENV LANG=C.UTF-8
ENV DEBIAN_FRONTEND=noninteractive

ARG version
ARG tar_version

#1,install jdk

RUN apt-get update \
    && apt-get -y install openjdk-8-jdk \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH


#install wget
RUN apt-get update && \
        apt-get -y install wget
#2,install ZK

RUN cd /opt && \
    wget https://www-us.apache.org/dist/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz  && \
    tar -zxvf zookeeper-3.4.14.tar.gz  && \
    mv zookeeper-3.4.14 zookeeper && \
    rm -rf ./zookeeper-*tar.gz && \
    mkdir -p /tmp/zookeeper && \
    rm -rf /opt/zookeeper/conf/zoo_sample.cfg

ADD ./dockerfile/conf/zookeeper/zoo.cfg /opt/zookeeper/conf
ENV ZK_HOME=/opt/zookeeper
ENV PATH $PATH:$ZK_HOME/bin

#3,install maven
RUN cd /opt && \
    wget http://apache-mirror.rbc.ru/pub/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz && \
    tar -zxvf apache-maven-3.3.9-bin.tar.gz && \
    mv apache-maven-3.3.9 maven && \
    rm -rf ./apache-maven-*tar.gz && \
    rm -rf /opt/maven/conf/settings.xml
ADD ./dockerfile/conf/maven/settings.xml /opt/maven/conf
ENV MAVEN_HOME=/opt/maven
ENV PATH $PATH:$MAVEN_HOME/bin

#4,install postgresql
RUN apt-get update && \
    apt-get install -y postgresql postgresql-contrib sudo && \
    sed -i 's/localhost/*/g' /etc/postgresql/10/main/postgresql.conf

#5,install nginx
RUN apt-get update && \
  apt-get install -y nginx && \
  rm -rf /var/lib/apt/lists/* && \
  echo "\ndaemon off;" >> /etc/nginx/nginx.conf && \
  chown -R www-data:www-data /var/lib/nginx

#6,install sudo,python,vim,ping and ssh command
RUN apt-get update && \
  apt-get -y install sudo && \
  apt-get -y install python && \
  apt-get -y install vim && \
  apt-get -y install iputils-ping && \
  apt-get -y install net-tools && \
  apt-get -y install openssh-server && \
  apt-get -y install python-pip && \
  pip install kazoo

#7,add dolphinscheduler source code to /opt/dolphinscheduler_source
ADD . /opt/dolphinscheduler_source


#8,backend compilation
RUN cd /opt/dolphinscheduler_source && \
    mvn clean package -Prelease -Dmaven.test.skip=true

#11,modify dolphinscheduler configuration file
#backend configuration
RUN tar -zxvf /opt/dolphinscheduler_source/dolphinscheduler-dist/dolphinscheduler-backend/target/apache-dolphinscheduler-incubating-${tar_version}-dolphinscheduler-backend-bin.tar.gz -C /opt && \
    mv /opt/apache-dolphinscheduler-incubating-${tar_version}-dolphinscheduler-backend-bin /opt/dolphinscheduler && \
    rm -rf /opt/dolphinscheduler/conf

ADD ./dockerfile/conf/dolphinscheduler/conf /opt/dolphinscheduler/conf
#frontend nginx configuration
ADD ./dockerfile/conf/nginx/dolphinscheduler.conf /etc/nginx/conf.d

#9,open port
EXPOSE 2181 2888 3888 3306 80 12345 8888

COPY ./dockerfile/startup.sh /root/startup.sh
#10,modify permissions and set soft links
RUN chmod +x /root/startup.sh && \
  chmod +x /opt/dolphinscheduler/script/create-dolphinscheduler.sh && \
  chmod +x /opt/zookeeper/bin/zkServer.sh && \
  chmod +x /opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh && \
  rm -rf /bin/sh && \
  ln -s /bin/bash /bin/sh && \
  mkdir -p /tmp/xls


ENTRYPOINT ["/root/startup.sh"]