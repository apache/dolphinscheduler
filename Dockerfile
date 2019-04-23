#Maintin by jimmy
#Email: zhengge2012@gmail.com
FROM anapsix/alpine-java:8_jdk
WORKDIR /tmp
RUN wget http://archive.apache.org/dist/maven/maven-3/3.6.1/binaries/apache-maven-3.6.1-bin.tar.gz
RUN tar -zxvf apache-maven-3.6.1-bin.tar.gz && rm apache-maven-3.6.1-bin.tar.gz 
RUN mv apache-maven-3.6.1 /usr/lib/mvn
RUN chown -R root:root /usr/lib/mvn
RUN ln -s /usr/lib/mvn/bin/mvn /usr/bin/mvn
RUN wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
RUN tar -zxvf zookeeper-3.4.6.tar.gz
RUN mv zookeeper-3.4.6 /opt/zookeeper
RUN rm -rf zookeeper-3.4.6.tar.gz
RUN echo "export ZOOKEEPER_HOME=/opt/zookeeper" >>/etc/profile
RUN echo "export PATH=$PATH:$ZOOKEEPER_HOME/bin"  >>/etc/profile
ADD conf/zoo.cfg /opt/zookeeper/conf/zoo.cfg
#RUN source /etc/profile
#RUN zkServer.sh start
RUN apk add --no-cache git npm nginx mariadb mariadb-client mariadb-server-utils pwgen
WORKDIR /opt
RUN git clone https://github.com/analysys/EasyScheduler.git
WORKDIR /opt/EasyScheduler
RUN mvn -U clean package assembly:assembly -Dmaven.test.skip=true
RUN mv /opt/EasyScheduler/target/escheduler-1.0.0-SNAPSHOT /opt/easyscheduler
WORKDIR /opt/EasyScheduler/escheduler-ui
RUN npm install
RUN npm audit fix
RUN npm run build
RUN mkdir -p /opt/escheduler/front/server
RUN cp -rfv dist/* /opt/escheduler/front/server
WORKDIR /
RUN rm -rf /opt/EasyScheduler
#configure mysql server https://github.com/yobasystems/alpine-mariadb/tree/master/alpine-mariadb-amd64
ADD conf/run.sh /scripts/run.sh
RUN mkdir /docker-entrypoint-initdb.d && \
    mkdir /scripts/pre-exec.d && \
    mkdir /scripts/pre-init.d && \
    chmod -R 755 /scripts
RUN rm -rf /var/cache/apk/*
EXPOSE 8888
ENTRYPOINT ["/scripts/run.sh"]
