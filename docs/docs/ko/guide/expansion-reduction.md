# DolphinScheduler 확장 및 축소

## 확장

이 문서에서는 기존 DolphinScheduler 클러스터에 새로운 마스터 서비스 또는 작업자 서비스를 추가하는 방법을 설명합니다.```
Attention: There cannot be more than one master service process or worker service process on a physical machine.
      If the physical machine which locate the expansion master or worker node has already installed the scheduled service, check the [1.4 Modify configuration] and edit the configuration file `bin/env/install_env.sh` on ** all ** nodes, add masters or workers parameter, and restart the scheduling cluster.
````

### 기본 소프트웨어 설치

* [필수] [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (버전 1.8+): `/etc/profile` 아래에 `JAVA_HOME` 및 `PATH` 변수를 설치, 설치 및 구성해야 합니다.
* [선택] 확장이 워커 노드인 경우 Hadoop, Hive, Spark 클라이언트 등 외부 클라이언트 설치 여부를 고려해야 합니다.```markdown
Attention: DolphinScheduler itself does not depend on Hadoop, Hive, Spark, but will only call their Client for the corresponding task submission.
````

### 설치 패키지 받기

- 기존 환경에서 사용하고 있는 DolphinScheduler의 버전을 확인하신 후, 해당 버전의 설치 패키지를 받으시기 바랍니다. 버전이 다를 경우 호환성에 문제가 있을 수 있습니다.
- 다른 노드의 통합 설치 디렉터리를 확인합니다. 이 문서에서는 DolphinScheduler가 `/opt/` 디렉터리에 설치되어 있고 전체 경로가 `/opt/dolphinscheduler`라고 가정합니다.
- 해당 버전의 설치 패키지를 서버 설치 디렉터리에 다운로드한 후 압축을 풀고 `dolphinscheduler`로 이름을 변경하여 `/opt` 디렉터리에 저장하시기 바랍니다.
- 데이터베이스 종속성 패키지를 추가합니다. 이 문서는 Mysql 데이터베이스를 사용하고 `/opt/dolphinscheduler/lib` 디렉토리에 `mysql-connector-java` 드라이버 패키지를 추가합니다.```shell
# create the installation directory, please do not create the installation directory in /root, /home and other high privilege directories 
mkdir -p /opt
cd /opt
# decompress
tar -zxvf apache-dolphinscheduler-<version>-bin.tar.gz -C /opt 
cd /opt
mv apache-dolphinscheduler-<version>-bin  dolphinscheduler
```````markdown
Attention: You can copy the installation package directly from an existing environment to an expanded physical machine.
````

### 배포 사용자 생성

- **모든** 확장 머신에 배포 사용자를 생성하고 sudo-free를 구성해야 합니다.4개의 확장 시스템인 ds1, ds2, ds3 및 ds4에 예약을 배포하려는 경우 각 시스템에 배포 사용자를 생성하는 것이 전제 조건입니다.```shell
# to create a user, you need to log in with root and set the deployment user name, modify it by yourself, the following take `dolphinscheduler` as an example:
useradd dolphinscheduler;

# set the user password, please change it by yourself, the following take `dolphinscheduler123` as an example
echo "dolphinscheduler123" | passwd --stdin dolphinscheduler

# configure sudo password-free
echo 'dolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers

```````markdown
Attention:
- Since it is `sudo -u {linux-user}` to switch between different Linux users to run multi-tenant jobs, the deploying user needs to have sudo privileges and be password free.
- If you find the line `Default requiretty` in the `/etc/sudoers` file, please also comment it out.
- If have needs to use resource uploads, you also need to assign read and write permissions to the deployment user on `HDFS or MinIO`.
````

### 구성 수정

- `Master/Worker` 등 기존 노드에서 구성 디렉터리를 직접 복사하여 새 노드의 구성 디렉터리를 교체합니다.파일 복사가 완료되면 구성 항목이 올바른지 확인하세요.  ```markdown
  Highlights:
  datasource.properties: database connection information 
  zookeeper.properties: information for connecting zk 
  common.properties: Configuration information about the resource store (if hadoop is set up, please check if the core-site.xml and hdfs-site.xml configuration files exist).
  dolphinscheduler_env.sh: environment Variables
````
- `bin/env/dolphinscheduler_env.sh` 디렉토리의 `dolphinscheduler_env.sh` 환경 변수를 머신 구성에 맞게 수정합니다. (다음은 사용되는 모든 소프트웨어가 `/opt/soft`에 설치되는 예입니다.)  ```shell
      export HADOOP_HOME=/opt/soft/hadoop
      export HADOOP_CONF_DIR=/opt/soft/hadoop/etc/hadoop
      export SPARK_HOME=/opt/soft/spark
      export PYTHON_LAUNCHER=/opt/soft/python/bin/python3
      export JAVA_HOME=/opt/soft/jav
      export HIVE_HOME=/opt/soft/hive
      export FLINK_HOME=/opt/soft/flink
      export DATAX_LAUNCHER=/opt/soft/datax/bin/datax.py
      export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_LAUNCHER:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH:$FLINK_HOME/bin:$DATAX_LAUNCHER:$PATH

````

`주의: 이 단계는 `JAVA_HOME`과 같이 매우 중요하며 무시 또는 주석 처리를 사용하지 않은 경우 구성하려면 `PATH`가 필요합니다.`

- `JDK`를 `/usr/bin/java`에 소프트 링크합니다(여전히 `JAVA_HOME=/opt/soft/java`를 예로 사용함).  ```shell
  sudo ln -s /opt/soft/java/bin/java /usr/bin/java
````
- **모든** 노드에서 `bin/env/install_env.sh` 구성 파일을 수정하여 다음 구성을 동기화합니다.
* 새로운 마스터 노드를 추가하려면 IP 및 마스터 매개변수를 수정해야 합니다.
* 새 작업자 노드를 추가하려면 IP 및 작업자 매개변수를 수정하세요.```shell
# which machines to deploy DS services on, separated by commas between multiple physical machines
ips="ds1,ds2,ds3,ds4"

# ssh port,default 22
sshPort="22"

# which machine the master service is deployed on
masters="existing master01,existing master02,ds1,ds2"

# the worker service is deployed on which machine, and specify the worker belongs to which worker group, the following example of "default" is the group name
workers="existing worker01:default,existing worker02:default,ds3:default,ds4:default"

````

- 워커 노드용 확장인 경우 워커 그룹 설정이 필요하며, [워커 그룹화](security/security.md)의 보안을 참고하세요.

- 모든 새 노드에서 배포 사용자가 DolphinScheduler 디렉터리에 액세스할 수 있도록 디렉터리 권한을 변경합니다.```shell
sudo chown -R dolphinscheduler:dolphinscheduler dolphinscheduler
````

### 클러스터를 다시 시작하고 확인합니다.

- 클러스터를 다시 시작```shell
# stop command:

bin/stop-all.sh # stop all services

bash bin/dolphinscheduler-daemon.sh stop master-server  # stop master service
bash bin/dolphinscheduler-daemon.sh stop worker-server  # stop worker service
bash bin/dolphinscheduler-daemon.sh stop api-server     # stop api    service
bash bin/dolphinscheduler-daemon.sh stop alert-server   # stop alert  service


# start command::
bin/start-all.sh # start all services

bash bin/dolphinscheduler-daemon.sh start master-server  # start master service
bash bin/dolphinscheduler-daemon.sh start worker-server  # start worker service
bash bin/dolphinscheduler-daemon.sh start api-server     # start api    service
bash bin/dolphinscheduler-daemon.sh start alert-server   # start alert  service

```````
Attention: When using `stop-all.sh` or `stop-all.sh`, if the physical machine execute the command is not configured to be ssh-free on all machines, it will prompt to enter the password
````

- 스크립트를 완료한 후 `jps` 명령을 사용하여 모든 노드 서비스가 시작되었는지 확인합니다. (`jps`는 `Java JDK`와 함께 제공됩니다.)```
MasterServer         ----- master service
WorkerServer         ----- worker service
ApiApplicationServer ----- api    service
AlertServer          ----- alert  service
````

성공적으로 시작되면 'logs' 폴더에 저장된 로그를 볼 수 있습니다.```Log Path
logs/
   ├── dolphinscheduler-alert-server.log
   ├── dolphinscheduler-master-server.log
   ├── dolphinscheduler-worker-server.log
   ├── dolphinscheduler-api-server.log
````

위의 서비스가 정상적으로 시작되고, 스케줄링 시스템 페이지가 정상이라면, 웹 시스템의 [모니터]에서 확장된 Master 또는 Worker 서비스가 있는지 확인하세요.존재하는 경우 확장이 완료된 것입니다.

----------------------------------------------------------------

## 감소

감소는 기존 DolphinScheduler 클러스터의 마스터 또는 작업자 서비스를 줄이는 것입니다.
축소에는 두 단계가 있습니다.다음 두 단계를 수행한 후 축소 작업을 완료할 수 있습니다.

### 축소된 노드에서 서비스 중지

* 마스터 노드를 축소하는 경우 마스터 서비스가 위치한 물리적 머신을 식별하고 물리적 머신에서 마스터 서비스를 중지합니다.
* 작업자 노드를 축소하는 경우 작업자 서비스가 축소되는 물리적 머신을 확인하고 물리적 머신에서 작업자 서비스를 중지합니다.```shell
# stop command:
bin/stop-all.sh # stop all services

bash bin/dolphinscheduler-daemon.sh stop master-server  # stop master service
bash bin/dolphinscheduler-daemon.sh stop worker-server  # stop worker service
bash bin/dolphinscheduler-daemon.sh stop api-server     # stop api    service
bash bin/dolphinscheduler-daemon.sh stop alert-server   # stop alert  service


# start command:
bin/start-all.sh # start all services

bash bin/dolphinscheduler-daemon.sh start master-server # start master service
bash bin/dolphinscheduler-daemon.sh start worker-server # start worker service
bash bin/dolphinscheduler-daemon.sh start api-server    # start api    service
bash bin/dolphinscheduler-daemon.sh start alert-server  # start alert  service

```````
Attention: When using `stop-all.sh` or `stop-all.sh`, if the machine without the command is not configured to be ssh-free for all machines, it will prompt to enter the password
````

- 스크립트가 완료된 후 `jps` 명령을 사용하여 모든 노드 서비스가 성공적으로 종료되었는지 확인합니다(`jps`는 `Java JDK`와 함께 제공됨).```
MasterServer         ----- master service
WorkerServer         ----- worker service
ApiApplicationServer ----- api    service
AlertServer          ----- alert  service
````

해당 마스터 서비스 또는 작업자 서비스가 없으면 마스터 또는 작업자 서비스가 성공적으로 종료됩니다.

### 구성 파일 수정

- **모든** 노드에서 'bin/env/install_env.sh' 구성 파일을 수정하여 다음 구성을 동기화합니다.
* 마스터 노드를 축소하려면 IP 및 마스터 매개변수를 수정하세요.
* 작업자 노드를 축소하려면 IP 및 작업자 매개변수를 수정하세요.```shell
# which machines to deploy DS services on, "localhost" for this machine
ips="ds1,ds2,ds3,ds4"

# ssh port,default: 22
sshPort="22"

# which machine the master service is deployed on
masters="existing master01,existing master02,ds1,ds2"

# The worker service is deployed on which machine, and specify which worker group this worker belongs to, the following example of "default" is the group name
workers="existing worker01:default,existing worker02:default,ds3:default,ds4:default"

````