# 내장 매개변수

## 기본 내장 매개변수

|변수 |선언방법 |의미 |
|---------------------------------|------------|--------------------------------------------------------------------------------------------|
|시스템.비즈.날짜 |`${system.biz.date}` |일일 예약 인스턴스의 예약 시간 전날 형식은 `yyyyMMdd` |
|system.biz.curdate |`${system.biz.curdate}` |일일 예약 인스턴스의 예약 시간, 형식은 `yyyyMMdd` |
|시스템.날짜시간 |`${system.datetime}` |일일 예약 인스턴스의 예약 시간, 형식은 `yyyyMMddHHmmss` |
|system.task.execute.path |`${system.task.execute.path}` |현재 실행 중인 작업의 절대 경로 |
|system.task.instance.id |`${system.task.instance.id}` |현재 작업의 인스턴스 ID |
|system.task.definition.name |`${system.task.definition.name}` |현재 작업의 정의 이름 |
|system.task.definition.code |`${system.task.definition.code}` |현재 작업의 정의 코드 |
|system.workflow.instance.id |`${system.workflow.instance.id}` |현재 작업이 속한 워크플로우의 인스턴스 ID |
|system.workflow.definition.name |`${system.workflow.definition.name}` |현재 작업이 속한 워크플로우의 정의 이름 |
|system.workflow.definition.code |`${system.workflow.definition.code}` |현재 작업이 속한 워크플로우의 정의 코드 |
|시스템.프로젝트.이름 |`${system.project.name}` |현재 작업이 속한 프로젝트 이름 |
|시스템.프로젝트.코드 |`${system.project.code}` |현재 작업이 속한 프로젝트의 코드 |

## 확장 내장 매개변수

- 코드에서 선언 방식으로 사용자 정의 변수를 지원합니다: `${변수 이름}`."시스템 매개변수"를 참조하십시오.

- 벤치마크 변수는 `$[...]` 형식으로 정의되며, 시간 형식 `$[yyyyMMddHHmmss]`는 `$[yyyyMMdd]`, `$[HHmmss]`, `$[yyyy-MM-dd]` 등과 같이 임의로 분해 및 결합될 수 있습니다.

- 또는 다음 두 가지 방법으로 정의합니다.1. add_month(yyyyMMdd, offset) 함수를 사용하여 개월 수를 더하거나 뺄 수 있습니다.
이 함수의 첫 번째 매개변수는 [yyyyMMdd]로 시간 형식을 나타내고 두 번째 매개변수는 오프셋으로 사용자가 추가하거나 빼기를 원하는 개월 수를 나타냅니다.
- 다음 N년:`$[add_months(yyyyMMdd,12*N)]`
- N년 전:`$[add_months(yyyyMMdd,-12*N)]`
- 다음 N개월：`$[add_months(yyyyMMdd,N)]`
- N개월 전:`$[add_months(yyyyMMdd,-N)]`
2. 시간 형식 바로 뒤에 숫자를 추가하거나 뺍니다.
- 다음 N주:`$[yyyyMMdd+7*N]`
- 처음 N주:`$[yyyyMMdd-7*N]`
- 다음 N일:`$[yyyyMMdd+N]`
- N일 전：`$[yyyyMMdd-N]`
- 다음 N시간：`$[HHmmss+N/24]`
- 처음 N시간：`$[HHmmss-N/24]`
- 다음 N분：`$[HHmmss+N/24/60]`
- 처음 N분:`$[HHmmss-N/24/60]`
3. 비즈니스 속성 방법
- 오늘:`$[this_day(yyyy-MM-dd)]`, 예: 2022-08-26 => 2022-08-26
- 어제:`$[last_day(yyyy-MM-dd)]`, 예: 2022-08-26 => 2022-08-25
- 해당 연도의 N주, 월요일에 주 시작: `$[year_week(yyyy-MM-dd)]`, 예: 2022-08-26 => 2022-34
- 해당 연도의 N주，N에 주를 시작합니다:`$[year_week(yyyy-MM-dd,N)]` 예를 들어 N=5인 경우, 2022-08-26 => 2022-35
- 전(-)/후(+) 월의 첫날(N의 단위는 월): `$[month_first_day(yyyy-MM-dd,-N)]`, 예를 들어 N=1인 경우, 2022-08-26 => 2022-07-01
- 이전(-)/이후(+) 월 말일(N 단위는 월): `$[month_last_day(yyyy-MM-dd,-N)]`,예: N=1인 경우, 2022-08-28 => 2022-07-31
- 전(-)/후(+) 주의 첫날(N의 단위는 주): `$[week_first_day(yyyy-MM-dd,-N)]`, 예를 들어 N=1인 경우, 2022-08-26 => 2022-08-15
- 이전(-)/After(+) 주의 마지막 날(N의 단위는 주): `$[week_last_day(yyyy-MM-dd,-N)]`, 예를 들어 N=1인 경우, 2022-08-26 => 2022-08-21