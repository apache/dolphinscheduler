FROM ubuntu:18.04

MAINTAINER journey "825193156@qq.com"

ENV LANG=C.UTF-8

ARG version
ARG tar_version

#1,安装jdk

RUN apt-get update \
    && apt-get -y install openjdk-8-jdk \
    && rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME /usr/lib/jvm/java-8-openjdk-amd64
ENV PATH $JAVA_HOME/bin:$PATH


#安装wget
RUN apt-get update && \
        apt-get -y install wget
#2,安装ZK
#RUN cd /opt && \
#    wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz && \
#    tar -zxvf zookeeper-3.4.6.tar.gz && \
#    mv zookeeper-3.4.6 zookeeper && \
#    rm -rf ./zookeeper-*tar.gz && \
#    mkdir -p /tmp/zookeeper && \
#    rm -rf /opt/zookeeper/conf/zoo_sample.cfg

RUN cd /opt && \
    wget https://www-us.apache.org/dist/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz  && \
    tar -zxvf zookeeper-3.4.14.tar.gz  && \
    mv zookeeper-3.4.14 zookeeper && \
    rm -rf ./zookeeper-*tar.gz && \
    mkdir -p /tmp/zookeeper && \
    rm -rf /opt/zookeeper/conf/zoo_sample.cfg

ADD ./conf/zookeeper/zoo.cfg /opt/zookeeper/conf
ENV ZK_HOME=/opt/zookeeper
ENV PATH $PATH:$ZK_HOME/bin

#3,安装maven
RUN cd /opt && \
    wget http://apache-mirror.rbc.ru/pub/apache/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.tar.gz && \
    tar -zxvf apache-maven-3.3.9-bin.tar.gz && \
    mv apache-maven-3.3.9 maven && \
    rm -rf ./apache-maven-*tar.gz && \
    rm -rf /opt/maven/conf/settings.xml
ADD ./conf/maven/settings.xml /opt/maven/conf
ENV MAVEN_HOME=/opt/maven
ENV PATH $PATH:$MAVEN_HOME/bin

#4,安装node
RUN cd /opt && \
    wget https://nodejs.org/download/release/v8.9.4/node-v8.9.4-linux-x64.tar.gz && \
    tar -zxvf node-v8.9.4-linux-x64.tar.gz && \
    mv node-v8.9.4-linux-x64 node && \
    rm -rf ./node-v8.9.4-*tar.gz
ENV NODE_HOME=/opt/node
ENV PATH $PATH:$NODE_HOME/bin

#5,下载escheduler
RUN cd /opt && \
    wget https://github.com/analysys/EasyScheduler/archive/${version}.tar.gz && \
    tar -zxvf ${version}.tar.gz && \
    mv EasyScheduler-${version} easyscheduler_source && \
    rm -rf ./${version}.tar.gz

#6,后端编译
RUN cd /opt/easyscheduler_source && \
    mvn -U clean package assembly:assembly -Dmaven.test.skip=true

#7,前端编译
RUN chmod -R 777 /opt/easyscheduler_source/escheduler-ui && \
    cd /opt/easyscheduler_source/escheduler-ui && \
    rm -rf /opt/easyscheduler_source/escheduler-ui/node_modules && \
    npm install node-sass --unsafe-perm && \
    npm install && \
    npm run build
#8,安装mysql
RUN echo "deb http://cn.archive.ubuntu.com/ubuntu/ xenial main restricted universe multiverse" >> /etc/apt/sources.list

RUN echo "mysql-server mysql-server/root_password password root" | debconf-set-selections
RUN echo "mysql-server mysql-server/root_password_again password root" | debconf-set-selections

RUN apt-get update && \
        apt-get -y install mysql-server-5.7 && \
        mkdir -p /var/lib/mysql && \
        mkdir -p /var/run/mysqld && \
        mkdir -p /var/log/mysql && \
        chown -R mysql:mysql /var/lib/mysql && \
        chown -R mysql:mysql /var/run/mysqld && \
        chown -R mysql:mysql /var/log/mysql


# UTF-8 and bind-address
RUN sed -i -e "$ a [client]\n\n[mysql]\n\n[mysqld]"  /etc/mysql/my.cnf && \
        sed -i -e "s/\(\[client\]\)/\1\ndefault-character-set = utf8/g" /etc/mysql/my.cnf && \
        sed -i -e "s/\(\[mysql\]\)/\1\ndefault-character-set = utf8/g" /etc/mysql/my.cnf && \
        sed -i -e "s/\(\[mysqld\]\)/\1\ninit_connect='SET NAMES utf8'\ncharacter-set-server = utf8\ncollation-server=utf8_general_ci\nbind-address = 0.0.0.0/g" /etc/mysql/my.cnf


#9,安装nginx
RUN apt-get update && \
  apt-get install -y nginx && \
  rm -rf /var/lib/apt/lists/* && \
  echo "\ndaemon off;" >> /etc/nginx/nginx.conf && \
  chown -R www-data:www-data /var/lib/nginx

#10,修改escheduler配置文件
#后端配置
RUN mkdir -p /opt/escheduler && \
    tar -zxvf /opt/easyscheduler_source/target/escheduler-${tar_version}.tar.gz -C /opt/escheduler && \
    rm -rf /opt/escheduler/conf
ADD ./conf/escheduler/conf /opt/escheduler/conf
#前端nginx配置
ADD ./conf/nginx/default.conf /etc/nginx/conf.d

#11,开放端口
EXPOSE 2181 2888 3888 3306 80 12345 8888

#12,安装sudo,python,vim,ping和ssh
RUN apt-get update && \
  apt-get -y install sudo && \
  apt-get -y install python && \
  apt-get -y install vim && \
  apt-get -y install iputils-ping && \
  apt-get -y install net-tools && \
  apt-get -y install openssh-server && \
  apt-get -y install python-pip && \
  pip install kazoo

COPY ./startup.sh /root/startup.sh
#13,修改权限和设置软连
RUN chmod +x /root/startup.sh && \
  chmod +x /opt/escheduler/script/create-escheduler.sh && \
  chmod +x /opt/zookeeper/bin/zkServer.sh && \
  chmod +x /opt/escheduler/bin/escheduler-daemon.sh && \
  rm -rf /bin/sh && \
  ln -s /bin/bash /bin/sh && \
  mkdir -p /tmp/xls


ENTRYPOINT ["/root/startup.sh"]
