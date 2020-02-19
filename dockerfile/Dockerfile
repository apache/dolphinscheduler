FROM nginx:alpine

ARG VERSION

ENV TZ Asia/Shanghai
ENV LANG C.UTF-8
ENV DEBIAN_FRONTEND noninteractive

#1. using aliyun mirror, and install dos2unix shadow bash openrc python sudo vim wget iputils net-tools ssh pip kazoo
RUN sed -i "s/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g" /etc/apk/repositories && \
    apk update && \
    apk add dos2unix shadow bash openrc python sudo vim wget iputils net-tools openssh-server py2-pip && \
    apk add --update procps && \
    openrc boot && \
    pip install kazoo

#2. install jdk
RUN apk add openjdk8
ENV JAVA_HOME /usr/lib/jvm/java-1.8-openjdk
ENV PATH $JAVA_HOME/bin:$PATH

#3. install zk
RUN cd /opt && \
    wget https://downloads.apache.org/zookeeper/zookeeper-3.5.7/apache-zookeeper-3.5.7-bin.tar.gz && \
    tar -zxvf apache-zookeeper-3.5.7-bin.tar.gz && \
    mv apache-zookeeper-3.5.7-bin zookeeper && \
    mkdir -p /tmp/zookeeper && \
    rm -rf ./zookeeper-*tar.gz && \
    rm -rf /opt/zookeeper/conf/zoo_sample.cfg
ADD ./conf/zookeeper/zoo.cfg /opt/zookeeper/conf
ENV ZK_HOME /opt/zookeeper
ENV PATH $ZK_HOME/bin:$PATH

#4. install pg
RUN apk add postgresql postgresql-contrib

#5. add dolphinscheduler
ADD ./apache-dolphinscheduler-incubating-${VERSION}-SNAPSHOT-dolphinscheduler-bin.tar.gz /opt/
RUN mv /opt/apache-dolphinscheduler-incubating-${VERSION}-SNAPSHOT-dolphinscheduler-bin/ /opt/dolphinscheduler/
ENV DOLPHINSCHEDULER_HOME /opt/dolphinscheduler

#6. modify nginx
RUN echo "daemon off;" >> /etc/nginx/nginx.conf && \
    rm -rf /etc/nginx/conf.d/*
ADD ./conf/nginx/dolphinscheduler.conf /etc/nginx/conf.d

#7. add configuration and modify permissions and set soft links
ADD ./startup-init-conf.sh /root/startup-init-conf.sh
ADD ./startup.sh /root/startup.sh
ADD ./conf/dolphinscheduler/*.tpl /opt/dolphinscheduler/conf/
ADD ./conf/dolphinscheduler/env/dolphinscheduler_env /opt/dolphinscheduler/conf/env/
RUN chmod +x /root/startup-init-conf.sh && \
    chmod +x /root/startup.sh && \
    chmod +x /opt/dolphinscheduler/conf/env/dolphinscheduler_env && \
    chmod +x /opt/dolphinscheduler/script/*.sh && \
    chmod +x /opt/dolphinscheduler/bin/*.sh && \
    chmod +x /opt/zookeeper/bin/*.sh && \
    dos2unix /root/startup-init-conf.sh && \
    dos2unix /root/startup.sh && \
    dos2unix /opt/dolphinscheduler/conf/env/dolphinscheduler_env && \
    dos2unix /opt/dolphinscheduler/script/*.sh && \
    dos2unix /opt/dolphinscheduler/bin/*.sh && \
    dos2unix /opt/zookeeper/bin/*.sh && \
    rm -rf /bin/sh && \
    ln -s /bin/bash /bin/sh && \
    mkdir -p /tmp/xls

#8. remove apk index cache
RUN rm -rf /var/cache/apk/*

#9. expose port
EXPOSE 2181 2888 3888 5432 12345 8888

ENTRYPOINT ["/root/startup.sh"]