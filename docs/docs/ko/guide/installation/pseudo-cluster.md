# 의사 클러스터 배포

의사 클러스터 배포의 목적은 DolphinScheduler 서비스를 단일 시스템에 배포하는 것입니다.이 모드에서는 DolphinScheduler의 마스터, 작업자, API 서버가 모두 동일한 시스템에 있습니다.

DolphinScheduler 기능을 처음 접하고 싶다면 [Standalone 배포](standalone.md)를 따라 설치하는 것이 좋습니다.보다 완전한 기능을 경험하고 대규모 작업을 예약하려면 후속 의사 클러스터 배포를 설치하는 것이 좋습니다.DolphinScheduler를 프로덕션 환경에 배포하려면 [클러스터 배포](cluster.md) 또는 [Kubernetes 배포](kubernetes.md)를 따르는 것이 좋습니다.

## 준비

DolphinScheduler의 의사 클러스터 배포에는 외부 소프트웨어 지원이 필요합니다.

- JDK: [JDK][jdk](1.8 또는 11)를 다운로드하고 `JAVA_HOME` 환경 변수를 설치 및 구성한 다음 `bin` 디렉토리(`JAVA_HOME`에 포함됨)를 `PATH` 변수에 추가합니다.사용자 환경에 이미 있는 경우 이 단계를 건너뛸 수 있습니다.
- 바이너리 패키지: [다운로드 페이지](https://dolphinscheduler.apache.org/en-us/download)에서 DolphinScheduler 바이너리 패키지를 다운로드하세요.
- 데이터베이스: [PostgreSQL](https://www.postgresql.org/download/)(8.2.15+) 또는 [MySQL](https://dev.mysql.com/downloads/mysql/)(5.7+), 둘 중 하나를 선택할 수 있습니다. 예를 들어 MySQL에는 JDBC 드라이버 8.0.33이 필요합니다.
- 레지스트리 센터: [ZooKeeper](https://zookeeper.apache.org/releases.html) (3.8.0+), [MYSQL](https://www.mysql.com/)(8.0.33), [ETCD](https://etcd.io/)
- 프로세스 트리 분석
- macOS용 `pstree`
- Fedora/Red/Hat/CentOS/Ubuntu/Debian용 `psmisc`

## 플러그인 종속성 다운로드

버전 3.3.0부터 바이너리 패키지는 더 이상 플러그인 종속성을 제공하지 않으며 사용자가 직접 다운로드해야 합니다.플러그인 종속성 패키지 다운로드 주소: [플러그인 종속성 패키지](https://repo.maven.apache.org/maven2/org/apache/dolphinscheduler)
다음 명령을 실행하여 플러그인 종속성을 설치할 수도 있습니다.```shell
bash ./bin/install-plugins.sh 3.3.0
````

일반적으로 모든 커넥터 플러그인이 필요한 것은 아니며 `conf/plugins_config`를 구성하여 필요한 플러그인을 지정할 수 있습니다.예를 들어 `dolphinscheduler-task-shell` 플러그인만 필요한 경우 다음과 같이 구성 파일을 수정할 수 있습니다.```
--task-plugins--
dolphinscheduler-task-shell
--end--
````

> **_참고:_** 플러그인 종속성 패키지는 일반적으로 바이너리 패키지에 포함되지 않습니다.서비스 시작 시 `ClassNotFoundException` 오류가 발생하는 경우 해당 플러그인 유형의 문서를 참조하여 플러그인 종속성 패키지가 누락되었는지 확인하세요.예를 들어 `dolphinscheduler-datasource-mysql`에는 `mysql-connector-java.jar`이 포함되지 않습니다.

## DolphinScheduler 시작 환경

> **_참고:_** DolphinScheduler 자체는 Hadoop, Hive, Spark에 의존하지 않지만, 이들에 의존하는 작업을 실행해야 하는 경우 해당 환경 지원이 필요합니다.

### 사용자 면제 및 권한 구성

배포 사용자를 생성하고 비밀번호 없이 `sudo`를 구성해야 합니다.다음은 `dolphinscheduler` 사용자를 생성하는 예입니다.```shell
# To create a user, login as root
useradd dolphinscheduler

# Add password
echo "dolphinscheduler" | passwd --stdin dolphinscheduler

# Configure sudo without password
sed -i '$adolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' /etc/sudoers
sed -i 's/Defaults    requiretty/#Defaults    requiretty/g' /etc/sudoers

# Modify directory permissions and grant permissions for user you created above
chown -R dolphinscheduler:dolphinscheduler apache-dolphinscheduler-*-bin
chmod -R 755 apache-dolphinscheduler-*-bin
````

> **_주의사항:_**
>
> - `sudo -u {linux-user} -i` 명령을 사용하는 DolphinScheduler의 다중 테넌트 작업 전환 사용자로 인해 배포 사용자는 `sudo` 권한이 있어야 하며 비밀번호가 없어야 합니다.초보 학습자가 이해하지 못한다면 지금은 이 점을 무시해도 됩니다.
> - `/etc/sudoers` 파일에서 "Defaults requiretty" 줄을 찾으면 내용에 주석을 달아주세요.

### 사육사 준비

Zookeeper를 레지스트리 센터로 사용하는 경우 먼저 Zookeeper를 설치하고 시작해야 합니다.

## 구성 수정

기본 환경 준비가 완료되면, 환경설정 파일에 맞게 수정해야 합니다.
당신이 사용한 환경.`export <ENV_NAME>=<VALUE>`를 통해 환경 구성을 변경합니다.구성 파일은 `bin/env` 디렉토리에 `dolphinscheduler_env.sh`로 위치합니다.

### `dolphinscheduler_env.sh` 수정

파일`./bin/env/dolphinscheduler_env.sh`,`api-server/conf/application.yaml`，
`master-server/conf/application.yaml`, `worker-server/conf/application.yaml`, `alert-server/conf/application.yaml`은 다음 구성을 설명합니다.

- DolphinScheduler의 데이터베이스 구성, 자세한 지침은 [데이터베이스 초기화](#initialize-the-database)를 참조하세요.
- `JAVA_HOME` 및 `SPARK_HOME`과 같은 외부 종속성이나 라이브러리가 필요한 일부 작업.
- 기본 레지스트리 센터는 mysql입니다.
- 캐시 유형, 시간대 등 서버 관련 구성

해당 작업을 사용하지 않는 경우 작업 외부 종속성을 무시할 수 있지만 `JAVA_HOME`, 레지스트리 센터 및 데이터베이스를 변경해야 합니다.
귀하의 환경에 따른 관련 구성.```shell
# JAVA_HOME, will use it to start DolphinScheduler server
export JAVA_HOME=${JAVA_HOME:-/opt/soft/java}

# Database related configuration, set database type, username and password
export DATABASE=${DATABASE:-postgresql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/dolphinscheduler"
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}

# DolphinScheduler server related configuration
export SPRING_CACHE_TYPE=${SPRING_CACHE_TYPE:-none}
export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-UTC}

# Registry center configuration, determines the type and link of the registry center
export REGISTRY_TYPE=${REGISTRY_TYPE:-zookeeper}
export REGISTRY_ZOOKEEPER_CONNECT_STRING=${REGISTRY_ZOOKEEPER_CONNECT_STRING:-localhost:2181}

# Tasks related configurations, need to change the configuration if you use the related tasks.
export HADOOP_HOME=${HADOOP_HOME:-/opt/soft/hadoop}
export HADOOP_CONF_DIR=${HADOOP_CONF_DIR:-/opt/soft/hadoop/etc/hadoop}
export SPARK_HOME=${SPARK_HOME:-/opt/soft/spark}
export PYTHON_LAUNCHER=${PYTHON_LAUNCHER:-/opt/soft/python/bin/python3}
export HIVE_HOME=${HIVE_HOME:-/opt/soft/hive}
export FLINK_HOME=${FLINK_HOME:-/opt/soft/flink}
export DATAX_LAUNCHER=${DATAX_LAUNCHER:-/opt/soft/datax/bin/datax.py}

export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_LAUNCHER:$JAVA_HOME/bin:$HIVE_HOME/bin:$FLINK_HOME/bin:$DATAX_LAUNCHER:$PATH
````

> **_참고:_** MySQL 데이터베이스를 사용하는 경우 `DATABASE`를 `mysql`로 설정하고 `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` 및 `SPRING_DATASOURCE_PASSWORD`를 데이터베이스 구성에 맞게 수정하세요.
>
> **_참고:_** `dolphinscheduler_env.sh`의 구성은 각 서비스의 구성 파일(application.yaml)의 구성을 덮어쓰므로, application.yaml 파일에 매개변수를 구성하고 `dolphinscheduler_env.sh`에서도 매개변수를 구성하면,
> `dolphinscheduler_env.sh`의 구성이 우선 적용됩니다.`dolphinscheduler_env.sh`의 구성 형식은 다음과 같습니다.
> `application.yaml`의 `SPRING_DATASOURCE_URL`은 `spring.datasource.url`입니다.

## 데이터베이스 초기화

[datasource-setting](datasource-setting.md) '의사 클러스터/클러스터 데이터베이스 초기화' 섹션의 지침에 따라 데이터베이스를 생성하고 초기화합니다.

## DolphinScheduler 시작

서버 로그는 `xxx-server/logs` 폴더에 저장됩니다.```
# Start api-server
bash ./bin/dolphinscheduler-daemon.sh start api-server

# Start master-server
bash ./bin/dolphinscheduler-daemon.sh start master-server

# Start worker-server
bash ./bin/dolphinscheduler-daemon.sh start worker-server

# Start alert-server
bash ./bin/dolphinscheduler-daemon.sh start alert-server
````

> **_참고:_** 처음 배포하는 경우 bash ./bin/dolphinscheduler-daemon.sh status xxx-server를 통해 서버 상태를 확인할 수 있습니다.

## 로그인 DolphinScheduler

`http://localhost:12345/dolphinscheduler/ui` 주소에 접속하여 DolphinScheduler UI에 로그인합니다.기본 사용자 이름과 비밀번호는 **admin/dolphinscheduler123**입니다.

## 서버 시작 또는 중지```shell
# Check the status of DolphinScheduler server
bash ./bin/dolphinscheduler-daemon.sh status xxx-server

# Start or stop DolphinScheduler Master
bash ./bin/dolphinscheduler-daemon.sh stop master-server
bash ./bin/dolphinscheduler-daemon.sh start master-server

# Start or stop DolphinScheduler Worker
bash ./bin/dolphinscheduler-daemon.sh start worker-server
bash ./bin/dolphinscheduler-daemon.sh stop worker-server

# Start or stop DolphinScheduler Api
bash ./bin/dolphinscheduler-daemon.sh start api-server
bash ./bin/dolphinscheduler-daemon.sh stop api-server

# Start or stop Alert
bash ./bin/dolphinscheduler-daemon.sh start alert-server
bash ./bin/dolphinscheduler-daemon.sh stop alert-server
````

> **_Note1:_**: 각 서버의 `<service>/conf/dolphinscheduler_env.sh` 경로에 `dolphinscheduler_env.sh` 파일이 있습니다.
> 마이크로서비스가 필요한 경우.이는 `<service>/bin/start.sh` 명령을 사용하여 다른 서버를 시작하여 모든 서버를 시작할 수 있음을 의미합니다.
> `<service>/conf/dolphinscheduler_env.sh`의 환경 변수.하지만 `bin/env/dolphinscheduler_env.sh` 파일을 덮어쓰게 됩니다.
> `/bin/dolphinscheduler-daemon.sh start <service>` 명령으로 서버를 시작하는 경우 `<service>/conf/dolphinscheduler_env.sh`.
>
> **_Note2:_**: 서비스 이용방법은 "시스템 아키텍처 설계" 부분을 참고하시기 바랍니다.Python 게이트웨이 서비스는
> api-server와 함께 시작되었으며 Python 게이트웨이 서비스를 시작하지 않으려면 다음을 변경하여 비활성화하십시오.
> api-server 구성 경로 `api-server/conf/application.yaml`의 yaml 구성 `python-gateway.enabled : false`
> **_Note3:_**: DS는 기본적으로 /tmp/dolphinscheduler 디렉터리를 리소스 센터로 사용합니다.리소스 센터의 디렉터리를 변경해야 하는 경우 conf/common.properties 파일에서 리소스 항목을 변경하세요.

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
[동물원 사육사]: https://zookeeper.apache.org/releases.html
[문제]: https://github.com/apache/dolphinscheduler/issues/6597