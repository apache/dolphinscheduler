# API 설계 표준

표준화되고 통합된 API는 프로젝트 설계의 초석입니다. DolphinScheduler의 API는 REST ful 표준을 따릅니다.REST ful은 현재 가장 널리 사용되는 인터넷 소프트웨어 아키텍처입니다.구조가 명확하고, 표준을 준수하며, 이해하고 확장하기 쉽습니다.

이 기사에서는 DolphinScheduler API를 예로 사용하여 Restful API를 구성하는 방법을 설명합니다.

## 1. URI 디자인

REST는 "표현 상태 전송"입니다. Restful URI의 디자인은 리소스를 기반으로 합니다. 리소스는 네트워크의 엔터티(예: 텍스트, 그림, 서비스)에 해당합니다.그리고 각 리소스는 URI에 해당합니다.

+ 한 종류의 리소스: `task-instances`、`groups`와 같이 복수형으로 표현됩니다.
+ 리소스: 단수로 표현하거나 ID를 사용하여 해당 리소스를 나타냅니다. 예: `group`、`groups/{groupId}`;
+ 하위 리소스: `/instances/{instanceId}/tasks`와 같은 특정 리소스 아래의 리소스입니다.
+ 하위 리소스：`/instances/{instanceId}/tasks/{taskId}`;

## 2. 방법 설계

URI로 특정 리소스를 찾은 다음 메서드를 사용하거나 경로 접미사에서 작업을 선언하여 리소스 작업을 반영해야 합니다.

### ① 쿼리 - GET

URI를 사용하여 리소스를 찾고 GET을 사용하여 쿼리를 나타냅니다.

+ URI가 리소스 유형인 경우 리소스 유형을 쿼리한다는 의미입니다.예를 들어 다음 예는 페이징 쿼리 'alter-groups'를 나타냅니다.```
Method: GET
/dolphinscheduler/alert-groups
````

+ URI가 단일 리소스인 경우 해당 리소스를 쿼리한다는 의미입니다.예를 들어, 다음 예는 지정된 `alter-group`을 쿼리한다는 의미입니다.```
Method: GET
/dolphinscheduler/alter-groups/{id}
````

+ 또한 다음과 같이 URI를 기반으로 쿼리 하위 리소스를 표현할 수도 있습니다.```
Method: GET
/dolphinscheduler/projects/{projectId}/tasks
````

**위의 예는 모두 페이징 쿼리를 나타냅니다.모든 데이터를 쿼리해야 하는 경우 구별을 위해 URI 뒤에 '/list'를 추가해야 합니다.페이징된 쿼리와 쿼리에 동일한 API를 혼합하지 마십시오.**```
Method: GET
/dolphinscheduler/alert-groups/list
````

### ② 생성 - POST

URI를 사용하여 리소스를 찾고, POST를 사용하여 생성을 표시한 다음 생성된 ID를 요청자에게 반환합니다.

+ `alter-group` 생성：```
Method: POST
/dolphinscheduler/alter-groups
````

+ 하위 리소스 생성도 위와 동일합니다.```
Method: POST
/dolphinscheduler/alter-groups/{alterGroupId}/tasks
````

### ③ 수정 - PUT

URI를 사용하여 리소스를 찾고, PUT을 사용하여 수정을 나타냅니다.
+ `경고 그룹` 수정```
Method: PUT
/dolphinscheduler/alter-groups/{alterGroupId}
````

### ④ 삭제 -DELETE

리소스를 찾으려면 URI를 사용하고, 삭제를 나타내려면 DELETE를 사용하세요.

+ `경고 그룹` 삭제```
Method: DELETE
/dolphinscheduler/alter-groups/{alterGroupId}
````

+ 일괄 삭제: ID 배열을 일괄 삭제합니다. POST를 사용해야 합니다.**(DELETE 요청의 본문에는 의미론적 의미가 없으며 일부 게이트웨이, 프록시 및 방화벽은 DELETE 요청을 받은 후 요청 본문을 직접 제거할 수 있으므로 DELETE 메서드를 사용하지 마십시오.)**```
Method: POST
/dolphinscheduler/alter-groups/batch-delete
````

### ⑤ 부분수정 -PATCH

리소스를 찾으려면 URI를 사용하고 부분 수정에는 PATCH를 사용합니다.```
Method: PATCH
/dolphinscheduler/alter-groups/{alterGroupId}
````

### ⑥ 기타

생성, 삭제, 수정 및 쿼리 외에도 URL을 통해 해당 리소스를 찾은 다음 경로 뒤에 다음과 같은 작업을 추가합니다.```
/dolphinscheduler/alert-groups/verify-name
/dolphinscheduler/projects/{projectCode}/process-instances/{code}/view-gantt
````

## 3. 매개변수 디자인

매개변수에는 두 가지 유형이 있는데, 하나는 요청 매개변수이고 다른 하나는 경로 매개변수입니다.그리고 매개변수는 작은 혹을 사용해야 합니다.

페이징의 경우 사용자가 입력한 매개변수가 1보다 작으면 프런트 엔드는 자동으로 1로 바뀌어 첫 번째 페이지가 요청되었음을 나타냅니다.백엔드는 사용자가 입력한 매개변수가 전체 페이지 수보다 크다는 것을 발견하면 바로 마지막 페이지로 돌아가야 합니다.

## 4. 기타 디자인

### 기본 URL

프로젝트의 URI는 `/<project_name>`을 기본 경로로 사용해야 해당 API가 이 프로젝트에 있음을 식별할 수 있습니다.```
/dolphinscheduler
````