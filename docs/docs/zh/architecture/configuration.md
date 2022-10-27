<!-- markdown-link-check-disable -->

# 前言

本文档为dolphinscheduler配置文件说明文档。

# 目录结构

DolphinScheduler的目录结构如下：

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
│   │   ├── bootstrap.yaml                      Spring Cloud 启动阶段配置文件, 通常不需要修改
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
│   │   ├── bootstrap.yaml                      Spring Cloud 启动阶段配置文件, 通常不需要修改
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
│   │   ├── bootstrap.yaml                      Spring Cloud 启动阶段配置文件, 通常不需要修改
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
│   │   ├── bootstrap.yaml                      Spring Cloud 启动阶段配置文件, 通常不需要修改
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
│       │   ├── bootstrap.yaml                  Spring Cloud 启动阶段配置文件, 通常不需要修改
│       │   ├── common.properties               公共服务（存储等信息）配置文件
│       │   ├── dolphinscheduler_env.sh         worker-server环境变量配置加载脚本
│       │   └── logback-spring.xml              worker-service日志配置文件
│       └── libs                                worker-server依赖jar包存放目录
│
└── ui                                          前端WEB资源目录
```

# 配置文件详解

## dolphinscheduler-daemon.sh [启动/关闭DolphinScheduler服务脚本]

dolphinscheduler-daemon.sh脚本负责DolphinScheduler的启动&关闭.
start-all.sh/stop-all.sh最终也是通过dolphinscheduler-daemon.sh对集群进行启动/关闭操作.
目前DolphinScheduler只是做了一个基本的设置,JVM参数请根据各自资源的实际情况自行设置.

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

> 不建议设置"-XX:DisableExplicitGC" , DolphinScheduler使用Netty进行通讯,设置该参数,可能会导致内存泄漏.

## 数据库连接相关配置

在DolphinScheduler中使用Spring Hikari对数据库连接进行管理，配置文件位置：

|服务名称| 配置文件 |
|--|--|
|Master Server | `master-server/conf/application.yaml`|
|Api Server| `api-server/conf/application.yaml`|
|Worker Server| `worker-server/conf/application.yaml`|
|Alert Server| `alert-server/conf/application.yaml`|

默认配置如下：

|参数 | 默认值| 描述|
|--|--|--|
|spring.datasource.driver-class-name| org.postgresql.Driver |数据库驱动|
|spring.datasource.url| jdbc:postgresql://127.0.0.1:5432/dolphinscheduler |数据库连接地址|
|spring.datasource.username|root|数据库用户名|
|spring.datasource.password|root|数据库密码|
|spring.datasource.hikari.connection-test-query|select 1|检测连接是否有效的sql|
|spring.datasource.hikari.minimum-idle| 5|最小空闲连接池数量|
|spring.datasource.hikari.auto-commit|true|是否自动提交|
|spring.datasource.hikari.pool-name|DolphinScheduler|连接池名称|
|spring.datasource.hikari.maximum-pool-size|50|连接池最大连接数|
|spring.datasource.hikari.connection-timeout|30000|连接超时时长|
|spring.datasource.hikari.idle-timeout|600000|空闲连接存活最大时间|
|spring.datasource.hikari.leak-detection-threshold|0|连接泄露检测阈值|
|spring.datasource.hikari.initialization-fail-timeout|1|连接池初始化失败timeout|

DolphinScheduler同样可以通过设置环境变量进行数据库连接相关的配置, 将以上小写字母转成大写并把`.`换成`_`作为环境变量名, 设置值即可。

## Zookeeper相关配置

DolphinScheduler使用Zookeeper进行集群管理、容错、事件监听等功能，配置文件位置：
|服务名称| 配置文件 |
|--|--|
|Master Server | `master-server/conf/application.yaml`|
|Api Server| `api-server/conf/application.yaml`|
|Worker Server| `worker-server/conf/application.yaml`|

默认配置如下：

|参数 |默认值| 描述|
|--|--|--|
|registry.zookeeper.namespace|dolphinscheduler|Zookeeper集群使用的namespace|
|registry.zookeeper.connect-string|localhost:2181| Zookeeper集群连接信息|
|registry.zookeeper.retry-policy.base-sleep-time|60ms|基本重试时间差|
|registry.zookeeper.retry-policy.max-sleep|300ms|最大重试时间|
|registry.zookeeper.retry-policy.max-retries|5|最大重试次数|
|registry.zookeeper.session-timeout|30s|session超时时间|
|registry.zookeeper.connection-timeout|30s|连接超时时间|
|registry.zookeeper.block-until-connected|600ms|阻塞直到连接成功的等待时间|
|registry.zookeeper.digest|{用户名:密码}|如果zookeeper打开了acl，则需要填写认证信息访问znode，认证信息格式为{用户名}:{密码}。关于Zookeeper ACL详见[https://zookeeper.apache.org/doc/r3.4.14/zookeeperAdmin.html](Apache Zookeeper官方文档)|

DolphinScheduler同样可以通过`bin/env/dolphinscheduler_env.sh`进行Zookeeper相关的配置。

## common.properties [hadoop、s3、yarn配置]

common.properties配置文件目前主要是配置hadoop/s3/yarn相关的配置，配置文件位置：
|服务名称| 配置文件 |
|--|--|
|Master Server | `master-server/conf/common.properties`|
|Api Server| `api-server/conf/common.properties`|
|Worker Server| `worker-server/conf/common.properties`|
|Alert Server| `alert-server/conf/common.properties`|

默认配置如下：

| 参数 | 默认值 | 描述 |
|--|--|--|
|data.basedir.path | /tmp/dolphinscheduler | 本地工作目录,用于存放临时文件|
|resource.storage.type | NONE | 资源文件存储类型: HDFS,S3,NONE|
|resource.upload.path | /dolphinscheduler | 资源文件存储路径|
|aws.access.key.id | minioadmin | S3 access key|
|aws.secret.access.key | minioadmin | S3 secret access key|
|aws.region | us-east-1 | S3 区域|
|aws.s3.endpoint | http://minio:9000 | S3 endpoint地址|
|hdfs.root.user | hdfs | 如果存储类型为HDFS,需要配置拥有对应操作权限的用户|
|fs.defaultFS | hdfs://mycluster:8020 | 请求地址如果resource.storage.type=S3,该值类似为: s3a://dolphinscheduler. 如果resource.storage.type=HDFS, 如果 hadoop 配置了 HA,需要复制core-site.xml 和 hdfs-site.xml 文件到conf目录|
|hadoop.security.authentication.startup.state | false | hadoop是否开启kerberos权限|
|java.security.krb5.conf.path | /opt/krb5.conf | kerberos配置目录|
|login.user.keytab.username | hdfs-mycluster@ESZ.COM | kerberos登录用户|
|login.user.keytab.path | /opt/hdfs.headless.keytab | kerberos登录用户keytab|
|kerberos.expire.time | 2 | kerberos过期时间,整数,单位为小时|
|yarn.resourcemanager.ha.rm.ids | 192.168.xx.xx,192.168.xx.xx | yarn resourcemanager 地址, 如果resourcemanager开启了HA, 输入HA的IP地址(以逗号分隔),如果resourcemanager为单节点, 该值为空即可|
|yarn.application.status.address | http://ds1:8088/ws/v1/cluster/apps/%s | 如果resourcemanager开启了HA或者没有使用resourcemanager,保持默认值即可. 如果resourcemanager为单节点,你需要将ds1 配置为resourcemanager对应的hostname|
|development.state | false | 是否处于开发模式|
|dolphin.scheduler.network.interface.preferred | NONE | 网卡名称|
|dolphin.scheduler.network.priority.strategy | default | ip获取策略 default优先获取内网|
|resource.manager.httpaddress.port | 8088 | resource manager的端口|
|yarn.job.history.status.address | http://ds1:19888/ws/v1/history/mapreduce/jobs/%s | yarn的作业历史状态URL|
|datasource.encryption.enable | false | 是否启用datasource 加密|
|datasource.encryption.salt | !@#$%^&* | datasource加密使用的salt|
|data-quality.jar.name | dolphinscheduler-data-quality-dev-SNAPSHOT.jar | 配置数据质量使用的jar包|
|support.hive.oneSession | false | 设置hive SQL是否在同一个session中执行|
|sudo.enable | true | 是否开启sudo|
|alert.rpc.port | 50052 | Alert Server的RPC端口|
|zeppelin.rest.url | http://localhost:8080 | zeppelin RESTful API 接口地址|

## Api-server相关配置

位置：`api-server/conf/application.yaml`
|参数 |默认值| 描述|
|--|--|--|
|server.port|12345|api服务通讯端口|
|server.servlet.session.timeout|120m|session超时时间|
|server.servlet.context-path|/dolphinscheduler/ |请求路径|
|spring.servlet.multipart.max-file-size|1024MB|最大上传文件大小|
|spring.servlet.multipart.max-request-size|1024MB|最大请求大小|
|server.jetty.max-http-post-size|5000000|jetty服务最大发送请求大小|
|spring.banner.charset|UTF-8|请求编码|
|spring.jackson.time-zone|UTC|设置时区|
|spring.jackson.date-format|"yyyy-MM-dd HH:mm:ss"|设置时间格式|
|spring.messages.basename|i18n/messages|i18n配置|
|security.authentication.type|PASSWORD|权限校验类型|
|security.authentication.ldap.user.admin|read-only-admin|LDAP登陆时，系统管理员账号|
|security.authentication.ldap.urls|ldap://ldap.forumsys.com:389/|LDAP urls|
|security.authentication.ldap.base.dn|dc=example,dc=com|LDAP base dn|
|security.authentication.ldap.username|cn=read-only-admin,dc=example,dc=com|LDAP账号|
|security.authentication.ldap.password|password|LDAP密码|
|security.authentication.ldap.user.identity.attribute|uid|LDAP用户身份标识字段名|
|security.authentication.ldap.user.email.attribute|mail|LDAP邮箱字段名|
|traffic.control.global.switch|false|流量控制全局开关|
|traffic.control.max-global-qps-rate|300|全局最大请求数/秒|
|traffic.control.tenant-switch|false|流量控制租户开关|
|traffic.control.default-tenant-qps-rate|10|默认租户最大请求数/秒限制|
|traffic.control.customize-tenant-qps-rate||自定义租户最大请求数/秒限制|

## Master Server相关配置

位置：`master-server/conf/application.yaml`
|参数 |默认值| 描述|
|--|--|--|
|master.listen-port|5678|master监听端口|
|master.fetch-command-num|10|master拉取command数量|
|master.pre-exec-threads|10|master准备执行任务的数量，用于限制并行的command|
|master.exec-threads|100|master工作线程数量,用于限制并行的流程实例数量|
|master.dispatch-task-number|3|master每个批次的派发任务数量|
|master.host-selector|lower_weight|master host选择器,用于选择合适的worker执行任务,可选值: random, round_robin, lower_weight|
|master.heartbeat-interval|10|master心跳间隔,单位为秒|
|master.task-commit-retry-times|5|任务重试次数|
|master.task-commit-interval|1000|任务提交间隔,单位为毫秒|
|master.state-wheel-interval|5|轮询检查状态时间|
|master.max-cpu-load-avg|-1|master最大cpuload均值,只有高于系统cpuload均值时,master服务才能调度任务. 默认值为-1: cpu cores * 2|
|master.reserved-memory|0.3|master预留内存,只有低于系统可用内存时,master服务才能调度任务,单位为G|
|master.failover-interval|10|failover间隔，单位为分钟|
|master.kill-yarn-job-when-task-failover|true|当任务实例failover时，是否kill掉yarn job|
|master.registry-disconnect-strategy.strategy|stop|当Master与注册中心失联之后采取的策略, 默认值是: stop. 可选值包括： stop, waiting|
|master.registry-disconnect-strategy.max-waiting-time|100s|当Master与注册中心失联之后重连时间, 之后当strategy为waiting时，该值生效。 该值表示当Master与注册中心失联时会在给定时间之内进行重连, 在给定时间之内重连失败将会停止自己，在重连时，Master会丢弃目前正在执行的工作流，值为0表示会无限期等待 |

## Worker Server相关配置

位置：`worker-server/conf/application.yaml`
|参数 |默认值| 描述|
|--|--|--|
|worker.listen-port|1234|worker监听端口|
|worker.exec-threads|100|worker工作线程数量,用于限制并行的任务实例数量|
|worker.heartbeat-interval|10|worker心跳间隔,单位为秒|
|worker.host-weight|100|派发任务时，worker主机的权重|
|worker.tenant-auto-create|true|租户对应于系统的用户,由worker提交作业.如果系统没有该用户,则在参数worker.tenant.auto.create为true后自动创建。|
|worker.max-cpu-load-avg|-1|worker最大cpuload均值,只有高于系统cpuload均值时,worker服务才能被派发任务. 默认值为-1: cpu cores * 2|
|worker.reserved-memory|0.3|worker预留内存,只有低于系统可用内存时,worker服务才能被派发任务,单位为G|
|worker.alert-listen-host|localhost|alert监听host|
|worker.alert-listen-port|50052|alert监听端口|
|worker.registry-disconnect-strategy.strategy|stop|当Worker与注册中心失联之后采取的策略, 默认值是: stop. 可选值包括： stop, waiting|
|worker.registry-disconnect-strategy.max-waiting-time|100s|当Worker与注册中心失联之后重连时间, 之后当strategy为waiting时，该值生效。 该值表示当Worker与注册中心失联时会在给定时间之内进行重连, 在给定时间之内重连失败将会停止自己，在重连时，Worker会丢弃kill正在执行的任务。值为0表示会无限期等待 |
|worker.task-execute-threads-full-policy|REJECT|如果是 REJECT, 当Worker中等待队列中的任务数达到exec-threads时, Worker将会拒绝接下来新接收的任务，Master将会重新分发该任务; 如果是 CONTINUE, Worker将会接收任务，放入等待队列中等待空闲线程去执行该任务|

## Alert Server相关配置

位置：`alert-server/conf/application.yaml`
|参数 |默认值| 描述|
|--|--|--|
|server.port|50053|Alert Server监听端口|
|alert.port|50052|alert监听端口|

## Quartz相关配置

这里面主要是quartz配置,请结合实际业务场景&资源进行配置,本文暂时不做展开，配置文件位置：

|服务名称| 配置文件 |
|--|--|
|Master Server | `master-server/conf/application.yaml`|
|Api Server| `api-server/conf/application.yaml`|

默认配置如下：

| 参数 | 默认值 |
|--|--|
|spring.quartz.properties.org.quartz.threadPool.threadPriority | 5|
|spring.quartz.properties.org.quartz.jobStore.isClustered | true|
|spring.quartz.properties.org.quartz.jobStore.class | org.quartz.impl.jdbcjobstore.JobStoreTX|
|spring.quartz.properties.org.quartz.scheduler.instanceId | AUTO|
|spring.quartz.properties.org.quartz.jobStore.tablePrefix | QRTZ_|
|spring.quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock|true|
|spring.quartz.properties.org.quartz.scheduler.instanceName | DolphinScheduler|
|spring.quartz.properties.org.quartz.threadPool.class | org.quartz.simpl.SimpleThreadPool|
|spring.quartz.properties.org.quartz.jobStore.useProperties | false|
|spring.quartz.properties.org.quartz.threadPool.makeThreadsDaemons | true|
|spring.quartz.properties.org.quartz.threadPool.threadCount | 25|
|spring.quartz.properties.org.quartz.jobStore.misfireThreshold | 60000|
|spring.quartz.properties.org.quartz.scheduler.makeSchedulerThreadDaemon | true|
|spring.quartz.properties.org.quartz.jobStore.driverDelegateClass | org.quartz.impl.jdbcjobstore.PostgreSQLDelegate|
|spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval | 5000|

## dolphinscheduler_env.sh [环境变量配置]

通过类似shell方式提交任务的的时候，会加载该配置文件中的环境变量到主机中。涉及到的 `JAVA_HOME` 任务类型的环境配置，其中任务类型主要有: Shell任务、Python任务、Spark任务、Flink任务、Datax任务等等。

```bash
# JAVA_HOME, will use it to start DolphinScheduler server
export JAVA_HOME=${JAVA_HOME:-/opt/soft/java}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/opt/soft/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/opt/soft/hadoop/etc/hadoop}
export SPARK_HOME=${SPARK_HOME:-/opt/soft/spark}
export PYTHON_HOME=${PYTHON_HOME:-/opt/soft/python}
export HIVE_HOME=${HIVE_HOME:-/opt/soft/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_HOME=${DATAX_HOME:-/opt/soft/datax}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_HOME/bin:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_HOME/bin:$PATH
```

## 日志相关配置

|服务名称| 配置文件 |
|--|--|
|Master Server | `master-server/conf/logback-spring.xml`|
|Api Server| `api-server/conf/logback-spring.xml`|
|Worker Server| `worker-server/conf/logback-spring.xml`|
|Alert Server| `alert-server/conf/logback-spring.xml`|
