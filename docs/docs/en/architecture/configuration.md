<!-- markdown-link-check-disable -->

# Configuration

## Preface

This document explains the DolphinScheduler application configurations.

## Directory Structure

The directory structure of DolphinScheduler is as follows:

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
│   │   ├── bootstrap.yaml                      configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
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
│   │   ├── bootstrap.yaml                      configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
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
│   │   ├── bootstrap.yaml                      configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
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
│   │   ├── bootstrap.yaml                      configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
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
│       │   ├── bootstrap.yaml                  configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
│       │   ├── common.properties               configurations of common-service like storage, credentials, etc.
│       │   ├── dolphinscheduler_env.sh         script to load environment variables for worker-server
│       │   └── logback-spring.xml              configurations of worker-service log
│       └── libs                                directory of worker-server libs
│
└── ui                                          directory of front-end web resources
```

## Configurations in Details

### dolphinscheduler-daemon.sh [startup or shutdown DolphinScheduler application]

dolphinscheduler-daemon.sh is responsible for DolphinScheduler startup and shutdown.
Essentially, start-all.sh or stop-all.sh startup and shutdown the cluster via dolphinscheduler-daemon.sh.
Currently, DolphinScheduler just makes a basic config, remember to config further JVM options based on your practical situation of resources.

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

> "-XX:DisableExplicitGC" is not recommended due to may lead to memory link (DolphinScheduler dependent on Netty to communicate).

### Database connection related configuration

DolphinScheduler uses Spring Hikari to manage database connections, configuration file location:

|Service| Configuration file  |
|--|--|
|Master Server | `master-server/conf/application.yaml`|
|Api Server| `api-server/conf/application.yaml`|
|Worker Server| `worker-server/conf/application.yaml`|
|Alert Server| `alert-server/conf/application.yaml`|

The default configuration is as follows:

|Parameters | Default value| Description|
|--|--|--|
|spring.datasource.driver-class-name| org.postgresql.Driver |datasource driver|
|spring.datasource.url| jdbc:postgresql://127.0.0.1:5432/dolphinscheduler |datasource connection url|
|spring.datasource.username|root|datasource username|
|spring.datasource.password|root|datasource password|
|spring.datasource.hikari.connection-test-query|select 1|validate connection by running the SQL|
|spring.datasource.hikari.minimum-idle| 5| minimum connection pool size number|
|spring.datasource.hikari.auto-commit|true|whether auto commit|
|spring.datasource.hikari.pool-name|DolphinScheduler|name of the connection pool|
|spring.datasource.hikari.maximum-pool-size|50| maximum connection pool size number|
|spring.datasource.hikari.connection-timeout|30000|connection timeout|
|spring.datasource.hikari.idle-timeout|600000|Maximum idle connection survival time|
|spring.datasource.hikari.leak-detection-threshold|0|Connection leak detection threshold|
|spring.datasource.hikari.initialization-fail-timeout|1|Connection pool initialization failed timeout|

Note that DolphinScheduler also supports database configuration through `bin/env/dolphinscheduler_env.sh`.

### Zookeeper related configuration

DolphinScheduler uses Zookeeper for cluster management, fault tolerance, event monitoring and other functions. Configuration file location:
|Service| Configuration file  |
|--|--|
|Master Server | `master-server/conf/application.yaml`|
|Api Server| `api-server/conf/application.yaml`|
|Worker Server| `worker-server/conf/application.yaml`|

The default configuration is as follows:

|Parameters | Default value| Description|
|--|--|--|
|registry.zookeeper.namespace|dolphinscheduler|namespace of zookeeper|
|registry.zookeeper.connect-string|localhost:2181| the connection string of zookeeper|
|registry.zookeeper.retry-policy.base-sleep-time|60ms|time to wait between subsequent retries|
|registry.zookeeper.retry-policy.max-sleep|300ms|maximum time to wait between subsequent retries|
|registry.zookeeper.retry-policy.max-retries|5|maximum retry times|
|registry.zookeeper.session-timeout|30s|session timeout|
|registry.zookeeper.connection-timeout|30s|connection timeout|
|registry.zookeeper.block-until-connected|600ms|waiting time to block until the connection succeeds|
|registry.zookeeper.digest|{username}:{password}|digest of zookeeper to access znode, works only when acl is enabled, for more details please check [https://zookeeper.apache.org/doc/r3.4.14/zookeeperAdmin.html](Apache Zookeeper doc) |

Note that DolphinScheduler also supports zookeeper related configuration through `bin/env/dolphinscheduler_env.sh`.

### common.properties [hadoop、s3、yarn config properties]

Currently, common.properties mainly configures Hadoop,s3a related configurations. Configuration file location:

|Service| Configuration file  |
|--|--|
|Master Server | `master-server/conf/common.properties`|
|Api Server| `api-server/conf/common.properties`|
|Worker Server| `worker-server/conf/common.properties`|
|Alert Server| `alert-server/conf/common.properties`|

The default configuration is as follows:

| Parameters | Default value | Description |
|--|--|--|
|data.basedir.path | /tmp/dolphinscheduler | local directory used to store temp files|
|resource.storage.type | NONE | type of resource files: HDFS, S3, NONE|
|resource.upload.path | /dolphinscheduler | storage path of resource files|
|aws.access.key.id | minioadmin | access key id of S3|
|aws.secret.access.key | minioadmin | secret access key of S3|
|aws.region | us-east-1 | region of S3|
|aws.s3.endpoint | http://minio:9000 | endpoint of S3|
|hdfs.root.user | hdfs | configure users with corresponding permissions if storage type is HDFS|
|fs.defaultFS | hdfs://mycluster:8020 | If resource.storage.type=S3, then the request url would be similar to 's3a://dolphinscheduler'. Otherwise if resource.storage.type=HDFS and hadoop supports HA, copy core-site.xml and hdfs-site.xml into 'conf' directory|
|hadoop.security.authentication.startup.state | false | whether hadoop grant kerberos permission|
|java.security.krb5.conf.path | /opt/krb5.conf | kerberos config directory|
|login.user.keytab.username | hdfs-mycluster@ESZ.COM | kerberos username|
|login.user.keytab.path | /opt/hdfs.headless.keytab | kerberos user keytab|
|kerberos.expire.time | 2 | kerberos expire time,integer,the unit is hour|
|yarn.resourcemanager.ha.rm.ids | 192.168.xx.xx,192.168.xx.xx | specify the yarn resourcemanager url. if resourcemanager supports HA, input HA IP addresses (separated by comma), or input null for standalone|
|yarn.application.status.address | http://ds1:8088/ws/v1/cluster/apps/%s | keep default if ResourceManager supports HA or not use ResourceManager, or replace ds1 with corresponding hostname if ResourceManager in standalone mode|
|development.state | false | specify whether in development state|
|dolphin.scheduler.network.interface.preferred | NONE | display name of the network card|
|dolphin.scheduler.network.priority.strategy | default | IP acquisition strategy, give priority to finding the internal network or the external network|
|resource.manager.httpaddress.port | 8088 | the port of resource manager|
|yarn.job.history.status.address | http://ds1:19888/ws/v1/history/mapreduce/jobs/%s | job history status url of yarn|
|datasource.encryption.enable | false | whether to enable datasource encryption|
|datasource.encryption.salt | !@#$%^&* | the salt of the datasource encryption|
|data-quality.jar.name | dolphinscheduler-data-quality-dev-SNAPSHOT.jar | the jar of data quality|
|support.hive.oneSession | false | specify whether hive SQL is executed in the same session|
|sudo.enable | true | whether to enable sudo|
|alert.rpc.port | 50052 | the RPC port of Alert Server|
|zeppelin.rest.url | http://localhost:8080 | the RESTful API url of zeppelin|

### Api-server related configuration

Location: `api-server/conf/application.yaml`

|Parameters | Default value| Description|
|--|--|--|
|server.port|12345|api service communication port|
|server.servlet.session.timeout|120m|session timeout|
|server.servlet.context-path|/dolphinscheduler/ |request path|
|spring.servlet.multipart.max-file-size|1024MB|maximum file size|
|spring.servlet.multipart.max-request-size|1024MB|maximum request size|
|server.jetty.max-http-post-size|5000000|jetty maximum post size|
|spring.banner.charset|UTF-8|message encoding|
|spring.jackson.time-zone|UTC|time zone|
|spring.jackson.date-format|"yyyy-MM-dd HH:mm:ss"|time format|
|spring.messages.basename|i18n/messages|i18n config|
|security.authentication.type|PASSWORD|authentication type|
|security.authentication.ldap.user.admin|read-only-admin|admin user account when you log-in with LDAP|
|security.authentication.ldap.urls|ldap://ldap.forumsys.com:389/|LDAP urls|
|security.authentication.ldap.base.dn|dc=example,dc=com|LDAP base dn|
|security.authentication.ldap.username|cn=read-only-admin,dc=example,dc=com|LDAP username|
|security.authentication.ldap.password|password|LDAP password|
|security.authentication.ldap.user.identity.attribute|uid|LDAP user identity attribute|
|security.authentication.ldap.user.email.attribute|mail|LDAP user email attribute|
|traffic.control.global.switch|false|traffic control global switch|
|traffic.control.max-global-qps-rate|300|global max request number per second|
|traffic.control.tenant-switch|false|traffic control tenant switch|
|traffic.control.default-tenant-qps-rate|10|default tenant max request number per second|
|traffic.control.customize-tenant-qps-rate||customize tenant max request number per second|

### Master Server related configuration

Location: `master-server/conf/application.yaml`

|Parameters | Default value| Description|
|--|--|--|
|master.listen-port|5678|master listen port|
|master.fetch-command-num|10|the number of commands fetched by master|
|master.pre-exec-threads|10|master prepare execute thread number to limit handle commands in parallel|
|master.exec-threads|100|master execute thread number to limit process instances in parallel|
|master.dispatch-task-number|3|master dispatch task number per batch|
|master.host-selector|lower_weight|master host selector to select a suitable worker, default value: LowerWeight. Optional values include random, round_robin, lower_weight|
|master.heartbeat-interval|10|master heartbeat interval, the unit is second|
|master.task-commit-retry-times|5|master commit task retry times|
|master.task-commit-interval|1000|master commit task interval, the unit is millisecond|
|master.state-wheel-interval|5|time to check status|
|master.max-cpu-load-avg|-1|master max CPU load avg, only higher than the system CPU load average, master server can schedule. default value -1: the number of CPU cores * 2|
|master.reserved-memory|0.3|master reserved memory, only lower than system available memory, master server can schedule. default value 0.3, the unit is G|
|master.failover-interval|10|failover interval, the unit is minute|
|master.kill-yarn-job-when-task-failover|true|whether to kill yarn job when failover taskInstance|
|master.registry-disconnect-strategy.strategy|stop|Used when the master disconnect from registry, default value: stop. Optional values include stop, waiting|
|master.registry-disconnect-strategy.max-waiting-time|100s|Used when the master disconnect from registry, and the disconnect strategy is waiting, this config means the master will waiting to reconnect to registry in given times, and after the waiting times, if the master still cannot connect to registry, will stop itself, if the value is 0s, the Master will waitting infinitely|

### Worker Server related configuration

Location: `worker-server/conf/application.yaml`

|Parameters | Default value| Description|
|--|--|--|
|worker.listen-port|1234|worker-service listen port|
|worker.exec-threads|100|worker-service execute thread number, used to limit the number of task instances in parallel|
|worker.heartbeat-interval|10|worker-service heartbeat interval, the unit is second|
|worker.host-weight|100|worker host weight to dispatch tasks|
|worker.tenant-auto-create|true|tenant corresponds to the user of the system, which is used by the worker to submit the job. If system does not have this user, it will be automatically created after the parameter worker.tenant.auto.create is true.|
|worker.max-cpu-load-avg|-1|worker max CPU load avg, only higher than the system CPU load average, worker server can be dispatched tasks. default value -1: the number of CPU cores * 2|
|worker.reserved-memory|0.3|worker reserved memory, only lower than system available memory, worker server can be dispatched tasks. default value 0.3, the unit is G|
|worker.alert-listen-host|localhost|the alert listen host of worker|
|worker.alert-listen-port|50052|the alert listen port of worker|
|worker.registry-disconnect-strategy.strategy|stop|Used when the worker disconnect from registry, default value: stop. Optional values include stop, waiting|
|worker.registry-disconnect-strategy.max-waiting-time|100s|Used when the worker disconnect from registry, and the disconnect strategy is waiting, this config means the worker will waiting to reconnect to registry in given times, and after the waiting times, if the worker still cannot connect to registry, will stop itself, if the value is 0s, will waitting infinitely |
|worker.task-execute-threads-full-policy|REJECT|If REJECT, when the task waiting in the worker reaches exec-threads, it will reject the received task and the Master will redispatch it; If CONTINUE, it will put the task into the worker's execution queue and wait for a free thread to start execution|

### Alert Server related configuration

Location: `alert-server/conf/application.yaml`

|Parameters | Default value| Description|
|--|--|--|
|server.port|50053|the port of Alert Server|
|alert.port|50052|the port of alert|

### Quartz related configuration

This part describes quartz configs and configure them based on your practical situation and resources.

|Service| Configuration file  |
|--|--|
|Master Server | `master-server/conf/application.yaml`|
|Api Server| `api-server/conf/application.yaml`|

The default configuration is as follows:

|Parameters | Default value|
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

### dolphinscheduler_env.sh [load environment variables configs]

When using shell to commit tasks, DolphinScheduler will export environment variables from `bin/env/dolphinscheduler_env.sh`. The
mainly configuration including `JAVA_HOME` and other environment paths.

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

### Log related configuration

|Service| Configuration file  |
|--|--|
|Master Server | `master-server/conf/logback-spring.xml`|
|Api Server| `api-server/conf/logback-spring.xml`|
|Worker Server| `worker-server/conf/logback-spring.xml`|
|Alert Server| `alert-server/conf/logback-spring.xml`|
