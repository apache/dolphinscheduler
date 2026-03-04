## DolphinScheduler Datasource SPI 메인 디자인

#### 데이터 소스를 어떻게 사용하나요?

데이터 소스 센터는 기본적으로 POSTGRESQL, HIVE/IMPALA, SPARK, CLICKHOUSE, SQLSERVER 데이터 소스를 지원합니다.

MySQL 또는 ORACLE 데이터 소스를 사용하는 경우 해당 드라이버 패키지를 lib 디렉터리에 배치해야 합니다.

#### 데이터소스 플러그인 개발은 어떻게 하나요?

org.apache.dolphinscheduler.spi.datasource.DataSourceChannel
org.apache.dolphinscheduler.spi.datasource.DataSourceChannelFactory
org.apache.dolphinscheduler.spi.datasource.client.DataSourceClient

1. 첫 번째 단계에서 데이터 소스 플러그인은 위의 인터페이스를 구현하고 일반 클라이언트를 상속할 수 있습니다.자세한 내용은 sqlserver, mysql 등 데이터 소스 플러그인 구현을 참고하세요.모든 RDBMS 플러그인의 추가 방법은 동일합니다.

2. 데이터 소스 플러그인 pom.xml에 드라이버 구성을 추가합니다.

돌핀 스케줄러 데이터 소스 API 모듈에서는 모든 데이터 소스에 대한 외부 접근을 위한 API를 제공합니다.

또한 `DataSourceChannelFactory`는 `PrioritySPI`에서 확장됩니다. 즉, 플러그인 우선순위를 설정할 수 있으며, 이름이 같은 두 플러그인이 있는 경우 `getIdentify` 메소드를 재정의하여 우선순위를 맞춤설정할 수 있습니다.우선순위가 높은 플러그인이 로드되지만 이름과 우선순위가 같은 두 개의 플러그인이 있는 경우 플러그인을 로드할 때 서버에서 'IllegalArgumentException'이 발생합니다.

#### **향후 계획**

kafka, http, 파일, SparkSQL, FlinkSQL 등과 같은 데이터 소스를 지원합니다.