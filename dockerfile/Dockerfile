FROM ww1516123/incubator-dolphinscheduler-base

MAINTAINER maple "ww1516123@126.com"

ARG version
ARG tar_version


ADD ./conf/zookeeper/zoo.cfg /opt/zookeeper/conf
ADD ./conf/maven/settings.xml /opt/maven/conf


# download
RUN cd /opt && \
    wget https://github.com/apache/incubator-dolphinscheduler/archive/${version}.tar.gz && \
    tar -zxvf ${version}.tar.gz && \
    mv incubator-dolphinscheduler-${version} incubator-dolphinscheduler_source && \
    rm -rf ./${version}.tar.gz

# backend build
RUN cd /opt/incubator-dolphinscheduler_source && \
    mvn -U clean package assembly:assembly -Dmaven.test.skip=true

# fontend build
RUN chmod -R 777 /opt/incubator-dolphinscheduler_source/escheduler-ui && \
    cd /opt/incubator-dolphinscheduler_source/escheduler-ui && \
    rm -rf /opt/incubator-dolphinscheduler_source/escheduler-ui/node_modules && \
    npm install node-sass --unsafe-perm && \
    npm install && \
    npm run build

# setting
# backend setting
RUN mkdir -p /opt/escheduler && \
    tar -zxvf /opt/incubator-dolphinscheduler_source/target/escheduler-${tar_version}.tar.gz -C /opt/escheduler && \
    rm -rf /opt/escheduler/conf
ADD ./conf/escheduler/conf /opt/escheduler/conf

# nginx config
ADD ./conf/nginx/default.conf /etc/nginx/conf.d


# mysql config

RUN sed -i -e "$ a [client]\n\n[mysql]\n\n[mysqld]"  /etc/mysql/my.cnf && \
        sed -i -e "s/\(\[client\]\)/\1\ndefault-character-set = utf8/g" /etc/mysql/my.cnf && \
        sed -i -e "s/\(\[mysql\]\)/\1\ndefault-character-set = utf8/g" /etc/mysql/my.cnf && \
        sed -i -e "s/\(\[mysqld\]\)/\1\ninit_connect='SET NAMES utf8'\ncharacter-set-server = utf8\ncollation-server=utf8_general_ci\nbind-address = 0.0.0.0/g" /etc/mysql/my.cnf

COPY ./startup.sh /root/startup.sh

# chomod
RUN chmod +x /root/startup.sh && \
  chmod +x /opt/escheduler/script/create_escheduler.sh && \
  chmod +x /opt/zookeeper/bin/zkServer.sh && \
  chmod +x /opt/escheduler/bin/escheduler-daemon.sh && \
  rm -rf /bin/sh && \
  ln -s /bin/bash /bin/sh && \
  mkdir -p /tmp/xls

# port
EXPOSE 2181 2888 3888 3306 80 12345 8888

ENTRYPOINT ["/root/startup.sh"]
