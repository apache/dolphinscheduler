# Apache Dolphinscheduler의 커미터가 되는 방법

Dolphinscheduler 프로젝트 관리 위원회(PMC)는 후보자의 기여도를 평가하는 일을 담당합니다.

많은 Apache 프로젝트와 마찬가지로 Dolphinscheduler는 코드 기여, 블로그 항목, 신규 사용자를 위한 가이드, 공개 연설, 다양한 방식의 프로젝트 개선을 포함한 모든 기여를 환영합니다.

Dolphinscheduler에 기여하기 시작하려면 커미터가 되어 기여하는 방법을 알아보세요. 누구든지 프로젝트에 패치, 문서 및 예제를 제출할 수 있습니다.

PMC는 Dolphinscheduler에 대한 기여를 기반으로 활성 기여자로부터 정기적으로 새로운 커미터를 추가합니다.새로운 커미터의 자격은 다음과 같습니다.

Dolphinscheduler에 대한 지속적인 기여: 커미터는 Dolphinscheduler에 대한 주요 기여 이력을 가지고 있어야 합니다.이상적인 커미터는 프로젝트 전반에 걸쳐 광범위하게 기여하고 "소유권" 역할을 맡은 부분에서 적어도 하나의 주요 구성 요소에 기여한 것입니다.소유권 역할은 기존 기여자가 이 사람에 의해 이 구성 요소에 대한 패치를 실행해야 한다고 생각한다는 것을 의미합니다.

기여의 질: 다른 커뮤니티 구성원보다 커미터는 간단하고 잘 테스트되었으며 잘 설계된 패치를 제출해야 합니다.또한 패치가 Dolphinscheduler의 엔지니어링 관행(테스트 가능성, 문서화, API 안정성, 코드 스타일 등)에 적합한지 확인하는 것을 포함하여 패치를 검토할 수 있는 충분한 전문 지식을 보여야 합니다.위원회는 Dolphinscheduler의 소프트웨어 품질과 유지 관리 가능성에 대해 공동으로 책임을 집니다.핵심 모듈과 같이 Dolphinscheduler의 중요한 부분에 대한 기여는 품질을 평가할 때 더 높은 기준으로 유지됩니다.이러한 영역의 기여자는 변경 사항에 대해 더 많은 검토를 받게 됩니다.

커뮤니티 참여: 커미터는 모든 커뮤니티 상호 작용에서 건설적이고 우호적인 태도를 취해야 합니다.또한 개발자 및 사용자 목록에서 활동하고 새로운 기여자와 사용자를 멘토링하는 데 도움을 주어야 합니다.디자인 논의에서 커미터는 의견 불일치에도 불구하고 전문적이고 외교적인 접근 방식을 유지해야 합니다.

## 새로운 커미터 지명

Dolphinscheduler에서는 **새 커미터 지명**은 기존 PMC 회원만 공식적으로 시작할 수 있습니다.새로운 커미터가 자신이 자격이 있다고 생각하면 기존 PMC 구성원에게 연락하여 논의해야 합니다.PMC 일부 구성원이 이에 동의하면 프로세스가 시작됩니다.

다음 단계를 권장합니다(기존 PMC 구성원만 시작).
1. `[DISCUSS] xxx를 새 커미터로 승격`이라는 제목의 이메일을 `private@dolphinscheduler.apache.org`로 보냅니다.후보자의 중요한 기여를 나열하십시오.
귀하의 제안에 대해 다른 PMC 구성원의 지지를 얻을 수 있습니다.
2. 명시적인 반대나 우려 사항이 없는 한 토론을 3일 이상, 최대 1주일 동안 열어두십시오.
3. PMC가 일반적으로 제안에 동의하는 경우 `[VOTE] Promote xxx as new committer`라는 제목의 이메일을 `private@dolphinscheduler.apache.org`로 보냅니다.
4. 투표 과정을 3일 이상, 최대 1주일 동안 열어두세요.'NO' 거부권이 있는 '3 + 1' 투표가 있는 경우 결과를 '합의 승인'으로 간주합니다.+1표 > -1표에 유의하세요.
5. `[RESULT][VOTE] Promote xxx as new committer`라는 제목의 이메일을 `private@dolphinscheduler.apache.org`로 보내고 유권자가 누구인지 포함한 투표 세부 정보를 나열합니다.

## 새로운 커미터 초대

프로모션을 시작하는 PMC 멤버는 새로운 커미터에게 초대장을 보내고 ASF 환경 설정을 안내하는 역할을 담당합니다.

PMC 구성원은 다음 템플릿을 사용하여 새 커미터에게 이메일을 보내야 합니다.```
To: <invitee name>@gmail.com
Cc: private@dolphinscheduler.apache.org
Subject: Invitation to become dolphinscheduler committer: <invitee name>

Hello <invitee name>,

The Dolphinscheduler Project Management Committee] (PMC) 
hereby offers you committer privileges to the project. These privileges are
offered on the understanding that you'll use them
reasonably and with common sense. We like to work on trust
rather than unnecessary constraints.

Being a committer enables you to more easily make 
changes without needing to go through the patch 
submission process. 

Being a committer does not require you to 
participate any more than you already do. It does 
tend to make one even more committed.  You will 
probably find that you spend more time here.

Of course, you can decline and instead remain as a 
contributor, participating as you do now.

A. This personal invitation is a chance for you to 
accept or decline in private.  Either way, please 
let us know in reply to the [private@dolphinscheduler.apache.org] 
address only.

B. If you accept, the next step is to register an iCLA:
    1. Details of the iCLA and the forms are found 
    through this link: http://www.apache.org/licenses/#clas

    2. Instructions for its completion and return to 
    the Secretary of the ASF are found at
    http://www.apache.org/licenses/#submitting

    3. When you transmit the completed iCLA, request 
    to notify the Apache Dolphinscheduler and choose a 
    unique Apache id. Look to see if your preferred 
    id is already taken at 
    http://people.apache.org/committer-index.html     
    This will allow the Secretary to notify the PMC 
    when your iCLA has been recorded.

When recording of your iCLA is noticed, you will 
receive a follow-up message with the next steps for 
establishing you as a committer.
````

## 초대 수락 과정

새로운 커미터는 `private@dolphinscheduler.apache.org`(`모두 응답` 선택)에 응답하고 초대를 수락하겠다는 의사를 표현해야 합니다.
그러면 이 초대는 프로젝트의 PMC에 의해 수락된 것으로 간주됩니다.물론, 새로운 커미터가 초대를 거절할 수도 있습니다.

초대가 수락되면 새 커미터는 다음 단계를 수행해야 합니다.
1. `dev@dolphinscheduler.apache.org`를 구독하세요.일반적으로 이 작업은 이미 완료되었습니다.
2. [apache 커미터 목록 페이지](http://people.apache.org/committer-index.html)에 없는 Apache ID를 선택하세요.
3. [ICLA](https://www.apache.org/licenses/icla.pdf)를 다운로드합니다(새 커미터가 본업으로 프로젝트에 기여하는 경우 [CCLA](http://www.apache.org/licenses/cla-corporate.pdf)가 필요합니다).
4. `icla.pdf`(또는 `ccla.pdf`)에 올바른 정보를 입력한 후 인쇄하여 자필 서명한 후 PDF로 스캔하여 첨부 파일로 [secretary@apache.org](mailto:secretary@apache.org)로 보냅니다.(전자 서명을 선호하는 경우 [이 페이지](http://www.apache.org/licenses/contributor-agreements.html#submitting)의 단계를 따르십시오.)
5. PMC는 Apache 비서가 ICLA(또는 CCLA) 제출을 확인할 때까지 기다립니다.새로운 커미터와 PMC는 다음 이메일을 받게 됩니다:```
Dear XXX,

This message acknowledges receipt of your ICLA, which has been filed in the Apache Software Foundation records.

Your account has been requested for you and you should receive email with next steps
within the next few days (can take up to a week).

Please refer to https://www.apache.org/foundation/how-it-works.html#developers
for more information about roles at Apache.
````

계정이 아직 요청되지 않은 경우 PMC 구성원은 프로젝트 V.P.에게 문의해야 합니다.
V.P.[Apache 계정 제출 도우미 양식](https://whimsy.apache.org/officers/acreq)을 통해 요청할 수 있습니다.

며칠 후 새 커미터는 'ASF(Apache Software Foundation)에 오신 것을 환영합니다!'라는 제목의 계정 생성을 확인하는 이메일을 받게 됩니다.
축하해요!이제 새로운 커미터는 공식 Apache ID를 갖게 되었습니다.

PMC 회원은 [roster](https://whimsy.apache.org/roster/pmc/dolphinscheduler)를 통해 공식 커미터 목록에 새로운 커미터를 추가해야 합니다.

## Apache ID 및 개발 환경 설정

1. [Apache 계정 유틸리티 플랫폼](https://id.apache.org/)으로 이동하여 비밀번호를 생성하고 개인 사서함(`전달 이메일 주소`)과 GitHub 계정(`GitHub 사용자 이름`)을 설정합니다.그 후 곧(2시간 이내) 이메일을 통해 조직 초대가 전송됩니다.
2. `xxx@apache.org` 이메일 서비스를 이용하시려면 [여기](https://infra.apache.org/committer-email.html)를 참고하시기 바랍니다.대부분의 사서함 서비스 설정에서 이 전달 모드를 찾기가 쉽지 않기 때문에 Gmail을 사용하는 것이 좋습니다.
3. 'Authorized GitHub 2FA wiki'를 따라 [GitHub](http://github.com/)에서 2단계 인증(2FA)을 활성화하세요.2FA를 "off"로 설정하면 다시 설정할 때까지 해당 Apache 커미터 쓰기 권한 그룹에 의해 목록에서 삭제됩니다.(**참고: 복구 코드도 비밀번호와 마찬가지로 주의 깊게 다루세요!**)
4. [GitBox 계정 연결 유틸리티](https://gitbox.apache.org/setup/)를 사용하여 Dolphinscheduler 프로젝트에 대한 쓰기 권한을 얻습니다.

Apache GitHub 조직에 공개적으로 표시하려면 [Apache GitHub 조직 사용자 페이지](https://github.com/orgs/apache/people)로 이동해야 합니다.
자신을 검색하고 '조직 공개'를 '공개'로 선택하세요.

## 커미터의 권리, 의무 및 책임

Dolphinscheduler 프로젝트는 커미터가 된 후에도 지속적인 기여를 요구하지 않지만, 우리 커뮤니티에서 계속해서 역할을 해주시기를 진심으로 바랍니다!

커미터로서 당신은 할 수 있습니다
1. Apache 리포지토리의 마스터 브랜치에 대한 풀 요청을 검토하고 병합합니다.풀 요청에는 여러 커밋이 포함되는 경우가 많습니다.이러한 커밋은 **설명 주석**이 포함된 단일 커밋으로 **눌러지고 병합되어야 합니다**.새로운 커미터는 선임 커미터에게 풀 요청 재확인을 요청하는 것이 좋습니다.
2. Apache 저장소의 새 분기에 코드를 생성하고 푸시합니다.
3. 새 릴리스를 준비합니다.준비하기 전에 커미터 팀에 확인하는 것을 잊지 마세요. 지금이 릴리스를 생성하기에 적절한 시기이기 때문입니다.

PMC는 비록 그들의 투표가 '+1 구속력 없음'으로 간주되더라도 새로운 커미터가 릴리스 투표뿐만 아니라 릴리스 프로세스에도 참여하기를 희망합니다.
PMC 회원으로 승진하려면 릴리스 프로세스를 잘 아는 것이 중요합니다.

## 프로젝트관리위원회

PMC(프로젝트 관리 위원회) 구성원은 코드 기여에 대한 특별한 권한이 없습니다.
그들은 단순히 프로젝트를 감독하고 프로젝트가 Apache 요구 사항을 준수하는지 확인합니다.그 기능은 다음과 같습니다:

1. 릴리스 및 라이선스 확인을 위한 구속력 있는 투표
2. 새로운 커미터 및 PMC 회원 표창;
3. 브랜드 이슈 식별 및 브랜드 보호그리고
4. ASF 이사회가 제기한 질문에 답변하고 필요한 조치를 취합니다.

V.P.PMC 의장은 이사회 보고서 초기화를 담당하는 간사입니다.

대부분의 경우 커미터 팀에서 새로운 PMC 멤버가 지명됩니다.그러나 PMC가 지명에 동의하고 후보자가 준비가 되었다고 확신하는 한 직접 PMC 회원이 되는 것도 가능합니다.예를 들어, 이는 아파치 회원, 아파치 임원 또는 다른 프로젝트의 PMC 회원이었다는 사실로 입증될 수 있습니다.새로운 PMC 투표 프로세스도 새 커미터에 대한 투표 프로세스와 마찬가지로 개인 메일 목록을 사용하여 `[DISCUSS]`, `[VOTE]` 및 `[RESULT][VOTE]` 절차를 따라야 합니다.
초대장을 보내기 전에 PMC는 Apache 보드에 NOTICE 메일도 보내야 합니다.```
To: board@apache.org
Cc: private@dolphinscheduler.apache.org
Subject: [NOTICE] Jane Doe for Dolphinscheduler PMC

Dolphinscheduler proposes to invite Jane Doe (janedoe) to join the PMC.

(include if a vote was held) The vote result is available here: https://lists.apache.org/...
````

72시간 후에도 이사회가 지명에 반대하지 않으면(대부분의 경우는 그렇지 않음) 후보자에게 초대장이 발송될 수 있습니다.

초대가 수락되면 PMC 회원은 [명부](https://whimsy.apache.org/roster/pmc/dolphinscheduler)를 통해 공식 PMC 목록에 새 회원을 추가해야 합니다.