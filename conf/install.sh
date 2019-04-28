#!/bin/sh

workDir=`/opt/easyscheduler`
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
    echo "Easy Scheduler not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "msys" ]]; then
    # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
    echo "Easy Scheduler not support Windows operating system"
    exit 1
elif [[ "$OSTYPE" == "win32" ]]; then
    echo "Easy Scheduler not support Windows operating system"
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

# mysql配置
# mysql 地址,端口
mysqlHost="127.0.0.1:3306"

# mysql 数据库名称
mysqlDb="easyscheduler"

# mysql 用户名
mysqlUserName="easyscheduler"

# mysql 密码
mysqlPassword="easyschedulereasyscheduler"

# conf/config/install_config.conf配置
# 安装路径,不要当前路径(pwd)一样
installPath="/opt/easyscheduler"

# 部署用户
deployUser="escheduler"

# zk集群
zkQuorum="192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181"

# 安装hosts
ips="ark0,ark1,ark2,ark3,ark4"

# conf/config/run_config.conf配置
# 运行Master的机器
masters="ark0,ark1"

# 运行Worker的机器
workers="ark2,ark3,ark4"

# 运行Alert的机器
alertServer="ark3"

# 运行Api的机器
apiServers="ark1"

# alert配置
# 邮件协议
mailProtocol="SMTP"

# 邮件服务host
mailServerHost="smtp.exmail.qq.com"

# 邮件服务端口
mailServerPort="25"

# 发送人
mailSender="xxxxxxxxxx"

# 发送人密码
mailPassword="xxxxxxxxxx"

# 下载Excel路径
xlsFilePath="/tmp/xls"


# hadoop 配置
# 是否启动hdfs,如果启动则为true,需要配置以下hadoop相关参数;
# 不启动设置为false,如果为false,以下配置不需要修改
hdfsStartupSate="false"

# namenode地址,支持HA,需要将core-site.xml和hdfs-site.xml放到conf目录下
namenodeFs="hdfs://mycluster:8020"

# resourcemanager HA配置，如果是单resourcemanager,这里为空即可
yarnHaIps="192.168.xx.xx,192.168.xx.xx"

# 如果是单 resourcemanager,只需要配置一个主机名称,如果是resourcemanager HA,则默认配置就好
singleYarnIp="ark1"

# hdfs根路径,根路径的owner必须是部署用户
hdfsPath="/escheduler"

# common 配置
# 程序路径
programPath="/tmp/escheduler"

#下载路径
downloadPath="/tmp/escheduler/download"

# 任务执行路径
execPath="/tmp/escheduler/exec"

# SHELL环境变量路径
shellEnvPath="$installPath/conf/env/.escheduler_env.sh"

# Python换将变量路径
pythonEnvPath="$installPath/conf/env/escheduler_env.py"

# 资源文件的后缀
resSuffixs="txt,log,sh,conf,cfg,py,java,sql,hql,xml"

# 开发状态,如果是true,对于SHELL脚本可以在execPath目录下查看封装后的SHELL脚本,如果是false则执行完成直接删除
devState="true"

# zk 配置
# zk根目录
zkRoot="/escheduler"

# 用来记录挂掉机器的zk目录
zkDeadServers="/escheduler/dead-servers"

# masters目录
zkMasters="/escheduler/masters"

# workers目录
zkWorkers="/escheduler/workers"

# zk master分布式锁
mastersLock="/escheduler/lock/masters"

# zk worker分布式锁
workersLock="/escheduler/lock/workers"

# zk master容错分布式锁
mastersFailover="/escheduler/lock/failover/masters"

# zk worker容错分布式锁
workersFailover="/escheduler/lock/failover/masters"

# zk session 超时
zkSessionTimeout="300"

# zk 连接超时
zkConnectionTimeout="300"

# zk 重试间隔
zkRetrySleep="100"

# zk重试最大次数
zkRetryMaxtime="5"


# master 配置
# master执行线程最大数,流程实例的最大并行度
masterExecThreads="100"

# master任务执行线程最大数,每一个流程实例的最大并行度
masterExecTaskNum="20"

# master心跳间隔
masterHeartbeatInterval="10"

# master任务提交重试次数
masterTaskCommitRetryTimes="5"

# master任务提交重试时间间隔
masterTaskCommitInterval="100"

# master最大cpu平均负载,用来判断master是否还有执行能力
masterMaxCupLoadAvg="10"

# master预留内存,用来判断master是否还有执行能力
masterReservedMemory="1"


# worker 配置
# worker执行线程
workerExecThreads="100"

# worker心跳间隔
workerHeartbeatInterval="10"

# worker一次抓取任务数
workerFetchTaskNum="10"

# worker最大cpu平均负载,用来判断master是否还有执行能力
workerMaxCupLoadAvg="10"

# worker预留内存,用来判断master是否还有执行能力
workerReservedMemory="1"

# api 配置
# api 服务端口
apiServerPort="12345"

# api session 超时
apiServerSessionTimeout="7200"

# api 上下文路径
apiServerContextPath="/escheduler/"

# spring 最大文件大小
springMaxFileSize="1024MB"

# spring 最大请求文件大小
springMaxRequestSize="1024MB"

# api 最大post请求大小
apiMaxHttpPostSize="5000000"

# 1,替换文件
echo "1,替换文件"
sed -i ${txt} "s#spring.datasource.url.*#spring.datasource.url=jdbc:mysql://${mysqlHost}/${mysqlDb}?characterEncoding=UTF-8#g" conf/dao/data_source.properties
sed -i ${txt} "s#spring.datasource.username.*#spring.datasource.username=${mysqlUserName}#g" conf/dao/data_source.properties
sed -i ${txt} "s#spring.datasource.password.*#spring.datasource.password=${mysqlPassword}#g" conf/dao/data_source.properties

sed -i ${txt} "s#org.quartz.dataSource.myDs.URL.*#org.quartz.dataSource.myDs.URL=jdbc:mysql://${mysqlHost}/${mysqlDb}?characterEncoding=UTF-8#g" conf/quartz.properties
sed -i ${txt} "s#org.quartz.dataSource.myDs.user.*#org.quartz.dataSource.myDs.user=${mysqlUserName}#g" conf/quartz.properties
sed -i ${txt} "s#org.quartz.dataSource.myDs.password.*#org.quartz.dataSource.myDs.password=${mysqlPassword}#g" conf/quartz.properties


sed -i ${txt} "s#fs.defaultFS.*#fs.defaultFS=${namenodeFs}#g" conf/common/hadoop/hadoop.properties
sed -i ${txt} "s#yarn.resourcemanager.ha.rm.ids.*#yarn.resourcemanager.ha.rm.ids=${yarnHaIps}#g" conf/common/hadoop/hadoop.properties
sed -i ${txt} "s#yarn.application.status.address.*#yarn.application.status.address=http://${singleYarnIp}:8088/ws/v1/cluster/apps/%s#g" conf/common/hadoop/hadoop.properties

sed -i ${txt} "s#data.basedir.path.*#data.basedir.path=${programPath}#g" conf/common/common.properties
sed -i ${txt} "s#data.download.basedir.path.*#data.download.basedir.path=${downloadPath}#g" conf/common/common.properties
sed -i ${txt} "s#process.exec.basepath.*#process.exec.basepath=${execPath}#g" conf/common/common.properties
sed -i ${txt} "s#data.store2hdfs.basepath.*#data.store2hdfs.basepath=${hdfsPath}#g" conf/common/common.properties
sed -i ${txt} "s#hdfs.startup.state.*#hdfs.startup.state=${hdfsStartupSate}#g" conf/common/common.properties
sed -i ${txt} "s#escheduler.env.path.*#escheduler.env.path=${shellEnvPath}#g" conf/common/common.properties
sed -i ${txt} "s#escheduler.env.py.*#escheduler.env.py=${pythonEnvPath}#g" conf/common/common.properties
sed -i ${txt} "s#resource.view.suffixs.*#resource.view.suffixs=${resSuffixs}#g" conf/common/common.properties
sed -i ${txt} "s#development.state.*#development.state=${devState}#g" conf/common/common.properties

sed -i ${txt} "s#zookeeper.quorum.*#zookeeper.quorum=${zkQuorum}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.root.*#zookeeper.escheduler.root=${zkRoot}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.dead.servers.*#zookeeper.escheduler.dead.servers=${zkDeadServers}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.masters.*#zookeeper.escheduler.masters=${zkMasters}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.workers.*#zookeeper.escheduler.workers=${zkWorkers}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.masters.*#zookeeper.escheduler.lock.masters=${mastersLock}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.workers.*#zookeeper.escheduler.lock.workers=${workersLock}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.failover.masters.*#zookeeper.escheduler.lock.failover.masters=${mastersFailover}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.escheduler.lock.failover.workers.*#zookeeper.escheduler.lock.failover.workers=${workersFailover}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.session.timeout.*#zookeeper.session.timeout=${zkSessionTimeout}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.connection.timeout.*#zookeeper.connection.timeout=${zkConnectionTimeout}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.retry.sleep.*#zookeeper.retry.sleep=${zkRetrySleep}#g" conf/zookeeper.properties
sed -i ${txt} "s#zookeeper.retry.maxtime.*#zookeeper.retry.maxtime=${zkRetryMaxtime}#g" conf/zookeeper.properties

sed -i ${txt} "s#master.exec.threads.*#master.exec.threads=${masterExecThreads}#g" conf/master.properties
sed -i ${txt} "s#master.exec.task.number.*#master.exec.task.number=${masterExecTaskNum}#g" conf/master.properties
sed -i ${txt} "s#master.heartbeat.interval.*#master.heartbeat.interval=${masterHeartbeatInterval}#g" conf/master.properties
sed -i ${txt} "s#master.task.commit.retryTimes.*#master.task.commit.retryTimes=${masterTaskCommitRetryTimes}#g" conf/master.properties
sed -i ${txt} "s#master.task.commit.interval.*#master.task.commit.interval=${masterTaskCommitInterval}#g" conf/master.properties
sed -i ${txt} "s#master.max.cpuload.avg.*#master.max.cpuload.avg=${masterMaxCupLoadAvg}#g" conf/master.properties
sed -i ${txt} "s#master.reserved.memory.*#master.reserved.memory=${masterReservedMemory}#g" conf/master.properties


sed -i ${txt} "s#worker.exec.threads.*#worker.exec.threads=${workerExecThreads}#g" conf/worker.properties
sed -i ${txt} "s#worker.heartbeat.interval.*#worker.heartbeat.interval=${workerHeartbeatInterval}#g" conf/worker.properties
sed -i ${txt} "s#worker.fetch.task.num.*#worker.fetch.task.num=${workerFetchTaskNum}#g" conf/worker.properties
sed -i ${txt} "s#worker.max.cpuload.avg.*#worker.max.cpuload.avg=${workerMaxCupLoadAvg}#g" conf/worker.properties
sed -i ${txt} "s#worker.reserved.memory.*#worker.reserved.memory=${workerReservedMemory}#g" conf/worker.properties


sed -i ${txt} "s#server.port.*#server.port=${apiServerPort}#g" conf/application.properties
sed -i ${txt} "s#server.session.timeout.*#server.session.timeout=${apiServerSessionTimeout}#g" conf/application.properties
sed -i ${txt} "s#server.context-path.*#server.context-path=${apiServerContextPath}#g" conf/application.properties
sed -i ${txt} "s#spring.http.multipart.max-file-size.*#spring.http.multipart.max-file-size=${springMaxFileSize}#g" conf/application.properties
sed -i ${txt} "s#spring.http.multipart.max-request-size.*#spring.http.multipart.max-request-size=${springMaxRequestSize}#g" conf/application.properties
sed -i ${txt} "s#server.max-http-post-size.*#server.max-http-post-size=${apiMaxHttpPostSize}#g" conf/application.properties


sed -i ${txt} "s#mail.protocol.*#mail.protocol=${mailProtocol}#g" conf/alert.properties
sed -i ${txt} "s#mail.server.host.*#mail.server.host=${mailServerHost}#g" conf/alert.properties
sed -i ${txt} "s#mail.server.port.*#mail.server.port=${mailServerPort}#g" conf/alert.properties
sed -i ${txt} "s#mail.sender.*#mail.sender=${mailSender}#g" conf/alert.properties
sed -i ${txt} "s#mail.passwd.*#mail.passwd=${mailPassword}#g" conf/alert.properties
sed -i ${txt} "s#xls.file.path.*#xls.file.path=${xlsFilePath}#g" conf/alert.properties


sed -i ${txt} "s#installPath.*#installPath=${installPath}#g" conf/config/install_config.conf
sed -i ${txt} "s#deployUser.*#deployUser=${deployUser}#g" conf/config/install_config.conf
sed -i ${txt} "s#ips.*#ips=${ips}#g" conf/config/install_config.conf


sed -i ${txt} "s#masters.*#masters=${masters}#g" conf/config/run_config.conf
sed -i ${txt} "s#workers.*#workers=${workers}#g" conf/config/run_config.conf
sed -i ${txt} "s#alertServer.*#alertServer=${alertServer}#g" conf/config/run_config.conf
sed -i ${txt} "s#apiServers.*#apiServers=${apiServers}#g" conf/config/run_config.conf
