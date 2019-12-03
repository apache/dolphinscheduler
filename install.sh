#!/bin/sh

workDir=`dirname $0`
workDir=`cd ${workDir};pwd`

#To be compatible with MacOS and Linux
txt=""
if [[ "$OSTYPE" == "darwin"* ]]; then
    # Mac OSX
    txt="''"
elif [[ "$OSTYPE" == "linux-gnu" ]]; then
    # linux
    txt=""
elif [[ "$OSTYPE" == "cygwin" ]]; then
    # POSIX compatibility layer and Linux environment emulation for Windows
    echo "Dolphin Scheduler not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    echo "Dolphin Scheduler not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "win32" ]]; then
    echo "Dolphin Scheduler not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "freebsd"* ]]; then
    # ...
    txt=""
else
    # Unknown.
    echo "Operating system unknown, please tell us(submit issue) for better service"
    exit 1
fi

source ${workDir}/conf/config/run_config.conf
source ${workDir}/conf/config/install_config.conf

# mysql config
# mysql address and port
mysqlHost="192.168.xx.xx:3306"

# mysql database
mysqlDb="escheduler"

# mysql username
mysqlUserName="xx"

# mysql passwprd
# Note: if there are special characters, please use the \ transfer character to transfer
mysqlPassword="xx"

# conf/config/install_config.conf config
# Note: the installation path is not the same as the current path (pwd)
installPath="/data1_1T/escheduler"

# deployment user
# Note: the deployment user needs to have sudo privileges and permissions to operate hdfs. If hdfs is enabled, the root directory needs to be created by itself
deployUser="escheduler"

# zk cluster
zkQuorum="192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181"

# install hosts
# Note: install the scheduled hostname list. If it is pseudo-distributed, just write a pseudo-distributed hostname
ips="ark0,ark1,ark2,ark3,ark4"

# conf/config/run_config.conf config
# run master machine
# Note: list of hosts hostname for deploying master
masters="ark0,ark1"

# run worker machine
# note: list of machine hostnames for deploying workers
workers="ark2,ark3,ark4"

# run alert machine
# note: list of machine hostnames for deploying alert server
alertServer="ark3"

# run api machine
# note: list of machine hostnames for deploying api server
apiServers="ark1"

# alert config
# mail protocol
mailProtocol="SMTP"

# mail server host
mailServerHost="smtp.exmail.qq.com"

# mail server port
mailServerPort="25"

# sender
mailSender="xxxxxxxxxx"

# sender password
mailPassword="xxxxxxxxxx"

# TLS mail protocol support
starttlsEnable="false"

# SSL mail protocol support
# note: The SSL protocol is enabled by default. 
# only one of TLS and SSL can be in the true state.
sslEnable="true"

# download excel path
xlsFilePath="/tmp/xls"

# Enterprise WeChat Enterprise ID Configuration
enterpriseWechatCorpId="xxxxxxxxxx"

# Enterprise WeChat application Secret configuration
enterpriseWechatSecret="xxxxxxxxxx"

# Enterprise WeChat Application AgentId Configuration
enterpriseWechatAgentId="xxxxxxxxxx"

# Enterprise WeChat user configuration, multiple users to , split
enterpriseWechatUsers="xxxxx,xxxxx"


# whether to start monitoring self-starting scripts
monitorServerState="false"

# resource Center upload and select storage method：HDFS,S3,NONE
resUploadStartupType="NONE"

# if resUploadStartupType is HDFS，defaultFS write namenode address，HA you need to put core-site.xml and hdfs-site.xml in the conf directory.
# if S3，write S3 address，HA，for example ：s3a://escheduler，
# Note，s3 be sure to create the root directory /escheduler
defaultFS="hdfs://mycluster:8020"

# if S3 is configured, the following configuration is required.
s3Endpoint="http://192.168.xx.xx:9010"
s3AccessKey="xxxxxxxxxx"
s3SecretKey="xxxxxxxxxx"

# resourcemanager HA configuration, if it is a single resourcemanager, here is yarnHaIps=""
yarnHaIps="192.168.xx.xx,192.168.xx.xx"

# if it is a single resourcemanager, you only need to configure one host name. If it is resourcemanager HA, the default configuration is fine.
singleYarnIp="ark1"

# hdfs root path, the owner of the root path must be the deployment user. 
# versions prior to 1.1.0 do not automatically create the hdfs root directory, you need to create it yourself.
hdfsPath="/escheduler"

# have users who create directory permissions under hdfs root path /
# Note: if kerberos is enabled, hdfsRootUser="" can be used directly.
hdfsRootUser="hdfs"

# common config
# Program root path
programPath="/tmp/escheduler"

# download path
downloadPath="/tmp/escheduler/download"

# task execute path
execPath="/tmp/escheduler/exec"

# SHELL environmental variable path
shellEnvPath="$installPath/conf/env/.escheduler_env.sh"

# suffix of the resource file
resSuffixs="txt,log,sh,conf,cfg,py,java,sql,hql,xml,properties"

# development status, if true, for the SHELL script, you can view the encapsulated SHELL script in the execPath directory. 
# If it is false, execute the direct delete
devState="true"

# kerberos config
# kerberos whether to start
kerberosStartUp="false"

# kdc krb5 config file path
krb5ConfPath="$installPath/conf/krb5.conf"

# keytab username
keytabUserName="hdfs-mycluster@ESZ.COM"

# username keytab path
keytabPath="$installPath/conf/hdfs.headless.keytab"

# zk config
# zk root directory
zkRoot="/escheduler"

# used to record the zk directory of the hanging machine
zkDeadServers="/escheduler/dead-servers"

# masters directory
zkMasters="$zkRoot/masters"

# workers directory
zkWorkers="$zkRoot/workers"

# zk master distributed lock
mastersLock="$zkRoot/lock/masters"

# zk worker distributed lock
workersLock="$zkRoot/lock/workers"

# zk master fault-tolerant distributed lock
mastersFailover="$zkRoot/lock/failover/masters"

# zk worker fault-tolerant distributed lock
workersFailover="$zkRoot/lock/failover/workers"

# zk master start fault tolerant distributed lock
mastersStartupFailover="$zkRoot/lock/failover/startup-masters"

# zk session timeout
zkSessionTimeout="300"

# zk connection timeout
zkConnectionTimeout="300"

# zk retry interval
zkRetrySleep="100"

# zk retry maximum number of times
zkRetryMaxtime="5"


# master config 
# master execution thread maximum number, maximum parallelism of process instance
masterExecThreads="100"

# the maximum number of master task execution threads, the maximum degree of parallelism for each process instance
masterExecTaskNum="20"

# master heartbeat interval
masterHeartbeatInterval="10"

# master task submission retries
masterTaskCommitRetryTimes="5"

# master task submission retry interval
masterTaskCommitInterval="100"

# master maximum cpu average load, used to determine whether the master has execution capability
masterMaxCpuLoadAvg="10"

# master reserve memory to determine if the master has execution capability
masterReservedMemory="1"


# worker config 
# worker execution thread
workerExecThreads="100"

# worker heartbeat interval
workerHeartbeatInterval="10"

# worker number of fetch tasks
workerFetchTaskNum="3"

# workerThe maximum cpu average load, used to determine whether the worker still has the ability to execute, 
# keep the system default, the default is twice the number of cpu cores, when the load reaches 2 times
#workerMaxCupLoadAvg="10"

# worker reserve memory to determine if the master has execution capability
workerReservedMemory="1"

# api config
# api server port
apiServerPort="12345"

# api session timeout
apiServerSessionTimeout="7200"

# api server context path
apiServerContextPath="/escheduler/"

# spring max file size
springMaxFileSize="1024MB"

# spring max request size
springMaxRequestSize="1024MB"

# api max http post size
apiMaxHttpPostSize="5000000"

# 1,replace file
echo "1,replace file"
sed -i ${txt} "s#spring.datasource.url.*#spring.datasource.url=jdbc:mysql://${mysqlHost}/${mysqlDb}?characterEncoding=UTF-8#g" conf/dao/data_source.properties
sed -i ${txt} "s#spring.datasource.username.*#spring.datasource.username=${mysqlUserName}#g" conf/dao/data_source.properties
sed -i ${txt} "s#spring.datasource.password.*#spring.datasource.password=${mysqlPassword}#g" conf/dao/data_source.properties

sed -i ${txt} "s#org.quartz.dataSource.myDs.URL.*#org.quartz.dataSource.myDs.URL=jdbc:mysql://${mysqlHost}/${mysqlDb}?characterEncoding=UTF-8#g" conf/quartz.properties
sed -i ${txt} "s#org.quartz.dataSource.myDs.user.*#org.quartz.dataSource.myDs.user=${mysqlUserName}#g" conf/quartz.properties
sed -i ${txt} "s#org.quartz.dataSource.myDs.password.*#org.quartz.dataSource.myDs.password=${mysqlPassword}#g" conf/quartz.properties


sed -i ${txt} "s#fs.defaultFS.*#fs.defaultFS=${defaultFS}#g" conf/common/hadoop/hadoop.properties
sed -i ${txt} "s#fs.s3a.endpoint.*#fs.s3a.endpoint=${s3Endpoint}#g" conf/common/hadoop/hadoop.properties
sed -i ${txt} "s#fs.s3a.access.key.*#fs.s3a.access.key=${s3AccessKey}#g" conf/common/hadoop/hadoop.properties
sed -i ${txt} "s#fs.s3a.secret.key.*#fs.s3a.secret.key=${s3SecretKey}#g" conf/common/hadoop/hadoop.properties
sed -i ${txt} "s#yarn.resourcemanager.ha.rm.ids.*#yarn.resourcemanager.ha.rm.ids=${yarnHaIps}#g" conf/common/hadoop/hadoop.properties
sed -i ${txt} "s#yarn.application.status.address.*#yarn.application.status.address=http://${singleYarnIp}:8088/ws/v1/cluster/apps/%s#g" conf/common/hadoop/hadoop.properties


sed -i ${txt} "s#data.basedir.path.*#data.basedir.path=${programPath}#g" conf/common/common.properties
sed -i ${txt} "s#data.download.basedir.path.*#data.download.basedir.path=${downloadPath}#g" conf/common/common.properties
sed -i ${txt} "s#process.exec.basepath.*#process.exec.basepath=${execPath}#g" conf/common/common.properties
sed -i ${txt} "s#hdfs.root.user.*#hdfs.root.user=${hdfsRootUser}#g" conf/common/common.properties
sed -i ${txt} "s#data.store2hdfs.basepath.*#data.store2hdfs.basepath=${hdfsPath}#g" conf/common/common.properties
sed -i ${txt} "s#res.upload.startup.type.*#res.upload.startup.type=${resUploadStartupType}#g" conf/common/common.properties
sed -i ${txt} "s#escheduler.env.path.*#escheduler.env.path=${shellEnvPath}#g" conf/common/common.properties
sed -i ${txt} "s#resource.view.suffixs.*#resource.view.suffixs=${resSuffixs}#g" conf/common/common.properties
sed -i ${txt} "s#development.state.*#development.state=${devState}#g" conf/common/common.properties
sed -i ${txt} "s#hadoop.security.authentication.startup.state.*#hadoop.security.authentication.startup.state=${kerberosStartUp}#g" conf/common/common.properties
sed -i ${txt} "s#java.security.krb5.conf.path.*#java.security.krb5.conf.path=${krb5ConfPath}#g" conf/common/common.properties
sed -i ${txt} "s#login.user.keytab.username.*#login.user.keytab.username=${keytabUserName}#g" conf/common/common.properties
sed -i ${txt} "s#login.user.keytab.path.*#login.user.keytab.path=${keytabPath}#g" conf/common/common.properties

sed -i ${txt} "s#zookeeper.quorum.*#zookeeper.quorum=${zkQuorum}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.root.*#zookeeper.escheduler.root=${zkRoot}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.dead.servers.*#zookeeper.escheduler.dead.servers=${zkDeadServers}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.masters.*#zookeeper.escheduler.masters=${zkMasters}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.workers.*#zookeeper.escheduler.workers=${zkWorkers}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.masters.*#zookeeper.escheduler.lock.masters=${mastersLock}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.workers.*#zookeeper.escheduler.lock.workers=${workersLock}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.failover.masters.*#zookeeper.escheduler.lock.failover.masters=${mastersFailover}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.failover.workers.*#zookeeper.escheduler.lock.failover.workers=${workersFailover}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.failover.startup.masters.*#zookeeper.escheduler.lock.failover.startup.masters=${mastersStartupFailover}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.session.timeout.*#zookeeper.session.timeout=${zkSessionTimeout}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.connection.timeout.*#zookeeper.connection.timeout=${zkConnectionTimeout}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.retry.sleep.*#zookeeper.retry.sleep=${zkRetrySleep}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.retry.maxtime.*#zookeeper.retry.maxtime=${zkRetryMaxtime}#g" conf/zookeeper.properties

sed -i ${txt} "s#master.exec.threads.*#master.exec.threads=${masterExecThreads}#g" conf/master.properties
sed -i ${txt} "s#master.exec.task.number.*#master.exec.task.number=${masterExecTaskNum}#g" conf/master.properties
sed -i ${txt} "s#master.heartbeat.interval.*#master.heartbeat.interval=${masterHeartbeatInterval}#g" conf/master.properties
sed -i ${txt} "s#master.task.commit.retryTimes.*#master.task.commit.retryTimes=${masterTaskCommitRetryTimes}#g" conf/master.properties
sed -i ${txt} "s#master.task.commit.interval.*#master.task.commit.interval=${masterTaskCommitInterval}#g" conf/master.properties
#sed -i ${txt} "s#master.max.cpuload.avg.*#master.max.cpuload.avg=${masterMaxCpuLoadAvg}#g" conf/master.properties
sed -i ${txt} "s#master.reserved.memory.*#master.reserved.memory=${masterReservedMemory}#g" conf/master.properties


sed -i ${txt} "s#worker.exec.threads.*#worker.exec.threads=${workerExecThreads}#g" conf/worker.properties
sed -i ${txt} "s#worker.heartbeat.interval.*#worker.heartbeat.interval=${workerHeartbeatInterval}#g" conf/worker.properties
sed -i ${txt} "s#worker.fetch.task.num.*#worker.fetch.task.num=${workerFetchTaskNum}#g" conf/worker.properties
#sed -i ${txt} "s#worker.max.cpuload.avg.*#worker.max.cpuload.avg=${workerMaxCupLoadAvg}#g" conf/worker.properties
sed -i ${txt} "s#worker.reserved.memory.*#worker.reserved.memory=${workerReservedMemory}#g" conf/worker.properties


sed -i ${txt} "s#server.port.*#server.port=${apiServerPort}#g" conf/application.properties
sed -i ${txt} "s#server.servlet.session.timeout.*#server.servlet.session.timeout=${apiServerSessionTimeout}#g" conf/application.properties
sed -i ${txt} "s#server.servlet.context-path.*#server.servlet.context-path=${apiServerContextPath}#g" conf/application.properties
sed -i ${txt} "s#spring.servlet.multipart.max-file-size.*#spring.servlet.multipart.max-file-size=${springMaxFileSize}#g" conf/application.properties
sed -i ${txt} "s#spring.servlet.multipart.max-request-size.*#spring.servlet.multipart.max-request-size=${springMaxRequestSize}#g" conf/application.properties
sed -i ${txt} "s#server.jetty.max-http-post-size.*#server.jetty.max-http-post-size=${apiMaxHttpPostSize}#g" conf/application.properties


sed -i ${txt} "s#mail.protocol.*#mail.protocol=${mailProtocol}#g" conf/alert.properties
sed -i ${txt} "s#mail.server.host.*#mail.server.host=${mailServerHost}#g" conf/alert.properties
sed -i ${txt} "s#mail.server.port.*#mail.server.port=${mailServerPort}#g" conf/alert.properties
sed -i ${txt} "s#mail.sender.*#mail.sender=${mailSender}#g" conf/alert.properties
sed -i ${txt} "s#mail.passwd.*#mail.passwd=${mailPassword}#g" conf/alert.properties
sed -i ${txt} "s#mail.smtp.starttls.enable.*#mail.smtp.starttls.enable=${starttlsEnable}#g" conf/alert.properties
sed -i ${txt} "s#mail.smtp.ssl.enable.*#mail.smtp.ssl.enable=${sslEnable}#g" conf/alert.properties
sed -i ${txt} "s#xls.file.path.*#xls.file.path=${xlsFilePath}#g" conf/alert.properties
sed -i ${txt} "s#enterprise.wechat.corp.id.*#enterprise.wechat.corp.id=${enterpriseWechatCorpId}#g" conf/alert.properties
sed -i ${txt} "s#enterprise.wechat.secret.*#enterprise.wechat.secret=${enterpriseWechatSecret}#g" conf/alert.properties
sed -i ${txt} "s#enterprise.wechat.agent.id.*#enterprise.wechat.agent.id=${enterpriseWechatAgentId}#g" conf/alert.properties
sed -i ${txt} "s#enterprise.wechat.users.*#enterprise.wechat.users=${enterpriseWechatUsers}#g" conf/alert.properties


sed -i ${txt} "s#installPath.*#installPath=${installPath}#g" conf/config/install_config.conf
sed -i ${txt} "s#deployUser.*#deployUser=${deployUser}#g" conf/config/install_config.conf
sed -i ${txt} "s#ips.*#ips=${ips}#g" conf/config/install_config.conf


sed -i ${txt} "s#masters.*#masters=${masters}#g" conf/config/run_config.conf
sed -i ${txt} "s#workers.*#workers=${workers}#g" conf/config/run_config.conf
sed -i ${txt} "s#alertServer.*#alertServer=${alertServer}#g" conf/config/run_config.conf
sed -i ${txt} "s#apiServers.*#apiServers=${apiServers}#g" conf/config/run_config.conf


# 2,create directory
echo "2,create directory"

if [ ! -d $installPath ];then
  sudo mkdir -p $installPath
  sudo chown -R $deployUser:$deployUser $installPath
fi

hostsArr=(${ips//,/ })
for host in ${hostsArr[@]}
do

# create if programPath does not exist
if ! ssh $host test -e $programPath; then
  ssh $host "sudo mkdir -p $programPath;sudo chown -R $deployUser:$deployUser $programPath"
fi

# create if downloadPath does not exist
if ! ssh $host test -e $downloadPath; then
  ssh $host "sudo mkdir -p $downloadPath;sudo chown -R $deployUser:$deployUser $downloadPath"
fi

# create if execPath does not exist
if ! ssh $host test -e $execPath; then
  ssh $host "sudo mkdir -p $execPath; sudo chown -R $deployUser:$deployUser $execPath"
fi

# create if xlsFilePath does not exist
if ! ssh $host test -e $xlsFilePath; then
  ssh $host "sudo mkdir -p $xlsFilePath; sudo chown -R $deployUser:$deployUser $xlsFilePath"
fi

done


# 3,stop server
echo "3,stop server"
sh ${workDir}/script/stop-all.sh

# 4,delete zk node
echo "4,delete zk node"
sleep 1
python ${workDir}/script/del-zk-node.py $zkQuorum $zkRoot

# 5,scp resources
echo "5,scp resources"
sh ${workDir}/script/scp-hosts.sh
if [ $? -eq 0 ]
then
	echo 'scp copy completed'
else
	echo 'sc copy failed to exit'
	exit -1
fi

# 6,startup
echo "6,startup"
sh ${workDir}/script/start-all.sh

# 7,start monitoring self-starting script
monitor_pid=${workDir}/monitor_server.pid
if [ "true" = $monitorServerState ];then
        if [ -f $monitor_pid ]; then
                TARGET_PID=`cat $monitor_pid`
                if kill -0 $TARGET_PID > /dev/null 2>&1; then
                        echo "monitor server running as process ${TARGET_PID}.Stopping"
                        kill $TARGET_PID
                        sleep 5
                        if kill -0 $TARGET_PID > /dev/null 2>&1; then
                                echo "monitor server did not stop gracefully after 5 seconds: killing with kill -9"
                                kill -9 $TARGET_PID
                        fi
                else
                        echo "no monitor server to stop"
                fi
                echo "monitor server running as process ${TARGET_PID}.Stopped success"
                rm -f $monitor_pid
        fi
        nohup python -u ${workDir}/script/monitor-server.py $installPath $zkQuorum $zkMasters $zkWorkers > ${workDir}/monitor-server.log 2>&1 &
        echo $! > $monitor_pid
        echo "start monitor server success as process `cat $monitor_pid`"

fi