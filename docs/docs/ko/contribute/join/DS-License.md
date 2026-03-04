# 라이센스 공지

DolphinScheduler는 ASF(Apache Software Foundation)에서 진행 중인 오픈 소스 프로젝트이므로 DolphinScheduler 기여자가 되려면 Apache 방식을 따라야 합니다.게다가 Apache는 라이센스에 따라 매우 엄격한 규칙을 가지고 있습니다.이 구절에서는 ASF 라이센스와 DolphinScheduler에 참여할 때 초기 단계에서 라이센스 위험을 피하는 방법을 설명합니다.

참고: 이 문서는 Apache 프로젝트에만 적용됩니다.

### Apache 프로젝트에 대한 라이센스가 허용될 수 있습니다.

DolphinScheduler(또는 다른 Apache 프로젝트)에 새로운 기능을 추가하려는 경우 Apache 프로젝트가 지원하는 다음 오픈 소스 소프트웨어 프로토콜에 주의해야 합니다. 이 기능은 다른 오픈 소스 소프트웨어 참조를 참조합니다.

[ASF 제3자 라이선스 정책](https://apache.org/legal/resolved.html)

위 정책에 타사 소프트웨어가 없는 경우 귀하의 코드가 감사를 통과할 수 없다는 점을 유감스럽게 생각하며 다른 대체 계획을 검색해 보시기 바랍니다.

또한 프로젝트에 새로운 종속성을 요구할 경우 영향의 이유와 결과에 대해 dev@dolphinscheduler.apache.org로 이메일을 보내 논의해 주시기 바랍니다.게다가 전체 단계를 완료하려면 PPMC로부터 최소 3개의 긍정적인 투표가 필요합니다.

### DolphinScheduler에서 타사 오픈 소스 소프트웨어를 합법적으로 사용하는 방법

또한, 새로운 소프트웨어(제3자 jar, 텍스트, CSS, js, 사진, 아이콘, 오디오 등 및 제3자 파일을 기반으로 한 수정 사항에 국한되지 않음)를 프로젝트에 참조하려는 경우 ASF의 허가와 더불어 해당 소프트웨어를 합법적으로 사용해야 합니다.다음 문서를 참조하세요.

* [커뮤니티 주도 개발 "APACHE 방식"](https://apache.org/dev/licensing-howto.html)

예를 들어 ZooKeeper를 사용할 때 프로젝트에 ZooKeeper의 NOTICE 파일(모든 오픈 소스 프로젝트에는 일반적으로 루트 디렉터리 아래에 NOTICE 파일이 있음)이 포함되어야 합니다.Apache에서 설명하는 것처럼 "저작물"은 저작물에 포함되거나 첨부된 저작권 표시에 표시된 대로 라이센스에 따라 제공되는 소스 또는 개체 형식의 저작물을 의미합니다.

우리는 모든 제3자 오픈 소스 라이선스 정책을 자세히 다루지는 않을 것입니다. 관심이 있다면 찾아보실 수 있습니다.

### DolphinScheduler-라이센스 확인 규칙

일반적으로 우리 프로젝트에는 라이센스 확인 스크립트가 있습니다.DolphinScheduler-License는 [kezhenxu94](https://github.com/kezhenxu94)에서 제공되는데, 이는 다른 오픈소스 프로젝트와 약간 다릅니다.전체적으로 우리는 처음에 라이센스 문제를 피하려고 노력하고 있습니다.

새로운 jar이나 외부 리소스를 추가해야 할 경우 다음 단계를 따라야 합니다.

* Known-dependent.txt에 jar 파일의 이름과 버전을 추가합니다.
* 'dolphinscheduler-dist/release-docs/LICENSE' 디렉터리에 관련 Maven 저장소 주소를 추가하세요.
* 'dolphinscheduler-dist/release-docs/NOTICE' 디렉터리에 관련 NOTICE 파일을 추가하고 원본 저장소와 다르지 않은지 확인하세요.
* 'dolphinscheduler-dist/release-docs/license/' 디렉토리에 해당 소스코드 프로토콜을 추가하고, 파일명은 License+filename.txt로 해야 합니다.예: License-zk.txt

### 참고자료

* [커뮤니티 주도 개발 "APACHE 방식"](https://apache.org/dev/licensing-howto.html)
* [ASF 제3자 라이선스 정책](https://apache.org/legal/resolved.html)