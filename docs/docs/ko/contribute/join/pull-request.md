# 풀 요청 공지

## 서문

풀 리퀘스트(Pull Request)는 소프트웨어 협력 방식으로, 서로 다른 기능이 포함된 코드를 트렁크로 가져오는 프로세스입니다.이 과정에서 코드에 대한 논의, 검토, 수정이 가능합니다.

Pull Request에서는 코드 구현에 대해 논의하지 않으려고 노력합니다.코드의 일반적인 구현과 해당 논리는 Issue에서 결정되어야 합니다.Pull Request에서는 구현에 대한 서로 다른 의견으로 인한 시간 낭비를 피하기 위해 코드 형식과 코드 사양에만 중점을 둡니다.

## 사양

### 풀 요청 제목

제목 형식: [`풀 요청 유형`-`문제 번호`][`모듈 이름`] `풀 요청 설명`

`Pull Request Type`과 `Issue Type`의 대응 관계는 다음과 같습니다.

<테이블>
<머리>
<tr>
<th style="width: 10%; text-align: center;">문제 유형</th>
<th style="width: 20%; text-align: center;">풀 요청 유형</th>
<th style="width: 20%; text-align: center;">예(이슈 번호가 3333이라고 가정)</th>
</tr>
</thead>
<몸>
<tr>
<td style="text-align: center;">기능</td>
<td style="text-align: center;">기능</td>
<td style="text-align: center;">[Feature-3333][서버] xxx 구현</td>
</tr>
<tr>
<td style="text-align: center;">버그</td>
<td style="text-align: center;">수정</td>
<td style="text-align: center;">[Fix-3333][ui] xxx 수정</td>
</tr>
<tr>
<td style="text-align: center;">개선</td>
<td style="text-align: center;">개선</td>
<td style="text-align: center;">[Improvement-3333][alert] xxx 성능 개선</td>
</tr>
<tr>
<td style="text-align: center;">테스트</td>
<td style="text-align: center;">테스트</td>
<td style="text-align: center;">[Test-3333][api] xxx의 e2e 테스트 추가</td>
</tr>
<tr>
<td style="text-align: center;">문서</td>
<td style="text-align: center;">문서</td>
<td style="text-align: center;">[Doc-3333] xxx 개선</td>
</tr>
<tr>
<td style="text-align: center;">E2E</td>
<td style="text-align: center;">E2E</td>
<td style="text-align: center;">[E2E-3333] xxx 구현</td>
</tr>
<tr>
<td style="text-align: center;">CI</td>
<td style="text-align: center;">CI</td>
<td style="text-align: center;">[CI] xxx 개선</td>
</tr>
<tr>
<td style="text-align: center;">자질구레</td>
<td style="text-align: center;">자질구레</td>
<td style="text-align: center;">[자주 하는 일] xxx 개선</td>
</tr>
</tbody>
</table>

`Issue No`는 해결하려는 현재 Pull Request에 해당하는 Issue 번호를 의미하며 `Module Name`은 Issue의 `Module Name`과 동일합니다.

### 풀 요청 분기

브랜치 이름 형식: `풀 요청 유형`-`이슈 번호`.예를 들어특집-3333

### 풀 요청 콘텐츠

커밋 메시지 섹션을 참조하세요.

### 끌어오기 요청 코드 스타일

[//]: # (TODO: 웹사이트 템플릿이 이 구문을 지원하면 아래에 주석이 달린 앵커를 사용하세요)
[//]: # (DolphinScheduler는 `Spotless`를 사용하여 코드 스타일과 형식 오류를 자동으로 수정합니다.)
[//]: # (자세한 내용은 [개발 환경 설정]&#40;../development-environment-setup.md#code-style&#41; `코드 스타일` 섹션을 참조하세요.)

DolphinScheduler는 'Spotless'를 사용하여 코드 스타일 및 형식 오류를 자동으로 수정합니다.
자세한 내용은 [개발 환경 설정](../development-environment-setup.md) `코드 스타일` 섹션을 참조하세요.

### 질문

- 여러 문제 시나리오에 대한 하나의 Pull Request를 처리하는 방법.우선, 여러 문제에 대한 하나의 끌어오기 요청에 대한 시나리오가 더 적습니다.
근본 원인은 여러 문제가 동일한 작업을 수행해야 한다는 것입니다.
일반적으로 이 시나리오에는 두 가지 솔루션이 있습니다. 첫 번째는 여러 이슈를 동일한 이슈로 병합한 다음 다른 이슈를 닫는 것입니다.
두 번째는 여러 문제에 미묘한 차이가 있다는 것입니다.
이 시나리오에서는 각 문제의 책임을 명확하게 나눌 수 있습니다.각 이슈의 유형은 Sub-Task로 표시되며, 이러한 하위 태스크 유형 이슈는 하나의 이슈와 연결됩니다.
그리고 제출된 각 끌어오기 요청은 하위 작업의 하나의 문제에만 연결되어야 합니다.