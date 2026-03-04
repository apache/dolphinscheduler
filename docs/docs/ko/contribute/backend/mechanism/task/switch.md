# SWITCH 태스크 개발

다음과 같이 작업 흐름 단계를 전환합니다.

* 사용자 정의 표현식 및 분기 정보는 `taskdefinition`의 `taskParams`에 저장됩니다.스위치가 실행되면 `SwitchParameters` 형식으로 지정됩니다.
* `SwitchTaskExecThread`는 `switch`에 정의된 표현식을 위에서 아래로 처리하고 `varPool`에서 변수 값을 얻은 후 `javascript`를 통해 표현식을 구문 분석합니다.표현식이 true를 반환하면 확인을 중지하고 표현식의 순서를 기록합니다. 여기서는 resultConditionLocation으로 기록합니다.SwitchTaskExecThread 작업이 끝났습니다.
* `switch` 작업이 실행된 후 오류가 없으면(일반적으로 사용자 정의 표현식이 사양을 벗어나거나 매개변수 이름에 문제가 있는 경우) `MasterExecThread.submitPostNode`는 `DAG`의 다운스트림 노드를 획득하여 실행을 계속합니다.
* `DagHelper.parsePostNodes`에서 현재 노드(작업을 완료한 노드)가 `switch` 노드인 경우 `resultConditionLocation`을 획득하고 SwitchParameters의 `resultConditionLocation`을 제외한 모든 브랜치를 건너뜁니다.이런 식으로 실행해야 할 Branch만 남게 됩니다.