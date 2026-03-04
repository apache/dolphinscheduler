# 커뮤니티 리뷰

[팀](https://dolphinscheduler.apache.org/en-us/community)에 언급된 GitHub 저장소에 이슈 및 풀 요청을 제출하는 것 외에도
DolphinScheduler에 기여하는 것은 GitHub 문제 또는 끌어오기 요청을 검토하고 있습니다.최신의 새로운 정보를 알 수 있을 뿐만 아니라
커뮤니티의 방향을 정할 뿐만 아니라 검토하는 동안 다른 사람들의 좋은 디자인도 이해합니다.동시에, 당신은 할 수 있습니다
커뮤니티에 더 많이 노출되고 명예를 쌓아보세요.

누구나 이슈와 풀 리퀘스트를 검토하는 것이 좋습니다.또한 기여자를 모집하기 위해 Help Wanted 이메일 토론을 진행합니다.
커뮤니티에서 검토해 보세요.[mail][mail-review-wanted]에서 자세한 내용을 볼 수 있으며, 메일 스레드의 결과를 게시합니다.
[GitHub 토론][토론-결과-검토-원함]에서.

> 참고: [GitHub 토론][discussion-result-review-wanted]에 언급된 사용자만 Issues를 검토하거나 Pull할 수 있습니다.
> 요청, 커뮤니티 지지자 **누구나 문제 및 끌어오기 요청을 검토하는 것이 좋습니다**.사용자
> [GitHub 토론][discussion-result-review-wanted] 메일 스레드에서 수집할 때 검토하려는 의지를 보여줍니다.
> 이 목록의 장점은 [팀](https://dolphinscheduler.apache.org/en-us/community)의 멤버를 언급하는 것 외에도 커뮤니티에서 토론을 할 때,
> [GitHub 토론][discussion-result-review-wanted] 사람들에게서 도움을 받을 수도 있습니다.가입하고 싶다면
> [GitHub 토론][discussion-result-review-wanted], 해당 토론에 댓글을 달고 관심 있는 모듈을 남겨주세요.
> in, 그러면 관리자가 당신을 목록에 추가할 것입니다.

## 검토 방법

DolphinScheduler는 GitHub를 통해 커뮤니티 기여를 받고 모든 문제 및 끌어오기 요청은 GitHub에서 호스팅됩니다.
검토를 통해 커뮤니티에 가입하려면 [문제 검토](#issues) 섹션으로 이동하세요. 끌어오기 요청을 선호하는 경우
[풀 요청 검토](#pull-requests) 섹션으로 이동하세요.

### 문제

이슈 검토는 GitHub에서 [문제][모든 이슈]에 대해 토론하고 이에 대한 제안을 제공하는 것을 의미합니다.다음 상황을 포함하되 이에 국한되지는 않습니다.

|상황 |이유 |라벨 |액션 |
|-------------------------|------------------|-----------------------------------------|---------------------------------------------------------|
|해결되지 않습니다 |dev 브랜치에서 수정되었습니다 |[wontfix][label-wontfix] |문제를 닫고 이미 출시된 경우 수정된 버전을 제작자에게 알립니다 |
|중복 문제 |이전에도 같은 문제가 발생했습니다 |[중복][라벨-중복] |문제를 종료하고 작성자에게 동일한 문제의 링크를 알립니다 |
|설명이 명확하지 않음 |세부 재현 단계 없이 |[추가 정보 필요][label-need-more-information] |작성자에게 설명 추가를 알리세요 |

제안을 제공하는 것 외에도 문제에 대한 라벨을 추가하는 것도 검토 중에 중요합니다.라벨이 지정된 문제를 검색할 수 있습니다.
더 나은, 추가 처리에 편리합니다.문제는 둘 이상의 라벨과 관련될 수 있습니다.일반적인 문제 범주는 다음과 같습니다.|라벨 |의미 |
|----------------|-------------------|
|[UI][라벨-ui] |UI 및 프론트엔드 관련 |
|[보안][레이블-보안] |보안 문제 |
|[사용자 경험][레이블-사용자 경험] |사용자 경험 문제 |
|[개발][라벨 개발] |개발 이슈 |
|[파이썬][레이블-파이썬] |파이썬 문제 |
|[플러그인][레이블-플러그인] |플러그인 문제 |
|[문서][레이블-문서] |문서 발행 |
|[docker][레이블-docker] |도커 문제 |
|[확인 필요][라벨-확인 필요] |문제 확인 필요 |
|[e2e][라벨-e2e] |E2E 이슈 |
|[win-os][라벨-win-os] |Windows 운영 체제 문제 |
|[제안][라벨 제안] |우리에게 제안해 주세요 |

분류 외에도 라벨은 문제의 우선순위를 설정할 수도 있습니다.우선순위가 높을수록 더 많은 관심을 기울인다
커뮤니티에서는 수정하거나 구현하기가 더 쉽습니다.우선순위 라벨은 다음과 같습니다

|라벨 |우선순위 |
|----------------|---|
|[우선순위:높음][라벨-우선순위-높음] |높은 우선순위 |
|[우선순위:중간][라벨-우선순위-중간] |중간 우선순위 |
|[우선순위:낮음][라벨-우선순위-낮음] |낮은 우선순위 |

위의 모든 라벨은 공통 라벨입니다.이 프로젝트의 모든 라벨은 [전체 라벨 목록][label-all-list]에서 볼 수 있습니다.

다음 내용을 읽기 전에 문제에 라벨을 지정했는지 확인하세요.

- 답장 후 라벨 [답장 대기 중][label-waiting-for-reply] 제거: 라벨 [답장 대기 중][label-waiting-for-reply]
[이슈 생성][이슈-선택] 시 추가됩니다.응답 취소 문제를 보다 편리하게 배치할 수 있으므로 제거해야 합니다.
이 라벨을 검토한 후 확인하세요.제거하지 않으면 다른 사람들이 같은 문제를 확인하는 데 시간을 낭비하게 됩니다.
- 문제 해결 여부가 확실하지 않은 경우 [Waiting for review][label-waiting-for-review]로 표시: 두 가지 상황이 있습니다.
문제를 검토할 때하나는 문제가 발견되었거나 해결되었다는 것입니다. [PR 생성](./submit-code.md)이 필요할 수도 있습니다.
필요할 때.둘째, 이 문제에 대해 확실하지 않은 경우 [검토 대기 중][label-waiting-for-review] 라벨을 붙일 수 있습니다.
두 번째 확인을 위해 다른 사람을 언급합니다.

이슈가 Pull Request를 생성해야 하는 경우 아래에서 라벨을 붙일 수도 있습니다.

|라벨 |평균 |
|------------------|--------------------------------|
|[자질구레][라벨-자질구레] |프로젝트의 집안일 |
|[좋은 첫 번째 이슈][label-good-first-issue] |새로운 기여자를 위한 좋은 첫 번째 호 |
|[수정하기 쉬움][라벨-수정하기 쉬움] |고치기는 쉽지만 '좋은 첫 번째 이슈'보다 어렵다 |
|[도움말 구함][label-help-구함] |도움 구함 |

> 참고: 라벨을 추가하거나 삭제할 수 있는 권한은 회원에게만 있습니다.레벨을 추가하거나 제거해야 하는데 회원이 아닌 경우,
> `@` 회원이 그렇게 할 수 있습니다.하지만 GitHub 계정이 있으면 문제에 대해 의견을 제시하고 제안을 제공할 수 있습니다.
> 커뮤니티의 모든 사람이 문제에 대해 의견을 제시하고 답변하도록 권장합니다.

### 풀 요청

<!-- markdown-link-check-disable -->

Review Pull은 GitHub의 [Pull Requests][all-prs]에서 논의하고 이에 대한 제안을 제공하는 것을 의미합니다.돌핀스케줄러의
Pull Request 검토는 [GitHub의 Pull Request 변경 사항 검토][gh-review-pr]와 동일합니다.당신은 당신의
Pull Reque의 제안-->- Pull Request가 merge에 괜찮다고 판단되면 "Approve" 과정에 따라 Pull Request에 동의하면 됩니다.
[GitHub의 풀 요청 변경 사항 검토][gh-review-pr]에서.
- Pull Request를 변경해야 한다고 생각되면, "Comment" 프로세스에 따라 Comment를 작성하시면 됩니다.
[GitHub의 풀 요청 변경 사항 검토 중][gh-review-pr].그리고 그 전에 해결해야 할 문제가 있다고 생각할 때
병합된 경우 [GitHub의 풀 요청 변경 사항 검토][gh-review-pr]의 "변경 요청"을 따라 기여자에게 문의하세요.
수정하세요.

<!-- markdown-link-check-enable -->

Labeled Pull Requests는 중요한 부분입니다.합리적인 분류는 검토자의 시간을 많이 절약할 수 있습니다.좋은 소식
[Issues](#issues)에서 Pull Request의 레이블 이름과 사용법이 동일하여 메모리를 줄일 수 있다는 점입니다.에 대한
예를 들어 Pull Request가 있는 경우 Docker 및 블록 배포와 관련이 있습니다.[docker][label-docker]로 라벨을 붙일 수 있습니다.
및 [우선순위:높음][라벨-우선순위-높음].

Pull Request에는 고유한 라벨이 있습니다.

|라벨 |평균 |
|--------------------------------------------|-----------------------------------------------|
|[문서 누락][label-miss-문서] |끌어오기 요청에 문서가 누락되어 추가해야 합니다 |
|[최초 기여자][label-first-time-contributor] |처음 기여자가 풀 요청을 제출 |
|[병합하지 않음][레이블-병합하지 않음] |Pull Request에는 문제가 있어 병합하면 안 됩니다 |

> 참고: 라벨을 추가하거나 삭제할 수 있는 권한은 회원에게만 있습니다.레벨을 추가하거나 제거해야 하는데 회원이 아닌 경우,
> `@` 회원이 그렇게 할 수 있습니다.하지만 GitHub 계정이 있으면 Pull Request에 댓글을 달고 제안을 할 수 있습니다.
> 커뮤니티의 모든 사람이 Pull Request를 검토하도록 권장합니다.[메일 검토 원함]: https://lists.apache.org/thread/9flwlzrp69xjn6v8tdkbytq8glqp2k51
[토론-결과-검토-원함]: https://github.com/apache/dolphinscheduler/discussions/7545
[라벨-wontfix]: https://github.com/apache/dolphinscheduler/labels/wontfix
[라벨-중복]: https://github.com/apache/dolphinscheduler/labels/duplicate
[라벨-필요-모어-정보]: https://github.com/apache/dolphinscheduler/labels/need%20more%20information
[라벨-win-os]: https://github.com/apache/dolphinscheduler/labels/win-os
[레이블-대기-응답]: https://github.com/apache/dolphinscheduler/labels/Waiting%20for%20reply
[라벨-대기-검토]: https://github.com/apache/dolphinscheduler/labels/Waiting%20for%20review
[라벨-사용자-경험]: https://github.com/apache/dolphinscheduler/labels/user%20experience
[라벨 개발]: https://github.com/apache/dolphinscheduler/labels/development
[라벨-ui]: https://github.com/apache/dolphinscheduler/labels/UI
[라벨 제안]: https://github.com/apache/dolphinscheduler/labels/suggestion
[라벨 보안]: https://github.com/apache/dolphinscheduler/labels/security
[라벨-파이썬]: https://github.com/apache/dolphinscheduler/labels/Python
[라벨 플러그인]: https://github.com/apache/dolphinscheduler/labels/plug-in
[라벨 문서]: https://github.com/apache/dolphinscheduler/labels/document
[라벨-docker]: https://github.com/apache/dolphinscheduler/labels/docker
[모든 라벨 목록]: https://github.com/apache/dolphinscheduler/labels
[라벨-chore]: https://github.com/apache/dolphinscheduler/labels/Chore
[label-good-first-issue]: https://github.com/apache/dolphinscheduler/labels/good%20first%20issue
[라벨-도움말-원함]: https://github.com/apache/dolphinscheduler/labels/help%20wanted
[수정하기 쉬운 라벨]: https://github.com/apache/dolphinscheduler/labels/easy%20to%20fix
[라벨 우선순위-높음]: https://github.com/apache/dolphinscheduler/labels/priority%3Ahigh
[라벨 우선순위-중간]: https://github.com/apache/dolphinscheduler/labels/priority%3Amiddle
[라벨 우선 순위 낮음]: https://github.com/apache/dolphinscheduler/labels/priority%3Alow
[라벨-미스-문서]: https://github.com/apache/dolphinscheduler/labels/miss%20document
[라벨 최초 기여자]: https://github.com/apache/dolphinscheduler/labels/first%20time%20contributor
[레이블 병합하지 않음]: https://github.com/apache/dolphinscheduler/labels/don%27t%20merge
[라벨-e2e]: https://github.com/apache/dolphinscheduler/labels/e2e
[라벨-필요-확인]: https://github.com/apache/dolphinscheduler/labels/need%20to%20verify
[문제 선택]: https://github.com/apache/dolphinscheduler/issues/new/choose
[모든 문제]: https://github.com/apache/dolphinscheduler/issues
[모든-prs]: https://github.com/apache/dolphinscheduler/pulls
[gh-review-pr]: https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/reviewing-changes-in-pull-requests/about-pull-request-reviews