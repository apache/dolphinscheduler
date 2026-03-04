# DolphinScheduler 업그레이드

## 준비

### 호환되지 않는 변경 사항 확인

호환되지 않는 변경 사항이 있으면 현재 기능이 중단될 수 있으므로 업그레이드하기 전에 [호환되지 않는 변경 사항](./in Compatible.md)을 확인해야 합니다.

### 이전 버전의 파일 및 데이터베이스 백업

일부 잘못된 조작으로 인한 데이터 손실을 방지하려면 업그레이드하기 전에 데이터를 백업하는 것이 좋습니다.귀하의 환경에 따른 백업 방법.

### 최신 버전 설치 패키지 다운로드

[다운로드](https://dolphinscheduler.apache.org/en-us/download)에서 최신 바이너리 배포 패키지를 다운로드한 후 다른 폴더에 넣으세요.
현재 서비스가 실행 중인 디렉터리입니다.그리고 아래 명령은 모두 이 디렉터리에서 실행됩니다.

## 업그레이드

### DolphinScheduler의 모든 서비스를 중지합니다.

배포 방법에 따라 DolphinScheduler의 모든 서비스를 중지합니다.

### 데이터베이스 업그레이드

다음 환경 변수를 설정하고({user} 및 {password}는 데이터베이스 사용자 이름과 비밀번호로 변경됨) 업그레이드 스크립트를 실행합니다.

MySQL을 예로 들어 다른 데이터베이스를 사용하는 경우 값을 변경하십시오.[mysql-connector-java 드라이버 jar](https://downloads.MySQL.com/archives/c-j/)를 수동으로 다운로드하세요.
jar 패키지를 `./tools/libs` 디렉토리에 추가한 후 다음 환경 변수를 내보냅니다.        ```shell
        export DATABASE=${DATABASE:-mysql}
        export SPRING_PROFILES_ACTIVE=${DATABASE}
        export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
        export SPRING_DATASOURCE_USERNAME={user}
        export SPRING_DATASOURCE_PASSWORD={password}
````

데이터베이스 업그레이드 스크립트 실행: `sh ./tools/bin/upgrade-schema.sh`

### 리소스 마이그레이션

버전 3.2.0에서 리소스 센터를 리팩터링한 후 원래 리소스는 관리되지 않습니다.대상 테넌트를 할당하고 일회성 마이그레이션 스크립트를 실행할 수 있습니다.모든 리소스는 대상 테넌트의 '. migration' 디렉터리로 마이그레이션됩니다.

#### 예

기존 대상 테넌트 `abc`를 할당합니다. 기본 리소스 경로는 `/dolphinscheduler/abc/`입니다.

스크립트 실행: `sh ./tools/bin/ migration-resource.sh abc`.

실행 결과:

- 원본 파일 리소스 `a/b.sh`는 `/dolphinscheduler/abc/resources/. migration/a/b.sh`로 마이그레이션됩니다.
- 원래 UDF 리소스 `x/y.jar`는 `/dolphinscheduler/abc/udf/. migration/x/y.jar`로 마이그레이션됩니다.
- UDF 함수의 바인딩된 리소스 정보를 업데이트합니다.

### 리니지 업그레이드

스크립트 실행: `sh ./tools/bin/ migration-lineage.sh`.

실행 결과:

- 계보 데이터를 새 테이블 `t_ds_workflow_task_lineage`로 마이그레이션합니다.
- 이 스크립트는 삭제 작업이 아닌 upsert 작업만 수행합니다.필요한 경우 수동으로 삭제할 수 있습니다.

### 업그레이드 서비스

- Pseudo-Cluster 배포로 배포하는 경우 [Pseudo-Cluster](../installation/pseudo-cluster.md) 섹션 "구성 수정"에 따라 변경합니다.
- 클러스터 배포로 배포하는 경우 [Cluster](../installation/cluster.md) 섹션 "구성 수정"에 따라 변경합니다.

## 공지사항

#### 업그레이드 버전 제한

- 버전 3.3.X 이후에는 3.0.0에서의 업그레이드만 지원합니다.이보다 낮은 버전의 경우, 과거 버전을 다운로드하여 3.0.0으로 업그레이드하시기 바랍니다.
- 버전 3.3.X 이상에서는 바이너리 패키지가 더 이상 기본적으로 플러그인 종속성을 제공하지 않으므로 처음 사용할 때 직접 다운로드하여 설치해야 합니다.자세한 내용은 [Pseudo-Cluster](../installation/pseudo-cluster.md)를 참고하세요.

#### 업그레이드 후 주의사항

경고 플러그인에 일부 더러운 데이터가 있을 수 있습니다.업그레이드 후 SQL을 참조하여 수동으로 삭제하세요.```sql
delete from t_ds_alertgroup where group_name = 'global alert group' and description = 'global alert group';
````