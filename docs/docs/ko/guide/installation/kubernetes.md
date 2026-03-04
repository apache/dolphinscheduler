# 쿠버네티스의 빠른 시작

Kubernetes 배포는 대규모 작업을 예약하고 프로덕션에서 사용할 수 있는 Kubernetes 클러스터의 DolphinScheduler 배포입니다.

DolphinScheduler 기능을 처음 접하고 싶다면 [Standalone 배포](standalone.md)를 따라 설치하는 것이 좋습니다.보다 완벽한 기능을 경험하고 대규모 작업을 예약하려면 [pseudo-cluster 배포](pseudo-cluster.md)에 따라 설치하는 것이 좋습니다.DolphinScheduler를 프로덕션 환경에 배포하려면 [클러스터 배포](cluster.md) 또는 [Kubernetes 배포](kubernetes.md)를 따르는 것이 좋습니다.

> **팁**: 현재 alpha1 단계에 있는 [DolphinScheduler K8S Operator](https://github.com/apache/dolphinscheduler-operator)를 사용해 볼 수도 있습니다.

## 전제 조건

- [헬름](https://helm.sh/) 버전 3.1.0+
- [Kubernetes](https://kubernetes.io/) 버전 1.12+
- 기본 인프라에서 PV 프로비저너 지원

## DolphinScheduler 설치```bash
# Choose the corresponding version yourself
helm upgrade --install dolphinscheduler --create-namespace --namespace dolphinscheduler oci://registry-1.docker.io/apache/dolphinscheduler-helm --version <version>
````

이러한 명령은 기본적으로 Kubernetes 클러스터에 DolphinScheduler를 배포하는 데 사용됩니다.[Appendix-Configuration](#appendix-configuration) 섹션에는 설치 중에 구성할 수 있는 매개변수가 나열되어 있습니다.

> **팁**: `helm list`를 사용하여 모든 릴리스를 나열하세요.

**PostgreSQL**(사용자 이름 `root`, 비밀번호 `root` 및 데이터베이스 `dolphinscheduler` 사용) 및 **ZooKeeper** 서비스가 기본적으로 시작됩니다.

## DolphinScheduler UI에 액세스

`values.yaml`의 `ingress.enabled`가 `true`로 설정되어 있으면 브라우저에서 `http://${ingress.host}/dolphinscheduler`에 액세스할 수 있습니다.

> **팁**: Ingress 액세스에 문제가 있는 경우 Kubernetes 관리자에게 문의하고 [Ingress](https://kubernetes.io/docs/concepts/services-networking/ingress/)를 참조하세요.

그렇지 않은 경우 `api.service.type=ClusterIP`인 경우 `port-forward` 명령을 실행해야 합니다.```bash
$ kubectl port-forward --address 0.0.0.0 svc/dolphinscheduler-api 12345:12345
$ kubectl port-forward --address 0.0.0.0 -n test svc/dolphinscheduler-api 12345:12345 # with test namespace
````

> **팁**: '포트 포워딩을 할 수 없습니다: socat을 찾을 수 없습니다'라는 오류가 나타나면 먼저 'socat'을 설치해야 합니다.

웹에 접속하십시오: `http://localhost:12345/dolphinscheduler/ui` (필요한 경우 IP 주소를 수정하십시오).

또는 `api.service.type=NodePort`인 경우 다음 명령을 실행해야 합니다.```bash
NODE_IP=$(kubectl get no -n {{ .Release.Namespace }} -o jsonpath="{.items[0].status.addresses[0].address}")
NODE_PORT=$(kubectl get svc {{ template "dolphinscheduler.fullname" . }}-api -n {{ .Release.Namespace }} -o jsonpath="{.spec.ports[0].nodePort}")
echo http://$NODE_IP:$NODE_PORT/dolphinscheduler
````

웹에 접속하세요: `http://$NODE_IP:$NODE_PORT/dolphinscheduler`.

기본 사용자 이름은 'admin'이고 기본 비밀번호는 'dolphinscheduler123'입니다.

DolphinScheduler 사용 방법은 [빠른 시작](../start/quick-start.md) 장의 '빠른 시작'을 참조하세요.

## 차트 제거

`dolphinscheduler` 배포를 제거하거나 삭제하려면 다음 안내를 따르세요.```bash
$ helm uninstall dolphinscheduler
````

이 명령은 'dolphinscheduler'와 연결된 모든 Kubernetes 구성 요소(PVC 제외)를 제거하고 릴리스를 삭제합니다.

`dolphinscheduler`와 연관된 PVC를 삭제하려면 아래 명령을 실행하십시오.```bash
$ kubectl delete pvc -l app.kubernetes.io/instance=dolphinscheduler
````

> **참고**: PVC를 삭제하면 모든 데이터도 삭제됩니다.하기 전에 주의하시기 바랍니다.

## [실험적] 작업자 자동 확장

> **경고**: 현재 이 기능은 실험적인 기능이므로 프로덕션에 적합하지 않을 수 있습니다!

`DolphinScheduler`는 작업자 자동 확장을 위해 [KEDA](https://github.com/kedacore/keda)를 사용합니다.그러나 `DolphinScheduler`는 비활성화됩니다.
이 기능은 기본적으로 제공됩니다.작업자 자동 확장을 켜려면 다음 안내를 따르세요.

먼저 `KEDA`에 대한 네임스페이스를 생성하고 `helm`을 사용하여 설치해야 합니다.```bash
helm repo add kedacore https://kedacore.github.io/charts

helm repo update

kubectl create namespace keda

helm install keda kedacore/keda \
    --namespace keda \
    --version "v2.0.0"
````

둘째, `values.yaml`에서 `worker.keda.enabled`를 `true`로 설정하거나 다음 방법으로 차트를 설치해야 합니다.```bash
helm upgrade --install dolphinscheduler --create-namespace --namespace dolphinscheduler oci://registry-1.docker.io/apache/dolphinscheduler-helm --version <version> --set worker.keda.enabled=true
````

자동 확장이 활성화되면 작업자 수는 상태에 따라 'minReplicaCount'와 'maxReplicaCount' 사이에서 조정됩니다.
당신의 작업 중.예를 들어 `DolphinScheduler` 인스턴스에서 실행 중인 작업이 없으면 작업자도 없습니다.
그러면 자원이 크게 절약됩니다.

작업자 자동 크기 조정 기능은 `DolphinScheduler 공식 helm 차트`와 함께 제공되는 `postgresql` 및 `mysql`과 호환됩니다.당신이
외부 데이터베이스를 사용하면 작업자 자동 크기 조정 기능은 외부 'mysql' 및 'postgresql' 데이터베이스만 지원합니다.

Auto Scaling 기능 사용 시 작업자 `WORKER_EXEC_THREADS` 값을 변경해야 하는 경우,
`configmap`을 통하는 대신 `values.yaml`에서 `worker.env.WORKER_EXEC_THREADS`를 변경하세요.

## 구성

구성 파일은 `values.yaml`이며, [Appendix-Configuration](#appendix-configuration) 테이블에는 DolphinScheduler의 구성 가능한 매개변수와 해당 기본값이 나열되어 있습니다.

## 지원 매트릭스|유형 |지원 |메모 |
|--------------------------------------------------|--------------|--------------------------|
|쉘 |예 ||
|파이썬2 |예 ||
|파이썬3 |간접 예 |자주 묻는 질문 |
|하둡2 |간접 예 |자주 묻는 질문 |
|하둡3 |확실하지 않음 |테스트되지 않음 |
|스파크-로컬(클라이언트) |간접 예 |자주 묻는 질문 |
|Spark-YARN(클러스터) |간접 예 |자주 묻는 질문 |
|Spark-독립형(클러스터) |아직은 아니다 ||
|Spark-Kubernetes(클러스터) |아직은 아니다 ||
|Flink-로컬(local>=1.11) |아직은 아니다 |일반 CLI 모드는 아직 지원되지 않습니다 |
|Flink-YARN(원사 클러스터) |간접 예 |자주 묻는 질문 |
|Flink-YARN(yarn-session/yarn-per-job/yarn-application>=1.11) |아직은 아니다 |일반 CLI 모드는 아직 지원되지 않습니다 |
|Flink-독립형(기본값) |아직은 아니다 ||
|Flink-독립형(원격>=1.11) |아직은 아니다 |일반 CLI 모드는 아직 지원되지 않습니다 |
|Flink-Kubernetes(기본값) |아직은 아니다 ||
|Flink-Kubernetes(원격>=1.11) |아직은 아니다 |일반 CLI 모드는 아직 지원되지 않습니다 |
|Flink-NativeKubernetes(kubernetes-session/application>=1.11) |아직은 아니다 |일반 CLI 모드는 아직 지원되지 않습니다 |
|맵리듀스 |간접 예 |자주 묻는 질문 |
|케르베로스 |간접 예 |자주 묻는 질문 |
|HTTP |예 ||
|데이터X |간접 예 |자주 묻는 질문 |
|스쿠프 |간접 예 |자주 묻는 질문 |
|SQL-MySQL |간접 예 |자주 묻는 질문 |
|SQL-PostgreSQL |예 ||
|SQL-하이브 |간접 예 |자주 묻는 질문 |
|SQL-스파크 |간접 예 |자주 묻는 질문 |
|SQL-클릭하우스 |간접 예 |자주 묻는 질문 |
|SQL-오라클 |간접 예 |자주 묻는 질문 |
|SQL-SQL서버 |간접 예 |자주 묻는 질문 |
|SQL-DB2 |간접 예 |자주 묻는 질문 |

## FAQ

### 포드 컨테이너의 로그를 보는 방법은 무엇입니까?

모든 포드('po'라고도 함)를 나열합니다.```
kubectl get po
kubectl get po -n test # with test namespace
````

'dolphinscheduler-master-0'이라는 Pod 컨테이너의 로그를 확인합니다.```
kubectl logs dolphinscheduler-master-0
kubectl logs -f dolphinscheduler-master-0 # follow log output
kubectl logs --tail 10 dolphinscheduler-master-0 -n test # show last 10 lines from the end of the logs
````

### Kubernetes에서 API, 마스터 및 작업자를 확장하는 방법은 무엇입니까?

모든 배포를 나열합니다('배포'라고도 함).```
kubectl get deploy
kubectl get deploy -n test # with test namespace
````

API를 3개의 복제본으로 확장합니다.```
kubectl scale --replicas=3 deploy dolphinscheduler-api
kubectl scale --replicas=3 deploy dolphinscheduler-api -n test # with test namespace
````

모든 상태 저장 세트(일명 `sts`)를 나열합니다.```
kubectl get sts
kubectl get sts -n test # with test namespace
````

마스터를 2개의 복제본으로 확장:```
kubectl scale --replicas=2 sts dolphinscheduler-master
kubectl scale --replicas=2 sts dolphinscheduler-master -n test # with test namespace
````

작업자를 6개의 복제본으로 확장:```
kubectl scale --replicas=6 sts dolphinscheduler-worker
kubectl scale --replicas=6 sts dolphinscheduler-worker -n test # with test namespace
````

### PostgreSQL 대신 MySQL을 DolphinScheduler의 데이터베이스로 사용하는 방법은 무엇입니까?

> 상용 라이센스로 인해 MySQL 드라이버를 직접 사용할 수 없습니다.
>
> MySQL을 사용하려면 다음 지침에 따라 `apache/dolphinscheduler-<service>` 이미지를 기반으로 새 이미지를 빌드할 수 있습니다.
>
> 버전 3.0.0부터 Dolphinscheduler는 마이크로서비스로 제공되었으며 메타데이터 저장소를 변경하려면 모든 서비스를 돌고래 스케줄러 도구, 돌핀 스케줄러-마스터, 돌핀 스케줄러-작업자, 돌핀 스케줄러-API, 돌핀 스케줄러-경고 서버를 포함한 MySQL 드라이버로 교체해야 합니다.

1. MySQL 드라이버 [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)를 다운로드합니다.

2. MySQL 드라이버를 추가하기 위해 새로운 `Dockerfile`을 생성합니다.```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:<version>
# For example
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-tools:<version>

# Attention Please, If the build is dolphinscheduler-tools image
# You need to change the following line to: COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/tools/libs
# The other services don't need any changes
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs
````

3. MySQL 드라이버를 포함하는 새 도커 이미지를 빌드합니다.```
docker build -t apache/dolphinscheduler-<service>:mysql-driver .
````

4. 도커 이미지 `apache/dolphinscheduler-<service>:mysql-driver`를 도커 레지스트리에 푸시합니다.

5. 이미지 `repository`를 수정하고 `values.yaml`에서 `tag`를 `mysql-driver`로 업데이트합니다.

6. `values.yaml`에서 postgresql `enabled`를 `false`로 수정합니다.

7. `values.yaml`에서 externalDatabase를 수정합니다(특히 `host`, `username` 및 `password` 수정).```yaml
externalDatabase:
  type: "mysql"
  host: "localhost"
  port: "3306"
  username: "root"
  password: "root"
  database: "dolphinscheduler"
  params: "useUnicode=true&characterEncoding=UTF-8"
````

8. Kubernetes에서 DolphinScheduler 릴리스를 실행합니다(**DolphinScheduler 설치** 참조).

### `데이터 소스 관리`에서 MySQL 또는 Oracle 데이터 소스를 지원하는 방법은 무엇입니까?

> 상용 라이센스로 인해 MySQL이나 Oracle의 드라이버를 직접 사용할 수 없습니다.
>
> MySQL 또는 Oracle 데이터 소스를 추가하려면 다음 지침에 따라 `apache/dolphinscheduler-<service>` 이미지를 기반으로 새 이미지를 빌드할 수 있습니다.
>
> Dolphinscheduler-worker, Dolphinscheduler-api 등 두 가지 서비스 이미지를 변경해야 합니다.

1. MySQL 드라이버 [mysql-connector-java-8.0.16.jar](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)를 다운로드합니다.
또는 Oracle 드라이버 [ojdbc8.jar](https://repo1.maven.org/maven2/com/oracle/database/jdbc/ojdbc8/)(예: `ojdbc8-19.9.0.0.jar`)을 다운로드하세요.

2. MySQL 또는 Oracle 드라이버를 추가하기 위해 새로운 `Dockerfile`을 생성합니다.```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-<service>:<version>
# For example
# FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>

# If you want to support MySQL Datasource
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs

# If you want to support Oracle Datasource
COPY ojdbc8-19.9.0.0.jar /opt/dolphinscheduler/libs
````

3. MySQL 또는 Oracle 드라이버를 포함하는 새 도커 이미지를 빌드합니다.```
docker build -t apache/dolphinscheduler-<service>:new-driver .
````

4. 도커 이미지 `apache/dolphinscheduler-<service>:new-driver`를 도커 레지스트리에 푸시합니다.

5. 이미지 `repository`를 수정하고 `values.yaml`에서 `tag`를 `new-driver`로 업데이트합니다.

6. Kubernetes에서 DolphinScheduler 릴리스를 실행합니다(**DolphinScheduler 설치** 참조).

7. '데이터 소스 관리'에 MySQL 또는 Oracle 데이터 소스를 추가합니다.

### Python 2 pip 및 사용자 정의 요구 사항.txt를 지원하는 방법은 무엇입니까?

> 돌고래 스케줄러-작업자 서비스의 이미지를 변경하면 됩니다.

1. pip를 설치할 새 `Dockerfile`을 만듭니다.```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>
COPY requirements.txt /tmp
RUN apt-get update && \
    apt-get install -y --no-install-recommends python-pip && \
    pip install --no-cache-dir -r /tmp/requirements.txt && \
    rm -rf /var/lib/apt/lists/*
````

이 명령은 기본 **pip 18.1**을 설치합니다.pip를 업그레이드하는 경우 다음 명령을 추가하면 됩니다.```
pip install --no-cache-dir -U pip && \
````

2. pip를 포함하는 새 Docker 이미지를 빌드합니다.```
docker build -t apache/dolphinscheduler-worker:pip .
````

3. 도커 이미지 'apache/dolphinscheduler-worker:pip'를 도커 레지스트리에 푸시합니다.

4. 이미지 `repository`를 수정하고 `values.yaml`에서 `tag`를 `pip`로 업데이트합니다.

5. Kubernetes에서 DolphinScheduler 릴리스를 실행합니다(**DolphinScheduler 설치** 참조).

6. 새 Python 작업에서 pip를 확인합니다.

### Python 3를 지원하는 방법은 무엇입니까?

> 돌고래 스케줄러-작업자 서비스의 이미지를 변경하면 됩니다.

1. Python 3을 설치하기 위한 새 `Dockerfile`을 만듭니다.```
FROM dolphinscheduler.docker.scarf.sh/apache/dolphinscheduler-worker:<version>
RUN apt-get update && \
    apt-get install -y --no-install-recommends python3 && \
    rm -rf /var/lib/apt/lists/*
````

이 명령은 기본 **Python 3.7.3**을 설치합니다.**pip3**도 설치하려면 다음과 같이 `python3`을 `python3-pip`로 바꾸세요.```
apt-get install -y --no-install-recommends python3-pip && \
````

2. Python 3을 포함하는 새 Docker 이미지를 빌드합니다.```
docker build -t apache/dolphinscheduler-worker:python3 .
````

3. 도커 이미지 `apache/dolphinscheduler-worker:python3`를 도커 레지스트리에 푸시합니다.

4. 이미지 `repository`를 수정하고 `values.yaml`에서 `tag`를 `python3`으로 업데이트합니다.

5. `values.yaml`에서 `PYTHON_LAUNCHER`를 `/usr/bin/python3`으로 수정합니다.

6. Kubernetes에서 DolphinScheduler 릴리스를 실행합니다(**DolphinScheduler 설치** 참조).

7. 새 Python 작업에서 Python 3을 확인합니다.

### Hadoop, Spark, Flink, Hive 또는 DataX를 지원하는 방법은 무엇입니까?

Spark 2.4.7을 예로 들어 보겠습니다.

1. Spark 2.4.7 릴리스 바이너리 `spark-2.4.7-bin-hadoop2.7.tgz`를 다운로드합니다.

2. 'common.sharedStoragePersistence.enabled'가 켜져 있는지 확인하세요.

3. Kubernetes에서 DolphinScheduler 릴리스를 실행합니다(**DolphinScheduler 설치** 참조).

4. Spark 2.4.7 릴리스 바이너리를 Docker 컨테이너에 복사합니다.```bash
kubectl cp spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft
kubectl cp -n test spark-2.4.7-bin-hadoop2.7.tgz dolphinscheduler-worker-0:/opt/soft # with test namespace
````

`sharedStoragePersistence` 볼륨이 `/opt/soft`에 마운트되어 있으므로 `/opt/soft`의 모든 파일은 손실되지 않습니다.

5. 컨테이너를 연결하고 'SPARK_HOME'이 있는지 확인하세요.```bash
kubectl exec -it dolphinscheduler-worker-0 bash
kubectl exec -n test -it dolphinscheduler-worker-0 bash # with test namespace
cd /opt/soft
tar zxf spark-2.4.7-bin-hadoop2.7.tgz
rm -f spark-2.4.7-bin-hadoop2.7.tgz
ln -s spark-2.4.7-bin-hadoop2.7 spark2 # or just mv
$SPARK_HOME/bin/spark-submit --version
````

모든 것이 제대로 진행되면 마지막 명령은 Spark 버전을 인쇄합니다.

6. Shell 작업에서 Spark를 확인합니다.```
$SPARK_HOME/bin/spark-submit --class org.apache.spark.examples.SparkPi $SPARK_HOME/examples/jars/spark-examples_2.11-2.4.7.jar
````

작업 로그에 'Pi는 대략 3.146015'와 같은 출력이 포함되어 있는지 확인하세요.

7. Spark 작업에서 Spark를 확인합니다.

`spark-examples_2.11-2.4.7.jar` 파일을 먼저 리소스에 업로드한 후 다음을 사용하여 Spark 작업을 생성해야 합니다.

- 메인 클래스: `org.apache.spark.examples.SparkPi`
- 메인 패키지: `spark-examples_2.11-2.4.7.jar`
- 배포 모드: `로컬`

마찬가지로 작업 로그에 'Pi는 대략 3.146015'와 같은 출력이 포함되어 있는지 확인하세요.

8. YARN에서 Spark를 확인합니다.

YARN의 Spark(배포 모드는 'cluster' 또는 'client')에는 Hadoop 지원이 필요합니다.Spark 지원과 유사하게 Hadoop 지원 작업은 이전 단계와 거의 동일합니다.

`$HADOOP_HOME` 및 `$HADOOP_CONF_DIR`이 있는지 확인하세요.

### 마스터, 작업자, API 서버 간 공유 저장소를 지원하는 방법은 무엇입니까?

예를 들어 마스터, 작업자 및 API 서버는 동시에 Hadoop을 사용할 수 있습니다.

1. `values.yaml`에서 다음 구성을 수정합니다.```yaml
common:
  sharedStoragePersistence:
    enabled: false
    mountPath: "/opt/soft"
    accessModes:
      - "ReadWriteMany"
    storageClassName: "-"
    storage: "20Gi"
````

'storageClassName' 및 'storage'를 실제 환경 값으로 수정합니다.

> **참고**: `storageClassName`은 `ReadWriteMany` 액세스 모드를 지원해야 합니다.

2. Hadoop을 '/opt/soft' 디렉터리에 복사합니다.

3. `$HADOOP_HOME` 및 `$HADOOP_CONF_DIR`이 올바른지 확인하세요.

### HDFS 및 S3 대신 로컬 파일 리소스 저장소를 지원하는 방법은 무엇입니까?

`values.yaml`에서 다음 구성을 수정합니다.```yaml
common:
  configmap:
    RESOURCE_STORAGE_TYPE: "HDFS"
    RESOURCE_UPLOAD_PATH: "/dolphinscheduler"
    FS_DEFAULT_FS: "file:///"
  fsFileResourcePersistence:
    enabled: true
    accessModes:
      - "ReadWriteMany"
    storageClassName: "-"
    storage: "20Gi"
````

'storageClassName' 및 'storage'를 실제 환경 값으로 수정합니다.

> **참고**: `storageClassName`은 `ReadWriteMany` 액세스 모드를 지원해야 합니다.

### MinIO와 같은 S3 리소스 스토리지를 지원하는 방법은 무엇입니까?

MinIO를 예로 들어 보겠습니다. `values.yaml`에서 다음 구성을 수정합니다.```yaml
common:
  configmap:
    RESOURCE_STORAGE_TYPE: "S3"
    ...
````

특정 필드에 대한 자세한 설명은 [리소스 센터 구성](../resource/configuration.md)을 참조하세요.

### 특정 구성 요소를 별도로 배포하는 방법은 무엇입니까?

`values.yaml` 파일에서 `api.enabled`, `alert.enabled`, `master.enabled` 또는 `worker.enabled` 구성 항목을 수정합니다.

예를 들어 클러스터의 CPU 및 GPU 서버 모두에 작업자를 배포해야 하고 작업자가 서로 다른 이미지를 사용하는 경우 다음을 수행할 수 있습니다.```bash
# Install master, api-server, alert-server, and other default components, but do not install worker
helm upgrade --install dolphinscheduler --create-namespace --namespace dolphinscheduler oci://registry-1.docker.io/apache/dolphinscheduler-helm --version <version> --set worker.enabled=false
# Disable the installation of other components, only install worker, use the self-built CPU image, deploy to CPU servers with the `x86` label through nodeselector, and use zookeeper as the external registry center
helm upgrade --install dolphinscheduler-cpu-worker --create-namespace --namespace dolphinscheduler oci://registry-1.docker.io/apache/dolphinscheduler-helm --version <version> \
     --set minio.enabled=false --set postgresql.enabled=false --set zookeeper.enabled=false \
     --set master.enabled=false  --set api.enabled=false --set alert.enabled=false \
     --set worker.enabled=true --set image.tag=latest-cpu --set worker.nodeSelector.cpu="x86" \
     --set externalRegistry.registryPluginName=zookeeper --set externalRegistry.registryServers=dolphinscheduler-zookeeper:2181
# Disable the installation of other components, only install worker, use the self-built GPU image, deploy to GPU servers with the `a100` label through nodeselector, and use zookeeper as the external registry center
helm upgrade --install dolphinscheduler-gpu-worker --create-namespace --namespace dolphinscheduler oci://registry-1.docker.io/apache/dolphinscheduler-helm --version <version> \
     --set minio.enabled=false --set postgresql.enabled=false --set zookeeper.enabled=false \
     --set master.enabled=false  --set api.enabled=false --set alert.enabled=false \
     --set worker.enabled=true --set image.tag=latest-gpu --set worker.nodeSelector.gpu="a100" \
     --set externalRegistry.registryPluginName=zookeeper --set externalRegistry.registryServers=dolphinscheduler-zookeeper:2181
````

> **참고**: 위 단계는 참고용일 뿐이며 실제 상황에 따라 특정 작업을 조정해야 합니다.
> **참고**: DS는 기본적으로 /tmp/dolphinscheduler 디렉토리를 리소스 센터로 사용합니다.리소스 센터의 디렉터리를 변경해야 하는 경우 conf/common.properties 파일에서 리소스 항목을 변경하세요.

## 부록 - 구성

참조: [DolphinScheduler Helm 차트](https://github.com/apache/dolphinscheduler/blob/dev/deploy/kubernetes/dolphinscheduler/README.md)