# 데이터소스 설정

## 독립형 스위칭 메타데이터 데이터베이스 구성

여기서는 외부 데이터베이스를 구성하는 방법을 설명하기 위해 MySQL을 예로 사용합니다.

> 참고: MySQL을 사용하는 경우 [mysql-connector-java 드라이버][mysql](8.0.16)을 수동으로 다운로드하여 DolphinScheduler의 libs 디렉터리로 이동해야 합니다.
> 이는 `api-server/libs` 및 `alert-server/libs`, `master-server/libs` 및 `worker-server/libs`입니다.

* 먼저 'Pseudo-Cluster/Cluster Database 초기화' 섹션의 지침에 따라 데이터베이스를 생성하고 초기화합니다.
* `{address}`, `{user}` 및 `{password}`에 대한 데이터베이스 주소, 사용자 이름 및 비밀번호를 사용하여 터미널에서 다음 환경 변수를 설정합니다.```shell
export DATABASE=mysql
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:mysql://{address}/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
````

* mysql-connector-java 드라이버를 `./standalone-server/libs/standalone-server/`에 추가합니다. 다운로드 위치는 [general-setting](general-setting.md) `Pseudo-Cluster/Cluster 초기화 데이터베이스` 섹션을 참조하세요.
* 독립형 서버를 시작합니다. 이제 mysql을 데이터베이스로 사용하고 있으며 독립형 서버를 중지하거나 다시 시작해도 데이터가 지워지지 않습니다.

## 의사 클러스터/클러스터 데이터베이스 초기화

DolphinScheduler는 '관계형 데이터베이스'에 메타데이터를 저장합니다.현재 `PostgreSQL`과 `MySQL`을 지원합니다.`MySQL` 및 `PostgreSQL`에서 데이터베이스를 초기화하는 방법을 살펴보겠습니다.

> MySQL을 사용하는 경우에는 [mysql-connector-java 드라이버][mysql](8.0.16)을 수동으로 다운로드하여 DolphinScheduler의 libs 디렉터리인 `api-server/libs` 및 `alert-server/libs`, `master-server/libs`, `worker-server/libs` 및 `tools/libs`로 이동해야 합니다.

MySQL 5.6/5.7의 경우```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# Replace {user} and {password} with your username and password
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';

mysql> flush privileges;
````

MySQL 8의 경우:```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# Replace {user} and {password} with your username and password
mysql> CREATE USER '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%';
mysql> CREATE USER '{user}'@'localhost' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost';
mysql> FLUSH PRIVILEGES;
````

PostgreSQL의 경우:```shell
# Use psql-tools to login PostgreSQL
psql
# Create a database
postgres=# CREATE DATABASE dolphinscheduler;
# Replace {user} and {password} with your username and password
postgres=# CREATE USER {user} PASSWORD {password};
postgres=# ALTER DATABASE dolphinscheduler OWNER TO {user};
# Logout PostgreSQL
postgres=#\q
# Exec cmd below in terminal, add config to pg_hba.conf and reload PostgreSQL config, replace {ip} to DS cluster ip addresses
echo "host    dolphinscheduler   {user}    {ip}     md5" >> $PGDATA/pg_hba.conf
pg_ctl reload
````

그런 다음, 다음 환경 변수를 내보내 데이터베이스 구성을 설정하고, {user} 및 {password}를 이전 단계에서 설정한 대로 변경합니다.

> **⚠️ 시간대 구성 공지**
>
> 'CST'와 같이 모호한 시간대 식별자를 사용하지 마세요. 일정 시간 오류가 발생할 수 있습니다.
>
> `serverTimezone=Asia/Shanghai`와 같이 명시적인 시간대를 사용하세요.

MySQL의 경우:```shell
# for mysql
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone={your_timezone}"
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
````

PostgreSQL의 경우:```shell
# for postgresql
export DATABASE=${DATABASE:-postgresql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:postgresql://127.0.0.1:5432/dolphinscheduler"
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
````

위 단계를 완료한 후 DolphinScheduler에 대한 새 데이터베이스를 만든 다음 Shell 스크립트를 실행하여 데이터베이스를 초기화합니다.```shell
bash tools/bin/upgrade-schema.sh
````

## 데이터소스 센터

DataSource는 MySQL, PostgreSQL, Hive/Impala, Spark, ClickHouse, Oracle, SQL Server 및 기타 DataSource를 지원합니다.

- 하단의 'Data Source Center -> Create Data Source'를 클릭하여 새로운 데이터소스를 생성합니다.
- 'Test Connection'을 클릭하여 DataSource가 성공적으로 연결될 수 있는지 테스트합니다. (연결 테스트를 통과한 경우에만 데이터소스를 저장할 수 있습니다.)

### Apache LICENSE V2 LICENSE와 호환되지 않는 데이터 소스 사용

일부 데이터 소스는 DolphinScheduler에서 기본적으로 지원되는 반면 다른 일부는 사용자가 JDBC 드라이버 패키지를 수동으로 다운로드해야 합니다.
해당 JDBC 드라이버는 Apache LICENSE V2 LICENSE와 호환되지 않기 때문입니다.이러한 이유로 우리는 DolphinScheduler를 출시해야 합니다.
사용자에게 더 복잡해지더라도 해당 패키지 없이 패키지를 배포하십시오.MySQL과 같은 데이터소스,
Oracle, SQL Server를 예로 들 수 있지만 이를 해결할 수 있는 솔루션이 있습니다.

### 예

예를 들어 MySQL 데이터 소스를 사용하려면 [mysql maven 저장소](https://repo1.maven.org/maven2/mysql/mysql-connector-java)에서 올바른 JDBC 드라이버를 다운로드해야 합니다.
그리고 이를 `api-server/libs` 및 `worker-server/libs` 디렉터리로 이동합니다.그런 다음 다음을 통해 MySQL 데이터 소스를 활성화할 수 있습니다.
`api-server` 및 `worker-server`를 다시 시작합니다.컨테이너를 사용하는 경우 동일한 경로의 컨테이너 볼륨에 마운트하고 다시 시작하십시오.
도커처럼.

> 참고: 데이터소스 센터에서만 MySQL을 사용하려는 경우에는 MySQL JDBC 드라이버 버전에 대한 요구 사항이 없습니다.
> 그러나 MySQL을 DolphinScheduler의 메타베이스로 사용하려는 경우에는 [8.0.16 이상](https:/ /repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar) 버전만 지원합니다.

[mysql]: https://downloads.MySQL.com/archives/c-j/