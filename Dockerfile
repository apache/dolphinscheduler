FROM anapsix/alpine-java:8_jdk
RUN cd /tmp 
RUN wget http://archive.apache.org/dist/maven/maven-3/3.6.1/binaries/apache-maven-3.6.1-bin.tar.gz
RUN tar -zxvf apache-maven-3.6.1-bin.tar.gz && rm apache-maven-3.6.1-bin.tar.gz && mv apache-maven-3.6.1 /usr/lib/mvn
RUN chown -R root:root /usr/lib/mvn
RUN ln -s /usr/lib/mvn/bin/mvn /usr/bin/mvn
RUN wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.6/zookeeper-3.4.6.tar.gz
RUN tar -zxvf zookeeper-3.4.6.tar.gz
RUN mv zookeeper-3.4.6 zookeeper
RUN echo "export ZOOKEEPER_HOME=/opt/zookeeper" >>/etc/profile
RUN echo "export PATH=$PATH:$ZOOKEEPER_HOME/bin"  >>/etc/profile
ADD conf/zoo.cfg /opt/zookeeper/conf/zoo.cfg
RUN source /etc/profile
RUN zkServer.sh start
RUN apk add --no-cache git npm nginx
RUN cd /opt
RUN git clone https://github.com/analysys/EasyScheduler.git
RUN cd EasyScheduler
RUN mvn -U clean package assembly:assembly -Dmaven.test.skip=true
RUN mv /opt/EasyScheduler/target/escheduler-1.0.0-SNAPSHOT /opt/easyscheduler
RUN rm -rf /var/cache/apk/*
