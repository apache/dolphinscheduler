# DolphinScheduler 워크플로 데모 초기화

## 준비

### 이전 버전의 파일 및 데이터베이스 백업

일부 잘못된 작업으로 인한 데이터 손실을 방지하려면 워크플로 데모를 초기화하기 전에 데이터를 백업하는 것이 좋습니다.귀하의 환경에 따른 백업 방법.

### 최신 버전 설치 패키지 다운로드

[다운로드](https://dolphinscheduler.apache.org/en-us/download)에서 최신 바이너리 배포 패키지를 다운로드한 후 다른 폴더에 넣으세요.
현재 서비스가 실행 중인 디렉터리입니다.그리고 아래 명령은 모두 이 디렉터리에서 실행됩니다.

## 시작

### DolphinScheduler 서비스 시작

배포 방법에 따라 DolphinScheduler의 모든 서비스를 시작합니다.[클러스터 배포](installation/cluster.md)에 따라 돌핀 스케줄러를 배포하는 경우 `sh ./script/start-all.sh` 명령을 사용하여 모든 서비스를 시작할 수 있습니다.

### 데이터베이스 구성

워크플로우 데모를 초기화하려면 MySQL 또는 PostgreSQL과 같은 다른 데이터베이스에 메타베이스를 저장해야 하며 일부 구성을 변경해야 합니다.데이터베이스를 생성하고 초기화하려면 [datasource-setting](installation/datasource-setting.md) `독립형 스위칭 메타데이터 데이터베이스 구성` 섹션의 지침을 따르세요.

### 테넌트 구성

#### `dolphinscheduler-tools/resources/application.yaml` 배치 세부정보 변경```
demo:
  tenant-code: default
  domain-name: localhost
  api-server-port: 5173
````

위에서 언급한 테넌트 코드는 기본 테넌트이며, 사용자는 운영 체제에 따라 사용자 이름을 수정할 수 있습니다. 이는 수동 테넌트 생성 작업을 대체하며, api-server-port는 서비스의 포트 번호입니다.

그런 다음 워크플로 데모 서비스를 초기화하는 시작 스크립트 `sh ./tools/bin/create-demo-processes.sh`를 실행하여 서비스를 시작합니다.

데모를 만들려면 [빠른 시작](start/quick-start.md)을 참조하세요.