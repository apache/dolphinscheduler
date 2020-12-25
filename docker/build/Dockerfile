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

FROM nginx:alpine

ARG VERSION

ENV TZ Asia/Shanghai
ENV LANG C.UTF-8
ENV DEBIAN_FRONTEND noninteractive

#1. install dos2unix shadow bash openrc python sudo vim wget iputils net-tools ssh pip tini kazoo.
#If install slowly, you can replcae alpine's mirror with aliyun's mirror, Example:
#RUN sed -i "s/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g" /etc/apk/repositories
RUN apk update && \
    apk --update add --no-cache dos2unix shadow bash openrc python2 python3 sudo vim wget iputils net-tools openssh-server py-pip tini && \
    apk add --update procps && \
    openrc boot && \
    pip install kazoo

#2. install jdk
RUN apk add openjdk8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $JAVA_HOME/bin:$PATH

#3. add dolphinscheduler
ADD ./apache-dolphinscheduler-incubating-${VERSION}-dolphinscheduler-bin.tar.gz /opt/
RUN mv /opt/apache-dolphinscheduler-incubating-${VERSION}-dolphinscheduler-bin/ /opt/dolphinscheduler/
ENV DOLPHINSCHEDULER_HOME /opt/dolphinscheduler

#4. install pg
RUN apk add postgresql postgresql-contrib
#5. modify nginx
RUN echo "daemon off;" >> /etc/nginx/nginx.conf && \
    rm -rf /etc/nginx/conf.d/*
ADD ./conf/nginx/dolphinscheduler.conf /etc/nginx/conf.d

#6. add configuration and modify permissions and set soft links
ADD ./checkpoint.sh /root/checkpoint.sh
ADD ./startup-init-conf.sh /root/startup-init-conf.sh
ADD ./startup.sh /root/startup.sh
ADD ./conf/dolphinscheduler/*.tpl /opt/dolphinscheduler/conf/
ADD ./conf/dolphinscheduler/logback/* /opt/dolphinscheduler/conf/
ADD conf/dolphinscheduler/env/dolphinscheduler_env.sh /opt/dolphinscheduler/conf/env/
RUN chmod +x /root/checkpoint.sh && \
    chmod +x /root/startup-init-conf.sh && \
    chmod +x /root/startup.sh && \
    chmod +x /opt/dolphinscheduler/conf/env/dolphinscheduler_env.sh && \
    chmod +x /opt/dolphinscheduler/script/*.sh && \
    chmod +x /opt/dolphinscheduler/bin/*.sh && \
    dos2unix /root/checkpoint.sh && \
    dos2unix /root/startup-init-conf.sh && \
    dos2unix /root/startup.sh && \
    dos2unix /opt/dolphinscheduler/conf/env/dolphinscheduler_env.sh && \
    dos2unix /opt/dolphinscheduler/script/*.sh && \
    dos2unix /opt/dolphinscheduler/bin/*.sh && \
    rm -rf /bin/sh && \
    ln -s /bin/bash /bin/sh && \
    mkdir -p /tmp/xls

#7. remove apk index cache and disable coredup for sudo
RUN rm -rf /var/cache/apk/* && \
    echo "Set disable_coredump false" >> /etc/sudo.conf

#8. expose port
EXPOSE 2181 2888 3888 5432 5678 1234 12345 50051 8888

ENTRYPOINT ["/sbin/tini", "--", "/root/startup.sh"]
