# DSIP

DolphinScheduler Improvement Proposal(DSIP)은 Apache DolphinScheduler 코드베이스에 주요 개선 사항을 도입합니다.그것은
소소하고 점진적인 개선을 위한 것이 아니며 DSIP의 목적은 완료되었거나 앞으로 있을 작업을 커뮤니티에 알리고 알리는 것입니다.
Apache DolphinScheduler의 큰 기능입니다.

## DSIP로 간주되는 것

- 주요 새로운 기능, 주요 개선, 구성 요소 도입 또는 제거
- API 엔드포인트, 웹 UI 등 공개 인터페이스의 주요 변경 사항

의심스러운 변경 사항과 커미터가 DSIP여야 한다고 생각하면 실제로 그렇습니다.

우리는 GitHub Issue와 Apache 메일 스레드를 사용하여 DSIP를 기록하고 보관합니다. 자세한 내용은 섹션을 참조하세요.
[현재 DSIP](#current-dsips) 및 [과거 DSIP](#past-dsips).

DSIP로서 다음을 수행해야 합니다.

- [dev@dolphinscheduler.apache.org][mail-to-dev]에서 `[DISCUSS]`로 시작되는 메일 스레드 제목을 갖습니다.
- 'DSIP' 라벨이 붙은 GitHub 문제가 있고 설명에 메일 스레드 링크가 포함되어 있습니다.

### 현재 DSIP

모든 DSIP를 포함한 현재 DSIP는 여전히 작업 중입니다. [현재 DSIP][현재-DSIP]에서 확인할 수 있습니다.

### 과거 DSIP

어떤 이유로 이미 완료되었거나 폐기된 모든 DSIP를 포함한 과거 DSIP는 [과거 DSIP][과거-DSIP]에서 확인할 수 있습니다.

## DSIP 프로세스

### GitHub 문제 생성

모든 DSIP는 GitHub 문제로 시작해야 합니다.

- 문제가 DSIP라고 확신하는 경우, 다음에서 "DSIP"를 클릭하고 선택할 수 있습니다.
[GitHub 문제][github-issue-choose]
- 문제가 DSIP인지 아닌지 확실하지 않은 경우에는 클릭하여 "기능 요청"을 선택할 수 있습니다.
[GitHub 문제][github-issue-choose].DolphinScheduler 관리자 팀은 'DSIP' 라벨을 추가하고
DSIP여야 한다고 생각할 때 이 문서를 발행하고 안내합니다.

특수 접두사 `[DSIP-XXX]`를 추가해야 합니다. `XXX`는 ID DSIP를 나타냅니다.자동 증가이므로 다음을 찾을 수 있습니다.
[모든 DSIP][모든 DSIP] 문제의 정수입니다.

### 토론 메일 보내기

"DSIP" 라벨이 붙은 문제가 발생한 후에는 [dev@dolphinscheduler.apache.org][mail-to-dev]로 이메일을 보내야 합니다.
귀하의 아이디어에 대한 목적과 초안 디자인을 설명하십시오.

메일용 템플릿은 다음과 같습니다.

- 제목: `[DISCUSS][DSIP-XXX] <CHANGE-TO-YOUR-LOVELY-PROPOSAL-TITLE>`, `XXX`를 방금 변경한 특수 정수로 변경하세요.
[GitHub 이슈](#create-github-issue) 및 제안 제목도 변경하세요.
- 콘텐츠:  ```text
  Hi community,

  <CHANGE-TO-YOUR-PROPOSAL-DETAIL>

  I already add a GitHub Issue for my proposal, which you could see in <CHANGE-TO-YOUR-GITHUB-ISSUE-LINK>.

  Looking forward any feedback for this thread.
````

커뮤니티에서 논의하고 모두가 DSIP만큼 가치가 있다고 생각한 후에는 [작업](#work-on-it-or-create-subtask-for-it)할 수 있습니다.
그러나 커뮤니티가 DSIP가 아니어야 한다고 생각하거나 심지어 이 변경 사항도 DolphinScheduler에 포함되어서는 안 된다고 생각한다면, 관리자는
메일 스레드를 종료하고 GitHub 문제에 대한 "DSIP" 레이블을 제거하거나, 변경해서는 안 되는 경우 문제를 닫을 수도 있습니다.

### 작업하거나 하위 작업을 생성하세요

귀하의 제안이 메일 스레드에 전달되면 손을 더럽히고 작업을 시작할 수 있습니다.관련 내용을 제출할 수 있습니다.
단일 커밋으로 변경해야 하는 경우 GitHub에서 풀 요청을 수행합니다.게다가 단일 커밋에서 제안이 너무 크면
[DSIP-1][DSIP-1]과 같은 GitHub 문제에서 하위 작업을 생성하고 여러 커밋으로 분리할 수 있습니다.

### 완료 후 닫기

DSIP가 완료되고 관련 PR이 모두 병합되면 에서 생성한 메일 스레드에 회신해야 합니다.
[2단계](#send-discuss-mail) 커뮤니티에 DSIP 결과를 알립니다.그 후 이 DSIP GitHub 문제는 다음과 같습니다.
닫고 [현재 DSIP][현재-DSIP]에서 [과거 DSIP][과거-DSIP]로 전환하지만 여전히 [모든 DSIP][all-DSIP]에서 찾을 수 있습니다.

## DSIP의 예

* [[DSIP-1][기능][부모] DolphinScheduler용 Python API 추가][DSIP-1]: 여러 하위 작업과 프로젝트가 있습니다.

[모든 DSIP]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+label%3A%22DSIP%22+
[현재 DSIP]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+is%3Aopen+label%3A%22DSIP%22
[과거 DSIP]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+is%3Aclosed+label%3A%22DSIP%22+
[github-issue-choose]: https://github.com/apache/dolphinscheduler/issues/new/choose
[개발자 메일]: mailto:dev@dolphinscheduler.apache.org
[DSIP-1]: https://github.com/apache/dolphinscheduler/issues/6407