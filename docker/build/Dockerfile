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

FROM openjdk:8-jdk-alpine

ARG VERSION

ENV TZ Asia/Shanghai
ENV LANG C.UTF-8
ENV DOCKER true

# 1. install command/library/software
# If install slowly, you can replcae alpine's mirror with aliyun's mirror, Example:
# RUN sed -i "s/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g" /etc/apk/repositories
# RUN sed -i 's/dl-cdn.alpinelinux.org/mirror.tuna.tsinghua.edu.cn/g' /etc/apk/repositories
RUN apk update && \
    apk add --no-cache tzdata dos2unix bash python2 python3 procps sudo shadow tini postgresql-client && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    apk del tzdata && \
    rm -rf /var/cache/apk/*

# 2. add dolphinscheduler
ADD ./apache-dolphinscheduler-incubating-${VERSION}-dolphinscheduler-bin.tar.gz /opt/
RUN ln -s /opt/apache-dolphinscheduler-incubating-${VERSION}-dolphinscheduler-bin /opt/dolphinscheduler
ENV DOLPHINSCHEDULER_HOME /opt/dolphinscheduler

# 3. add configuration and modify permissions and set soft links
COPY ./checkpoint.sh /root/checkpoint.sh
COPY ./startup-init-conf.sh /root/startup-init-conf.sh
COPY ./startup.sh /root/startup.sh
COPY ./conf/dolphinscheduler/*.tpl /opt/dolphinscheduler/conf/
COPY ./conf/dolphinscheduler/logback/* /opt/dolphinscheduler/conf/
COPY ./conf/dolphinscheduler/env/dolphinscheduler_env.sh /opt/dolphinscheduler/conf/env/
RUN dos2unix /root/checkpoint.sh && \
    dos2unix /root/startup-init-conf.sh && \
    dos2unix /root/startup.sh && \
    dos2unix /opt/dolphinscheduler/conf/env/dolphinscheduler_env.sh && \
    dos2unix /opt/dolphinscheduler/script/*.sh && \
    dos2unix /opt/dolphinscheduler/bin/*.sh && \
    rm -rf /bin/sh && \
    ln -s /bin/bash /bin/sh && \
    mkdir -p /var/mail /tmp/xls && \
    echo "Set disable_coredump false" >> /etc/sudo.conf

# 4. expose port
EXPOSE 5678 1234 12345 50051 50052

ENTRYPOINT ["/sbin/tini", "--", "/root/startup.sh"]
