FROM ubuntu:18.04

ENV LANG=C.UTF-8

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

#4,install node
RUN cd /opt && \
    wget https://nodejs.org/download/release/v8.9.4/node-v8.9.4-linux-x64.tar.gz && \
    tar -zxvf node-v8.9.4-linux-x64.tar.gz && \
    mv node-v8.9.4-linux-x64 node && \
    rm -rf ./node-v8.9.4-*tar.gz
ENV NODE_HOME=/opt/node
ENV PATH $PATH:$NODE_HOME/bin

#5,add dolphinscheduler source code to /opt/dolphinscheduler_source
ADD . /opt/dolphinscheduler_source


#5,backend compilation
RUN cd /opt/dolphinscheduler_source && \
    mvn -U clean package assembly:assembly -Dmaven.test.skip=true

#6,frontend compilation
RUN chmod -R 777 /opt/dolphinscheduler_source/dolphinscheduler-ui && \
    cd /opt/dolphinscheduler_source/dolphinscheduler-ui && \
    rm -rf /opt/dolphinscheduler_source/dolphinscheduler-ui/node_modules && \
    npm install node-sass --unsafe-perm && \
    npm install && \
    npm run build
#7,install mysql
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


#8,install nginx
RUN apt-get update && \
  apt-get install -y nginx && \
  rm -rf /var/lib/apt/lists/* && \
  echo "\ndaemon off;" >> /etc/nginx/nginx.conf && \
  chown -R www-data:www-data /var/lib/nginx

#9,modify dolphinscheduler configuration file
#backend configuration
RUN mkdir -p /opt/dolphinscheduler && \
    tar -zxvf /opt/dolphinscheduler_source/target/dolphinscheduler-${tar_version}.tar.gz -C /opt/dolphinscheduler && \
    rm -rf /opt/dolphinscheduler/conf
ADD ./dockerfile/conf/dolphinscheduler/conf /opt/dolphinscheduler/conf
#frontend nginx configuration
ADD ./dockerfile/conf/nginx/dolphinscheduler.conf /etc/nginx/conf.d

#10,open port
EXPOSE 2181 2888 3888 3306 80 12345 8888

#11,install sudo,python,vim,ping and ssh command
RUN apt-get update && \
  apt-get -y install sudo && \
  apt-get -y install python && \
  apt-get -y install vim && \
  apt-get -y install iputils-ping && \
  apt-get -y install net-tools && \
  apt-get -y install openssh-server && \
  apt-get -y install python-pip && \
  pip install kazoo

COPY ./dockerfile/startup.sh /root/startup.sh
#12,modify permissions and set soft links
RUN chmod +x /root/startup.sh && \
  chmod +x /opt/dolphinscheduler/script/create-dolphinscheduler.sh && \
  chmod +x /opt/zookeeper/bin/zkServer.sh && \
  chmod +x /opt/dolphinscheduler/bin/dolphinscheduler-daemon.sh && \
  rm -rf /bin/sh && \
  ln -s /bin/bash /bin/sh && \
  mkdir -p /tmp/xls


ENTRYPOINT ["/root/startup.sh"]
