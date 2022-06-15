<!-- markdown-link-check-disable -->

# Configuration

## Preface

This document explains the DolphinScheduler application configurations according to DolphinScheduler-1.3.x versions.

## Directory Structure

Currently, all the configuration files are under [conf ] directory. 
Check the following simplified DolphinScheduler installation directories to have a direct view about the position of [conf] directory and configuration files it has. 
This document only describes DolphinScheduler configurations and other topics are not going into.

[Note: the DolphinScheduler (hereinafter called the ‘DS’) .]

```
├── LICENSE
│
├── NOTICE
│
├── licenses                                    directory of licenses
│
├── bin                                         directory of DolphinScheduler application commands, configrations scripts 
│   ├── dolphinscheduler-daemon.sh              script to start or shut down DolphinScheduler application
│   ├── env                                     directory of scripts to load environment variables
│   │   ├── dolphinscheduler_env.sh             script to export environment variables [eg: JAVA_HOME,HADOOP_HOME, HIVE_HOME ...] when you start or stop service using script `dolphinscheduler-daemon.sh`
│   │   └── install_env.sh                      script to export environment variables for DolphinScheduler installation when you use scripts `install.sh` `start-all.sh` `stop-all.sh` `status-all.sh`
│   ├── install.sh                              script to auto-setup services when you deploy DolphinScheduler in `psuedo-cluster` mode or `cluster` mode
│   ├── remove-zk-node.sh                       script to cleanup ZooKeeper caches 
│   ├── scp-hosts.sh                            script to copy installation files to target hosts 
│   ├── start-all.sh                            script to start all services when you deploy DolphinScheduler in `psuedo-cluster` mode or `cluster` mode
│   ├── status-all.sh                           script to check the status of all services when you deploy DolphinScheduler in `psuedo-cluster` mode or `cluster` mode
│   └── stop-all.sh                             script to shut down all services when you deploy DolphinScheduler in `psuedo-cluster` mode or `cluster` mode
│
├── alert-server                                directory of DolphinScheduler alert-server commands, configrations scripts and libs
│   ├── bin
│   │   └── start.sh                            script to start DolphinScheduler alert-server
│   ├── conf
│   │   ├── application.yaml                    configurations of alert-server
│   │   ├── common.properties                   configurations of common-service like storage, credentials, etc. 
│   │   ├── dolphinscheduler_env.sh             script to load environment variables for alert-server
│   │   └── logback-spring.xml                  configurations of alert-service log
│   └── libs                                    directory of alert-server libs
│
├── api-server                                  directory of DolphinScheduler api-server commands, configrations scripts and libs
│   ├── bin
│   │   └── start.sh                            script to start DolphinScheduler api-server
│   ├── conf
│   │   ├── application.yaml                    configurations of api-server
│   │   ├── common.properties                   configurations of common-service like storage, credentials, etc.
│   │   ├── dolphinscheduler_env.sh             script to load environment variables for api-server
│   │   └── logback-spring.xml                  configurations of api-service log
│   ├── libs                                    directory of api-server libs
│   └── ui                                      directory of api-server related front-end web resources 
│
├── master-server                               directory of DolphinScheduler master-server commands, configrations scripts and libs
│   ├── bin                                
│   │   └── start.sh                            script to start DolphinScheduler master-server
│   ├── conf
│   │   ├── application.yaml                    configurations of master-server
│   │   ├── common.properties                   configurations of common-service like storage, credentials, etc.
│   │   ├── dolphinscheduler_env.sh             script to load environment variables for master-server
│   │   └── logback-spring.xml                  configurations of master-service log
│   └── libs                                    directory of master-server libs
│
├── standalone-server                           directory of DolphinScheduler standalone-server commands, configrations scripts and libs
│   ├── bin
│   │   └── start.sh                            script to start DolphinScheduler standalone-server
│   ├── conf
│   │   ├── application.yaml                    configurations of standalone-server
│   │   ├── common.properties                   configurations of common-service like storage, credentials, etc.
│   │   ├── dolphinscheduler_env.sh             script to load environment variables for standalone-server
│   │   ├── logback-spring.xml                  configurations of standalone-service log
│   │   └── sql                                 .sql files to create or upgrade DolphinScheduler metadata
│   ├── libs                                    directory of standalone-server libs
│   └── ui                                      directory of standalone-server related front-end web resources
│       
├── tools                                       directory of DolphinScheduler metadata tools commands, configrations scripts and libs
│   ├── bin
│   │   └── upgrade-schema.sh                   script to initialize or upgrade DolphinScheduler metadata
│   ├── conf
│   │   ├── application.yaml                    configurations of tools
│   │   └── common.properties                   configurations of common-service like storage, credentials, etc.
│   ├── libs                                    directory of tool libs
│   └── sql                                     .sql files to create or upgrade DolphinScheduler metadata
│     
├── worker-server                               directory of DolphinScheduler worker-server commands, configrations scripts and libs
│       ├── bin
│       │   └── start.sh                        script to start DolphinScheduler worker-server
│       ├── conf
│       │   ├── application.yaml                configurations of worker-server
│       │   ├── common.properties               configurations of common-service like storage, credentials, etc.
│       │   ├── dolphinscheduler_env.sh         script to load environment variables for worker-server
│       │   └── logback-spring.xml              configurations of worker-service log
│       └── libs                                directory of worker-server libs
│
└── ui                                          directory of front-end web resources
```

## Configurations in Details

serial number| service classification| config file|
|--|--|--|
1|startup or shutdown DS application|dolphinscheduler-daemon.sh
2|datasource config properties|datasource.properties
3|ZooKeeper config properties|zookeeper.properties
4|common-service[storage] config properties|common.properties
5|API-service config properties|application-api.properties
6|master-service config properties|master.properties
7|worker-service config properties|worker.properties
8|alert-service config properties|alert.properties
9|quartz config properties|quartz.properties
10|DS environment variables configuration script[install/start DS]|install_config.conf
11|load environment variables configs <br /> [eg: JAVA_HOME,HADOOP_HOME, HIVE_HOME ...]|dolphinscheduler_env.sh
12|services log config files|API-service log config : logback-api.xml  <br /> master-service log config  : logback-master.xml    <br /> worker-service log config : logback-worker.xml  <br /> alert-service log config : logback-alert.xml


### dolphinscheduler-daemon.sh [startup or shutdown DS application]

dolphinscheduler-daemon.sh is responsible for DS startup and shutdown.
Essentially, start-all.sh or stop-all.sh startup and shutdown the cluster via dolphinscheduler-daemon.sh.
Currently, DS just makes a basic config, remember to config further JVM options based on your practical situation of resources.

Default simplified parameters are:
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

> "-XX:DisableExplicitGC" is not recommended due to may lead to memory link (DS dependent on Netty to communicate).

### datasource.properties [datasource config properties]

DS uses Druid to manage database connections and default simplified configs are:
|Parameters | Default value| Description|
|--|--|--|
spring.datasource.driver-class-name||datasource driver
spring.datasource.url||datasource connection url
spring.datasource.username||datasource username
spring.datasource.password||datasource password
spring.datasource.initialSize|5| initial connection pool size number
spring.datasource.minIdle|5| minimum connection pool size number
spring.datasource.maxActive|5| maximum connection pool size number
spring.datasource.maxWait|60000| max wait milliseconds
spring.datasource.timeBetweenEvictionRunsMillis|60000| idle connection check interval
spring.datasource.timeBetweenConnectErrorMillis|60000| retry interval
spring.datasource.minEvictableIdleTimeMillis|300000| connections over minEvictableIdleTimeMillis will be collect when idle check
spring.datasource.validationQuery|SELECT 1| validate connection by running the SQL
spring.datasource.validationQueryTimeout|3| validate connection timeout[seconds]
spring.datasource.testWhileIdle|true| set whether the pool validates the allocated connection when a new connection request comes
spring.datasource.testOnBorrow|true| validity check when the program requests a new connection
spring.datasource.testOnReturn|false| validity check when the program recalls a connection
spring.datasource.defaultAutoCommit|true| whether auto commit
spring.datasource.keepAlive|true| runs validationQuery SQL to avoid the connection closed by pool when the connection idles over minEvictableIdleTimeMillis
spring.datasource.poolPreparedStatements|true| open PSCache
spring.datasource.maxPoolPreparedStatementPerConnectionSize|20| specify the size of PSCache on each connection


### zookeeper.properties [zookeeper config properties]

|Parameters | Default value| Description|
|--|--|--|
zookeeper.quorum|localhost:2181| ZooKeeper cluster connection info
zookeeper.dolphinscheduler.root|/dolphinscheduler| DS is stored under ZooKeeper root directory
zookeeper.session.timeout|60000|  session timeout
zookeeper.connection.timeout|30000| connection timeout
zookeeper.retry.base.sleep|100| time to wait between subsequent retries
zookeeper.retry.max.sleep|30000| maximum time to wait between subsequent retries
zookeeper.retry.maxtime|10| maximum retry times


### common.properties [hadoop、s3、yarn config properties]

Currently, common.properties mainly configures Hadoop,s3a related configurations.
|Parameters | Default value| Description|
|--|--|--|
data.basedir.path|/tmp/dolphinscheduler| local directory used to store temp files
resource.storage.type|NONE| type of resource files: HDFS, S3, NONE
resource.upload.path|/dolphinscheduler| storage path of resource files
hadoop.security.authentication.startup.state|false| whether hadoop grant kerberos permission
java.security.krb5.conf.path|/opt/krb5.conf|kerberos config directory
login.user.keytab.username|hdfs-mycluster@ESZ.COM|kerberos username
login.user.keytab.path|/opt/hdfs.headless.keytab|kerberos user keytab
kerberos.expire.time|2|kerberos expire time,integer,the unit is hour
resource.view.suffixs| txt,log,sh,conf,cfg,py,java,sql,hql,xml,properties| file types supported by resource center
hdfs.root.user|hdfs| configure users with corresponding permissions if storage type is HDFS
fs.defaultFS|hdfs://mycluster:8020|If resource.storage.type=S3, then the request url would be similar to 's3a://dolphinscheduler'. Otherwise if resource.storage.type=HDFS and hadoop supports HA, copy core-site.xml and hdfs-site.xml into 'conf' directory
fs.s3a.endpoint||s3 endpoint url
fs.s3a.access.key||s3 access key
fs.s3a.secret.key||s3 secret key
yarn.resourcemanager.ha.rm.ids||specify the yarn resourcemanager url. if resourcemanager supports HA, input HA IP addresses (separated by comma), or input null for standalone
yarn.application.status.address|http://ds1:8088/ws/v1/cluster/apps/%s|keep default if ResourceManager supports HA or not use ResourceManager, or replace ds1 with corresponding hostname if ResourceManager in standalone mode
dolphinscheduler.env.path|env/dolphinscheduler_env.sh|load environment variables configs [eg: JAVA_HOME,HADOOP_HOME, HIVE_HOME ...]
development.state|false| specify whether in development state
task.resource.limit.state|false|specify whether in resource limit state


### application-api.properties [API-service log config]

|Parameters | Default value| Description|
|--|--|--|
server.port|12345|api service communication port
server.servlet.session.timeout|7200|session timeout
server.servlet.context-path|/dolphinscheduler | request path
spring.servlet.multipart.max-file-size|1024MB| maximum file size
spring.servlet.multipart.max-request-size|1024MB| maximum request size
server.jetty.max-http-post-size|5000000| jetty maximum post size
spring.messages.encoding|UTF-8| message encoding
spring.jackson.time-zone|GMT+8| time zone
spring.messages.basename|i18n/messages| i18n config
security.authentication.type|PASSWORD| authentication type
security.authentication.ldap.user.admin|read-only-admin|admin user account when you log-in with LDAP
security.authentication.ldap.urls|ldap://ldap.forumsys.com:389/|LDAP urls
security.authentication.ldap.base.dn|dc=example,dc=com|LDAP base dn
security.authentication.ldap.username|cn=read-only-admin,dc=example,dc=com|LDAP username
security.authentication.ldap.password|password|LDAP password
security.authentication.ldap.user.identity.attribute|uid|LDAP user identity attribute 
security.authentication.ldap.user.email.attribute|mail|LDAP user email attribute

### master.properties [master-service log config]

|Parameters | Default value| Description|
|--|--|--|
master.listen.port|5678|master listen port
master.exec.threads|100|master-service execute thread number, used to limit the number of process instances in parallel
master.exec.task.num|20|defines the number of parallel tasks for each process instance of the master-service
master.dispatch.task.num|3|defines the number of dispatch tasks for each batch of the master-service
master.host.selector|LowerWeight|master host selector, to select a suitable worker to run the task, optional value: random, round-robin, lower weight
master.heartbeat.interval|10|master heartbeat interval, the unit is second
master.task.commit.retryTimes|5|master commit task retry times
master.task.commit.interval|1000|master commit task interval, the unit is millisecond
master.max.cpuload.avg|-1|master max CPU load avg, only higher than the system CPU load average, master server can schedule. default value -1: the number of CPU cores * 2
master.reserved.memory|0.3|master reserved memory, only lower than system available memory, master server can schedule. default value 0.3, the unit is G


### worker.properties [worker-service log config]

|Parameters | Default value| Description|
|--|--|--|
worker.listen.port|1234|worker-service listen port
worker.exec.threads|100|worker-service execute thread number, used to limit the number of task instances in parallel
worker.heartbeat.interval|10|worker-service heartbeat interval, the unit is second
worker.max.cpuload.avg|-1|worker max CPU load avg, only higher than the system CPU load average, worker server can be dispatched tasks. default value -1: the number of CPU cores * 2
worker.reserved.memory|0.3|worker reserved memory, only lower than system available memory, worker server can be dispatched tasks. default value 0.3, the unit is G
worker.groups|default|worker groups separated by comma, e.g., 'worker.groups=default,test' <br> worker will join corresponding group according to this config when startup
worker.tenant.auto.create|true|tenant corresponds to the user of the system, which is used by the worker to submit the job. If system does not have this user, it will be automatically created after the parameter worker.tenant.auto.create is true.
worker.tenant.distributed.user|false|Scenes to be used for distributed users.For example,users created by FreeIpa are stored in LDAP.This parameter only applies to Linux, When this parameter is true, worker.tenant.auto.create has no effect and will not automatically create tenants.

### alert.properties [alert-service log config]

|Parameters | Default value| Description|
|--|--|--|
alert.type|EMAIL|alter type|
mail.protocol|SMTP|mail server protocol
mail.server.host|xxx.xxx.com|mail server host
mail.server.port|25|mail server port
mail.sender|xxx@xxx.com|mail sender email
mail.user|xxx@xxx.com|mail sender email name
mail.passwd|111111|mail sender email password
mail.smtp.starttls.enable|true|specify mail whether open tls
mail.smtp.ssl.enable|false|specify mail whether open ssl
mail.smtp.ssl.trust|xxx.xxx.com|specify mail ssl trust list
xls.file.path|/tmp/xls|mail attachment temp storage directory
||following configure WeCom[optional]|
enterprise.wechat.enable|false|specify whether enable WeCom
enterprise.wechat.corp.id|xxxxxxx|WeCom corp id
enterprise.wechat.secret|xxxxxxx|WeCom secret
enterprise.wechat.agent.id|xxxxxxx|WeCom agent id
enterprise.wechat.users|xxxxxxx|WeCom users
enterprise.wechat.token.url|https://qyapi.weixin.qq.com/cgi-bin/gettoken?  <br /> corpid=$corpId&corpsecret=$secret|WeCom token url
enterprise.wechat.push.url|https://qyapi.weixin.qq.com/cgi-bin/message/send?  <br /> access_token=$token|WeCom push url
enterprise.wechat.user.send.msg||send message format
enterprise.wechat.team.send.msg||group message format
plugin.dir|/Users/xx/your/path/to/plugin/dir|plugin directory


### quartz.properties [quartz config properties]

This part describes quartz configs and configure them based on your practical situation and resources.
|Parameters | Default value| Description|
|--|--|--|
org.quartz.jobStore.driverDelegateClass | org.quartz.impl.jdbcjobstore.StdJDBCDelegate |
org.quartz.jobStore.driverDelegateClass | org.quartz.impl.jdbcjobstore.PostgreSQLDelegate |
org.quartz.scheduler.instanceName | DolphinScheduler |
org.quartz.scheduler.instanceId | AUTO |
org.quartz.scheduler.makeSchedulerThreadDaemon | true |
org.quartz.jobStore.useProperties | false |
org.quartz.threadPool.class | org.quartz.simpl.SimpleThreadPool |
org.quartz.threadPool.makeThreadsDaemons | true |
org.quartz.threadPool.threadCount | 25 |
org.quartz.threadPool.threadPriority | 5 |
org.quartz.jobStore.class | org.quartz.impl.jdbcjobstore.JobStoreTX |
org.quartz.jobStore.tablePrefix | QRTZ_ |
org.quartz.jobStore.isClustered | true |
org.quartz.jobStore.misfireThreshold | 60000 |
org.quartz.jobStore.clusterCheckinInterval | 5000 |
org.quartz.jobStore.acquireTriggersWithinLock|true |
org.quartz.jobStore.dataSource | myDs |
org.quartz.dataSource.myDs.connectionProvider.class | org.apache.dolphinscheduler.service.quartz.DruidConnectionProvider |


### install_config.conf [DS environment variables configuration script[install or start DS]]

install_config.conf is a bit complicated and is mainly used in the following two places.
* DS Cluster Auto Installation.

> System will load configs in the install_config.conf and auto-configure files below, based on the file content when executing 'install.sh'.
> Files such as dolphinscheduler-daemon.sh, datasource.properties, zookeeper.properties, common.properties, application-api.properties, master.properties, worker.properties, alert.properties, quartz.properties, etc.

* Startup and Shutdown DS Cluster.

> The system will load masters, workers, alert-server, API-servers and other parameters inside the file to startup or shutdown DS cluster.

#### File Content

```bash

# Note:  please escape the character if the file contains special characters such as `.*[]^${}\+?|()@#&`.
#   eg: `[` escape to `\[`

# Database type (DS currently only supports PostgreSQL and MySQL)
dbtype="mysql"

# Database url and port
dbhost="192.168.xx.xx:3306"

# Database name
dbname="dolphinscheduler"


# Database username
username="xx"

# Database password
password="xx"

# ZooKeeper url
zkQuorum="192.168.xx.xx:2181,192.168.xx.xx:2181,192.168.xx.xx:2181"

# DS installation path, such as '/data1_1T/dolphinscheduler'
installPath="/data1_1T/dolphinscheduler"

# Deployment user
# Note: Deployment user needs 'sudo' privilege and has rights to operate HDFS.
#     Root directory must be created by the same user if using HDFS, otherwise permission related issues will be raised.
deployUser="dolphinscheduler"


# Followings are alert-service configs
# Mail server host
mailServerHost="smtp.exmail.qq.com"

# Mail server port
mailServerPort="25"

# Mail sender
mailSender="xxxxxxxxxx"

# Mail user
mailUser="xxxxxxxxxx"

# Mail password
mailPassword="xxxxxxxxxx"

# Whether mail supports TLS
starttlsEnable="true"

# Whether mail supports SSL. Note: starttlsEnable and sslEnable cannot both set true.
sslEnable="false"

# Mail server host, same as mailServerHost
sslTrust="smtp.exmail.qq.com"

# Specify which resource upload function to use for resources storage, such as sql files. And supported options are HDFS, S3 and NONE. HDFS for upload to HDFS and NONE for not using this function.
resourceStorageType="NONE"

# if S3, write S3 address. HA, for example: s3a://dolphinscheduler，
# Note: s3 make sure to create the root directory /dolphinscheduler
defaultFS="hdfs://mycluster:8020"

# If parameter 'resourceStorageType' is S3, following configs are needed:
s3Endpoint="http://192.168.xx.xx:9010"
s3AccessKey="xxxxxxxxxx"
s3SecretKey="xxxxxxxxxx"

# If ResourceManager supports HA, then input master and standby node IP or hostname, eg: '192.168.xx.xx,192.168.xx.xx'. Or else ResourceManager run in standalone mode, please set yarnHaIps="" and "" for not using yarn.
yarnHaIps="192.168.xx.xx,192.168.xx.xx"


# If ResourceManager runs in standalone, then set ResourceManager node ip or hostname, or else remain default.
singleYarnIp="yarnIp1"

# Storage path when using HDFS/S3
resourceUploadPath="/dolphinscheduler"


# HDFS/S3 root user
hdfsRootUser="hdfs"

# Followings are Kerberos configs

# Specify Kerberos enable or not
kerberosStartUp="false"

# Kdc krb5 config file path
krb5ConfPath="$installPath/conf/krb5.conf"

# Keytab username
keytabUserName="hdfs-mycluster@ESZ.COM"

# Username keytab path
keytabPath="$installPath/conf/hdfs.headless.keytab"


# API-service port
apiServerPort="12345"


# All hosts deploy DS
ips="ds1,ds2,ds3,ds4,ds5"

# Ssh port, default 22
sshPort="22"

# Master service hosts
masters="ds1,ds2"

# All hosts deploy worker service
# Note: Each worker needs to set a worker group name and default name is "default"
workers="ds1:default,ds2:default,ds3:default,ds4:default,ds5:default"

#  Host deploy alert-service
alertServer="ds3"

# Host deploy API-service
apiServers="ds1"
```

### dolphinscheduler_env.sh [load environment variables configs]

When using shell to commit tasks, DolphinScheduler will export environment variables from `bin/env/dolphinscheduler_env.sh`. The
mainly configuration including `JAVA_HOME`, mata database, registry center, and task configuration.

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

### Services logback configs

Services name| logback config name |
--|--|
API-service logback config |logback-api.xml|
master-service logback config|logback-master.xml |
worker-service logback config|logback-worker.xml |
alert-service logback config|logback-alert.xml |
