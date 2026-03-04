# 도커 빠른 시작

Docker로 DolphinScheduler를 시작하는 세 가지 방법이 있습니다.

- [Standalone-server](#using-standalone-server-docker-image)는 초보자로서 DolphinScheduler를 시작하고 사용해 보고 싶은 경우 찾을 수 있는 방법입니다.
- [docker-compose](#using-docker-compose-to-start-server)는 일상 업무에서 소규모 또는 중간 규모의 워크플로에 DolphinScheduler를 배포하려는 일부 사용자를 위한 것입니다.
- [exist postgresql 및 Zookeeper 서버 사용](#using-exists-postgresql-zookeeper)은 이미 존재하는 데이터베이스 또는 Zookeeper 서버를 재사용하려는 사용자를 위한 것입니다.

## 준비

[Docker](https://docs.docker.com/engine/install/) 1.13.1+ 및 [Docker Compose](https://docs.docker.com/compose/) 1.28.0+를 설치해야 합니다.
Docker로 DolphinScheduler를 시작하기 전에

## 서버 시작

### 독립형 서버 Docker 이미지 사용

독립형 서버 Docker 이미지로 DolphinScheduler를 시작하는 것이 이를 경험하고 폭발시키는 가장 쉬운 방법입니다.이런 식으로,
최소한의 비용으로 DolphinScheduler의 개념과 사용법을 배울 수 있습니다.```shell
$ DOLPHINSCHEDULER_VERSION=<version>
$ docker run --name dolphinscheduler-standalone-server -p 12345:12345 -p 25333:25333 -d apache/dolphinscheduler-standalone-server:"${DOLPHINSCHEDULER_VERSION}"
````

> 참고: 프로덕션 환경에서는 apache/dolphinscheduler-standalone-server Docker 이미지를 사용하지 마세요. 맛만 보시기 바랍니다.
> 처음으로 DolphinScheduler.모든 서비스를 하나의 단일 프로세스에서 실행하기 때문일 뿐만 아니라 H2를 다음과 같이 사용합니다.
> 중지 후 메타데이터가 손실되는 데이터베이스(이를 방지하기 위해 다른 데이터베이스로 변경할 수 있음).또한,
> apache/dolphinscheduler-standalone-server에는 DolphinScheduler 핵심 서비스, Spark 및 Flink와 같은 일부 작업만 포함됩니다.
> 이를 실행하려면 외부 구성요소나 환경이 필요합니다.

### docker-compose를 사용하여 서버 시작

docker-compose와 독립형 서버에 의한 시작 서비스의 차이점은 서버가 하나의 단일 프로세스에서 실행된다는 것입니다.
아니면.서비스는 별도의 컨테이너와 다양한 프로세스에서 실행되는 docker-compose로 시작됩니다.메타데이터는
docker-compose 구성을 변경한 후 디스크에 저장되며 실행하려는 사람에게 강력하고 안정적입니다.
장기적으로 DolphinScheduler.먼저 [docker-compose](https://docs.docker.com/compose/install/)를 설치해야 합니다.
서버를 시작합니다.

설치가 완료되면 [다운로드 페이지](https://dolphinscheduler.apache.org/en-us/download/<version>)에서 `docker-compose.yaml` 파일을 받으세요.
소스 패키지를 구성하고 올바른 버전을 얻었는지 확인하십시오.패키지를 다운로드한 후 아래와 같이 명령을 실행할 수 있습니다.```shell
$ DOLPHINSCHEDULER_VERSION=<version>
$ tar -zxf apache-dolphinscheduler-"${DOLPHINSCHEDULER_VERSION}"-src.tar.gz
# Going to docker-compose's location
# For Mac or Linux users
$ cd apache-dolphinscheduler-"${DOLPHINSCHEDULER_VERSION}"-src/deploy/docker
# For Windows users, you should run command `cd apache-dolphinscheduler-"${DOLPHINSCHEDULER_VERSION}"-src\deploy\docker`

# Initialize the database, use profile schema
$ docker-compose --profile schema up -d

# start all dolphinscheduler server, use profile all
$ docker-compose --profile all up -d
````

> 참고: docker-compose를 설치한 후 더 나은 경험을 위해 일부 구성을 수정하는 것이 좋습니다.우리는 고도로
> 도커 데몬 메모리를 최대 4GB까지 수정하는 것이 좋습니다. [도커 컨테이너에 더 많은 메모리를 할당하는 방법](https://stackoverflow.com/a/44533437/7152658)을 참조하세요.
>자세한 내용은.
>
> DolphinScheduler 서버뿐만 아니라 PostgreSQL(`root` 사용)과 같은 다른 필수 서비스도 시작할 것입니다.
> 사용자로 'root', 비밀번호로 'root', 데이터베이스로 'dolphinscheduler') 및 docker-compose로 시작할 때 ZooKeeper를 사용합니다.

### Exists PostgreSQL ZooKeeper 사용

[docker-compose를 사용하여 서버 시작](#using-docker-compose-to-start-server)은 새 데이터베이스와 ZooKeeper를 생성합니다.
컨테이너가 올라올 때.기존 서비스를 재사용하려면 DolphinScheduler 서버를 별도로 시작할 수 있습니다.```shell
$ DOLPHINSCHEDULER_VERSION=<version>
# Initialize the database, make sure database <DATABASE> already exists
$ docker run -d --name dolphinscheduler-tools \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/<DATABASE>" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e SPRING_JACKSON_TIME_ZONE="UTC" \
    --net host \
    apache/dolphinscheduler-tools:"${DOLPHINSCHEDULER_VERSION}" tools/bin/upgrade-schema.sh
# Starting DolphinScheduler service
$ docker run -d --name dolphinscheduler-master \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/<DATABASE>" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e SPRING_JACKSON_TIME_ZONE="UTC" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-master:"${DOLPHINSCHEDULER_VERSION}"
$ docker run -d --name dolphinscheduler-worker \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/<DATABASE>" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e SPRING_JACKSON_TIME_ZONE="UTC" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-worker:"${DOLPHINSCHEDULER_VERSION}"
$ docker run -d --name dolphinscheduler-api \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/<DATABASE>" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e SPRING_JACKSON_TIME_ZONE="UTC" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-api:"${DOLPHINSCHEDULER_VERSION}"
$ docker run -d --name dolphinscheduler-alert-server \
    -e DATABASE="postgresql" \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/<DATABASE>" \
    -e SPRING_DATASOURCE_USERNAME="<USER>" \
    -e SPRING_DATASOURCE_PASSWORD="<PASSWORD>" \
    -e SPRING_JACKSON_TIME_ZONE="UTC" \
    -e REGISTRY_ZOOKEEPER_CONNECT_STRING="localhost:2181" \
    --net host \
    -d apache/dolphinscheduler-alert-server:"${DOLPHINSCHEDULER_VERSION}"
````

> 참고: [PostgreSQL](https://www.postgresql.org/download/)(8.2.15+) 및 [ZooKeeper](https://zookeeper.apache.org/releases.html)(3.8.0)를 설치하고 시작해야 합니다.
> 이 방법을 사용하여 Dolphinscheduler를 시작하고 싶지만 해당 서비스가 없는 경우

## 로그인 DolphinScheduler

[http://localhost:12345/dolphinscheduler/ui](http://localhost:12345/dolphinscheduler/ui)를 클릭하여 DolphinScheduler 웹 UI에 액세스할 수 있습니다.
로그인 페이지의 기본 사용자 이름과 비밀번호로 'admin' 및 'dolphinscheduler123'을 사용하세요.

![로그인](../../../../img/new_ui/dev/quick-start/login.png)

> 참고: [exists PostgreSQL ZooKeeper를 사용하여](#using-exists-postgresql-zookeeper) 방식으로 서비스를 시작하는 경우
> 여러 머신으로 시작하는 경우 URL 도메인을 'localhost'에서 실행 중인 API 서버의 IP 또는 호스트 이름으로 변경해야 합니다.

## 환경변수 변경

Docker를 통해 서버를 시작할 때 일부 환경 변수를 수정하여 구성을 변경할 수 있습니다.우리는
데이터베이스 및 ZooKeeper 구성을 변경하기 위한 [exists PostgreSQL ZooKeeper 사용](#using-exists-postgresql-zookeeper)의 예,
[모든 환경 변수](https://github.com/apache/dolphinscheduler/blob/<version>/script/env/dolphinscheduler_env.sh) <!-- markdown-link-check-disable-line -->에서 모든 환경 변수를 찾을 수 있습니다.
원하는 경우 변경하세요.