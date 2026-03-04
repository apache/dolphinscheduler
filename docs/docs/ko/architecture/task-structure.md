# 태스크 구조

## 전체 작업 저장 구조

DolphinScheduler의 모든 작업은 `t_ds_process_definition` 테이블에 저장됩니다.

다음은 `t_ds_process_definition` 테이블 구조를 보여줍니다.

|아니요 |필드 |유형 |설명 |
|------|------------|-------------|---------------------------------------------------------------|
|1 |아이디 |정수(11) |기본 키 |
|2 |이름 |varchar(255) |프로세스 정의 이름 |
|3 |버전 |정수(11) |프로세스 정의 버전 |
|4 |릴리스_상태 |Tinyint(4) |프로세스 정의의 릴리스 상태: 0은 릴리스되지 않음, 1은 릴리스됨 |
|5 |프로젝트_ID |정수(11) |프로젝트 ID |
|6 |사용자 ID |정수(11) |프로세스 정의의 사용자 ID |
|7 |process_definition_json |긴 텍스트 |프로세스 정의 JSON |
|8 |설명 |텍스트 |프로세스 정의 설명 |
|9 |글로벌_파라미터 |텍스트 |전역 매개변수 |
|10 |플래그 |Tinyint(4) |프로세스가 사용 가능한지 여부를 지정합니다. 0은 사용할 수 없음, 1은 사용 가능 |
|11 |위치 |텍스트 |노드 위치 정보 |
|12 |연결 |텍스트 |노드 연결 정보 |
|13 |수신기 |텍스트 |수신기 |
|14 |수신기_cc |텍스트 |CC 수신기 |
|15 |생성_시간 |날짜/시간 |시간을 창조하다 |
|16 |시간 초과 |정수(11) |시간 초과 |
|17 |세입자_ID |정수(11) |테넌트 ID |
|18 |업데이트 시간 |날짜/시간 |업데이트 시간 |
|19 |수정 기준 |varchar(36) |수정한 사용자 지정 |
|20 |리소스_ID |varchar(255) |리소스 ID |

`process_definition_json` 필드는 DAG 다이어그램의 작업 정보를 정의하는 핵심 필드로 JSON 형식으로 저장됩니다.

다음 표에서는 일반적인 데이터 구조를 설명합니다.
아니요 |필드 |유형 |설명
-------- |---------|-------- |---------
1|globalParams|배열|전역 매개변수
2|tasks|Array|프로세스의 작업 모음 [각 유형의 구조는 다음 섹션을 참조하세요]
3|tenantId|int|테넌트 ID
4|시간 초과|int|시간 초과

데이터 예:```bash
{
    "globalParams":[
        {
            "prop":"golbal_bizdate",
            "direct":"IN",
            "type":"VARCHAR",
            "value":"${system.biz.date}"
        }
    ],
    "tasks":Array[1],
    "tenantId":0,
    "timeout":0
}
````

## 각 태스크 유형별 저장 구조에 대한 자세한 설명

### 쉘 노드

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형 |SHELL
3|이름||문자열|작업 이름 |
4|매개변수||객체|맞춤형 매개변수 |JSON 형식
5||rawScript |문자열|쉘 스크립트 |
6||localParams|배열|맞춤형 로컬 매개변수||
7||자원목록|배열|리소스 파일||
8|설명 ||문자열|설명 ||
9|런플래그 ||문자열 |실행 플래그||
10|조건결과 ||객체|조건 분기 ||
11||성공노드|배열|성공하면 노드로 점프||
12||failedNode|어레이|실패할 경우 노드로 점프|
13|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
14|최대재시도 횟수 ||문자열|최대 재시도 횟수 ||
15|재시도 간격 ||문자열 |재시도 간격||
16|시간 초과 ||객체|시간 초과 ||
17|taskInstancePriority||문자열|작업 우선순위 ||
18|작업자그룹 ||문자열 |작업자 그룹||
19|사전 작업 ||배열|전치사 작업 ||

**노드 데이터 예:**```bash
{
    "type":"SHELL",
    "id":"tasks-80760",
    "name":"Shell Task",
    "params":{
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "rawScript":"echo "This is a shell script""
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### SQL 노드

SQL을 통해 지정된 데이터 소스에 대한 데이터 쿼리 및 업데이트 작업을 수행합니다.

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형 |SQL
3|이름||문자열|작업 이름|
4|매개변수||객체|맞춤형 매개변수|JSON 형식
5||유형 |문자열 |데이터베이스 유형
6||데이터 소스 |Int |데이터 소스 ID
7||sql |문자열 |query SQL 문
9||SQL유형 |문자열|SQL 노드 유형 |쿼리의 경우 0, 비쿼리 SQL의 경우 1
10||제목 |문자열 |메일 제목
11||수신자 |문자열 |수신자
12||receiversCc |문자열 |CC 수신기
13||쇼타입 |문자열|메일 표시 유형|옵션: TABLE 또는 ATTACHMENT
14||connParams |문자열|매개변수 연결
15||사전 진술 |배열|전치사 SQL 문
16||게시물문|배열|후치 SQL 문||
17||localParams|배열|맞춤형 매개변수||
18|설명 ||문자열|설명 ||
19|런플래그 ||문자열 |실행 플래그||
20|조건결과 ||객체|조건 분기 ||
21||성공노드|배열|성공하면 노드로 점프||
22||failedNode|어레이|실패할 경우 노드로 점프|
23|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
24|최대재시도 횟수 ||문자열|최대 재시도 횟수 ||
25|재시도 간격 ||문자열 |재시도 간격||
26|시간 초과 ||객체|시간 초과 ||
27|taskInstancePriority||문자열|작업 우선순위 ||
28|작업자그룹 ||문자열 |작업자 그룹||
29|사전 작업 ||배열|전치사 작업 ||

**노드 데이터 예:**```bash
{
    "type":"SQL",
    "id":"tasks-95648",
    "name":"SqlTask-Query",
    "params":{
        "type":"MYSQL",
        "datasource":1,
        "sql":"select id , namge , age from emp where id =  ${id}",
        "sqlType":"0",
        "title":"xxxx@xxx.com",
        "receivers":"xxxx@xxx.com",
        "receiversCc":"",
        "showType":"TABLE",
        "localParams":[
            {
                "prop":"id",
                "direct":"IN",
                "type":"INTEGER",
                "value":"1"
            }
        ],
        "connParams":"",
        "preStatements":[
            "insert into emp ( id,name ) value (1,'Li' )"
        ],
        "postStatements":[

        ]
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### 프로시저 [저장 프로시저] 노드

**다음은 노드 데이터 구조를 보여줍니다.**
**노드 데이터 예:**

### 스파크 노드

**다음은 노드 데이터 구조를 보여줍니다.**

|아니요 |매개변수 이름 ||유형 |설명 |메모 |
|------|---------|---|-------|----------------|------------------|
|1 |아이디 ||문자열 |작업 ID |
|2 |유형 ||문자열 |작업 유형 |스파크 |
|3 |이름 ||문자열 |작업 이름 |
|4 |매개변수 ||개체 |맞춤형 매개변수 |JSON 형식 |
|5 ||메인클래스 |문자열 |메인 클래스 |
|6 ||메인Args |문자열 |실행 인수 |
|7 ||기타 |문자열 |다른 주장 |
|8 ||메인병 |개체 |애플리케이션 항아리 패키지 |
|9 ||배포 모드 |문자열 |배포 모드 |로컬, 클라이언트, 클러스터 |
|10 ||드라이버 코어 |문자열 |드라이버 코어 |
|11 ||드라이버메모리 |문자열 |드라이버 메모리 |
|12 ||실행자 수 |문자열 |실행자 수 |
|13 ||집행자메모리 |문자열 |실행기 메모리 |
|14 ||실행기 코어 |문자열 |실행기 코어 |
|15 ||프로그램 유형 |문자열 |프로그램 유형 |자바, 스칼라, 파이썬 |
|16 ||localParams |배열 |맞춤형 로컬 매개변수 |
|17 ||자원목록 |배열 |리소스 파일 |
|18 |설명 ||문자열 |설명 ||
|19 |실행 플래그 ||문자열 |실행 플래그 ||
|20 |조건결과 ||개체 |조건 분기 ||
|21 ||성공노드 |배열 |성공하면 노드로 점프 ||
|22 ||실패노드 |배열 |실패하면 노드로 점프 |
|23 |의존성 ||개체 |작업 종속성 |매개변수를 사용한 상호 배제 |
|24 |최대재시도 횟수 ||문자열 |최대 재시도 횟수 ||
|25 |재시도 간격 ||문자열 |재시도 간격 ||
|26 |시간 초과 ||개체 |시간 초과 ||
|27 |taskInstance우선순위 ||문자열 |작업 우선순위 ||
|28 |작업자그룹 ||문자열 |노동자 그룹 ||
|29 |사전 작업 ||배열 |전치사 작업 ||

**노드 데이터 예:**```bash
{
    "type":"SPARK",
    "id":"tasks-87430",
    "name":"SparkTask",
    "params":{
        "mainClass":"org.apache.spark.examples.SparkPi",
        "mainJar":{
            "id":4
        },
        "deployMode":"cluster",
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "driverCores":1,
        "driverMemory":"512M",
        "numExecutors":2,
        "executorMemory":"2G",
        "executorCores":2,
        "mainArgs":"10",
        "others":"",
        "programType":"SCALA"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### 맵리듀스(MR) 노드

**다음은 노드 데이터 구조를 보여줍니다.**

|아니요 |매개변수 이름 ||유형 |설명 |메모 |
|------|---------|-------------|---------|------------------|------------------|
|1 |아이디 ||문자열 |작업 ID |
|2 |유형 ||문자열 |작업 유형 |씨 |
|3 |이름 ||문자열 |작업 이름 |
|4 |매개변수 ||개체 |맞춤형 매개변수 |JSON 형식 |
|5 ||메인클래스 |문자열 |메인 클래스 |
|6 ||메인Args |문자열 |실행 인수 |
|7 ||기타 |문자열 |다른 주장 |
|8 ||메인병 |개체 |애플리케이션 항아리 패키지 |
|9 ||프로그램 유형 |문자열 |프로그램 유형 |자바,파이썬 |
|10 ||localParams |배열 |맞춤형 로컬 매개변수 |
|11 ||자원목록 |배열 |리소스 파일 |
|12 |설명 ||문자열 |설명 ||
|13 |실행 플래그 ||문자열 |실행 플래그 ||
|14 |조건결과 ||개체 |조건 분기 ||
|15 ||성공노드 |배열 |성공하면 노드로 점프 ||
|16 ||실패노드 |배열 |실패하면 노드로 점프 |
|17 |의존성 ||개체 |작업 종속성 |매개변수를 사용한 상호 배제 |
|18 |최대재시도 횟수 ||문자열 |최대 재시도 횟수 ||
|19 |재시도 간격 ||문자열 |재시도 간격 ||
|20 |시간 초과 ||개체 |시간 초과 ||
|21 |taskInstance우선순위 ||문자열 |작업 우선순위 ||
|22 |작업자그룹 ||문자열 |노동자 그룹 ||
|23 |사전 작업 ||배열 |전치사 작업 ||

**노드 데이터 예:**```bash
{
    "type":"MR",
    "id":"tasks-28997",
    "name":"MRTask",
    "params":{
        "mainClass":"wordcount",
        "mainJar":{
            "id":5
        },
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "mainArgs":"/tmp/wordcount/input /tmp/wordcount/output/",
        "others":"",
        "programType":"JAVA"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### Python 노드

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형|PYTHON
3|이름||문자열|작업 이름|
4|매개변수||객체|맞춤형 매개변수 |JSON 형식
5||rawScript |문자열|파이썬 스크립트|
6||localParams|배열|맞춤형 로컬 매개변수||
7||자원목록|배열|리소스 파일||
8|설명 ||문자열|설명 ||
9|런플래그 ||문자열 |실행 플래그||
10|조건결과 ||객체|조건 분기||
11||성공노드|배열|성공하면 노드로 점프||
12||failedNode|Array|실패 시 노드로 점프 |
13|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
14|최대재시도 횟수 ||문자열|최대 재시도 횟수 ||
15|재시도 간격 ||문자열 |재시도 간격||
16|시간 초과 ||객체|시간 초과 ||
17|taskInstancePriority||문자열|작업 우선순위 ||
18|작업자그룹 ||문자열 |작업자 그룹||
19|사전 작업 ||배열|전치사 작업||

**노드 데이터 예:**```bash
{
    "type":"PYTHON",
    "id":"tasks-5463",
    "name":"Python Task",
    "params":{
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "rawScript":"print("This is a python script")"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### 플링크 노드

**다음은 노드 데이터 구조를 보여줍니다.**

|아니요 |매개변수 이름 ||유형 |설명 |메모 |
|------|---------|------|---------|------------|------------------|
|1 |아이디 ||문자열 |작업 ID |
|2 |유형 ||문자열 |작업 유형 |플링크 |
|3 |이름 ||문자열 |작업 이름 |
|4 |매개변수 ||개체 |맞춤형 매개변수 |JSON 형식 |
|5 ||메인클래스 |문자열 |메인 클래스 |
|6 ||메인Args |문자열 |실행 인수 |
|7 ||기타 |문자열 |다른 주장 |
|8 ||메인병 |개체 |애플리케이션 항아리 패키지 |
|9 ||배포 모드 |문자열 |배포 모드 |로컬, 클라이언트, 클러스터 |
|10 ||슬롯 |문자열 |슬롯 수 |
|11 ||작업 관리자 |문자열 |taskManager 개수 |
|12 ||taskManager메모리 |문자열 |taskManager 메모리 크기 |
|13 ||jobManager메모리 |문자열 |jobManager 메모리 크기 |
|14 ||프로그램 유형 |문자열 |프로그램 유형 |자바, 스칼라, 파이썬 |
|15 ||localParams |배열 |로컬 매개변수 |
|16 ||자원목록 |배열 |리소스 파일 |
|17 |설명 ||문자열 |설명 ||
|18 |실행 플래그 ||문자열 |실행 플래그 ||
|19 |조건결과 ||개체 |조건 분기 ||
|20 ||성공노드 |배열 |성공하면 노드 점프 ||
|21 ||실패노드 |배열 |실패 시 노드 점프 |
|22 |의존성 ||개체 |작업 종속성 |매개변수를 사용한 상호 배제 |
|23 |최대재시도 횟수 ||문자열 |최대 재시도 횟수 ||
|24 |재시도 간격 ||문자열 |재시도 간격 ||
|25 |시간 초과 ||개체 |시간 초과 ||
|26 |taskInstance우선순위 ||문자열 |작업 우선순위 ||
|27 |작업자그룹 ||문자열 |노동자 그룹 ||
|38 |사전 작업 ||배열 |전치사 작업 ||

**노드 데이터 예:**```bash
{
    "type":"FLINK",
    "id":"tasks-17135",
    "name":"FlinkTask",
    "params":{
        "mainClass":"com.flink.demo",
        "mainJar":{
            "id":6
        },
        "deployMode":"cluster",
        "resourceList":[
            {
                "id":3,
                "name":"run.sh",
                "res":"run.sh"
            }
        ],
        "localParams":[

        ],
        "slot":1,
        "taskManager":"2",
        "jobManagerMemory":"1G",
        "taskManagerMemory":"2G",
        "executorCores":2,
        "mainArgs":"100",
        "others":"",
        "programType":"SCALA"
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### HTTP 노드

**다음은 노드 데이터 구조를 보여줍니다.**

|아니요 |매개변수 이름 ||유형 |설명 |메모 |
|------|---------|-------|---------|------|------------------|
|1 |아이디 ||문자열 |작업 ID |
|2 |유형 ||문자열 |작업 유형 |HTTP |
|3 |이름 ||문자열 |작업 이름 |
|4 |매개변수 ||개체 |맞춤형 매개변수 |JSON 형식 |
|5 ||URL |문자열 |요청 URL |
|6 ||http메서드 |문자열 |http 메소드 |GET, POST, HEAD, PUT, 삭제 |
|7 ||httpParams |배열 |http 매개변수 |
|8 ||httpCheckCondition |문자열 |HTTP 코드 상태 확인 |기본 코드 200 |
|9 ||상태 |문자열 |검증 조건 |
|10 ||localParams |배열 |맞춤형 로컬 매개변수 |
|11 |설명 ||문자열 |설명 ||
|12 |실행 플래그 ||문자열 |실행 플래그 ||
|13 |조건결과 ||개체 |조건 분기 ||
|14 ||성공노드 |배열 |성공하면 노드 점프 ||
|15 ||실패노드 |배열 |실패 시 노드 점프 |
|16 |의존성 ||개체 |작업 종속성 |매개변수를 사용한 상호 배제 |
|17 |최대재시도 횟수 ||문자열 |최대 재시도 횟수 ||
|18 |재시도 간격 ||문자열 |재시도 간격 ||
|19 |시간 초과 ||개체 |시간 초과 ||
|20 |taskInstance우선순위 ||문자열 |작업 우선순위 ||
|21 |작업자그룹 ||문자열 |노동자 그룹 ||
|22 |사전 작업 ||배열 |전치사 작업 ||

**노드 데이터 예:**```bash
{
    "type":"HTTP",
    "id":"tasks-60499",
    "name":"HttpTask",
    "params":{
        "localParams":[

        ],
        "httpParams":[
            {
                "prop":"id",
                "httpParametersType":"PARAMETER",
                "value":"1"
            },
            {
                "prop":"name",
                "httpParametersType":"PARAMETER",
                "value":"Bo"
            }
        ],
        "url":"https://www.xxxxx.com:9012",
        "httpMethod":"POST",
        "httpCheckCondition":"STATUS_CODE_DEFAULT",
        "condition":""
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### DataX 노드

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형|DATAX
3|이름||문자열|작업 이름|
4|매개변수||객체|맞춤형 매개변수 |JSON 형식
5||customConfig |Int |사용자 정의 구성 사용 여부 지정|0개는 맞춤설정되지 않았으며 1개는 맞춤설정되었습니다.
6||dsType |문자열 |데이터 소스 유형
7||데이터소스 |Int |데이터 소스 ID
8||dt유형 |문자열|대상 데이터베이스 유형
9||데이터대상 |Int|대상 데이터베이스 ID
10||SQL |문자열 |SQL 문
11||targetTable |문자열 |대상 테이블
12||jobSpeedByte |Int |작업 속도 제한(바이트)
13||작업속도기록 |Int|작업 속도 제한(기록)
14||사전 진술 |배열|전치사 SQL
15||게시물문|배열|후치 SQL
16||JSON|문자열|맞춤형 구성|customConfig=1인 경우 유효
17||localParams|배열|맞춤형 매개변수|customConfig=1인 경우 유효
18|설명 ||문자열|설명||
19|런플래그 ||문자열 |실행 플래그||
20|조건결과 ||객체|조건 분기||
21||성공노드|배열|성공 시 노드 점프||
22||failedNode|어레이|실패 시 노드 점프|
23|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
24|최대재시도 횟수 ||문자열|최대 재시도 횟수||
25|재시도 간격 ||문자열 |재시도 간격||
26|시간 초과 ||객체|시간 초과 ||
27|taskInstancePriority||문자열|작업 우선순위||
28|작업자그룹 ||문자열 |작업자 그룹||
29|사전 작업 ||배열|전치사 작업||

**노드 데이터 예:**```bash
{
    "type":"DATAX",
    "id":"tasks-91196",
    "name":"DataxTask-DB",
    "params":{
        "customConfig":0,
        "dsType":"MYSQL",
        "dataSource":1,
        "dtType":"MYSQL",
        "dataTarget":1,
        "sql":"select id, name ,age from user ",
        "targetTable":"emp",
        "jobSpeedByte":524288,
        "jobSpeedRecord":500,
        "preStatements":[
            "truncate table emp "
        ],
        "postStatements":[
            "truncate table user"
        ]
    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            ""
        ],
        "failedNode":[
            ""
        ]
    },
    "dependence":{

    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[

    ]
}
````

### Sqoop 노드

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형|SQOOP
3|이름||문자열|작업 이름|
4|매개변수||객체|맞춤형 매개변수 |JSON 형식
5||동시성|Int|동시성 비율
6||modelType|문자열 |흐름 방향|가져오기,내보내기
7||sourceType|문자열 |데이터소스 유형|
8||sourceParams |String|데이터소스 매개변수|JSON 형식
9||targetType|문자열 |대상 데이터 소스 유형
10||targetParams |문자열|대상 데이터 소스 매개변수|JSON 형식
11||localParams |배열 |사용자 정의된 로컬 매개변수
12|설명 ||문자열|설명||
13|런플래그 ||문자열 |실행 플래그||
14|조건결과 ||객체|조건 분기||
15||성공노드|배열|성공 시 노드 점프||
16||failedNode|어레이|실패 시 노드 점프|
17|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
18|최대재시도 횟수 ||문자열|최대 재시도 횟수||
19|재시도 간격 ||문자열 |재시도 간격||
20|시간 초과 ||객체|시간 초과 ||
21|taskInstancePriority||문자열|작업 우선순위||
22|작업자그룹 ||문자열 |작업자 그룹||
23|사전 작업 ||배열|전치사 작업||

**노드 데이터 예:**```bash
{
            "type":"SQOOP",
            "id":"tasks-82041",
            "name":"Sqoop Task",
            "params":{
                "concurrency":1,
                "modelType":"import",
                "sourceType":"MYSQL",
                "targetType":"HDFS",
                "sourceParams":"{"srcType":"MYSQL","srcDatasource":1,"srcTable":"","srcQueryType":"1","srcQuerySql":"selec id , name from user","srcColumnType":"0","srcColumns":"","srcConditionList":[],"mapColumnHive":[{"prop":"hivetype-key","direct":"IN","type":"VARCHAR","value":"hivetype-value"}],"mapColumnJava":[{"prop":"javatype-key","direct":"IN","type":"VARCHAR","value":"javatype-value"}]}",
                "targetParams":"{"targetPath":"/user/hive/warehouse/ods.db/user","deleteTargetDir":false,"fileType":"--as-avrodatafile","compressionCodec":"snappy","fieldsTerminated":",","linesTerminated":"@"}",
                "localParams":[

                ]
            },
            "description":"",
            "runFlag":"NORMAL",
            "conditionResult":{
                "successNode":[
                    ""
                ],
                "failedNode":[
                    ""
                ]
            },
            "dependence":{

            },
            "maxRetryTimes":"0",
            "retryInterval":"1",
            "timeout":{
                "strategy":"",
                "interval":null,
                "enable":false
            },
            "taskInstancePriority":"MEDIUM",
            "workerGroup":"default",
            "preTasks":[

            ]
        }
````

### 조건 분기 노드

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형 |SHELL
3|이름||문자열|작업 이름 |
4|매개변수||객체|사용자 정의 매개변수 |null
5|설명 ||문자열|설명||
6|런플래그 ||문자열 |실행 플래그||
7|조건결과 ||객체|조건 분기 ||
8||성공노드|배열|성공하면 노드로 점프||
9||failedNode|어레이|실패할 경우 노드로 점프|
10|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
11|최대재시도 횟수 ||문자열|최대 재시도 횟수 ||
12|재시도 간격 ||문자열 |재시도 간격||
13|시간 초과 ||객체|시간 초과 ||
14|taskInstancePriority||문자열|작업 우선순위 ||
15|작업자그룹 ||문자열 |작업자 그룹||
16|사전 작업 ||배열|전치사 작업||

**노드 데이터 예:**```bash
{
    "type":"CONDITIONS",
    "id":"tasks-96189",
    "name":"条件",
    "params":{

    },
    "description":"",
    "runFlag":"NORMAL",
    "conditionResult":{
        "successNode":[
            "test04"
        ],
        "failedNode":[
            "test05"
        ]
    },
    "dependence":{
        "relation":"AND",
        "dependTaskList":[

        ]
    },
    "maxRetryTimes":"0",
    "retryInterval":"1",
    "timeout":{
        "strategy":"",
        "interval":null,
        "enable":false
    },
    "taskInstancePriority":"MEDIUM",
    "workerGroup":"default",
    "preTasks":[
        "test01",
        "test02"
    ]
}
````

### 하위 프로세스 노드

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형|SHELL
3|이름||문자열|작업 이름|
4|매개변수||객체|맞춤형 매개변수 |JSON 형식
5||processDefinitionId |Int|프로세스 정의 ID
6|설명 ||문자열|설명 ||
7|런플래그 ||문자열 |실행 플래그||
8|조건결과 ||객체|조건 분기 ||
9||성공노드|배열|성공하면 노드로 점프||
10||failedNode|어레이|실패할 경우 노드로 점프|
11|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
12|최대재시도 횟수 ||문자열|최대 재시도 횟수||
13|재시도 간격 ||문자열 |재시도 간격||
14|시간 초과 ||객체|시간 초과||
15|taskInstancePriority||문자열|작업 우선순위||
16|작업자그룹 ||문자열 |작업자 그룹||
17|사전 작업 ||배열|전치사 작업||

**노드 데이터 예:**```bash
{
            "type":"SUB_WORKFLOW",
            "id":"tasks-14806",
            "name":"SubProcessTask",
            "params":{
                "processDefinitionId":2
            },
            "description":"",
            "runFlag":"NORMAL",
            "conditionResult":{
                "successNode":[
                    ""
                ],
                "failedNode":[
                    ""
                ]
            },
            "dependence":{

            },
            "timeout":{
                "strategy":"",
                "interval":null,
                "enable":false
            },
            "taskInstancePriority":"MEDIUM",
            "workerGroup":"default",
            "preTasks":[

            ]
        }
````

### 종속 노드

**다음은 노드 데이터 구조를 보여줍니다.**
번호|매개변수 이름||유형|설명 |참고
-------- |---------|---------|-------- |--------- |---------
1|ID ||문자열|작업 ID|
2|유형 ||문자열 |작업 유형|종속
3|이름||문자열|작업 이름|
4|매개변수||객체|맞춤형 매개변수 |JSON 형식
5||rawScript |String|쉘 스크립트|
6||localParams|배열|맞춤형 로컬 매개변수||
7||자원목록|배열|리소스 파일||
8|설명 ||문자열|설명||
9|런플래그 ||문자열 |실행 플래그||
10|조건결과 ||객체|조건 분기||
11||성공노드|배열|성공하면 노드로 점프||
12||failedNode|어레이|실패할 경우 노드로 점프|
13|의존성||객체 |작업 종속성 |매개변수를 사용한 상호 배제
14||관계|문자열 |관계|AND,OR
15||dependencyTaskList|배열 |종속 작업 목록|
16|최대재시도 횟수 ||문자열|최대 재시도 횟수||
17|재시도 간격 ||문자열 |재시도 간격||
18|시간 초과 ||객체|시간 초과||
19|taskInstancePriority||문자열|작업 우선순위||
20|작업자그룹 ||문자열 |작업자 그룹||
21|사전 작업 ||배열|전치사 작업||

**노드 데이터 예:**```bash
{
            "type":"DEPENDENT",
            "id":"tasks-57057",
            "name":"DenpendentTask",
            "params":{

            },
            "description":"",
            "runFlag":"NORMAL",
            "conditionResult":{
                "successNode":[
                    ""
                ],
                "failedNode":[
                    ""
                ]
            },
            "dependence":{
                "relation":"AND",
                "dependTaskList":[
                    {
                        "relation":"AND",
                        "dependItemList":[
                            {
                                "projectId":1,
                                "definitionId":7,
                                "definitionList":[
                                    {
                                        "value":8,
                                        "label":"MRTask"
                                    },
                                    {
                                        "value":7,
                                        "label":"FlinkTask"
                                    },
                                    {
                                        "value":6,
                                        "label":"SparkTask"
                                    },
                                    {
                                        "value":5,
                                        "label":"SqlTask-Update"
                                    },
                                    {
                                        "value":4,
                                        "label":"SqlTask-Query"
                                    },
                                    {
                                        "value":3,
                                        "label":"SubProcessTask"
                                    },
                                    {
                                        "value":2,
                                        "label":"Python Task"
                                    },
                                    {
                                        "value":1,
                                        "label":"Shell Task"
                                    }
                                ],
                                "depTasks":"ALL",
                                "cycle":"day",
                                "dateValue":"today"
                            }
                        ]
                    },
                    {
                        "relation":"AND",
                        "dependItemList":[
                            {
                                "projectId":1,
                                "definitionId":5,
                                "definitionList":[
                                    {
                                        "value":8,
                                        "label":"MRTask"
                                    },
                                    {
                                        "value":7,
                                        "label":"FlinkTask"
                                    },
                                    {
                                        "value":6,
                                        "label":"SparkTask"
                                    },
                                    {
                                        "value":5,
                                        "label":"SqlTask-Update"
                                    },
                                    {
                                        "value":4,
                                        "label":"SqlTask-Query"
                                    },
                                    {
                                        "value":3,
                                        "label":"SubProcessTask"
                                    },
                                    {
                                        "value":2,
                                        "label":"Python Task"
                                    },
                                    {
                                        "value":1,
                                        "label":"Shell Task"
                                    }
                                ],
                                "depTasks":"SqlTask-Update",
                                "cycle":"day",
                                "dateValue":"today"
                            }
                        ]
                    }
                ]
            },
            "maxRetryTimes":"0",
            "retryInterval":"1",
            "timeout":{
                "strategy":"",
                "interval":null,
                "enable":false
            },
            "taskInstancePriority":"MEDIUM",
            "workerGroup":"default",
            "preTasks":[

            ]
        }
````