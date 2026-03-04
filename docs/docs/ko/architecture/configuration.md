<!-- markdown-link-check-disable -->

# 구성

## 서문

이 문서에서는 DolphinScheduler 애플리케이션 구성을 설명합니다.

## 디렉토리 구조

DolphinScheduler의 디렉토리 구조는 다음과 같습니다.```
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
````

## 세부 구성

### DolphinScheduler-daemon.sh [DolphinScheduler 애플리케이션 시작 또는 종료]

DolphinScheduler-daemon.sh는 DolphinScheduler 시작 및 종료를 담당합니다.
기본적으로 start-all.sh 또는 stop-all.sh는 돌고래 스케줄러-daemon.sh를 통해 클러스터를 시작하고 종료합니다.
현재 DolphinScheduler는 기본 구성만 수행하므로 실제 상황에 따라 추가 JVM 옵션을 구성해야 합니다.
자원상황.

기본 단순화 매개변수는 다음과 같습니다.```bash
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
````

> "-XX:DisableExplicitGC"는 메모리 링크로 이어질 수 있으므로 권장되지 않습니다(Netty에 의존하는 DolphinScheduler는
> 의사소통).
> "-Djava.net.preferIPv6Addresses=true"를 추가하면 ipv6 주소를 사용하고, "-Djava.net.preferIPv4Addresses=true"를 추가하면
> ipv4 주소를 사용합니다. 설정하지 않으면 두 매개변수가 ipv4 또는 ipv6을 사용합니다.

### 데이터베이스 연결 관련 설정

DolphinScheduler는 Spring Hikari를 사용하여 데이터베이스 연결, 구성 파일 위치를 관리합니다.

|서비스 |구성 파일 |
|---------------|-------------|
|마스터 서버 |`마스터 서버/conf/application.yaml` |
|API 서버 |`api-server/conf/application.yaml` |
|작업자 서버 |`worker-server/conf/application.yaml` |
|경보 서버 |`경고 서버/conf/application.yaml` |

기본 구성은 다음과 같습니다.

|매개변수 |기본값 |설명 |
|------------------------------------------------------|-------------------------|-----------------------------------------------|
|spring.datasource.driver-클래스 이름 |org.postgresql.드라이버 |데이터 소스 드라이버 |
|spring.datasource.url |jdbc:postgresql://127.0.0.1:5432/dolphinscheduler |데이터 소스 연결 URL |
|spring.datasource.사용자 이름 |루트 |데이터 소스 사용자 이름 |
|spring.datasource.password |루트 |데이터 소스 비밀번호 |
|spring.datasource.hikari.connection-테스트-쿼리 |1개 선택 |SQL을 실행하여 연결을 검증 |
|spring.datasource.hikari.minimum-idle |5 |최소 연결 풀 크기 |
|spring.datasource.hikari.auto-커밋 |사실 |자동 커밋 여부 |
|spring.datasource.hikari.pool-name |돌핀스케줄러 |연결 풀의 이름 |
|spring.datasource.hikari.maximum-pool-size |50 |최대 연결 풀 크기 |
|spring.datasource.hikari.connection-timeout |30000 |연결 시간 초과 |
|spring.datasource.hikari.idle-timeout |600000 |최대 유휴 연결 생존 시간 |
|spring.datasource.hikari.leak-감지-임계값 |0 |연결 누출 감지 임계값 |
|spring.datasource.hikari.initialization-fail-timeout |1 |연결 풀 초기화 실패 시간 초과 |

DolphinScheduler는 `bin/env/dolphinscheduler_env.sh`를 통해 데이터베이스 구성도 지원합니다.

### 레지스트리 관련 구성

DolphinScheduler는 클러스터 관리, 내결함성, 이벤트 모니터링 및 기타 기능을 위해 Zookeeper를 사용합니다.
구성 파일 위치:
|서비스|구성 파일 |
|--|--|
|마스터 서버 |`master-server/conf/application.yaml`|
|API 서버|`api-server/conf/application.yaml`|
|작업자 서버|`worker-server/conf/application.yaml`|

기본 구성은 다음과 같습니다.|매개변수 |기본값 |설명 |
|-------------------------------------------------|------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
|레지스트리.zookeeper.namespace |돌고래 스케줄러 |사육사의 네임스페이스 |
|Registry.zookeeper.connect-string |로컬호스트:2181 |사육사의 연결 문자열 |
|Registry.zookeeper.retry-policy.base-sleep-time |60ms |후속 재시도 사이에 대기하는 시간 |
|Registry.zookeeper.retry-policy.max-sleep |300ms |후속 재시도 사이에 대기하는 최대 시간 |
|Registry.zookeeper.retry-policy.max-retries |5 |최대 재시도 횟수 |
|Registry.zookeeper.session-timeout |30대 |세션 시간 초과 |
|Registry.zookeeper.connection-timeout |30대 |연결 시간 초과 |
|Registry.zookeeper.block-until-connected |600ms |연결이 성공할 때까지 차단되는 대기 시간 |
|레지스트리.zookeeper.digest |{사용자 이름}:{비밀번호} |znode에 액세스하기 위한 Zookeeper 다이제스트는 acl이 활성화된 경우에만 작동합니다. 자세한 내용은 [https://zookeeper.apache.org/doc/r3.4.14/zookeeperAdmin.html](Apache Zookeeper 문서) |

DolphinScheduler는 `bin/env/dolphinscheduler_env.sh`를 통해 사육사 관련 구성도 지원합니다.

ETCD 레지스트리의 경우 자세한 내용을 참조하세요.
[링크](../guide/installation/registry-plugins/etcd.md)에서.
JDBC 레지스트리의 경우 자세한 내용을 참조하세요.
[링크](../guide/installation/registry-plugins/jdbc.md).

### common.properties [hadoop、s3、yarn 구성 속성]

현재 common.properties는 주로 Hadoop,s3a 관련 구성을 구성합니다.구성 파일 위치:

|서비스 |구성 파일 |
|---------------|---------------------------------------------|
|마스터 서버 |`마스터 서버/conf/common.properties` |
|API 서버 |`api-server/conf/common.properties`, `api-server/conf/aws.yaml` |
|작업자 서버 |`worker-server/conf/common.properties`, `worker-server/conf/aws.yaml` |
|경보 서버 |`경고 서버/conf/common.properties` |

기본 구성은 다음과 같습니다.|                  Parameters                   |                  Default value                   |                                                                                                                                                                                                             Description                                                                                                                                                                                                              |
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
| zeppelin.rest.url                             | http://localhost:8080                            | the RESTful API url of zeppelin                                                                                                                                                                                                                                                                                                                                                                                                      |
| appId.collect                                 | log                                              | way to collect applicationId, if use aop, alter the configuration from log to aop, annotation of applicationId auto collection related configuration in `bin/env/dolphinscheduler_env.sh` should be removed. Note: Aop way doesn't support submitting yarn job on remote host by client mode like Beeline, and will failure if override applicationId collection-related environment configuration in dolphinscheduler_env.sh, and . |### API 서버 관련 구성

위치: `api-server/conf/application.yaml`|                      Parameters                       |            Default value             |                                          Description                                           |
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
| api.traffic.control.customize-tenant-qps-rate         |                                      | customize tenant max request number per second                                                 |### 마스터 서버 관련 구성

위치: `master-server/conf/application.yaml`|매개변수 |기본값 |설명 |
|----------------------------------------------------------------|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
|master.listen-port |5678 |마스터 청취 포트 |
|master.logic-task-config.task-executor-thread-count |2 * CPU +1 |논리 작업을 실행하는 데 사용되는 스레드 크기 |
|master.worker-load-balancer-configuration-properties.type |DYNAMIC_WEIGHTED_ROUND_ROBIN |마스터는 작업자의 CPU/메모리/threadPool 사용량을 사용하여 작업자 로드를 계산합니다. 로드가 낮을수록 작업을 파견할 변경 사항이 더 많아집니다 |
|master.max-하트비트-간격 |10대 |마스터 최대 하트비트 간격 |
|master.server-load-protection.enabled |사실 |true로 설정되면 마스터 과부하 보호 |
|master.server-load-protection.max-system-cpu-usage-percentage-thresholds |0.8 |마스터 최대 시스템 CPU 사용량. 마스터의 시스템 CPU 사용량이 이 값보다 작으면 마스터 서버가 워크플로를 실행할 수 있습니다.|
|master.server-load-protection.max-jvm-cpu-usage-percentage-thresholds |0.8 |마스터 최대 JVM CPU 사용량, 마스터의 JVM CPU 사용량이 이 값보다 작으면 마스터 서버가 워크플로를 실행할 수 있습니다.|
|master.server-load-protection.max-system-memory-usage-percentage-thresholds |0.8 |마스터 최대 시스템 메모리 사용량, 마스터의 시스템 메모리 사용량이 이 값보다 작으면 마스터 서버가 워크플로를 실행할 수 있습니다.|
|master.server-load-protection.max-disk-usage-percentage-thresholds |0.8 |마스터 최대 디스크 사용량 - 마스터의 디스크 사용량이 이 값보다 작으면 마스터 서버가 워크플로를 실행할 수 있습니다.|
|master.server-load-protection.max-concurrent-workflow-instances |2147483647 |마스터 최대 동시 워크플로 인스턴스, 마스터의 워크플로 인스턴스 수가 이 값에 도달하거나 초과하면 마스터 서버가 사용 중인 것으로 표시됩니다.|
|master.worker-그룹-새로 고침-간격 |10대 |DB에서 메모리로 작업자 그룹을 새로 고치는 간격 |
|master.command-fetch-strategy.type |ID_SLOT_BASED |명령 가져오기 전략은 `ID_SLOT_BASED`만 지원 |
|master.command-fetch-strategy.config.id-step |1 |db에서 t_ds_command의 ID 자동 증분 단계 |
|master.command-fetch-strategy.config.fetch-size |10 |마스터가 가져온 명령 수 |### Worker Server 관련 구성

위치: `worker-server/conf/application.yaml`|                                 Parameters                                  | Default value |                                                                                                                                                    Description                                                                                                                                                    |
|-----------------------------------------------------------------------------|---------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| worker.listen-port                                                          | 1234          | worker-service listen port                                                                                                                                                                                                                                                                                        |
| worker.max-heartbeat-interval                                               | 10s           | worker-service max heartbeat interval                                                                                                                                                                                                                                                                             |
| worker.host-weight                                                          | 100           | worker host weight to dispatch tasks                                                                                                                                                                                                                                                                              |
| worker.server-load-protection.enabled                                       | true          | If set true will open worker overload protection                                                                                                                                                                                                                                                                  |
| worker.server-load-protection.max-system-cpu-usage-percentage-thresholds    | 0.8           | Worker max system cpu usage, when the worker's system cpu usage is smaller then this value, master server can execute workflow.                                                                                                                                                                                   |
| worker.server-load-protection.max-jvm-cpu-usage-percentage-thresholds       | 0.8           | Worker max JVM cpu usage, when the worker's jvm cpu usage is smaller then this value, master server can execute workflow.                                                                                                                                                                                         |
| worker.server-load-protection.max-system-memory-usage-percentage-thresholds | 0.8           | Worker max system memory usage , when the worker's system memory usage is smaller then this value, master server can execute workflow.                                                                                                                                                                            |
| worker.server-load-protection.max-disk-usage-percentage-thresholds          | 0.8           | Worker max disk usage , when the worker's disk usage is smaller then this value, master server can execute workflow.                                                                                                                                                                                              |
| worker.registry-disconnect-strategy.strategy                                | stop          | Used when the worker disconnect from registry, default value: stop. Optional values include stop, waiting                                                                                                                                                                                                         |
| worker.registry-disconnect-strategy.max-waiting-time                        | 100s          | Used when the worker disconnect from registry, and the disconnect strategy is waiting, this config means the worker will waiting to reconnect to registry in given times, and after the waiting times, if the worker still cannot connect to registry, will stop itself, if the value is 0s, will wait infinitely |
| worker.physical-task-config.task-executor-thread-size                       | 100           | The thread size used to execute physical task                                                                                                                                                                                                                                                                     |
| worker.tenant-config.auto-create-tenant-enabled                             | true          | tenant corresponds to the user of the system, which is used by the worker to submit the job. If system does not have this user, it will be automatically created after the parameter worker.tenant.auto.create is true.                                                                                           |
| worker.tenant-config.default-tenant-enabled                                 | false         | If set true, will use worker bootstrap user as the tenant to execute task when the tenant is `default`.                                                                                                                                                                                                           |### Alert Server 관련 구성

위치: `alert-server/conf/application.yaml`

|매개변수 |기본값 |설명 |
|---------------|---------------|-------------|
|서버.포트 |50053 |서버 포트 |
|경고.포트 |50052 |경계항 |

### Quartz 관련 구성

이 부분에서는 석영 구성을 설명하고 실제 상황과 리소스에 따라 구성합니다.

|서비스 |구성 파일 |
|---------------|-------------|
|마스터 서버 |`마스터 서버/conf/application.yaml` |
|API 서버 |`api-server/conf/application.yaml` |

기본 구성은 다음과 같습니다.

|매개변수 |기본값 |
|------------------------------------------------------------|-----------------------|
|spring.quartz.properties.org.quartz.jobStore.isClustered |사실 |
|spring.quartz.properties.org.quartz.jobStore.class |org.quartz.impl.jdbcjobstore.JobStoreTX |
|spring.quartz.properties.org.quartz.scheduler.instanceId |자동 |
|spring.quartz.properties.org.quartz.jobStore.tablePrefix |QRTZ_ |
|spring.quartz.properties.org.quartz.jobStore.acquireTriggersWithinLock |사실 |
|spring.quartz.properties.org.quartz.scheduler.instanceName |돌핀스케줄러 |
|spring.quartz.properties.org.quartz.jobStore.useProperties |거짓 |
|spring.quartz.properties.org.quartz.jobStore.misfireThreshold |60000 |
|spring.quartz.properties.org.quartz.scheduler.makeSchedulerThreadDaemon |사실 |
|spring.quartz.properties.org.quartz.jobStore.driverDelegateClass |org.quartz.impl.jdbcjobstore.PostgreSQLDelegate |
|spring.quartz.properties.org.quartz.jobStore.clusterCheckinInterval |5000 |

위의 구성 항목은 *마스터 서버*와 *Api 서버*에서 동일하지만 *Quartz Scheduler* 스레드 풀
구성이 다릅니다.

*마스터 서버*의 기본 석영 스레드 풀 구성은 다음과 같습니다.

|매개변수 |기본값 |
|------------------------------------------------------|----------------------|
|spring.quartz.properties.org.quartz.threadPool.makeThreadsDaemons |사실 |
|spring.quartz.properties.org.quartz.threadPool.threadCount |25 |
|spring.quartz.properties.org.quartz.threadPool.threadPriority |5 |
|spring.quartz.properties.org.quartz.threadPool.class |org.quartz.simpl.SimpleThreadPool |

*Api Server*는 *Quartz Scheduler* 인스턴스를 클라이언트로만 시작하지 않으므로 스레드 풀이 구성됩니다.
스레드가 0인 `QuartzZeroSizeThreadPool`로;
기본 구성은 다음과 같습니다.

|매개변수 |기본값 |
|------------------------------------------------------|---------------------------------------------------------|
|spring.quartz.properties.org.quartz.threadPool.class |org.apache.dolphinscheduler.scheduler.quartz.QuartzZeroSizeThreadPool |

### 돌핀스케줄러_env.sh [환경 변수 구성 로드]쉘을 사용하여 작업을 커밋할 때 DolphinScheduler는 환경 변수를 내보냅니다.
`bin/env/dolphinscheduler_env.sh`에서.는
주로 `JAVA_HOME` 및 기타 환경 경로를 포함한 구성입니다.```bash
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
````

### 로그 관련 구성

|서비스 |구성 파일 |
|---------------|----------------------------|
|마스터 서버 |`마스터 서버/conf/logback-spring.xml` |
|API 서버 |`api-server/conf/logback-spring.xml` |
|작업자 서버 |`작업자 서버/conf/logback-spring.xml` |
|경보 서버 |`경고 서버/conf/logback-spring.xml` |