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

FROM maven:3.6-openjdk-8-slim AS builder
WORKDIR /app
ADD . ./

RUN mvn -B clean compile package -Prelease -Dmaven.test.skip=true
RUN mkdir /opt/dolphinscheduler && \
    tar xf /app/dolphinscheduler-dist/target/*.tar.gz -C /opt/dolphinscheduler --strip 1

RUN apt update && \
    apt install dos2unix
RUN cp docker/build/conf/dolphinscheduler/*.tpl /opt/dolphinscheduler/conf/ && \
    cp docker/build/conf/dolphinscheduler/logback/* /opt/dolphinscheduler/conf/ && \
    cp docker/build/conf/dolphinscheduler/env/dolphinscheduler_env.sh /opt/dolphinscheduler/conf/env/ && \
    cp docker/build/conf/nginx/dolphinscheduler.conf /tmp/
RUN dos2unix docker/build/checkpoint.sh && \
    dos2unix docker/build/startup-init-conf.sh && \
    dos2unix docker/build/startup.sh && \
    dos2unix /opt/dolphinscheduler/conf/env/dolphinscheduler_env.sh && \
    dos2unix /opt/dolphinscheduler/script/*.sh && \
    dos2unix /opt/dolphinscheduler/bin/*.sh && \
    chmod +x docker/build/checkpoint.sh && \
    chmod +x docker/build/startup-init-conf.sh && \
    chmod +x docker/build/startup.sh && \
    chmod +x /opt/dolphinscheduler/conf/env/dolphinscheduler_env.sh && \
    chmod +x /opt/dolphinscheduler/script/*.sh && \
    chmod +x /opt/dolphinscheduler/bin/*.sh

# the docker image for service run
FROM nginx:alpine
WORKDIR /root

RUN apk update && \
    apk add --no-cache postgresql-client openjdk8 bash python3 python tini procps sudo shadow openrc openssh-server

RUN rm -rf /var/cache/apk/* && \
    rm -rf /etc/nginx/conf.d/* && \
    echo "Set disable_coredump false" >> /etc/sudo.conf && \
    echo "daemon off;" >> /etc/nginx/nginx.conf

COPY --from=builder /app/docker/build/*.sh ./
COPY --from=builder /opt/dolphinscheduler/ /opt/dolphinscheduler/
COPY --from=builder /tmp/dolphinscheduler.conf /etc/nginx/conf.d/

ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $JAVA_HOME/bin:$PATH
ENV DOLPHINSCHEDULER_HOME /opt/dolphinscheduler

EXPOSE 2181 2888 3888 5432 5678 1234 12345 50051 8888
ENTRYPOINT ["/sbin/tini", "--", "/root/startup.sh"]
