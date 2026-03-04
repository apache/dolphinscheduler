# 호환되지 않음

이 문서에는 각 버전 간 호환되지 않는 업데이트가 기록되어 있습니다.관련 버전으로 업그레이드하기 전에 이 문서를 확인해야 합니다.

## 3.0.0

* '복사' 접미사 없이 워크플로 복사 및 가져오기 [#10607](https://github.com/apache/dolphinscheduler/pull/10607)
* 기본 SQL 세그먼트 구분 기호로 세미콜론을 사용합니다. [#10869](https://github.com/apache/dolphinscheduler/pull/10869)

## 3.2.0

* `common.properties`에서 `data-quality.jar.name` 속성의 이름을 `data-quality.jar.dir`로 바꾸고 디렉터리를 나타냅니다([#15563](https://github.com/apache/dolphinscheduler/pull/15563)).
* `common.properties`에서 `data-quality.jar.name` 속성의 기본 키를 제거합니다([#15551](https://github.com/apache/dolphinscheduler/pull/15551)).
* `StorageOperate`의 `download()`에서 `deleteSource`를 제거합니다([#14084](https://github.com/apache/dolphinscheduler/pull/14084)).
* 기본 Unix 셸 실행기를 sh에서 bash로 변경합니다([#12180](https://github.com/apache/dolphinscheduler/pull/12180)).
* Spark 작업의 Spark 버전을 제거합니다([#11860](https://github.com/apache/dolphinscheduler/pull/11860)).
* SQL 작업 플러그인에서 정규식 일치 SQL 매개변수를 변경합니다([#13378](https://github.com/apache/dolphinscheduler/pull/13378)).
* 환경 `PYTHON_HOME`을 `PYTHON_LAUNCHER`로, `DATAX_HOME`을 `DATAX_LAUNCHER`로 변경합니다([#14523](https://github.com/apache/dolphinscheduler/pull/14523)).
* mysql 드라이버 버전을 8.0.16에서 8.0.33으로 업그레이드하세요. ([#14684](https://github.com/apache/dolphinscheduler/pull/14684))
* /datasources/tables && /datasources/tableColumns Api에 필수 필드 `database`를 추가합니다. [#14406](https://github.com/apache/dolphinscheduler/pull/14406)
* 새 리소스 센터의 공개 인터페이스에서 '설명' 매개변수를 제거합니다([#14394](https://github.com/apache/dolphinscheduler/pull/14394)).

## 3.3.0

* `리소스 센터`에서 `udf-manage` 기능을 제거합니다. ([#16209])(https://github.com/apache/dolphinscheduler/pull/16209)
* `Task Plugin`에서 `Pigeon`을 제거합니다([#16218])(https://github.com/apache/dolphinscheduler/pull/16218)
* 코드의 '프로세스' 이름을 '워크플로'로 통일합니다. ([#16515])(https://github.com/apache/dolphinscheduler/pull/16515)
* 1.x 및 2.x의 더 이상 사용되지 않는 업그레이드 코드([#16543])(https://github.com/apache/dolphinscheduler/pull/16543)
* '데이터 품질' 모듈 제거([#16794])(https://github.com/apache/dolphinscheduler/pull/16794)
* `application.yaml`에서 `registry-disconnect-strategy`를 제거합니다([#16821])(https://github.com/apache/dolphinscheduler/pull/16821)
* 작업자의 `application.yaml`에서 `exec-threads`를 제거하려면 `physical-task-config`를 사용하세요. 마스터의 `application.yaml`에서 `master-async-task-executor-thread-pool-size`를 제거하고 `logic-task-config`를 사용하세요. ([#16790])(https://github.com/apache/dolphinscheduler/pull/16790)
* `t_ds_worker_group`에서 사용되지 않은 열 `other_params_json`을 삭제합니다. ([#16860])(https://github.com/apache/dolphinscheduler/pull/16860)
* `작업 플러그인`에서 `동적`을 제거합니다([#16482])(https://github.com/apache/dolphinscheduler/pull/16842)

## 3.4.0

* 데이터 소스 구성 아래 SSH 연결 매개변수에서 publicKey 필드의 이름을 privateKey로 변경했습니다.([#17666])(https://github.com/apache/dolphinscheduler/pull/17666)
* t_ds_serial_command 테이블을 추가합니다.([#17531])(https://github.com/apache/dolphinscheduler/pull/17531)
* `api-server/application.yaml`에서 `python-gateway.auth-token`의 기본값을 제거합니다.([#17801])(https://github.com/apache/dolphinscheduler/pull/17801)
* ShellCommandExecutor를 사용하는 작업 플러그인을 리팩터링합니다([#17790])(https://github.com/apache/dolphinscheduler/pull/17790)
* `작업 플러그인`에서 `Pytorch`를 제거하세요([#17808])(https://github.com/apache/dolphinscheduler/pull/17808). 이 작업 유형을 계속 사용하고 있다면 업그레이드하기 전에 `t_ds_task_definition` 및 `t_ds_task_definition_log`에서 `task_type = 'PYTORCH'`가 있는 데이터를 삭제하세요.