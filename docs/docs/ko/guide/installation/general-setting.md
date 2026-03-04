# 일반 설정

## 언어

DolphinScheduler는 '영어'와 '중국어'를 포함하는 두 가지 유형의 내장 언어를 지원합니다.버튼을 클릭하면 돼요
상단 컨트롤 바에 'English'와 'English'를 입력하고 언어를 전환하고 싶을 때 다른 언어로 변경하세요.
언어 선택을 전환하면 전체 DolphinScheduler 페이지 언어가 전환됩니다.

## 테마

DolphinScheduler는 'Dark'와 'Light'를 포함하는 두 가지 유형의 내장 테마를 지원합니다.테마를 바꾸고 싶을 때
DolphinScheduler의 상단 컨트롤 바와 왼쪽에 있는 'Dark'(또는 'Light') 버튼을 클릭하기만 하면 됩니다.
[언어](#언어) 제어 버튼을 누르세요.

## 시간대

DolphinScheduler는 시간대 설정을 지원합니다.

서버 시간대

`bin/dolphinshceduler_daemon.sh`를 사용하여 서버를 시작할 때 기본 시간대는 UTC입니다. `bin/env/dolphinscheduler_env.sh`에서 `SPRING_JACKSON_TIME_ZONE`을 업데이트할 수 있습니다(예: `export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-Asia/Shanghai}`).<br>
IDEA에서 서버를 시작하는 경우 기본 시간대는 현지 시간대입니다. JVM 매개변수를 추가하여 '-Duser.timezone=UTC'와 같은 서버 시간대를 업데이트할 수 있습니다.시간대 목록은 [tz 데이터베이스 시간대 목록](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)을 참조하세요.

사용자 시간대

사용자의 기본 시간대는 DolphinScheduler 서비스를 실행하는 시간대를 기반으로 합니다.
[언어](#언어) 버튼 오른쪽에 있는 버튼을 클릭한 후 '시간대 선택'을 클릭하여 시간대를 선택하세요.
당신은 전환하고 싶습니다.모든 시간 관련 구성 요소는 선택한 시간대 설정에 따라 시간대를 조정합니다.