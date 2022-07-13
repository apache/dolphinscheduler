<!-- markdown-link-check-disable -->

# 前言
本文档为dolphinscheduler配置文件说明文档,针对版本为 dolphinscheduler-1.3.x 版本.

# 目录结构
目前dolphinscheduler 所有的配置文件都在 [conf ] 目录中.
为了更直观的了解[conf]目录所在的位置以及包含的配置文件,请查看下面dolphinscheduler安装目录的简化说明.
本文主要讲述dolphinscheduler的配置文件.其他部分先不做赘述.

[注:以下 dolphinscheduler 简称为DS.]

```
├── LICENSE
│
├── NOTICE
│
├── licenses                                    licenses存放目录
│
├── bin                                         DolphinScheduler命令和环境变量配置存放目录 
│   ├── dolphinscheduler-daemon.sh              启动/关闭DolphinScheduler服务脚本
│   ├── env                                     环境变量配置存放目录
│   │   ├── dolphinscheduler_env.sh             当使用`dolphinscheduler-daemon.sh`脚本起停服务时，运行此脚本加载环境变量配置文件 [如：JAVA_HOME,HADOOP_HOME, HIVE_HOME ...] 
│   │   └── install_env.sh                      当使用`install.sh` `start-all.sh` `stop-all.sh` `status-all.sh`脚本时，运行此脚本为DolphinScheduler安装加载环境变量配置 
│   ├── install.sh                              当使用`集群`模式或`伪集群`模式部署DolphinScheduler时，运行此脚本自动安装服务  
│   ├── remove-zk-node.sh                       清理zookeeper缓存文件脚本 
│   ├── scp-hosts.sh                            安装文件传输脚本
│   ├── start-all.sh                            当使用`集群`模式或`伪集群`模式部署DolphinScheduler时，运行此脚本启动所有服务
│   ├── status-all.sh                           当使用`集群`模式或`伪集群`模式部署DolphinScheduler时，运行此脚本获取所有服务状态
│   └── stop-all.sh                             当使用`集群`模式或`伪集群`模式部署DolphinScheduler时，运行此脚本终止所有服务
│
├── alert-server                                DolphinScheduler alert-server命令、配置和依赖存放目录
│   ├── bin
│   │   └── start.sh                            DolphinScheduler alert-server启动脚本
│   ├── conf
│   │   ├── application.yaml                    alert-server配置文件
│   │   ├── common.properties                   公共服务（存储等信息）配置文件 
│   │   ├── dolphinscheduler_env.sh             alert-server环境变量配置加载脚本
│   │   └── logback-spring.xml                  alert-service日志配置文件
│   └── libs                                    alert-server依赖jar包存放目录
│
├── api-server                                  DolphinScheduler api-server命令、配置和依赖存放目录
│   ├── bin
│   │   └── start.sh                            DolphinScheduler api-server启动脚本
│   ├── conf
│   │   ├── application.yaml                    api-server配置文件
│   │   ├── common.properties                   公共服务（存储等信息）配置文件
│   │   ├── dolphinscheduler_env.sh             api-server环境变量配置加载脚本
│   │   └── logback-spring.xml                  api-service日志配置文件
│   ├── libs                                    api-server依赖jar包存放目录
│   └── ui                                      api-server相关前端WEB资源存放目录 
│
├── master-server                               DolphinScheduler master-server命令、配置和依赖存放目录
│   ├── bin                                
│   │   └── start.sh                            DolphinScheduler master-server启动脚本
│   ├── conf
│   │   ├── application.yaml                    master-server配置文件
│   │   ├── common.properties                   公共服务（存储等信息）配置文件
│   │   ├── dolphinscheduler_env.sh             master-server环境变量配置加载脚本
│   │   └── logback-spring.xml                  master-service日志配置文件
│   └── libs                                    master-server依赖jar包存放目录
│
├── standalone-server                           DolphinScheduler standalone-server命令、配置和依赖存放目录
│   ├── bin
│   │   └── start.sh                            DolphinScheduler standalone-server启动脚本
│   ├── conf
│   │   ├── application.yaml                    standalone-server配置文件
│   │   ├── common.properties                   公共服务（存储等信息）配置文件
│   │   ├── dolphinscheduler_env.sh             standalone-server环境变量配置加载脚本
│   │   ├── logback-spring.xml                  standalone-service日志配置文件
│   │   └── sql                                 DolphinScheduler元数据创建/升级sql文件
│   ├── libs                                    standalone-server依赖jar包存放目录
│   └── ui                                      standalone-server相关前端WEB资源存放目录
│       
├── tools                                       DolphinScheduler元数据工具命令、配置和依赖存放目录
│   ├── bin
│   │   └── upgrade-schema.sh                   DolphinScheduler元数据创建/升级脚本
│   ├── conf
│   │   ├── application.yaml                    元数据工具配置文件
│   │   └── common.properties                   公共服务（存储等信息）配置文件
│   ├── libs                                    元数据工具依赖jar包存放目录
│   └── sql                                     DolphinScheduler元数据创建/升级sql文件
│     
├── worker-server                               DolphinScheduler worker-server命令、配置和依赖存放目录
│       ├── bin
│       │   └── start.sh                        DolphinScheduler worker-server启动脚本
│       ├── conf
│       │   ├── application.yaml                worker-server配置文件
│       │   ├── common.properties               公共服务（存储等信息）配置文件
│       │   ├── dolphinscheduler_env.sh         worker-server环境变量配置加载脚本
│       │   └── logback-spring.xml              worker-service日志配置文件
│       └── libs                                worker-server依赖jar包存放目录
│
└── ui                                          前端WEB资源目录
```

# 配置文件详解

序号| 服务分类 |  配置文件|
|--|--|--|
1|启动/关闭DS服务脚本|dolphinscheduler-daemon.sh
2|数据库连接配置 | datasource.properties
3|zookeeper连接配置|zookeeper.properties
4|公共[存储]配置|common.properties
5|API服务配置|application-api.properties
6|Master服务配置|master.properties
7|Worker服务配置|worker.properties
8|Alert 服务配置|alert.properties
9|Quartz配置|quartz.properties
10|DS环境变量配置脚本[用于DS安装/启动]|install_config.conf
11|运行脚本加载环境变量配置文件 <br />[如: JAVA_HOME,HADOOP_HOME, HIVE_HOME ...]|dolphinscheduler_env.sh
12|各服务日志配置文件|api服务日志配置文件 : logback-api.xml  <br /> master服务日志配置文件  : logback-master.xml    <br /> worker服务日志配置文件 : logback-worker.xml  <br /> alert服务日志配置文件 : logback-alert.xml 


## 1.dolphinscheduler-daemon.sh [启动/关闭DS服务脚本]
dolphinscheduler-daemon.sh脚本负责DS的启动&关闭. 
start-all.sh/stop-all.sh最终也是通过dolphinscheduler-daemon.sh对集群进行启动/关闭操作.
目前DS只是做了一个基本的设置,JVM参数请根据各自资源的实际情况自行设置.

默认简化参数如下:
```bash
export DOLPHINSCHEDULER_OPTS="
-server 
-Xmx16g 
-Xms1g 
-Xss512k 
-XX:+UseConcMarkSweepGC 
-XX:+CMSParallelRemarkEnabled 
-XX:+UseFastAccessorMethods 
-XX:+UseCMSInitiatingOccupancyOnly 
-XX:CMSInitiatingOccupancyFraction=70
"
```

> 不建议设置"-XX:DisableExplicitGC" , DS使用Netty进行通讯,设置该参数,可能会导致内存泄漏.

## 2.datasource.properties [数据库连接]
在DS中使用Druid对数据库连接进行管理,默认简化配置如下.
|参数 | 默认值| 描述|
|--|--|--|
spring.datasource.driver-class-name| |数据库驱动
spring.datasource.url||数据库连接地址
spring.datasource.username||数据库用户名
spring.datasource.password||数据库密码
spring.datasource.initialSize|5| 初始连接池数量
spring.datasource.minIdle|5| 最小连接池数量
spring.datasource.maxActive|5| 最大连接池数量
spring.datasource.maxWait|60000| 最大等待时长
spring.datasource.timeBetweenEvictionRunsMillis|60000| 连接检测周期
spring.datasource.timeBetweenConnectErrorMillis|60000| 重试间隔
spring.datasource.minEvictableIdleTimeMillis|300000| 连接保持空闲而不被驱逐的最小时间
spring.datasource.validationQuery|SELECT 1|检测连接是否有效的sql
spring.datasource.validationQueryTimeout|3| 检测连接是否有效的超时时间[seconds]
spring.datasource.testWhileIdle|true| 申请连接的时候检测，如果空闲时间大于timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效。
spring.datasource.testOnBorrow|true| 申请连接时执行validationQuery检测连接是否有效
spring.datasource.testOnReturn|false| 归还连接时执行validationQuery检测连接是否有效
spring.datasource.defaultAutoCommit|true| 是否开启自动提交
spring.datasource.keepAlive|true| 连接池中的minIdle数量以内的连接，空闲时间超过minEvictableIdleTimeMillis，则会执行keepAlive操作。
spring.datasource.poolPreparedStatements|true| 开启PSCache
spring.datasource.maxPoolPreparedStatementPerConnectionSize|20| 要启用PSCache，必须配置大于0，当大于0时，poolPreparedStatements自动触发修改为true。


## 3.zookeeper.properties [zookeeper连接配置]
|参数 |默认值| 描述| 
|--|--|--|
zookeeper.quorum|localhost:2181| zk集群连接信息
zookeeper.dolphinscheduler.root|/dolphinscheduler| DS在zookeeper存储根目录
zookeeper.session.timeout|60000|  session 超时
zookeeper.connection.timeout|30000|  连接超时
zookeeper.retry.base.sleep|100| 基本重试时间差
zookeeper.retry.max.sleep|30000| 最大重试时间
zookeeper.retry.maxtime|10|最大重试次数


## 4.common.properties [hadoop、s3、yarn配置]
common.properties配置文件目前主要是配置hadoop/s3a相关的配置. 
| 参数 | 默认值 | 描述 |
|--|--|--|
data.basedir.path | /tmp/dolphinscheduler | 本地工作目录,用于存放临时文件
resource.storage.type | NONE | 资源文件存储类型: HDFS,S3,NONE
resource.storage.upload.base.path | /dolphinscheduler | 资源文件存储路径
resource.aws.access.key.id | minioadmin | S3 access key
resource.aws.secret.access.key | minioadmin | S3 secret access key
resource.aws.region | us-east-1 | S3 区域
resource.aws.s3.bucket.name | dolphinscheduler | S3 存储桶名称
resource.aws.s3.endpoint | http://minio:9000 | s3 endpoint地址
resource.hdfs.root.user | hdfs | 如果存储类型为HDFS,需要配置拥有对应操作权限的用户
resource.hdfs.fs.defaultFS | hdfs://mycluster:8020 | 请求地址如果resource.storage.type=S3,该值类似为: s3a://dolphinscheduler. 如果resource.storage.type=HDFS, 如果 hadoop 配置了 HA,需要复制core-site.xml 和 hdfs-site.xml 文件到conf目录
hadoop.security.authentication.startup.state | false | hadoop是否开启kerberos权限
java.security.krb5.conf.path | /opt/krb5.conf | kerberos配置目录
login.user.keytab.username | hdfs-mycluster@ESZ.COM | kerberos登录用户
login.user.keytab.path | /opt/hdfs.headless.keytab | kerberos登录用户keytab
kerberos.expire.time | 2 | kerberos过期时间,整数,单位为小时
yarn.resourcemanager.ha.rm.ids |  | yarn resourcemanager 地址, 如果resourcemanager开启了HA, 输入HA的IP地址(以逗号分隔),如果resourcemanager为单节点, 该值为空即可
yarn.application.status.address | http://ds1:8088/ws/v1/cluster/apps/%s | 如果resourcemanager开启了HA或者没有使用resourcemanager,保持默认值即可. 如果resourcemanager为单节点,你需要将ds1 配置为resourcemanager对应的hostname
dolphinscheduler.env.path | env/dolphinscheduler_env.sh | 运行脚本加载环境变量配置文件[如: JAVA_HOME,HADOOP_HOME, HIVE_HOME ...]
development.state | false | 是否处于开发模式
task.resource.limit.state | false | 是否启用资源限制模式


## 5.application-api.properties [API服务配置]
|参数 |默认值| 描述| 
|--|--|--|
server.port|12345|api服务通讯端口
server.servlet.session.timeout|7200|session超时时间
server.servlet.context-path|/dolphinscheduler |请求路径
spring.servlet.multipart.max-file-size|1024MB|最大上传文件大小
spring.servlet.multipart.max-request-size|1024MB|最大请求大小
server.jetty.max-http-post-size|5000000|jetty服务最大发送请求大小
spring.messages.encoding|UTF-8|请求编码
spring.jackson.time-zone|GMT+8|设置时区
spring.messages.basename|i18n/messages|i18n配置
security.authentication.type|PASSWORD|权限校验类型
security.authentication.ldap.user.admin|read-only-admin|LDAP登陆时，系统管理员账号
security.authentication.ldap.urls|ldap://ldap.forumsys.com:389/|LDAP urls
security.authentication.ldap.base-dn|dc=example,dc=com|LDAP base dn
security.authentication.ldap.username|cn=read-only-admin,dc=example,dc=com|LDAP账号
security.authentication.ldap.password|password|LDAP密码
security.authentication.ldap.user.identity-attribute|uid|LDAP用户身份标识字段名
security.authentication.ldap.user.email-attribute|mail|LDAP邮箱字段名
security.authentication.ldap.user.not-exist-action|CREATE|当LDAP用户不存在时执行的操作。CREATE：当用户不存在时自动新建用户, DENY：当用户不存在时拒绝登陆
traffic.control.global.switch|false|流量控制全局开关
traffic.control.max-global-qps-rate|300|全局最大请求数/秒
traffic.control.tenant-switch|false|流量控制租户开关
traffic.control.default-tenant-qps-rate|10|默认租户最大请求数/秒限制
traffic.control.customize-tenant-qps-rate||自定义租户最大请求数/秒限制

## 6.master.properties [Master服务配置]
|参数 |默认值| 描述| 
|--|--|--|
master.listen.port|5678|master监听端口
master.exec.threads|100|master工作线程数量,用于限制并行的流程实例数量
master.exec.task.num|20|master每个流程实例的并行任务数量
master.dispatch.task.num|3|master每个批次的派发任务数量
master.host.selector|LowerWeight|master host选择器,用于选择合适的worker执行任务,可选值: Random, RoundRobin, LowerWeight
master.heartbeat.interval|10|master心跳间隔,单位为秒
master.task.commit.retryTimes|5|任务重试次数
master.task.commit.interval|1000|任务提交间隔,单位为毫秒
master.max.cpuload.avg|-1|master最大cpuload均值,只有高于系统cpuload均值时,master服务才能调度任务. 默认值为-1: cpu cores * 2
master.reserved.memory|0.3|master预留内存,只有低于系统可用内存时,master服务才能调度任务,单位为G


## 7.worker.properties [Worker服务配置]
|参数 |默认值| 描述| 
|--|--|--|
worker.listen.port|1234|worker监听端口
worker.exec.threads|100|worker工作线程数量,用于限制并行的任务实例数量
worker.heartbeat.interval|10|worker心跳间隔,单位为秒
worker.max.cpuload.avg|-1|worker最大cpuload均值,只有高于系统cpuload均值时,worker服务才能被派发任务. 默认值为-1: cpu cores * 2
worker.reserved.memory|0.3|worker预留内存,只有低于系统可用内存时,worker服务才能被派发任务,单位为G
worker.groups|default|worker分组配置,逗号分隔,例如'worker.groups=default,test' <br> worker启动时会根据该配置自动加入对应的分组
worker.tenant.auto.create|true|租户对应于系统的用户,由worker提交作业.如果系统没有该用户,则在参数worker.tenant.auto.create为true后自动创建。
worker.tenant.distributed.user|false|使用场景为分布式用户例如使用FreeIpa创建的用户存于LDAP中.该参数只适用于Linux,当该参数为true时worker.tenant.auto.create将不生效,不会自动去创建租户


## 8.alert.properties [Alert 告警服务配置]
|参数 |默认值| 描述| 
|--|--|--|
alert.type|EMAIL|告警类型|
mail.protocol|SMTP| 邮件服务器协议
mail.server.host|xxx.xxx.com|邮件服务器地址
mail.server.port|25|邮件服务器端口
mail.sender|xxx@xxx.com|发送人邮箱
mail.user|xxx@xxx.com|发送人邮箱名称
mail.passwd|111111|发送人邮箱密码
mail.smtp.starttls.enable|true|邮箱是否开启tls
mail.smtp.ssl.enable|false|邮箱是否开启ssl
mail.smtp.ssl.trust|xxx.xxx.com|邮箱ssl白名单
xls.file.path|/tmp/xls|邮箱附件临时工作目录
||以下为企业微信配置[选填]|
enterprise.wechat.enable|false|企业微信是否启用
enterprise.wechat.corp.id|xxxxxxx|
enterprise.wechat.secret|xxxxxxx|
enterprise.wechat.agent.id|xxxxxxx|
enterprise.wechat.users|xxxxxxx|
enterprise.wechat.token.url|https://qyapi.weixin.qq.com/cgi-bin/gettoken?  <br /> corpid=$corpId&corpsecret=$secret|
enterprise.wechat.push.url|https://qyapi.weixin.qq.com/cgi-bin/message/send?  <br /> access_token=$token|
enterprise.wechat.user.send.msg||发送消息格式
enterprise.wechat.team.send.msg||群发消息格式
plugin.dir|/Users/xx/your/path/to/plugin/dir|插件目录


## 9.quartz.properties [Quartz配置]
这里面主要是quartz配置,请结合实际业务场景&资源进行配置,本文暂时不做展开.
|参数 |默认值| 描述| 
|--|--|--|
org.quartz.jobStore.driverDelegateClass | org.quartz.impl.jdbcjobstore.StdJDBCDelegate
org.quartz.jobStore.driverDelegateClass | org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
org.quartz.scheduler.instanceName | DolphinScheduler
org.quartz.scheduler.instanceId | AUTO
org.quartz.scheduler.makeSchedulerThreadDaemon | true
org.quartz.jobStore.useProperties | false
org.quartz.threadPool.class | org.quartz.simpl.SimpleThreadPool
org.quartz.threadPool.makeThreadsDaemons | true
org.quartz.threadPool.threadCount | 25
org.quartz.threadPool.threadPriority | 5
org.quartz.jobStore.class | org.quartz.impl.jdbcjobstore.JobStoreTX
org.quartz.jobStore.tablePrefix | QRTZ_
org.quartz.jobStore.isClustered | true
org.quartz.jobStore.misfireThreshold | 60000
org.quartz.jobStore.clusterCheckinInterval | 5000
org.quartz.jobStore.acquireTriggersWithinLock|true
org.quartz.jobStore.dataSource | myDs
org.quartz.dataSource.myDs.connectionProvider.class | org.apache.dolphinscheduler.service.quartz.DruidConnectionProvider


## 10.install_config.conf [DS环境变量配置脚本[用于DS安装/启动]]
install_config.conf这个配置文件比较繁琐,这个文件主要有两个地方会用到.
* 1.DS集群的自动安装. 

> 调用install.sh脚本会自动加载该文件中的配置.并根据该文件中的内容自动配置上述的配置文件中的内容. 
> 比如:dolphinscheduler-daemon.sh、datasource.properties、zookeeper.properties、common.properties、application-api.properties、master.properties、worker.properties、alert.properties、quartz.properties 等文件.


* 2.DS集群的启动&关闭.
>DS集群在启动&关闭的时候,会加载该配置文件中的masters,workers,alertServer,apiServers等参数,启动/关闭DS集群.

文件内容如下:
```bash

# 注意: 该配置文件中如果包含特殊字符,如: `.*[]^${}\+?|()@#&`, 请转义,
#      示例: `[` 转义为 `\[`

# 数据库类型, 目前仅支持 postgresql 或者 mysql
dbtype="mysql"

# 数据库 地址 & 端口
dbhost="192.168.xx.xx:3306"

# 数据库 名称
dbname="dolphinscheduler"


# 数据库 用户名
username="xx"

# 数据库 密码
password="xx"

# Zookeeper地址
zkQuorum="192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181"

# 将DS安装到哪个目录，如: /data1_1T/dolphinscheduler，
installPath="/data1_1T/dolphinscheduler"

# 使用哪个用户部署
# 注意: 部署用户需要sudo 权限, 并且可以操作 hdfs .
#     如果使用hdfs的话,根目录必须使用该用户进行创建.否则会有权限相关的问题.
deployUser="dolphinscheduler"


# 以下为告警服务配置
# 邮件服务器地址
mailServerHost="smtp.exmail.qq.com"

# 邮件服务器 端口
mailServerPort="25"

# 发送者
mailSender="xxxxxxxxxx"

# 发送用户
mailUser="xxxxxxxxxx"

# 邮箱密码
mailPassword="xxxxxxxxxx"

# TLS协议的邮箱设置为true，否则设置为false
starttlsEnable="true"

# 开启SSL协议的邮箱配置为true，否则为false。注意: starttlsEnable和sslEnable不能同时为true
sslEnable="false"

# 邮件服务地址值，同 mailServerHost
sslTrust="smtp.exmail.qq.com"

#业务用到的比如sql等资源文件上传到哪里，可以设置：HDFS,S3,NONE。如果想上传到HDFS，请配置为HDFS；如果不需要资源上传功能请选择NONE。
resourceStorageType="NONE"

# if S3，write S3 address，HA，for example ：s3a://dolphinscheduler，
# Note，s3 be sure to create the root directory /dolphinscheduler
defaultFS="hdfs://mycluster:8020"

# 如果resourceStorageType 为S3 需要配置的参数如下:
s3Endpoint="http://192.168.xx.xx:9010"
s3AccessKey="xxxxxxxxxx"
s3SecretKey="xxxxxxxxxx"

# 如果ResourceManager是HA，则配置为ResourceManager节点的主备ip或者hostname,比如"192.168.xx.xx,192.168.xx.xx"，否则如果是单ResourceManager或者根本没用到yarn,请配置yarnHaIps=""即可，如果没用到yarn，配置为""
yarnHaIps="192.168.xx.xx,192.168.xx.xx"

# 如果是单ResourceManager，则配置为ResourceManager节点ip或主机名，否则保持默认值即可。
singleYarnIp="yarnIp1"

# 资源文件在 HDFS/S3  存储路径
resourceUploadPath="/dolphinscheduler"


# HDFS/S3  操作用户 
hdfsRootUser="hdfs"

# 以下为 kerberos 配置

# kerberos是否开启
kerberosStartUp="false"
# kdc krb5 config file path
krb5ConfPath="$installPath/conf/krb5.conf"
# keytab username
keytabUserName="hdfs-mycluster@ESZ.COM"
# username keytab path
keytabPath="$installPath/conf/hdfs.headless.keytab"


# api 服务端口
apiServerPort="12345"


# 部署DS的所有主机hostname
ips="ds1,ds2,ds3,ds4,ds5"

# ssh 端口 , 默认 22
sshPort="22"

# 部署master服务主机
masters="ds1,ds2"

# 部署 worker服务的主机
# 注意: 每一个worker都需要设置一个worker 分组的名称,默认值为 "default"
workers="ds1:default,ds2:default,ds3:default,ds4:default,ds5:default"

#  部署alert服务主机
alertServer="ds3"

# 部署api服务主机 
apiServers="ds1"
```

## 11.dolphinscheduler_env.sh [环境变量配置]

通过类似shell方式提交任务的的时候,会加载该配置文件中的环境变量到主机中. 涉及到的 `JAVA_HOME`、元数据库、注册中心和任务类型配置，其中任务
类型主要有: Shell任务、Python任务、Spark任务、Flink任务、Datax任务等等

```bash
# JAVA_HOME, will use it to start DolphinScheduler server
export JAVA_HOME=${JAVA_HOME:-/opt/soft/java}

# Database related configuration, set database type, username and password
export DATABASE=${DATABASE:-postgresql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL
export SPRING_DATASOURCE_USERNAME
export SPRING_DATASOURCE_PASSWORD

# DolphinScheduler server related configuration
export SPRING_CACHE_TYPE=${SPRING_CACHE_TYPE:-none}
export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-UTC}
export MASTER_FETCH_COMMAND_NUM=${MASTER_FETCH_COMMAND_NUM:-10}

# Registry center configuration, determines the type and link of the registry center
export REGISTRY_TYPE=${REGISTRY_TYPE:-zookeeper}
export REGISTRY_ZOOKEEPER_CONNECT_STRING=${REGISTRY_ZOOKEEPER_CONNECT_STRING:-localhost:2181}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/opt/soft/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/opt/soft/hadoop/etc/hadoop}
export SPARK_HOME1=${SPARK_HOME1:-/opt/soft/spark1}
export SPARK_HOME2=${SPARK_HOME2:-/opt/soft/spark2}
export PYTHON_HOME=${PYTHON_HOME:-/opt/soft/python}
export HIVE_HOME=${HIVE_HOME:-/opt/soft/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_HOME=${DATAX_HOME:-/opt/soft/datax}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH
```

## 12.各服务日志配置文件
对应服务服务名称| 日志文件名 |
|--|--|--|
api服务日志配置文件 |logback-api.xml|
master服务日志配置文件|logback-master.xml |
worker服务日志配置文件|logback-worker.xml |
alert服务日志配置文件|logback-alert.xml |
