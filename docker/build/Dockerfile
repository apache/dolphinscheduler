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

ARG VERSION
ARG DEBIAN_FRONTEND=noninteractive

ENV TZ Asia/Shanghai
ENV LANG C.UTF-8
ENV DOCKER true
ENV DOLPHINSCHEDULER_HOME /opt/dolphinscheduler

# 1. install command/library/software
# If install slowly, you can replcae debian's mirror with new mirror, Example:
# RUN { \
#     echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ buster main contrib non-free"; \
#     echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ buster-updates main contrib non-free"; \
#     echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian/ buster-backports main contrib non-free"; \
#     echo "deb http://mirrors.tuna.tsinghua.edu.cn/debian-security buster/updates main contrib non-free"; \
# } > /etc/apt/sources.list
RUN apt-get update && \
    apt-get install -y --no-install-recommends tzdata dos2unix python supervisor procps psmisc netcat sudo tini && \
    echo "Asia/Shanghai" > /etc/timezone && \
    rm -f /etc/localtime && \
    dpkg-reconfigure tzdata && \
    rm -rf /var/lib/apt/lists/* /tmp/*

# 2. add dolphinscheduler
ADD ./apache-dolphinscheduler-${VERSION}-bin.tar.gz /opt/
RUN ln -s -r /opt/apache-dolphinscheduler-${VERSION}-bin /opt/dolphinscheduler
WORKDIR /opt/apache-dolphinscheduler-${VERSION}-bin

# 3. add configuration and modify permissions and set soft links
COPY ./checkpoint.sh /root/checkpoint.sh
COPY ./startup-init-conf.sh /root/startup-init-conf.sh
COPY ./startup.sh /root/startup.sh
COPY ./conf/dolphinscheduler/*.tpl /opt/dolphinscheduler/conf/
COPY ./conf/dolphinscheduler/logback/* /opt/dolphinscheduler/conf/
COPY ./conf/dolphinscheduler/supervisor/supervisor.ini /etc/supervisor/conf.d/
COPY ./conf/dolphinscheduler/env/dolphinscheduler_env.sh.tpl /opt/dolphinscheduler/conf/env/
RUN sed -i 's/*.conf$/*.ini/' /etc/supervisor/supervisord.conf && \
    dos2unix /root/checkpoint.sh && \
    dos2unix /root/startup-init-conf.sh && \
    dos2unix /root/startup.sh && \
    dos2unix /opt/dolphinscheduler/script/*.sh && \
    dos2unix /opt/dolphinscheduler/bin/*.sh && \
    rm -f /bin/sh && \
    ln -s /bin/bash /bin/sh && \
    mkdir -p /tmp/xls && \
    echo PS1=\'\\w \\$ \' >> ~/.bashrc && \
    echo "Set disable_coredump false" >> /etc/sudo.conf

# 4. expose port
EXPOSE 5678 1234 12345 50051 50052

ENTRYPOINT ["/usr/bin/tini", "--", "/root/startup.sh"]
