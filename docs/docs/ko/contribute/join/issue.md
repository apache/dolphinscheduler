# 이슈 공지

## 서문

이슈 기능은 다양한 기능, 버그, 기능 등을 추적하는 데 사용됩니다. 프로젝트 관리자는 이슈를 통해 완료할 작업을 구성할 수 있습니다.

이슈는 기능이나 버그를 도출하는 중요한 단계이며,
그리고 이슈에서 논의할 수 있는 내용은 기능, 기존 버그의 원인, 예비 방안에 대한 연구, 해당 구현 설계 및 코드 설계에만 국한되지 않습니다.

그리고 이슈가 승인된 경우에만 해당 Pull Request를 구현해야 합니다.

이슈가 큰 기능에 해당하는 경우 기능 모듈 및 기타 차원에 따라 여러 개의 작은 이슈로 나누는 것이 좋습니다.

## 사양

### 문제 제목

제목 형식: [`문제 유형`][`모듈 이름`] `문제 설명`

'문제 유형'은 다음과 같습니다.

<테이블>
<머리>
<tr>
<th style="width: 10%; text-align: center;">문제 유형</th>
<th style="width: 20%; text-align: center;">설명</th>
<th style="width: 20%; text-align: center;">예</th>
</tr>
</thead>
<몸>
<tr>
<td style="text-align: center;">기능</td>
<td style="text-align: center;">예상되는 기능 포함</td>
<td style="text-align: center;">[기능][api] xxx 컨트롤러에 xxx api 추가</td>
</tr>
<tr>
<td style="text-align: center;">버그</td>
<td style="text-align: center;">프로그램 내 버그</td>
<td style="text-align: center;">[Bug][api] xxx일 때 예외 발생</td>
</tr>
<tr>
<td style="text-align: center;">개선</td>
<td style="text-align: center;">코드 형식, 프로그램 성능 등에 국한되지 않고 현재 프로그램의 일부 개선</td>
<td style="text-align: center;">[개선][서버] 마스터와 작업자 간 xxx 개선</td>
</tr>
<tr>
<td style="text-align: center;">테스트</td>
<td style="text-align: center;">특히 테스트 사례의 경우</td>
<td style="text-align: center;">[테스트][서버] xxx e2e 테스트 추가</td>
</tr>
<tr>
<td style="text-align: center;">하위 작업</td>
<td style="text-align: center;">이러한 작업은 일반적으로 기능 클래스의 하위 작업입니다.대규모 기능의 경우 여러 개의 작은 하위 작업으로 나누어 하나씩 완료할 수 있습니다.</td>
<td style="text-align: center;">[하위 작업][서버] xxx에서 xxx 구현</td>
</tr>
</tbody>
</table>

'모듈 이름'은 다음과 같습니다.<테이블>
<머리>
<tr>
<th style="width: 10%; text-align: center;">모듈 이름</th>
<th style="width: 20%; text-align: center;">설명</th>
</tr>
</thead>
<몸>
<tr>
<td style="text-align: center;">경고</td>
<td style="text-align: center;">경고 모듈</td>
</tr>
<tr>
<td style="text-align: center;">API</td>
<td style="text-align: center;">응용 프로그램 인터페이스 계층 모듈</td>
</tr>
<tr>
<td style="text-align: center;">서비스</td>
<td style="text-align: center;">애플리케이션 서비스 레이어 모듈</td>
</tr>
<tr>
<td style="text-align: center;">다오</td>
<td style="text-align: center;">애플리케이션 데이터 액세스 레이어 모듈</td>
</tr>
<tr>
<td style="text-align: center;">플러그인</td>
<td style="text-align: center;">플러그인 모듈</td>
</tr>
<tr>
<td style="text-align: center;">원격</td>
<td style="text-align: center;">통신 모듈</td>
</tr>
<tr>
<td style="text-align: center;">서버</td>
<td style="text-align: center;">서버 모듈</td>
</tr>
<tr>
<td style="text-align: center;">UI</td>
<td style="text-align: center;">프런트 엔드 모듈</td>
</tr>
<tr>
<td style="text-align: center;">문서-zh</td>
<td style="text-align: center;">중국어 문서 모듈</td>
</tr>
<tr>
<td style="text-align: center;">문서</td>
<td style="text-align: center;">영어 문서 모듈</td>
</tr>
<tr>
<td style="text-align: center;">...</td>
<td style="text-align: center;">-</td>
</tr>
</tbody>
</table>

### 이슈 콘텐츠 템플릿

https://github.com/apache/dolphinscheduler/tree/dev/.github/ISSUE_TEMPLATE

### 기여자

일부 특별한 경우를 제외하고는 이슈별 논의나 메일링 리스트를 통해 디자인 방식을 결정하거나 디자인 방식을 제공하는 것이 좋습니다.
문제를 완료하기 전에 코드 구현 설계도 마찬가지입니다.

해결 방법이 여러 가지일 경우 메일링 리스트나 이슈가 되는 투표를 통해 결정하는 것이 좋습니다.
이 문제는 최종 계획과 코드 구현 설계가 승인된 후에 구현될 수 있습니다.
이는 풀 리퀘스트 검토 단계에서 구현 설계나 재구성에 대한 서로 다른 의견으로 인해 발생하는 시간 낭비를 방지하기 위한 것입니다.

### 질문

- 문제를 제기한 사용자가 해당 문제에 해당하는 모듈을 알지 못하는 경우 어떻게 대처해야 합니까?

문제를 제기할 때 대부분의 사용자는 문제가 어느 모듈에 속하는지 알지 못하는 것이 사실입니다.
실제로 이는 많은 오픈 소스 커뮤니티에서 매우 일반적입니다.이 경우 커미터/기여자는 실제로 문제의 영향을 받는 모듈을 알고 있습니다.
커미터와 기여자가 승인한 후 이슈가 정말 가치 있는 경우 커미터는 이슈와 관련된 특정 모듈에 따라 이슈 제목을 수정할 수 있습니다.
또는 문제를 제기한 사용자에게 메시지를 남겨 해당 제목으로 수정하도록 하세요.