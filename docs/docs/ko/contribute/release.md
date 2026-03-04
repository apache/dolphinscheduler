# 출시 안내

## 준비

이 섹션은 출시 전 작업으로 대부분 일회성이므로 **첫 번째 릴리스에만 필요합니다**.만약 당신이
이전에 출시된 경우 이 섹션을 다음 섹션으로 건너뛰세요.

### 환경 확인

DolphinScheduler 릴리스를 성공적으로 완료하려면 환경을 확인하고 다음 사항을 확인해야 합니다.
모든 조건이 충족되었으나 누락된 경우 설치하고 작동하는지 확인해야 합니다.```shell
# JDK 1.8 above is requests
java -version
# Maven requests
mvn -version
````

### GPG 설정

#### GPG 설치

[GnuPG 공식 홈페이지](https://www.gnupg.org/download/index.html)에서 설치 패키지를 다운로드하세요.
GnuPG 1.x 버전의 명령은 2.x 버전의 명령과 약간 다를 수 있습니다.
다음 지침에서는 `GnuPG-2.1.23` 버전을 예로 들어 설명합니다.

설치 후 다음 명령을 실행하여 버전 번호를 확인하세요.```shell
gpg --version
````

#### 키 생성

설치 후 다음 명령어를 실행하여 키를 생성합니다.

이 명령은 `GnuPG-2.x`를 사용할 수 있음을 나타냅니다.```shell
gpg --full-gen-key
````

이 명령은 `GnuPG-1.x`를 사용할 수 있음을 나타냅니다.```shell
gpg --gen-key
````

**주의 사항: 키 생성에는 Apache 메일과 해당 비밀번호를 사용하십시오.** 지침에 따라 키 생성을 완료합니다.```shell
gpg (GnuPG) 2.0.12; Copyright (C) 2009 Free Software Foundation, Inc.
This is free software: you are free to change and redistribute it.
There is NO WARRANTY, to the extent permitted by law.

Please select what kind of key you want:
  (1) RSA and RSA (default)
  (2) DSA and Elgamal
  (3) DSA (sign only)
  (4) RSA (sign only)
Your selection? 1
RSA keys may be between 1024 and 4096 bits long.
What keysize do you want? (2048) 4096
Requested keysize is 4096 bits
Please specify how long the key should be valid.
        0 = key does not expire
     <n>  = key expires in n days
     <n>w = key expires in n weeks
     <n>m = key expires in n months
     <n>y = key expires in n years
Key is valid for? (0)
Key does not expire at all
Is this correct? (y/N) y

GnuPG needs to construct a user ID to identify your key.

Real name: ${Input username}
Email address: ${Input email}
Comment: ${Input comment}
You selected this USER-ID:
   "${Inputed username} (${Inputed comment}) <${Inputed email}>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
You need a Passphrase to protect your secret key. # Input your Apache mail passwords
````

#### 생성된 키 확인```shell
gpg --list-keys
````

실행 결과:```shell
pub   4096R/85E11560 2019-11-15
uid                  ${Username} (${Comment}) <{Email}>
sub   4096R/A63BC462 2019-11-15
````

그 중 85E11560이 공개키 ID이다.

gpg2.0 버전 이후 형식이 변경되었습니다.```shell
pub   rsa4096 2023-07-01 [SC]
1234ABCD5678EFGH9012IJKL3456MNOP7890QRST
uid           [ultimate] ${用户名} <{邮件地址}>
sub   rsa4096 2023-07-01 [E]
````

그 중 1234ABCD5678EFGH9012IJKL3456MNOP7890QRST가 공개키 ID입니다.

#### 키 서버에 공개 키 업로드

명령은 다음과 같습니다:```shell
gpg --keyserver hkp://pool.sks-keyservers.net --send-key 85E11560
````

`pool.sks-keyservers.net`은 [공개 키 서버](https://keyserver.ubuntu.com)에서 무작위로 선택됩니다.
각 서버는 자동으로 서로 동기화되므로 백업 키 서버 중 하나를 선택해도 괜찮습니다.
`gpg --keyserver hkp://keyserver.ubuntu.com --send-key <YOUR_KEY_ID>`입니다.

### Apache Maven 중앙 저장소 구성

#### `settings-security.xml` 및 `settings.xml` 설정

이 섹션에서는 릴리스를 준비하기 위해 Apache 서버 Maven 구성을 추가합니다. 이에 따라 'settings-security.xml'을 추가해야 합니다.
먼저 [여기](http://maven.apache.org/guides/mini/guide-encryption.html)로 이동한 다음 `~/.m2/settings.xml`을 아래와 같이 변경하세요.```xml
<settings>
  <servers>
    <server>
      <id>apache.snapshots.https</id>
      <username> <!-- APACHE LDAP username --> </username>
      <password> <!-- APACHE LDAP encrypted password --> </password>
    </server>
    <server>
      <id>apache.releases.https</id>
      <username> <!-- APACHE LDAP username --> </username>
      <password> <!-- APACHE LDAP encrypted password --> </password>
    </server>
  </servers>
</settings>
````

## 출시

### 릴리스 문서 확인

마지막 릴리스와 비교하여 종속성 및 버전 변경이 있는 경우 현재 릴리스의 `release-docs`를 최신으로 업데이트해야 합니다.

- `dolphinscheduler-dist/release-docs/LICENSE`
- `dolphinscheduler-dist/release-docs/NOTICE`
- `dolphinscheduler-dist/release-docs/licenses`

### 환경에서 릴리스 설정

아래의 릴리스 버전, github 이름, Apache 사용자 이름을 여러 번 사용할 것이므로 저장하는 것이 좋습니다.
더 쉽게 사용할 수 있도록 변수를 bash합니다.```shell
VERSION=<THE-VERSION-YOU-RELEASE>
SOURCE_CODE_DIR=<YOUR-SOURCE-CODE-ROOT-DIR>  # the directory of your source code hold, the location of parent pom.xml instead of binary package

GH_USERNAME=<YOUR-GITHUB-USERNAME>
GH_REMOTE=<GITHUB-REMOTE>  # we use `upstream` or `origin` mostly base on your release environment

A_USERNAME=<YOUR-APACHE-USERNAME>
SVN_DIR=<PATH-TO-SVN-ROOT>  # to keep binary package checkout from SVN, the sub path end with `/dolphinscheduler/dev` and `/dolphinscheduler/release` will be create
````

> 참고: 환경을 설정한 후 아무것도 변경하지 않고 bash에서 직접 변수를 사용할 수 있습니다.예를 들어, 우리는
> `git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git` 명령을 사용하여 릴리스 브랜치를 복제할 수 있습니다.
> `"${VERSION}"`을 `<THE-VERSION-YOU-RELEASE>`로 변환하면 성공할 수 있습니다.하지만 `<VERSION>`을 수동으로 변경해야 합니다.
> 일부는 [vote mail](#vote-procedure)과 같은 bash 단계가 아니며, 출시를 알리기 위해 `"${VERSION}"` 대신 `<VERSION>`을 사용하고 있습니다.
> 관리자가 직접 변경해야 합니다.

### 문서 또는 코드 버전 업데이트

Maven 릴리스 전에 일부 문서를 업데이트해야 합니다.예를 들어 'VERSION' 버전을 출시하려면 다음 업데이트가 필요합니다.

- 코드 버전:
- `sql`:
- `dolphinscheduler_mysql.sql`: `t_ds_version`을 `VERSION`으로 업데이트해야 합니다.
- `dolphinscheduler_postgre.sql`: `t_ds_version`을 `VERSION`으로 업데이트해야 합니다.
- `dolphinscheduler_h2.sql`: `t_ds_version`을 `VERSION`으로 업데이트해야 합니다.
- `업그레이드`: 업그레이드 DDL 또는 DML이 있는 경우 `VERSION_schema`를 추가할지 여부, 추가된 DDL 또는 DML이 없으면 이 단계를 건너뛸 수 있습니다.
- `soft_version`: `VERSION`으로 업데이트해야 합니다.
- `deploy/docker/.env`: `HUB`가 `apache`로 변경되고 `TAG`가 `VERSION`으로 변경됩니다.
- `배포/kubernetes/dolphinscheduler`:
- `Chart.yaml`: `appVersion` 및 `version`을 x.y.z로 업데이트해야 합니다.
- `values.yaml`: `image.tag`를 x.y.z로 업데이트해야 합니다.
-`구성`
- `install-plugins.sh`: `dev-SNAPSHOT`을 x.y.z로 업데이트해야 합니다.
- 문서 버전:
- `<version>`(`pom` 제외)을 `docs` 디렉토리의 `x.y.z`로 변경합니다.
- 새로운 기록 버전 추가
- `docs/docs/en/history-versions.md` 및 `docs/docs/zh/history-versions.md`: 새 버전을 추가하고 `x.y.z`에 대한 링크를 추가합니다.
- `docs/configs/docsdev.js`: `/dev/`를 `/x.y.z/`로 변경하세요. **이 파일 이름을 변경하지 마세요** 웹사이트 도구에 의해 자동으로 변경됩니다.

> 참고: `VERSION`은 자리 유지 문자열이며 `VERSION=<THE-VERSION-YOU-RELEASE>`에 설정한 버전과 동일합니다.
> 웹사이트의 마스터 브랜치를 병합하기 전에 메인 웨어하우스의 dev 브랜치를 병합하세요.수정된 풀 요청이 병합되면 적용됩니다.

### NOTICE 파일에서 올바른 연도를 수정하세요.

NOTICE 파일을 확인하여 두 번째 줄의 올바른 연도를 현재 연도로 변경해야 합니다.확인할 파일은 다음과 같습니다
- `dolphinscheduler-dist/release-docs/NOTICE`
- `공지사항`

### 메이븐 릴리스

#### Maven 릴리스 확인

준비 분기에 릴리스 분기 기반을 만듭니다.```shell
cd "${SOURCE_CODE_DIR}"
git checkout -b "${VERSION}"-release "${VERSION}"-prepare
git push "${GH_REMOTE}" "${VERSION}"-release
export GPG_TTY=$(tty)
````

> 참고: 소스 코드 없이 원격 호스트에 릴리스하는 경우 `git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git`을 실행해야 합니다.
> 먼저 소스 코드를 복제하세요.그런 다음 `GH_REMOTE="origin"`을 설정하여 모든 명령이 제대로 작동하는지 확인하세요.```shell
mvn release:prepare -Papache-release,release -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dspotless.check.skip=true -Dmaven.javadoc.skip=true" -DautoVersionSubmodules=true -DdryRun=true -Dusername="${GH_USERNAME}"
````

- `-Prelease`: 모든 소스 코드, jar 파일 및 실행 가능한 바이너리 패키지를 압축하는 릴리스 프로필을 선택합니다.
- `-DautoVersionSubmodules=true`: 버전 번호가 각 하위 모듈에 대해 입력되지 않고 한 번만 입력되도록 할 수 있습니다.
- `-DdryRun=true`: 새 버전 번호와 새 태그를 생성하거나 제출하지 않음을 의미하는 연습 실행입니다.

#### Maven 릴리스 확인 준비

먼저, 현지 사전 출시 확인 정보를 정리합니다.```shell
mvn release:clean
````

그런 다음 릴리스 실행을 준비합니다.```shell
mvn release:prepare -Papache-release,release -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dspotless.check.skip=true -Dmaven.javadoc.skip=true" -DautoVersionSubmodules=true -DpushChanges=false -Dusername="${GH_USERNAME}"
````

기본적으로는 이전 리허설 명령과 동일하지만 `-DdryRun=true` 매개변수를 삭제합니다.

- `-DpushChanges=false`: 편집된 버전 번호와 태그를 GitHub에 자동으로 제출하지 않습니다.

> 참고: `git config --global user.email "you@example.com"` 명령을 사용하여 git `user.name` 및 `user.password`를 구성해야 합니다.
> 그리고 `git config --global user.name "Your Name"` **당신이 누구인지 알려주세요.**와 같은 실수를 만난다면
> git에서.

로컬 파일에 오류가 없는지 확인한 후 GitHub에 제출하세요.```shell
git push -u "${GH_REMOTE}" "${VERSION}"-release
git push "${GH_REMOTE}" --tags
````

<!-- markdown-link-check-disable -->

> 참고 1: 이 단계에서는 기본 비밀번호가 더 이상 지원되지 않으므로 비밀번호로 github 토큰을 사용해야 합니다.
> 자세한 내용은 https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
> 이에 대한 토큰을 생성하는 방법에 대해 자세히 설명합니다.
>
> 참고 2: 명령이 완료되면 `release.properties` 파일과 `*.Backup` 파일이 자동 생성되며 해당 파일이 필요합니다.
> 다음 명령에 삭제하지 마십시오.

<!-- markdown-link-check-enable -->

#### Maven 릴리스 배포```shell
mvn release:perform -Papache-release,release -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dspotless.check.skip=true -Dmaven.javadoc.skip=true -Dmaven.deploy.skip=false" -DautoVersionSubmodules=true -Dusername="${GH_USERNAME}"
````

해당 명령이 실행되면 릴리스될 버전이 자동으로 Apache 스테이징 저장소에 업로드됩니다.
[Apache 스테이징 저장소](https://repository.apache.org/#stagingRepositories)로 이동하여 Apache LDAP로 로그인하세요.그러면 업로드된 버전을 볼 수 있으며 `Repository` 열의 내용은 `${STAGING.REPOSITORY}`입니다.
'닫기'를 클릭하면 구축이 완료되었음을 Nexus에 알릴 수 있습니다. 그래야만 이 버전을 사용할 수 있기 때문입니다.
gpg 서명에 문제가 있으면 'Close'가 실패하는데, 'Activity'를 통해 실패 정보를 확인할 수 있습니다.

### SVN

#### Dolphinscheduler 릴리스 디렉토리 확인

Dolphinscheduler dev 릴리스 디렉토리를 로컬로 체크아웃해야 합니다.```shell
SVN_DIR_DEV="${SVN_DIR}/dolphinscheduler/dev"
SVN_DIR_RELEASE="${SVN_DIR}/dolphinscheduler/release"
# Optional, only if the SVN root path not exists.
mkdir -p "${SVN_DIR_DEV}"

# When you first time checkout from this path
svn --username="${A_USERNAME}" co https://dist.apache.org/repos/dist/dev/dolphinscheduler "${SVN_DIR_DEV}"
# Or update when the svn directory exists, and you already checkout
svn --username="${A_USERNAME}" update "${SVN_DIR_DEV}"
````

> 참고: 처음 결제할 때 모든 파일을 다운로드하므로 미러에 동기화하는 데 몇 분이 걸릴 수 있습니다.

#### 새 GPG 키를 KEYS로 내보내기(선택 사항)

이 gpg KEY를 사용하여 처음 릴리스하는 경우(첫 번째 릴리스이거나 KEY를 변경하는 경우)에만 해당됩니다.당신은해야
이 단계에서는 체크아웃이 필요하므로 작업 디렉터리를 다른 디렉터리로 변경하고 릴리스 디렉터리에서 KEYS를 변경하세요.```shell
# Optional, only if the SVN root path not exists.
mkdir -p "${SVN_DIR_RELEASE}"

cd "${SVN_DIR_RELEASE}"
svn --username="${A_USERNAME}" co https://dist.apache.org/repos/dist/release/dolphinscheduler
# Change the placeholder <YOUR-GPG-KEY-ID> to your id
gpg -a --export <YOUR-GPG-KEY-ID> >> KEYS
svn add *
svn --username="${A_USERNAME}" commit -m "new key <YOUR-GPG-KEY-ID> add"
````

#### SVN에 릴리스 콘텐츠 추가

버전 번호별로 폴더를 만들고 소스 코드 패키지, 바이너리 패키지 및 실행 가능한 바이너리 패키지를 SVN 작업 디렉터리로 이동합니다.```shell
mkdir -p "${SVN_DIR_DEV}/${VERSION}"

# Add to SVN
cp -f "${SOURCE_CODE_DIR}"/dolphinscheduler-dist/target/*.tar.gz "${SVN_DIR_DEV}/${VERSION}"
cp -f "${SOURCE_CODE_DIR}"/dolphinscheduler-dist/target/*.tar.gz.asc "${SVN_DIR_DEV}/${VERSION}"

# Create sign
cd "${SVN_DIR_DEV}/${VERSION}"
shasum -a 512 apache-dolphinscheduler-"${VERSION}"-src.tar.gz >> apache-dolphinscheduler-"${VERSION}"-src.tar.gz.sha512
shasum -b -a 512 apache-dolphinscheduler-"${VERSION}"-bin.tar.gz >> apache-dolphinscheduler-"${VERSION}"-bin.tar.gz.sha512

# Check sha512
shasum -c apache-dolphinscheduler-"${VERSION}"-src.tar.gz.sha512
shasum -c apache-dolphinscheduler-"${VERSION}"-bin.tar.gz.sha512
# Check gpg signature
gpg --verify apache-dolphinscheduler-"${VERSION}"-src.tar.gz.asc
gpg --verify apache-dolphinscheduler-"${VERSION}"-bin.tar.gz.asc

# Commit to Apache SVN
cd "${SVN_DIR_DEV}"
svn add "${VERSION}"
svn --username="${A_USERNAME}" commit -m "release ${VERSION}"
````

> 참고: `asc` 파일을 찾을 수 없는 경우 gpg 서명을 수동으로 생성해야 합니다.
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-bin.tar.gz` 및
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-src.tar.gz`가 생성합니다.

### 출시된 파일 확인

#### 소스 패키지 확인

`apache-dolphinscheduler-<VERSION>-src.tar.gz`의 압축을 풀고 다음 항목을 확인하세요.

- 불필요한 파일을 포함하기 위해 소스 타르볼의 크기가 너무 큰지 확인하십시오.
- `LICENSE`, `NOTICE` 파일이 존재합니다.
- `NOTICE` 파일의 정확한 연도
- 텍스트 파일만 있고 바이너리 파일은 없습니다.
- 모든 소스 파일에는 ASF 헤더가 있습니다.
- 코드를 컴파일하고 단위 테스트를 통과할 수 있습니다(mvn install).
- 릴리스 내용이 버전 관리에 태그된 내용과 일치합니다(diff -r verify_dir tag_dir).
- 추가 파일이나 폴더(예: 빈 폴더)가 있는지 확인하세요.

#### 바이너리 패키지 확인

`apache-dolphinscheduler-<VERSION>-bin.tar.gz`의 압축을 풀어 다음 항목을 확인하세요.

- `LICENSE`, `NOTICE` 파일이 존재합니다.
- `NOTICE` 파일의 정확한 연도
- 타사 종속성 라이센스를 확인하십시오.
- 소프트웨어에 호환되는 라이센스가 있습니다.
- `LICENSE`에 언급된 모든 소프트웨어 라이센스
- 모든 타사 종속 라이센스는 'licenses' 폴더 아래에 있습니다.
- Apache 라이선스에 따라 달라지며 `NOTICE` 파일이 있는 경우 해당 `NOTICE` 파일을 릴리스의 `NOTICE` 파일에 추가해야 합니다.

## 투표

### 업데이트 릴리스 노트

[새 릴리스 노트](https://github.com/apache/dolphinscheduler/releases/new)를 통해 GitHub에서 릴리스 노트를 생성해야 합니다.
메일로 릴리스 노트가 필요하기 때문에 투표 메일 전에 완료해야 합니다.명령을 사용할 수 있습니다
`tools/release` 디렉터리의 `python release.py 변경 로그`를 사용하여 변경 로그를 생성합니다.([사용법](https://github.com/apache/dolphinscheduler/blob/dev/tools/release/README.md)

> 참고: 또는 수동으로 생성하려는 경우 `git log --pretty="- %s" <PREVIOUS-RELEASE-SHA>..<CURRENT-RELEASE-SHA> >changelog.md` 명령을 사용할 수 있습니다.
> (일부 로그는 정확하지 않을 수 있으므로 직접 필터링해야 합니다.) 분류하여 GitHub 릴리스 노트 페이지에 붙여넣습니다.

### 투표 절차

DolphinScheduler 커뮤니티 투표: `dev@dolphinscheduler.apache.org`로 투표 이메일을 보냅니다.PMC는 다음 사항을 확인해야 합니다.
투표하기 전에 문서에 따른 버전의 정확성.최소 72시간 후, 최소 3시간 후
'+1 및 no -1 PMC 회원' 투표를 통해 투표의 다음 단계로 넘어갈 수 있습니다.

투표 결과 발표: 결과 투표 이메일을 `dev@dolphinscheduler.apache.org`로 보냅니다.

### 템플릿

#### 투표 템플릿

제목：```txt
[VOTE] Release Apache DolphinScheduler <VERSION>
````

본체：```txt
Hello DolphinScheduler Community,

This is a call for vote to release Apache DolphinScheduler version <VERSION>

Release notes: https://github.com/apache/dolphinscheduler/releases/tag/<VERSION>

The release candidates: https://dist.apache.org/repos/dist/dev/dolphinscheduler/<VERSION>/

Maven 2 staging repository: https://repository.apache.org/content/repositories/<STAGING.REPOSITORY>/org/apache/dolphinscheduler/

Git tag for the release: https://github.com/apache/dolphinscheduler/tree/<VERSION>

Release Commit ID: https://github.com/apache/dolphinscheduler/commit/<SHA-VALUE>

Keys to verify the Release Candidate: https://downloads.apache.org/dolphinscheduler/KEYS

Look at here for how to verify this release candidate: https://github.com/apache/dolphinscheduler/blob/dev/docs/docs/en/contribute/release.md

The vote will be open for at least 72 hours or until necessary number of votes are reached.

Please vote accordingly:

[ ] +1 approve
[ ] +0 no opinion
[ ] -1 disapprove with the reason

Checklist for reference:

[ ] Download links are valid.
[ ] Checksums and PGP signatures are valid.
[ ] Source code artifacts have correct names matching the current release.
[ ] LICENSE and NOTICE files are correct for each DolphinScheduler repo.
[ ] All files have license headers if necessary.
[ ] No compiled archives bundled in source archive.
````

#### 결과 템플릿

제목：```txt
[RESULT][VOTE] Release Apache DolphinScheduler <VERSION>
````

본체：```txt
The vote to release Apache DolphinScheduler <VERSION> has passed.Here is the vote result,

4 PMC member +1 votes:

xxx
xxx
xxx
xxx

1 community +1 vote:
xxx

Thanks everyone for taking time to check this release and help us.
````

## 발표

### 릴리스 Tarball 처리 및 릴리스 분기 제거```shell
# move to release directory
svn mv -m "release ${VERSION}" https://dist.apache.org/repos/dist/dev/dolphinscheduler/"${VERSION}" https://dist.apache.org/repos/dist/release/dolphinscheduler/

# remove old release directory
svn delete -m "remove old release" https://dist.apache.org/repos/dist/release/dolphinscheduler/<PREVIOUS-RELEASE-VERSION>

````

그런 다음 [Apache 스테이징 저장소](https://repository.apache.org/#stagingRepositories)에서 DolphinScheduler를 찾아 `Release`를 클릭하세요.

### 문서 업데이트

공지 메일을 보내기 전에 웹사이트가 있어야 합니다. 이 섹션에서는 웹사이트를 변경하는 방법을 설명합니다.예를 들어,
릴리스 버전이 `<VERSION>`인 경우 다음 업데이트가 필요합니다(PR이 병합되면 즉시 적용됩니다).

- 저장소 **apache/dolphinscheduler-웹사이트**:
- `config/download.json`: `<VERSION>` 릴리스 패키지 다운로드를 추가합니다.
- `scripts/conf.sh`: 새 릴리스 버전 `<VERSION>` 키-값 쌍을 `DEV_RELEASE_DOCS_VERSIONS` 변수에 추가합니다.
- 저장소 **apache/dolphinscheduler**(개발 브랜치):
- `docs/configs/site.js`:
- `docsLatest`: `<버전>`으로 업데이트
- `docs0`: `en-us/zh-cn` 두 곳의 `text`를 `최신(<VERSION>)`으로 업데이트해야 합니다.
- `docs/configs/index.md.jsx`: `<VERSION>: docsxyzConfig`를 추가하고 새 `docsxyzConfig`에 대한 새 `import`를 추가합니다.
- `docs/docs/en/history-versions.md` 및 `docs/docs/zh/history-versions.md`: 새로운 `<VERSION>` 릴리스 문서를 추가합니다.
- `.github/ISSUE_TEMPLATE/bug-report.yml`: DolphinScheduler의 GitHub [버그 보고서](https://github.com/apache/dolphinscheduler/blob/dev/.github/ISSUE_TEMPLATE/bug-report.yml)
이슈 템플릿에는 **버전** 선택 하단이 있습니다.따라서 출시 후에는 새로운 `<VERSION>`을 추가해야 합니다.
버그 보고서.yml

### Docker 이미지 및 Helm 차트 게시

Docker 이미지를 자동으로 게시하는 [워크플로](../../../../.github/workflows/publish-docker.yaml)가 있습니다.
그리고 Helm Chart를 Docker Hub에 자동으로 게시하는 [워크플로](../../../../.github/workflows/publish-helm-chart.yaml),
릴리스 노드를 생성하면 워크플로가 트리거됩니다.당신이해야 할 모든 것
앞서 언급한 워크플로를 관찰하는 것입니다. 워크플로가 완료되면 Docker 이미지를 로컬로 가져와서
예상대로 작동하는지 확인하십시오.

### 공지사항 이메일 보내기 커뮤니티

릴리스 프로세스가 완료된 후 공지 이메일을 보내야 합니다.이메일은 `dev@dolphinscheduler.apache.org`로 보내야 합니다.
참조는 `announce@apache.org`입니다. 참고: **메일 형식에는 일반 텍스트 형식이 필요합니다**.

공지사항 이메일 템플릿은 아래와 같습니다:

제목：```txt
[ANNOUNCE] Release Apache DolphinScheduler <VERSION>
````

본체：```txt
Hi all,

We are glad to announce the release of Apache DolphinScheduler <VERSION>. Once again I would like to express my thanks to your help.

Dolphin Scheduler is a distributed and easy-to-extend visual workflow scheduler system,
dedicated to solving the complex task dependencies in data processing, making the scheduler system out of the box for data processing.


Download Links: https://dolphinscheduler.apache.org/en-us/download

Release Notes: https://github.com/apache/dolphinscheduler/releases/tag/<VERSION>

Website: https://dolphinscheduler.apache.org/

DolphinScheduler Resources:
- Issue: https://github.com/apache/dolphinscheduler/issues/
- Mailing list: dev@dolphinscheduler.apache.org
- Documents: https://dolphinscheduler.apache.org/en-us/docs/<VERSION>/about/introduction
````

## 준비 분기 제거```shell
cd "${SOURCE_CODE_DIR}"
git push --delete "${GH_REMOTE}" "${VERSION}-prepare"
````

## 뉴스

모든 설정이 완료되면 기사를 작성하여 커뮤니티에 게시해야 합니다. 기사에는 다음이 포함되어야 합니다.

- 버전, 기능 추가, 버그 수정 또는 둘 다의 주요 목적은 무엇입니까?
- 주요 신기능과 사용법을 사진이나 gif로 더 잘 알려드립니다.
- 주요 버그 수정 및 이전 버전과의 차이점을 사진이나 gif로 개선
- 이전 릴리스 이후 모든 기여자

### 모든 기여자 확보

릴리스 뉴스나 공지 사항을 게시하려면 현재 릴리스의 모든 기여자가 필요할 수 있습니다.
기여자 GitHub ID를 자동 생성하려면 `tools/release` 디렉터리에서 `python release.py contributor` 명령을 사용하세요.