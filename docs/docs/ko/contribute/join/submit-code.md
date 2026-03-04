# 코드 제출

* 먼저 원격 저장소에서 *https://github.com/apache/dolphinscheduler.git* 코드 복사본을 자신의 저장소에 포크하세요.

* 현재 원격 저장소에는 세 가지 분기가 있습니다.

* 마스터 일반 배송 지점
안정 릴리스 후에는 안정 브랜치의 코드를 마스터에 병합하세요.

* 개발자 일일 개발 지점
매일 개발 개발 브랜치에서 새로 제출된 코드는 이 브랜치에 요청을 가져올 수 있습니다.
* 저장소를 로컬에 복제하세요.
`git clone https://github.com/apache/dolphinscheduler.git`
* upstream이라는 이름의 원격 저장소 주소를 추가합니다.
`git 원격 추가 업스트림 https://github.com/apache/dolphinscheduler.git`
* 저장소 보기
`git 원격 -v`

> 이때 리포지토리는 오리진(자신의 리포지토리)과 업스트림(원격 리포지토리) 2개로 구성됩니다.

* 원격 저장소 코드 가져오기/업데이트
`git fetch upstream`

* 원격 저장소 코드를 로컬 저장소에 동기화```
git checkout origin/dev
git merge --no-ff upstream/dev
````

원격 브랜치에 'dev-1.0'과 같은 새 브랜치가 있는 경우 이 브랜치를 로컬 저장소와 동기화해야 합니다.```
git checkout -b dev-1.0 upstream/dev-1.0
git push --set-upstream origin dev-1.0
````

* 새 지점 만들기```
git checkout -b xxx origin/dev
````

공식 개발 브랜치의 최신 코드에서 `xxx` 브랜치가 성공적으로 빌드되고 있는지 확인하세요.
* 새 브랜치에서 로컬로 코드를 수정한 후 자신의 저장소에 제출하세요.

`git commit -m '콘텐츠 커밋'`

`git push Origin xxx --set-upstream`

* 원격 저장소에 변경 사항 제출

* github 페이지에서 "New pull request"를 클릭하세요.

* 수정된 로컬 브랜치와 과거에 병합하려는 브랜치를 선택하고 "풀 리퀘스트 만들기"를 클릭하세요.

* 그런 다음 커뮤니티 커미터가 CodeReview를 수행한 후 몇 가지 세부 사항(설계, 구현, 성능 등 포함)에 대해 논의할 것입니다.팀의 모든 사람이 이 수정 사항에 만족하면 커밋이 dev 브랜치에 병합됩니다.

* 마지막으로 Dolphinscheduler의 공식 기여자가 되신 것을 축하합니다!