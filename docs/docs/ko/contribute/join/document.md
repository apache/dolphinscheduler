# 문서 공지

모든 유형의 소프트웨어에는 좋은 문서화가 중요합니다.DolphinScheduler 문서를 개선할 수 있는 기여를 환영합니다.

### 문서 프로젝트 가져오기

DolphinScheduler 프로젝트에 대한 문서는 별도의 [git 저장소](https://github.com/apache/dolphinscheduler-website)에 유지관리됩니다.

먼저 문서 프로젝트를 자신의 github 저장소로 포크한 다음 문서를 로컬 컴퓨터에 복제해야 합니다.```
git clone https://github.com/<your-github-user-name>/dolphinscheduler-website
````

### 문서 제작 가이드

1. 루트 디렉터리에서 `yarn`을 실행하여 종속성을 설치합니다.
2. 명령을 실행하여 리소스 수집
2.1.`export PROTOCOL_MODE=ssh`를 실행하면 HTTPS 프로토콜 대신 SSH 프로토콜을 통해 Git 복제 리소스를 알려줍니다.
2.2.`./scripts/prepare_docs.sh`를 실행하여 모든 관련 리소스를 준비하세요. 자세한 내용은 [스크립트 작업 준비 방법](https://github.com/apache/dolphinscheduler-website/blob/master/HOW_PREPARE_WORK.md)을 참조하세요.
3. 루트 디렉터리에서 `yarn generate`을 실행하여 데이터 형식을 지정하고 준비합니다.
4. 루트 디렉터리에서 `yarn dev`를 실행하여 로컬 서버를 시작하면 'http://localhost:3000'에 해당 웹사이트가 표시됩니다.```
Note: if you clone the code in Windows, not Mac or Linux. Please read the details below.
If you execute the commands like the two steps above, you will get the exception "UnhandledPromiseRejectionWarning: Error: EPERM: operation not permitted, symlink '2.0.3' -> 'latest'".
If you get the exception "Can't resolve 'antd' in xxx",you can run `yarn add antd` and `yarn install`.
Because the `./scripts/prepare_docs.sh` command requires a Linux environment, if you are on a Windows system, you can use WSL to complete this step.
When you encounter this problem. You can run the two steps in cmd.exe as an administrator on your Windows system.
````

5. `yarn build`를 실행하여 소스 코드를 빌드하면 `build`라는 디렉터리가 자동으로 생성되고 실행이 완료될 때까지 기다렸다가 `build` 디렉터리에 들어갑니다.
6. 로컬에서 변경 사항을 확인하세요. `python -m SimpleHTTPServer 8000`, Python 버전이 3인 경우 `python3 -m http.server 8000`을 대신 사용하세요.

더 높은 버전의 노드가 설치되어 있는 경우 'nvm'을 고려하여 머신에 서로 다른 버전의 '노드'가 공존하도록 허용할 수 있습니다.

1. [지침](http://nvm.sh)에 따라 nvm을 설치하세요.
2. `nvm install v18.12.1`을 실행하여 노드 v18을 설치합니다.
3. `nvm use v18.12.1`을 실행하여 작업 환경을 노드 v18로 전환합니다.

그러면 웹 사이트를 실행하고 구축할 준비가 모두 완료되었습니다.자세한 내용은 위의 빌드 지침을 따르세요.

### 문서 사양

1. ** 한자와 영어 또는 숫자 사이에는 공백이 필요하며 ** 한자 구두점과 영어 또는 숫자 사이에는 공백이 필요하지 않습니다. 이는 한-영 혼합의 심미성과 가독성을 향상시킵니다.

2. 일반적으로 '당신'을 사용하는 것이 좋습니다.물론, 경고 메시지가 나타날 때 등 필요한 경우에는 이 용어를 사용할 수 있습니다.

### 문서 Pull Request 제출 방법

1. "git add"를 사용하지 마십시오.모든 변경 사항을 커밋합니다.

2. 변경된 파일을 푸시하면 됩니다. 예:

* `*.md`
* `blog.js 또는 docs.js 또는 site.js`

3. 풀 요청을 **마스터** 브랜치에 제출합니다.

### 문서 참조

[Apache Flink 번역 사양](https://cwiki.apache.org/confluence/display/FLINK/Flink+Translation+Specifications)