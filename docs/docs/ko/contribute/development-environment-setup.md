# DolphinScheduler 개발

## 소프트웨어 요구 사항

DolphinScheduler 개발 환경을 설정하기 전에 아래와 같은 소프트웨어가 설치되어 있는지 확인하십시오.

- [Git](https://git-scm.com/downloads)
- [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html): v1.8+
- [메이븐](http://maven.apache.org/download.cgi): v3.5+
- [노드](https://nodejs.org/en/download): v16.0+
- [Pnpm](https://pnpm.io/installation): v8.0+(pnpm이 Node.js와 호환되는지 확인하세요. [호환성](https://pnpm.io/installation#compatibility)도 참조하세요.)

### Git 저장소 복제

Git 관리 도구를 통해 Git 저장소를 다운로드하세요. 여기서는 git-core를 예로 사용합니다.```shell
mkdir dolphinscheduler
cd dolphinscheduler
git clone git@github.com:apache/dolphinscheduler.git
````

### 소스 코드 컴파일

지원 시스템:

- 맥OS
- Liunx

`./mvnw clean install -Prelease -Dmaven.test.skip=true`를 실행하세요.

### 코드 스타일

DolphinScheduler는 코드 스타일 및 형식 확인을 위해 `Spotless`를 사용합니다.
다음 명령을 실행하면 `Spotless`가 자동으로 수정됩니다.
코드 스타일 및 형식 오류:```shell
./mvnw spotless:apply
````

또한 쉬운 구성을 위해 `pre-commit` 구성 파일을 제공했습니다.사용하시려면 Python이 설치되어 있어야 합니다.
그런 다음 다음 명령을 실행하여 `pre-commit`을 설치합니다.```shell
python -m pip install pre-commit
````

그 후 다음 명령을 실행하여 'pre-commit' 후크를 설치할 수 있습니다.```shell
pre-commit install
````

이제 코드를 커밋할 때마다 'pre-commit'이 자동으로 'Spotless'를 실행하여 코드 스타일과 형식을 확인합니다.

### Helm 템플릿 지침

Helm 템플릿과 관련된 파일을 수정한 후 다음 명령을 사용하여 Helm 템플릿을 디버깅할 수 있습니다.```shell
helm template ./deploy/kubernetes/dolphinscheduler --debug 
````

Helm 템플릿이 디버깅되고 확인되면 다음 명령을 사용하여 README.md 파일을 자동으로 업데이트합니다(수동으로 업데이트하면 형식이 잘못될 수 있음).```shell
./mvnw validate -P helm-doc -pl :dolphinscheduler
````

## Docker 이미지 빌드

DolphinScheduler는 출시 후 새로운 Docker 이미지를 출시할 예정이며, [Docker Hub](https://hub.docker.com/search?q=DolphinScheduler)에서 찾을 수 있습니다.

- DolphinScheduler 소스 코드를 수정하고 Docker 이미지를 로컬로 빌드하려는 경우 수정이 완료되면 실행할 수 있습니다.

> -Pstaging에는 네트워크 환경 없이 개발 및 테스트는 물론 오프라인 배포에도 적합한 플러그인이 포함되어 있습니다.
> -Prelease에는 프로덕션 환경에 적합한 플러그인이 포함되어 있지 않으며 플러그인에 액세스할 수 있는 네트워크에서 요청 시 플러그인을 다운로드할 수 있습니다.```shell
cd dolphinscheduler
./mvnw -B clean package \
       -Dmaven.test.skip \
       -Dspotless.skip = true \
       -Ddocker.tag=<TAG> \
       -Pdocker,[release|staging]
````

명령이 완료되면 `docker Images` 명령으로 찾을 수 있습니다.

- DolphinScheduler 소스 코드를 수정하려면 Docker 이미지를 빌드하고 레지스트리 <HUB_URL>에 푸시하세요. 수정이 완료되면 실행할 수 있습니다.```shell
cd dolphinscheduler
./mvnw -B clean deploy \
       -Dmaven.test.skip \
       -Dspotless.skip = true \
       -Ddocker.tag=<TAG> \
       -Ddocker.hub=<HUB_URL> \
       -Pdocker,[release|staging]
````

- DolphinScheduler 소스 코드를 수정하고 Docker 이미지의 사용자 정의 종속성을 추가하려는 경우 소스 코드를 수정한 후 Dockerfile의 정의를 수정하면 됩니다.다음 명령을 실행하여 모든 Dockerfile 파일을 찾을 수 있습니다.```shell
cd dolphinscheduler
find . -iname 'Dockerfile'
````

그런 다음 위의 Docker 빌드 명령을 실행하십시오.

- 일부 종속성을 추가하거나 패키지를 업그레이드하는 등 이미지를 변경하려는 경우 해당 이미지를 기반으로 사용자 지정 Docker 이미지를 생성할 수 있습니다.```Dockerfile
FROM dolphinscheduler-standalone-server
RUN apt update ; \
    apt install -y <YOUR-CUSTOM-DEPENDENCE> ; \
````

> **_참고:_** Docker는 기본적으로 linux/amd64,linux/arm64 다중 아키텍처 이미지를 빌드하고 푸시합니다.
>
> 19.03 이후 docker에는 buildx가 포함되어 있으므로 Docker 19.03 이후 버전을 사용해야 합니다.

## 공지사항

DolphinScheduler 개발 환경을 구성하는 방법에는 독립형 모드와 일반 모드의 두 가지가 있습니다.

- [독립 실행형 모드](#dolphinscheduler-standalone-quick-start): **권장**, 개발 환경 구축이 더 편리하며 대부분의 장면을 다룰 수 있습니다.
- [일반 모드](#dolphinscheduler-normal-mode) : 서버 마스터, 워커, API가 분리되어 독립형보다 더 많은 테스트 환경을 수용할 수 있으며 실제 프로덕션 환경에 가깝습니다.

## DolphinScheduler 독립 실행형 빠른 시작

> **_참고:_** H2 데이터베이스를 기본 데이터베이스로 사용하고 프로덕션에서는 안정적이지 않을 수 있는 Zookeeper 테스트 서버를 사용하므로 개발 및 디버깅용으로만 독립형 서버를 사용하십시오.
>
> 독립 실행형은 DolphinScheduler 1.3.9 이상 버전에서만 지원됩니다.
>
> 독립형 서버는 mysql, postgresql과 같은 외부 데이터베이스에 연결할 수 있습니다. 지침은 [독립형 배포](https://dolphinscheduler.apache.org/en-us/docs/3.1.2/guide/installation/standalone)를 참조하세요.

### Git 브랜치 선택

다른 Git 브랜치를 사용하여 다른 코드 개발

- 바이너리 패키지를 기반으로 개발하려면 git 브랜치를 특정 릴리스 브랜치로 전환하세요. 예를 들어 1.3.9를 기반으로 개발하려면 '1.3.9-release' 브랜치를 선택해야 합니다.
- 최신 코드를 개발하려면 브랜치 `dev`를 선택하세요.

### 백엔드 서버 시작

IntelliJ IDEA에서 `org.apache.dolphinscheduler.StandaloneServer` 클래스를 찾아 run main function을 클릭하여 시작하세요.

> 참고: 시작 시 종속성을 찾을 수 없는 문제를 방지하려면 시작하기 전에 시작 구성에서 `클래스 경로에 "제공된" 범위의 종속성 추가` 옵션을 확인하세요.

### 프런트엔드 서버 시작

프런트엔드 종속성을 설치하고 실행합니다.

> 참고: [프론트엔드 개발](./frontend-development.md)에서 프론트엔드 설정에 대한 자세한 내용을 확인할 수 있습니다.

아직 'pnpm'을 설치하지 않은 경우 프런트엔드 구성 요소를 실행하기 전에 다음 명령을 사용하여 설치할 수 있습니다.```shell
npm install -g pnpm
````

`pnpm`이 설치되었는지 확인한 후 다음 명령을 실행합니다.```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
````

브라우저 접속 주소[http://localhost:5173](http://localhost:5173)로 DolphinScheduler UI에 로그인할 수 있습니다.기본 사용자 이름과 비밀번호는 **admin/dolphinscheduler123**입니다.

## DolphinScheduler 일반 모드

### 준비

#### 사육사

[ZooKeeper](https://zookeeper.apache.org/releases.html)를 다운로드하고 추출하세요.

- `zkData` 및 `zkLog` 디렉토리 생성
- Zookeeper 설치 디렉터리로 이동하여 `zoo_sample.cfg` 구성 파일을 `conf/zoo.cfg`로 복사하고 conf/zoo.cfg의 dataDir 값을 dataDir=./tmp/zookeeper로 변경합니다.  ```shell
  # We use path /data/zookeeper/data and /data/zookeeper/datalog here as example
  dataDir=/data/zookeeper/data
  dataLogDir=/data/zookeeper/datalog
````
- 터미널에서 `./bin/zkServer.sh start` 명령으로 `./bin/zkServer.sh`를 실행합니다.

#### 데이터베이스

DolphinScheduler의 메타데이터는 관계형 데이터베이스에 저장됩니다.현재 MySQL과 Postgresql을 지원합니다.우리는 MySQL을 예로 사용합니다.데이터베이스를 시작하고 DolphinScheduler 메타베이스로 DolphinScheduler라는 새 데이터베이스를 만듭니다.

새로운 데이터베이스 생성 후, MySQL에서 직접 `dolphinscheduler/dolphinscheduler-dao/src/main/resources/sql/dolphinscheduler_mysql.sql` 아래의 sql 파일을 실행하여 데이터베이스 초기화를 완료합니다.

#### 백엔드 서버 시작

다음 단계는 DolphinScheduler 백엔드 서비스를 시작하는 방법을 안내합니다.

##### 백엔드 시작 준비

- 프로젝트 열기: IDE를 사용하여 프로젝트를 엽니다. 여기서는 IntelliJ IDEA를 예로 사용합니다. 연 후 IntelliJ IDEA가 종속 다운로드를 완료하는 데 시간이 걸립니다.

- 파일 변경

- MySQL을 메타데이터 데이터베이스로 사용하는 경우 `dolphinscheduler-bom/pom.xml`을 수정하고 `mysql-connector-j` 종속성의 `범위`를 `컴파일`로 변경해야 합니다.PostgreSQL을 사용하는 데는 이 단계가 필요하지 않습니다.
- 데이터베이스 구성 수정, `dolphinscheduler-master/src/main/resources/application.yaml`에서 데이터베이스 구성 수정
- 데이터베이스 구성 수정, `dolphinscheduler-api/src/main/resources/application.yaml`에서 데이터베이스 구성 수정
- 데이터베이스 구성 수정, `dolphinscheduler-alert/dolphinscheduler-alert-server/src/main/resources/application.yaml`에서 데이터베이스 구성 수정

여기서는 데이터베이스, 사용자 이름, 돌고래라는 이름의 비밀번호와 함께 MySQL을 예로 사용합니다.```application.yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
    username: dolphinscheduler
    password: dolphinscheduler
````

##### 서버 시작

MasterServer, WorkerServer, ApiApplicationServer를 포함하여 시작해야 하는 세 가지 서비스가 있습니다.

- MasterServer: _VM 옵션_ `-DDOCKER=true -Dspring.profiles.active=mysql` 구성을 사용하여 IntelliJ IDEA의 `org.apache.dolphinscheduler.server.master.MasterServer` 클래스에서 `main` 함수를 실행합니다.
- WorkerServer: _VM 옵션_ `-DDOCKER=true` 구성을 사용하여 IntelliJ IDEA의 `org.apache.dolphinscheduler.server.worker.WorkerServer` 클래스에서 `main` 함수를 실행합니다.
- AlertServer: _VM 옵션_ `-DDOCKER=true -Dspring.profiles.active=mysql` 구성을 사용하여 IntelliJ IDEA의 `org.apache.dolphinscheduler.alert.AlertServer` 클래스에서 `main` 함수를 실행합니다.
- ApiApplicationServer: _VM 옵션_ `-DDOCKER=true -Dspring.profiles.active=mysql` 구성을 사용하여 IntelliJ IDEA의 `org.apache.dolphinscheduler.api.ApiApplicationServer` 클래스에서 `main` 함수를 실행합니다.시작된 후 http://localhost:12345/dolphinscheduler/swagger-ui/index.html에서 Open API 문서를 찾을 수 있습니다.

> VM 옵션 `-Dspring.profiles.active=mysql`의 `mysql`은 지정된 구성 파일을 의미합니다.

### 프런트엔드 서버 시작

프런트엔드 종속성을 설치하고 실행합니다.```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
````

브라우저 접속 주소[http://localhost:5173](http://localhost:5173)로 DolphinScheduler UI에 로그인할 수 있습니다.기본 사용자 이름과 비밀번호는 **admin/dolphinscheduler123**입니다.