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
├── bin                                         directory of DolphinScheduler application commands, configurations scripts
│   ├── dolphinscheduler-daemon.sh              script to start or shut down DolphinScheduler application
│   ├── env                                     directory of scripts to load environment variables
│   │   ├── dolphinscheduler_env.sh             script to export environment variables [eg: JAVA_HOME,HADOOP_HOME, HIVE_HOME ...] when you start or stop service using script `dolphinscheduler-daemon.sh`
│
├── alert-server                                directory of DolphinScheduler alert-server commands, configurations scripts and libs
│   ├── bin
│   │   └── start.sh                            script to start DolphinScheduler alert-server
│   │   └── jvm_args_env.sh                     script to set JVM args of DolphinScheduler alert-server
│   ├── conf
│   │   ├── application.yaml                    configurations of alert-server
│   │   ├── bootstrap.yaml                      configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
│   │   ├── common.properties                   configurations of common-service like storage, credentials, etc.
│   │   ├── dolphinscheduler_env.sh             script to load environment variables for alert-server
│   │   └── logback-spring.xml                  configurations of alert-service log
│   └── libs                                    directory of alert-server libs
│
├── api-server                                  directory of DolphinScheduler api-server commands, configurations scripts and libs
│   ├── bin
│   │   └── start.sh                            script to start DolphinScheduler api-server
│   │   └── jvm_args_env.sh                     script to set JVM args of DolphinScheduler api-server
│   ├── conf
│   │   ├── application.yaml                    configurations of api-server
│   │   ├── bootstrap.yaml                      configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
│   │   ├── common.properties                   configurations of common-service like storage, credentials, etc.
│   │   ├── dolphinscheduler_env.sh             script to load environment variables for api-server
│   │   └── logback-spring.xml                  configurations of api-service log
│   ├── libs                                    directory of api-server libs
│   └── ui                                      directory of api-server related front-end web resources
│
├── master-server                               directory of DolphinScheduler master-server commands, configurations scripts and libs
│   ├── bin
│   │   └── start.sh                            script to start DolphinScheduler master-server
│   │   └── jvm_args_env.sh                     script to set JVM args of DolphinScheduler master-server
│   ├── conf
│   │   ├── application.yaml                    configurations of master-server
│   │   ├── bootstrap.yaml                      configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
│   │   ├── common.properties                   configurations of common-service like storage, credentials, etc.
│   │   ├── dolphinscheduler_env.sh             script to load environment variables for master-server
│   │   └── logback-spring.xml                  configurations of master-service log
│   └── libs                                    directory of master-server libs
│
├── standalone-server                           directory of DolphinScheduler standalone-server commands, configurations scripts and libs
│   ├── bin
│   │   └── start.sh                            script to start DolphinScheduler standalone-server
│   │   └── jvm_args_env.sh                     script to set JVM args of DolphinScheduler standalone-server
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
├── tools                                       directory of DolphinScheduler metadata tools commands, configurations scripts and libs
│   ├── bin
│   │   └── upgrade-schema.sh                   script to initialize or upgrade DolphinScheduler metadata
│   ├── conf
│   │   ├── application.yaml                    configurations of tools
│   │   └── common.properties                   configurations of common-service like storage, credentials, etc.
│   ├── libs                                    directory of tool libs
│   └── sql                                     .sql files to create or upgrade DolphinScheduler metadata
│  
├── worker-server                               directory of DolphinScheduler worker-server commands, configurations scripts and libs
│   ├── bin
│   │   └── start.sh                        script to start DolphinScheduler worker-server
│   │   └── jvm_args_env.sh                 script to set JVM args of DolphinScheduler worker-server
│   ├── conf
│   │   ├── application.yaml                configurations of worker-server
│   │   ├── bootstrap.yaml                  configurations for Spring Cloud bootstrap, mostly you don't need to modify this,
│   │   ├── common.properties               configurations of common-service like storage, credentials, etc.
│   │   ├── dolphinscheduler_env.sh         script to load environment variables for worker-server
│   │   └── logback-spring.xml              configurations of worker-service log
│   └── libs                                directory of worker-server libs
│
└── ui                                          directory of front-end web resources
```

## Configurations in Details

### dolphinscheduler-daemon.sh [startup or shutdown DolphinScheduler application]

dolphinscheduler-daemon.sh is responsible for DolphinScheduler startup and shutdown.
Essentially, start-all.sh or stop-all.sh startup and shutdown the cluster via dolphinscheduler-daemon.sh.
Currently, DolphinScheduler just makes a basic config, remember to config further JVM options based on your practical
situation of resources.

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

> "-XX:DisableExplicitGC" is not recommended due to may lead to memory link (DolphinScheduler dependent on Netty to
> communicate).
> If add "-Djava.net.preferIPv6Addresses=true" will use ipv6 address, if add "-Djava.net.preferIPv4Addresses=true" will
> use ipv4 address, if doesn't set the two parameter will use ipv4 or ipv6.

### Database connection related configuration

DolphinScheduler uses Spring Hikari to manage database connections, configuration file location:

|    Service    |          Configuration file           |
|---------------|---------------------------------------|
| Master Server | `master-server/conf/application.yaml` |
| Api Server    | `api-server/conf/application.yaml`    |
| Worker Server | `worker-server/conf/application.yaml` |
| Alert Server  | `alert-server/conf/application.yaml`  |

The default configuration is as follows:

|                      Parameters                      |                   Default value                   |                  Description                  |
|------------------------------------------------------|---------------------------------------------------|-----------------------------------------------|
| spring.datasource.driver-class-name                  | org.postgresql.Driver                             | datasource driver                             |
| spring.datasource.url                                | jdbc:postgresql://127.0.0.1:5432/dolphinscheduler | datasource connection url                     |
| spring.datasource.username                           | root                                              | datasource username                           |
| spring.datasource.password                           | root                                              | datasource password                           |
| spring.datasource.hikari.connection-test-query       | select 1                                          | validate connection by running the SQL        |
| spring.datasource.hikari.minimum-idle                | 5                                                 | minimum connection pool size number           |
| spring.datasource.hikari.auto-commit                 | true                                              | whether auto commit                           |
| spring.datasource.hikari.pool-name                   | DolphinScheduler                                  | name of the connection pool                   |
| spring.datasource.hikari.maximum-pool-size           | 50                                                | maximum connection pool size number           |
| spring.datasource.hikari.connection-timeout          | 30000                                             | connection timeout                            |
| spring.datasource.hikari.idle-timeout                | 600000                                            | Maximum idle connection survival time         |
| spring.datasource.hikari.leak-detection-threshold    | 0                                                 | Connection leak detection threshold           |
| spring.datasource.hikari.initialization-fail-timeout | 1                                                 | Connection pool initialization failed timeout |

Note that DolphinScheduler also supports database configuration through `bin/env/dolphinscheduler_env.sh`.

### Registry Related configuration

DolphinScheduler uses Zookeeper for cluster management, fault tolerance, event monitoring and other functions.
Configuration file location:
|Service| Configuration file |
|--|--|
|Master Server | `master-server/conf/application.yaml`|
|Api Server| `api-server/conf/application.yaml`|
|Worker Server| `worker-server/conf/application.yaml`|

The default configuration is as follows:

|                   Parameters                    |     Default value     |                                                                                       Description                                                                                       |
|-------------------------------------------------|-----------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| registry.zookeeper.namespace                    | dolphinscheduler      | namespace of zookeeper                                                                                                                                                                  |
| registry.zookeeper.connect-string               | localhost:2181        | the connection string of zookeeper                                                                                                                                                      |
| registry.zookeeper.retry-policy.base-sleep-time | 60ms                  | time to wait between subsequent retries                                                                                                                                                 |
| registry.zookeeper.retry-policy.max-sleep       | 300ms                 | maximum time to wait between subsequent retries                                                                                                                                         |
| registry.zookeeper.retry-policy.max-retries     | 5                     | maximum retry times                                                                                                                                                                     |
| registry.zookeeper.session-timeout              | 30s                   | session timeout                                                                                                                                                                         |
| registry.zookeeper.connection-timeout           | 30s                   | connection timeout                                                                                                                                                                      |
| registry.zookeeper.block-until-connected        | 600ms                 | waiting time to block until the connection succeeds                                                                                                                                     |
| registry.zookeeper.digest                       | {username}:{password} | digest of zookeeper to access znode, works only when acl is enabled, for more details please check [https://zookeeper.apache.org/doc/r3.4.14/zookeeperAdmin.html](Apache Zookeeper doc) |

Note that DolphinScheduler also supports zookeeper related configuration through `bin/env/dolphinscheduler_env.sh`.

For ETCD Registry, please see more details
on [link](https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-etcd/README.md).
For JDBC Registry, please see more details
on [link](https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-jdbc/README.md).

### common.properties [hadoop、s3、yarn config properties]

Currently, common.properties mainly configures Hadoop,s3a related configurations. Configuration file location:

|    Service    |                          Configuration file                           |
|---------------|-----------------------------------------------------------------------|
| Master Server | `master-server/conf/common.properties`                                |
| Api Server    | `api-server/conf/common.properties`, `api-server/conf/aws.yaml`       |
| Worker Server | `worker-server/conf/common.properties`, `worker-server/conf/aws.yaml` |
| Alert Server  | `alert-server/conf/common.properties`                                 |

The default configuration is as follows:

|                  Parameters                   |                  Default value                   |                                                                                                                                                                                                             Description                                                                                                                                                                                                              |
|-----------------------------------------------|--------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| data.basedir.path                             | /tmp/dolphinscheduler                            | local directory used to store temp files                                                                                                                                                                                                                                                                                                                                                                                             |
| resource.storage.type                         | NONE                                             | type of resource files: HDFS, S3, OSS, GCS, ABS, NONE                                                                                                                                                                                                                                                                                                                                                                                |
| resource.upload.path                          | /dolphinscheduler                                | storage path of resource files                                                                                                                                                                                                                                                                                                                                                                                                       |
| hdfs.root.user                                | hdfs                                             | configure users with corresponding permissions if storage type is HDFS                                                                                                                                                                                                                                                                                                                                                               |
| fs.defaultFS                                  | hdfs://mycluster:8020                            | If resource.storage.type=S3, then the request url would be similar to 's3a://dolphinscheduler'. Otherwise if resource.storage.type=HDFS and hadoop supports HA, copy core-site.xml and hdfs-site.xml into 'conf' directory                                                                                                                                                                                                           |
| hadoop.security.authentication.startup.state  | false                                            | whether hadoop grant kerberos permission                                                                                                                                                                                                                                                                                                                                                                                             |
| java.security.krb5.conf.path                  | /opt/krb5.conf                                   | kerberos config directory                                                                                                                                                                                                                                                                                                                                                                                                            |
| login.user.keytab.username                    | hdfs-mycluster@ESZ.COM                           | kerberos username                                                                                                                                                                                                                                                                                                                                                                                                                    |
| login.user.keytab.path                        | /opt/hdfs.headless.keytab                        | kerberos user keytab                                                                                                                                                                                                                                                                                                                                                                                                                 |
| kerberos.expire.time                          | 2                                                | kerberos expire time,integer,the unit is hour                                                                                                                                                                                                                                                                                                                                                                                        |
| yarn.resourcemanager.ha.rm.ids                | 192.168.xx.xx,192.168.xx.xx                      | specify the yarn resourcemanager url. if resourcemanager supports HA, input HA IP addresses (separated by comma), or input null for standalone                                                                                                                                                                                                                                                                                       |
| yarn.application.status.address               | http://ds1:8088/ws/v1/cluster/apps/%s            | keep default if ResourceManager supports HA or not use ResourceManager, or replace ds1 with corresponding hostname if ResourceManager in standalone mode                                                                                                                                                                                                                                                                             |
| development.state                             | false                                            | specify whether in development state                                                                                                                                                                                                                                                                                                                                                                                                 |
| dolphin.scheduler.network.interface.preferred | NONE                                             | display name of the network card which will be used                                                                                                                                                                                                                                                                                                                                                                                  |
| dolphin.scheduler.network.interface.restrict  | docker0                                          | display name of the network card which shouldn't be used                                                                                                                                                                                                                                                                                                                                                                             |
| dolphin.scheduler.network.priority.strategy   | default                                          | IP acquisition strategy, give priority to finding the internal network or the external network                                                                                                                                                                                                                                                                                                                                       |
| resource.manager.httpaddress.port             | 8088                                             | the port of resource manager                                                                                                                                                                                                                                                                                                                                                                                                         |
| yarn.job.history.status.address               | http://ds1:19888/ws/v1/history/mapreduce/jobs/%s | job history status url of yarn                                                                                                                                                                                                                                                                                                                                                                                                       |
| datasource.encryption.enable                  | false                                            | whether to enable datasource encryption                                                                                                                                                                                                                                                                                                                                                                                              |
| datasource.encryption.salt                    | !@#$%^&*                                         | the salt of the datasource encryption                                                                                                                                                                                                                                                                                                                                                                                                |
| support.hive.oneSession                       | false                                            | specify whether hive SQL is executed in the same session                                                                                                                                                                                                                                                                                                                                                                             |
| sudo.enable                                   | true                                             | whether to enable sudo                                                                                                                                                                                                                                                                                                                                                                                                               |
| alert.rpc.port                                | 50052                                            | the RPC port of Alert Server                                                                                                                                                                                                                                                                                                                                                                                                         |
| zeppelin.rest.url                             | http://localhost:8080                            | the RESTful API url of zeppelin                                                                                                                                                                                                                                                                                                                                                                                                      |
| appId.collect                                 | log                                              | way to collect applicationId, if use aop, alter the configuration from log to aop, annotation of applicationId auto collection related configuration in `bin/env/dolphinscheduler_env.sh` should be removed. Note: Aop way doesn't support submitting yarn job on remote host by client mode like Beeline, and will failure if override applicationId collection-related environment configuration in dolphinscheduler_env.sh, and . |

### Api-server related configuration

Location: `api-server/conf/application.yaml`

|                      Parameters                       |            Default value             |                                          Description                                           |
|-------------------------------------------------------|--------------------------------------|------------------------------------------------------------------------------------------------|
| server.port                                           | 12345                                | api service communication port                                                                 |
| server.servlet.session.timeout                        | 120m                                 | session timeout                                                                                |
| server.servlet.context-path                           | /dolphinscheduler/                   | request path                                                                                   |
| spring.servlet.multipart.max-file-size                | 1024MB                               | maximum file size                                                                              |
| spring.servlet.multipart.max-request-size             | 1024MB                               | maximum request size                                                                           |
| server.jetty.max-http-post-size                       | 5000000                              | jetty maximum post size                                                                        |
| spring.banner.charset                                 | UTF-8                                | message encoding                                                                               |
| spring.jackson.time-zone                              | UTC                                  | time zone                                                                                      |
| spring.jackson.date-format                            | "yyyy-MM-dd HH:mm:ss"                | time format                                                                                    |
| spring.messages.basename                              | i18n/messages                        | i18n config                                                                                    |
| security.authentication.type                          | PASSWORD                             | authentication type                                                                            |
| security.authentication.ldap.user.admin               | read-only-admin                      | admin user account when you log-in with LDAP                                                   |
| security.authentication.ldap.urls                     | ldap://ldap.forumsys.com:389/        | LDAP urls                                                                                      |
| security.authentication.ldap.base.dn                  | dc=example,dc=com                    | LDAP base dn                                                                                   |
| security.authentication.ldap.username                 | cn=read-only-admin,dc=example,dc=com | LDAP username                                                                                  |
| security.authentication.ldap.password                 | password                             | LDAP password                                                                                  |
| security.authentication.ldap.user.identity-attribute  | uid                                  | LDAP user identity attribute                                                                   |
| security.authentication.ldap.user.email-attribute     | mail                                 | LDAP user email attribute                                                                      |
| security.authentication.ldap.user.not-exist-action    | CREATE                               | action when ldap user is not exist,default value: CREATE. Optional values include(CREATE,DENY) |
| security.authentication.ldap.ssl.enable               | false                                | LDAP ssl switch                                                                                |
| security.authentication.ldap.ssl.trust-store          | ldapkeystore.jks                     | LDAP jks file absolute path                                                                    |
| security.authentication.ldap.ssl.trust-store-password | password                             | LDAP jks password                                                                              |
| security.authentication.casdoor.user.admin            |                                      | admin user account when you log-in with Casdoor                                                |
| casdoor.endpoint                                      |                                      | Casdoor server url                                                                             |
| casdoor.client-id                                     |                                      | id in Casdoor                                                                                  |
| casdoor.client-secret                                 |                                      | secret in Casdoor                                                                              |
| casdoor.certificate                                   |                                      | certificate in Casdoor                                                                         |
| casdoor.organization-name                             |                                      | organization name in Casdoor                                                                   |
| casdoor.application-name                              |                                      | application name in Casdoor                                                                    |
| casdoor.redirect-url                                  |                                      | doplhinscheduler login url                                                                     |
| api.traffic.control.global.switch                     | false                                | traffic control global switch                                                                  |
| api.traffic.control.max-global-qps-rate               | 300                                  | global max request number per second                                                           |
| api.traffic.control.tenant-switch                     | false                                | traffic control tenant switch                                                                  |
| api.traffic.control.default-tenant-qps-rate           | 10                                   | default tenant max request number per second                                                   |
| api.traffic.control.customize-tenant-qps-rate         |                                      | customize tenant max request number per second                                                 |

### Master Server related configuration

Location: `master-server/conf/application.yaml`

|                                 Parameters                                  |        Default value         |                                                                    Description                                                                    |
|-----------------------------------------------------------------------------|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|
| master.listen-port                                                          | 5678                         | master listen port                                                                                                                                |
| master.logic-task-config.task-executor-thread-count                         | 2 * CPU +1                   | The thread size used to execute logic task                                                                                                        |
| master.worker-load-balancer-configuration-properties.type                   | DYNAMIC_WEIGHTED_ROUND_ROBIN | Master will use the worker's cpu/memory/threadPool usage to calculate the worker load, the lower load will have more change to be dispatched task |
| master.max-heartbeat-interval                                               | 10s                          | master max heartbeat interval                                                                                                                     |
| master.server-load-protection.enabled                                       | true                         | If set true, will open master overload protection                                                                                                 |
| master.server-load-protection.max-system-cpu-usage-percentage-thresholds    | 0.7                          | Master max system cpu usage, when the master's system cpu usage is smaller then this value, master server can execute workflow.                   |
| master.server-load-protection.max-jvm-cpu-usage-percentage-thresholds       | 0.7                          | Master max JVM cpu usage, when the master's jvm cpu usage is smaller then this value, master server can execute workflow.                         |
| master.server-load-protection.max-system-memory-usage-percentage-thresholds | 0.7                          | Master max system memory usage , when the master's system memory usage is smaller then this value, master server can execute workflow.            |
| master.server-load-protection.max-disk-usage-percentage-thresholds          | 0.7                          | Master max disk usage , when the master's disk usage is smaller then this value, master server can execute workflow.                              |
| master.worker-group-refresh-interval                                        | 10s                          | The interval to refresh worker group from db to memory                                                                                            |
| master.command-fetch-strategy.type                                          | ID_SLOT_BASED                | The command fetch strategy, only support `ID_SLOT_BASED`                                                                                          |
| master.command-fetch-strategy.config.id-step                                | 1                            | The id auto incremental step of t_ds_command in db                                                                                                |
| master.command-fetch-strategy.config.fetch-size                             | 10                           | The number of commands fetched by master                                                                                                          |

### Worker Server related configuration

Location: `worker-server/conf/application.yaml`

|                                 Parameters                                  | Default value |                                                                                                                                                    Description                                                                                                                                                    |
|-----------------------------------------------------------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| worker.listen-port                                                          | 1234          | worker-service listen port                                                                                                                                                                                                                                                                                        |
| worker.max-heartbeat-interval                                               | 10s           | worker-service max heartbeat interval                                                                                                                                                                                                                                                                             |
| worker.host-weight                                                          | 100           | worker host weight to dispatch tasks                                                                                                                                                                                                                                                                              |
| worker.server-load-protection.enabled                                       | true          | If set true will open worker overload protection                                                                                                                                                                                                                                                                  |
| worker.server-load-protection.max-system-cpu-usage-percentage-thresholds    | 0.7           | Worker max system cpu usage, when the worker's system cpu usage is smaller then this value, master server can execute workflow.                                                                                                                                                                                   |
| worker.server-load-protection.max-jvm-cpu-usage-percentage-thresholds       | 0.7           | Worker max JVM cpu usage, when the worker's jvm cpu usage is smaller then this value, master server can execute workflow.                                                                                                                                                                                         |
| worker.server-load-protection.max-system-memory-usage-percentage-thresholds | 0.7           | Worker max system memory usage , when the worker's system memory usage is smaller then this value, master server can execute workflow.                                                                                                                                                                            |
| worker.server-load-protection.max-disk-usage-percentage-thresholds          | 0.7           | Worker max disk usage , when the worker's disk usage is smaller then this value, master server can execute workflow.                                                                                                                                                                                              |
| worker.registry-disconnect-strategy.strategy                                | stop          | Used when the worker disconnect from registry, default value: stop. Optional values include stop, waiting                                                                                                                                                                                                         |
| worker.registry-disconnect-strategy.max-waiting-time                        | 100s          | Used when the worker disconnect from registry, and the disconnect strategy is waiting, this config means the worker will waiting to reconnect to registry in given times, and after the waiting times, if the worker still cannot connect to registry, will stop itself, if the value is 0s, will wait infinitely |
| worker.physical-task-config.task-executor-thread-size                       | 100           | The thread size used to execute physical task                                                                                                                                                                                                                                                                     |
| worker.tenant-config.auto-create-tenant-enabled                             | true          | tenant corresponds to the user of the system, which is used by the worker to submit the job. If system does not have this user, it will be automatically created after the parameter worker.tenant.auto.create is true.                                                                                           |
| worker.tenant-config.default-tenant-enabled                                 | false         | If set true, will use worker bootstrap user as the tenant to execute task when the tenant is `default`.                                                                                                                                                                                                           |

### Alert Server related configuration

Location: `alert-server/conf/application.yaml`

| Parameters  | Default value |       Description        |
|-------------|---------------|--------------------------|
| server.port | 50053         | the port of Alert Server |
| alert.port  | 50052         | the port of alert        |

### Quartz related configuration

This part describes quartz configs and configure them based on your practical situation and resources.

|    Service    |          Configuration file           |
|---------------|---------------------------------------|
| Master Server | `master-server/conf/application.yaml` |
| Api Server    | `api-server/conf/application.yaml`    |

The default configuration is as follows:

|                               Parameters                                |                  Default value                  |
|-------------------------------------------------------------------------|-------------------------------------------------|
| spring.quartz.properties.org.quartz.jobStore.isClustered                | true                                            |
| spring.quartz.properties.org.quartz.jobStore.class                      | org.quartz.impl.jdbcjobstore.JobStoreTX         |
| spring.quartz.properties.org.quartz.scheduler.instanceId                | AUTO                                            |
| spring.quartz.properties.org.quartz.jobStore.tablePrefix                | QRTZ_                                           |
| spring.quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock  | true                                            |
| spring.quartz.properties.org.quartz.scheduler.instanceName              | DolphinScheduler                                |
| spring.quartz.properties.org.quartz.jobStore.useProperties              | false                                           |
| spring.quartz.properties.org.quartz.jobStore.misfireThreshold           | 60000                                           |
| spring.quartz.properties.org.quartz.scheduler.makeSchedulerThreadDaemon | true                                            |
| spring.quartz.properties.org.quartz.jobStore.driverDelegateClass        | org.quartz.impl.jdbcjobstore.PostgreSQLDelegate |
| spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval     | 5000                                            |

The above configuration items is the same in *Master Server* and *Api Server*, but their *Quartz Scheduler* threadpool
configuration is different.

The default quartz threadpool configuration in *Master Server* is as follows:

|                            Parameters                             |           Default value           |
|-------------------------------------------------------------------|-----------------------------------|
| spring.quartz.properties.org.quartz.threadPool.makeThreadsDaemons | true                              |
| spring.quartz.properties.org.quartz.threadPool.threadCount        | 25                                |
| spring.quartz.properties.org.quartz.threadPool.threadPriority     | 5                                 |
| spring.quartz.properties.org.quartz.threadPool.class              | org.quartz.simpl.SimpleThreadPool |

Since *Api Server* will not start *Quartz Scheduler* instance, as a client only, therefore it's threadpool is configured
as `QuartzZeroSizeThreadPool` which has zero thread;
The default configuration is as follows:

|                      Parameters                      |                             Default value                             |
|------------------------------------------------------|-----------------------------------------------------------------------|
| spring.quartz.properties.org.quartz.threadPool.class | org.apache.dolphinscheduler.scheduler.quartz.QuartzZeroSizeThreadPool |

### dolphinscheduler_env.sh [load environment variables configs]

When using shell to commit tasks, DolphinScheduler will export environment variables
from `bin/env/dolphinscheduler_env.sh`. The
mainly configuration including `JAVA_HOME` and other environment paths.

```bash
# JAVA_HOME, will use it to start DolphinScheduler server
export JAVA_HOME=${JAVA_HOME:-/opt/soft/java}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/opt/soft/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/opt/soft/hadoop/etc/hadoop}
export SPARK_HOME=${SPARK_HOME:-/opt/soft/spark}
export PYTHON_LAUNCHER=${PYTHON_LAUNCHER:-/opt/soft/python/bin/python3}
export HIVE_HOME=${HIVE_HOME:-/opt/soft/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_LAUNCHER=${DATAX_LAUNCHER:-/opt/soft/datax/bin/datax.py}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_LAUNCHER:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_LAUNCHER:$PATH

# applicationId auto collection related configuration, the following configurations are unnecessary if setting appId.collect=log
export HADOOP_CLASSPATH=`hadoop classpath`:${DOLPHINSCHEDULER_HOME}/tools/libs/*
export SPARK_DIST_CLASSPATH=$HADOOP_CLASSPATH:$SPARK_DIST_CLASS_PATH
export HADOOP_CLIENT_OPTS="-javaagent:${DOLPHINSCHEDULER_HOME}/tools/libs/aspectjweaver-1.9.7.jar":$HADOOP_CLIENT_OPTS
export SPARK_SUBMIT_OPTS="-javaagent:${DOLPHINSCHEDULER_HOME}/tools/libs/aspectjweaver-1.9.7.jar":$SPARK_SUBMIT_OPTS
export FLINK_ENV_JAVA_OPTS="-javaagent:${DOLPHINSCHEDULER_HOME}/tools/libs/aspectjweaver-1.9.7.jar":$FLINK_ENV_JAVA_OPTS
```

### Log related configuration

|    Service    |           Configuration file            |
|---------------|-----------------------------------------|
| Master Server | `master-server/conf/logback-spring.xml` |
| Api Server    | `api-server/conf/logback-spring.xml`    |
| Worker Server | `worker-server/conf/logback-spring.xml` |
| Alert Server  | `alert-server/conf/logback-spring.xml`  |

