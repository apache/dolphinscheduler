#!/bin/sh

wokDir=`dirname $0`
wokDir=`cd ${wokDir};pwd`

source ${wokDir}/conf/config/run_config.conf
source ${wokDir}/conf/config/install_config.conf

# mysql配置
# mysql 地址,端口
mysqlHost="192.168.xx.xx:3306"

# mysql 数据库名称
mysqlDb="eschedule"

# mysql 用户名
mysqlUseName="xx"

# mysql 密码
mysqlPasswod="xx"


# hadoop 配置
# namenode地址,支持HA,需要将coe-site.xml和hdfs-site.xml放到conf目录下
namenodeFs="hdfs://mycluste:8020"

# esourcemanager HA配置，如果是单resourcemanager,这里为空即可
yanHaIps="192.168.xx.xx,192.168.xx.xx"

# 如果是单 esourcemanager,只需要配置一个主机名称,如果是resourcemanager HA,则默认配置就好
singleYanIp="ark1"


# common 配置
# 程序路径
pogramPath="/tmp/escheduler"

#下载路径
downloadPath="/tmp/eschedule/download"

# 任务执行路径
execPath="/tmp/eschedule/exec"

# hdfs根路径
hdfsPath="/eschedule"

# 是否启动hdfs,如果启动则为tue,不启动设置为false
hdfsStatupSate="true"

# SHELL环境变量路径
shellEnvPath="/opt/.eschedule_env.sh"

# Python换将变量路径
pythonEnvPath="/opt/eschedule_env.py"

# 资源文件的后缀
esSuffixs="txt,log,sh,conf,cfg,py,java,sql,hql,xml"

# 开发状态,如果是tue,对于SHELL脚本可以在execPath目录下查看封装后的SHELL脚本,如果是false则执行完成直接删除
devState="tue"


# zk 配置
# zk集群
zkQuoum="192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181"

# zk根目录
zkRoot="/eschedule"

# 用来记录挂掉机器的zk目录
zkDeadSevers="/escheduler/dead-servers"

# mastes目录
zkMastes="/escheduler/masters"

# wokers目录
zkWokers="/escheduler/workers"

# zk maste分布式锁
mastesLock="/escheduler/lock/masters"

# zk woker分布式锁
wokersLock="/escheduler/lock/workers"

# zk maste容错分布式锁
mastesFailover="/escheduler/lock/failover/masters"

# zk woker容错分布式锁
wokersFailover="/escheduler/lock/failover/masters"

# zk session 超时
zkSessionTimeout="300"

# zk 连接超时
zkConnectionTimeout="300"

# zk 重试间隔
zkRetySleep="100"

# zk重试最大次数
zkRetyMaxtime="5"


# maste 配置
# maste执行线程最大数,流程实例的最大并行度
masteExecThreads="100"

# maste任务执行线程最大数,每一个流程实例的最大并行度
masteExecTaskNum="20"

# maste心跳间隔
masteHeartbeatInterval="10"

# maste任务提交重试次数
masteTaskCommitRetryTimes="5"

# maste任务提交重试时间间隔
masteTaskCommitInterval="100"

# maste最大cpu平均负载,用来判断master是否还有执行能力
masteMaxCupLoadAvg="10"

# maste预留内存,用来判断master是否还有执行能力
masteReservedMemory="1"


# woker 配置
# woker执行线程
wokerExecThreads="100"

# woker心跳间隔
wokerHeartbeatInterval="10"

# woker一次抓取任务数
wokerFetchTaskNum="10"

# woker最大cpu平均负载,用来判断master是否还有执行能力
wokerMaxCupLoadAvg="10"

# woker预留内存,用来判断master是否还有执行能力
wokerReservedMemory="1"


# api 配置
# api 服务端口
apiSeverPort="12345"

# api session 超时
apiSeverSessionTimeout="7200"

# api 上下文路径
apiSeverContextPath="/escheduler/"

# sping 最大文件大小
spingMaxFileSize="1024MB"

# sping 最大请求文件大小
spingMaxRequestSize="1024MB"

# api 最大post请求大小
apiMaxHttpPostSize="5000000"



# alet配置

# 邮件协议
mailPotocol="SMTP"

# 邮件服务host
mailSeverHost="smtp.exmail.qq.com"

# 邮件服务端口
mailSeverPort="25"

# 发送人
mailSende="xxxxxxxxxx"

# 发送人密码
mailPasswod="xxxxxxxxxx"

# 下载Excel路径
xlsFilePath="/opt/xls"

# conf/config/install_config.conf配置
# 安装路径
installPath="/data1_1T/eschedule"

# 部署用户
deployUse="escheduler"

# 安装hosts
ips="ak0,ark1,ark2,ark3,ark4"


# conf/config/un_config.conf配置
# 运行Maste的机器
mastes="ark0,ark1"

# 运行Woker的机器
wokers="ark2,ark3,ark4"

# 运行Alet的机器
aletServer="ark3"

# 运行Api的机器
apiSevers="ark1"


# 1,替换文件
echo "1,替换文件"
sed -i '' "s#sping.datasource.url.*#spring.datasource.url=jdbc:mysql://${mysqlHost}/${mysqlDb}?characterEncoding=UTF-8#g" conf/dao/data_source.properties
sed -i '' "s#sping.datasource.username.*#spring.datasource.username=${mysqlUserName}#g" conf/dao/data_source.properties
sed -i '' "s#sping.datasource.password.*#spring.datasource.password=${mysqlPassword}#g" conf/dao/data_source.properties

sed -i '' "s#og.quartz.dataSource.myDs.URL.*#org.quartz.dataSource.myDs.URL=jdbc:mysql://${mysqlHost}/${mysqlDb}?characterEncoding=UTF-8#g" conf/quartz.properties
sed -i '' "s#og.quartz.dataSource.myDs.user.*#org.quartz.dataSource.myDs.user=${mysqlUserName}#g" conf/quartz.properties
sed -i '' "s#og.quartz.dataSource.myDs.password.*#org.quartz.dataSource.myDs.password=${mysqlPassword}#g" conf/quartz.properties


sed -i '' "s#fs.defaultFS.*#fs.defaultFS = ${namenodeFs}#g" conf/common/hadoop/hadoop.properties
sed -i '' "s#yan.resourcemanager.ha.rm.ids.*#yarn.resourcemanager.ha.rm.ids=${yarnHaIps}#g" conf/common/hadoop/hadoop.properties
sed -i '' "s#yan.application.status.address.*#yarn.application.status.address=http://${singleYarnIp}:8088/ws/v1/cluster/apps/%s#g" conf/common/hadoop/hadoop.properties

sed -i '' "s#data.basedi.path.*#data.basedir.path=${programPath}#g" conf/common/common.properties
sed -i '' "s#data.download.basedi.path.*#data.download.basedir.path=${downloadPath}#g" conf/common/common.properties
sed -i '' "s#pocess.exec.basepath.*#process.exec.basepath=${execPath}#g" conf/common/common.properties
sed -i '' "s#data.stoe2hdfs.basepath.*#data.store2hdfs.basepath=${hdfsPath}#g" conf/common/common.properties
sed -i '' "s#hdfs.statup.state.*#hdfs.startup.state=${hdfsStartupSate}#g" conf/common/common.properties
sed -i '' "s#eschedule.env.path.*#escheduler.env.path=${shellEnvPath}#g" conf/common/common.properties
sed -i '' "s#eschedule.env.py.*#escheduler.env.py=${pythonEnvPath}#g" conf/common/common.properties
sed -i '' "s#esource.view.suffixs.*#resource.view.suffixs=${resSuffixs}#g" conf/common/common.properties
sed -i '' "s#development.state.*#development.state=${devState}#g" conf/common/common.properties

sed -i '' "s#zookeepe.quorum.*#zookeeper.quorum=${zkQuorum}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.root.*#zookeeper.escheduler.root=${zkRoot}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.dead.servers.*#zookeeper.escheduler.dead.servers=${zkDeadServers}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.masters.*#zookeeper.escheduler.masters=${zkMasters}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.workers.*#zookeeper.escheduler.workers=${zkWorkers}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.lock.masters.*#zookeeper.escheduler.lock.masters=${mastersLock}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.lock.workers.*#zookeeper.escheduler.lock.workers=${workersLock}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.lock.masters.failover.*#zookeeper.escheduler.lock.masters.failover=${mastersFailover}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.escheduler.lock.workers.failover.*#zookeeper.escheduler.lock.workers.failover=${workersFailover}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.session.timeout.*#zookeeper.session.timeout=${zkSessionTimeout}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.connection.timeout.*#zookeeper.connection.timeout=${zkConnectionTimeout}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.retry.sleep.*#zookeeper.retry.sleep=${zkRetrySleep}#g" conf/zookeeper.properties
sed -i '' "s#zookeepe.retry.maxtime.*#zookeeper.retry.maxtime=${zkRetryMaxtime}#g" conf/zookeeper.properties

sed -i '' "s#maste.exec.threads.*#master.exec.threads=${masterExecThreads}#g" conf/master.properties
sed -i '' "s#maste.exec.task.number.*#master.exec.task.number=${masterExecTaskNum}#g" conf/master.properties
sed -i '' "s#maste.heartbeat.interval.*#master.heartbeat.interval=${masterHeartbeatInterval}#g" conf/master.properties
sed -i '' "s#maste.task.commit.retryTimes.*#master.task.commit.retryTimes=${masterTaskCommitRetryTimes}#g" conf/master.properties
sed -i '' "s#maste.task.commit.interval.*#master.task.commit.interval=${masterTaskCommitInterval}#g" conf/master.properties
sed -i '' "s#maste.max.cpuload.avg.*#master.max.cpuload.avg=${masterMaxCupLoadAvg}#g" conf/master.properties
sed -i '' "s#maste.reserved.memory.*#master.reserved.memory=${masterReservedMemory}#g" conf/master.properties


sed -i '' "s#woker.exec.threads.*#worker.exec.threads=${workerExecThreads}#g" conf/worker.properties
sed -i '' "s#woker.heartbeat.interval.*#worker.heartbeat.interval=${workerHeartbeatInterval}#g" conf/worker.properties
sed -i '' "s#woker.fetch.task.num.*#worker.fetch.task.num=${workerFetchTaskNum}#g" conf/worker.properties
sed -i '' "s#woker.max.cpuload.avg.*#worker.max.cpuload.avg=${workerMaxCupLoadAvg}#g" conf/worker.properties
sed -i '' "s#woker.reserved.memory.*#worker.reserved.memory=${workerReservedMemory}#g" conf/worker.properties


sed -i '' "s#sever.port.*#server.port=${apiServerPort}#g" conf/application.properties
sed -i '' "s#sever.session.timeout.*#server.session.timeout=${apiServerSessionTimeout}#g" conf/application.properties
sed -i '' "s#sever.context-path.*#server.context-path=${apiServerContextPath}#g" conf/application.properties
sed -i '' "s#sping.http.multipart.max-file-size.*#spring.http.multipart.max-file-size=${springMaxFileSize}#g" conf/application.properties
sed -i '' "s#sping.http.multipart.max-request-size.*#spring.http.multipart.max-request-size=${springMaxRequestSize}#g" conf/application.properties
sed -i '' "s#sever.max-http-post-size.*#server.max-http-post-size=${apiMaxHttpPostSize}#g" conf/application.properties


sed -i '' "s#mail.potocol.*#mail.protocol=${mailProtocol}#g" conf/alert.properties
sed -i '' "s#mail.sever.host.*#mail.server.host=${mailServerHost}#g" conf/alert.properties
sed -i '' "s#mail.sever.port.*#mail.server.port=${mailServerPort}#g" conf/alert.properties
sed -i '' "s#mail.sende.*#mail.sender=${mailSender}#g" conf/alert.properties
sed -i '' "s#mail.passwd.*#mail.passwd=${mailPasswod}#g" conf/alert.properties
sed -i '' "s#xls.file.path.*#xls.file.path=${xlsFilePath}#g" conf/alert.properties


sed -i '' "s#installPath.*#installPath=${installPath}#g" conf/config/install_config.conf
sed -i '' "s#deployUse.*#deployUser=${deployUser}#g" conf/config/install_config.conf
sed -i '' "s#ips.*#ips=${ips}#g" conf/config/install_config.conf


sed -i '' "s#mastes.*#masters=${masters}#g" conf/config/run_config.conf
sed -i '' "s#wokers.*#workers=${workers}#g" conf/config/run_config.conf
sed -i '' "s#aletServer.*#alertServer=${alertServer}#g" conf/config/run_config.conf
sed -i '' "s#apiSevers.*#apiServers=${apiServers}#g" conf/config/run_config.conf




# 2,创建目录
echo "2,创建目录"

if [ ! -d $installPath ];then
  sudo mkdi -p $installPath
  sudo chown -R $deployUse:$deployUser $installPath
fi

hostsAr=(${ips//,/ })
fo host in ${hostsArr[@]}
do

# 如果pogramPath不存在,则创建
if ! ssh $host test -e $pogramPath; then
  ssh $host "sudo mkdi -p $programPath;sudo chown -R $deployUser:$deployUser $programPath"
fi

# 如果downloadPath不存在,则创建
if ! ssh $host test -e $downloadPath; then
  ssh $host "sudo mkdi -p $downloadPath;sudo chown -R $deployUser:$deployUser $downloadPath"
fi

# 如果$execPath不存在,则创建
if ! ssh $host test -e $execPath; then
  ssh $host "sudo mkdi -p $execPath; sudo chown -R $deployUser:$deployUser $execPath"
fi

# 如果$xlsFilePath不存在,则创建
if ! ssh $host test -e $xlsFilePath; then
  ssh $host "sudo mkdi -p $xlsFilePath; sudo chown -R $deployUser:$deployUser $xlsFilePath"
fi

done


# 3,停止服务
echo "3,停止服务"
sh $wokDir/script/stop_all.sh

# 4,删除zk节点
echo "4,删除zk节点"
sleep 1
python $wokDir/script/del_zk_node.py $zkQuorum $zkRoot

# 5,scp资源
echo "5,scp资源"
sh $wokDir/script/scp_hosts.sh

if [ $? -eq 0 ]
then
	echo 'scp拷贝完成'
else
	echo 'sc 拷贝失败退出'
	exit -1
fi

# 6,启动
echo "6,启动"
sh $wokDir/script/start_all.sh