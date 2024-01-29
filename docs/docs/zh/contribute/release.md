# 发版指南

## 准备

这部分是预发布工作，大部分是一次性的，意思是**它只在你的第一次发布时需要**。 如果你有之前发布过，请跳过本节到下一个部分。

### 检查环境

为确保您可以成功完成 DolphinScheduler 的发布，您应该检查您的环境并确保满足所有条件，如果缺少任何条件，您应该安装它们并确保它们正常工作。

```shell
# 需要 JDK 1.8 及以上的版本
java -version
# 需要 Maven
mvn -version
```

### GPG 设置

#### 安装 GPG

在[GnuPG 官网](https://www.gnupg.org/download/index.html)下载安装包。
GnuPG 的 1.x 版本和 2.x 版本的命令有细微差别，下列说明以`GnuPG-2.1.23`版本为例。

安装完成后，执行以下命令查看版本号。

```shell
gpg --version
```

#### 创建 key

安装完成后，执行以下命令创建 key。

`GnuPG-2.x`可使用：

```shell
gpg --full-gen-key
```

`GnuPG-1.x`可使用：

```shell
gpg --gen-key
```

根据提示完成 key，**注意：请使用 Apache mail 和 对应的密码生成 GPG 的 Key。**

```shell
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

Real name: ${输入用户名}
Email address: ${输入邮件地址}
Comment: ${输入注释}
You selected this USER-ID:
   "${输入的用户名} (${输入的注释}) <${输入的邮件地址}>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
You need a Passphrase to protect your secret key. # 输入apache登录密码
```

注意：如果遇到以下错误：

```
gpg: cancelled by user
gpg: Key generation canceled.
```

需要使用自己的用户登录服务器，而不是 root 切到自己的账户

#### 查看生成的 key

```shell
gpg --list-keys
```

执行结果：

```shell
pub   4096R/85E11560 2019-11-15
uid                  ${用户名} (${注释}) <{邮件地址}>
sub   4096R/A63BC462 2019-11-15
```

其中 85E11560 为公钥 ID。

#### 将公钥同步到服务器

命令如下：

```shell
gpg --keyserver hkp://pool.sks-keyservers.net --send-key 85E11560
```

`pool.sks-keyservers.net`为随意挑选的[公钥服务器](https://sks-keyservers.net/status/)，每个服务器之间是自动同步的，选任意一个即可。

注意：如果同步到公钥服务器，可以在服务器上查到新建的公钥
http://keyserver.ubuntu.com:11371/pks/lookup?search=${用户名}&fingerprint=on&op=index
备用公钥服务器 gpg --keyserver hkp://keyserver.ubuntu.com --send-key ${公钥 ID}

### 配置 Apache Maven Central Repository

#### 设置 `settings-security.xml` 和 `settings.xml` 文件

在本节中，我们添加 Apache 服务器 maven 配置以准备发布，请参考[这里](http://maven.apache.org/guides/mini/guide-encryption.html) 添加
`settings-security.xml` 文件，并且像下面这样更改你的 `~/.m2/settings.xml`

```xml
<settings>
  <servers>
    <server>
      <id>apache.snapshots.https</id>
      <username> <!-- APACHE LDAP 用户名 --> </username>
      <password> <!-- APACHE LDAP 加密后的密码 --> </password>
    </server>
    <server>
      <id>apache.releases.https</id>
      <username> <!-- APACHE LDAP 用户名 --> </username>
      <password> <!-- APACHE LDAP 加密后的密码 --> </password>
    </server>
  </servers>
</settings>
```

## Releasing

## 检查 release-docs

和上一个版本比较，如果有依赖及版本发生了变化，当前版本的 `release-docs` 需要被更新到最新

- `dolphinscheduler-dist/release-docs/LICENSE`
- `dolphinscheduler-dist/release-docs/NOTICE`
- `dolphinscheduler-dist/release-docs/licenses`

### 配置环境变量

我们将多次使用发布版本 `VERSION`，github 名称 `GH_USERNAME`，以及 Apache 用户名 `<YOUR-APACHE-USERNAME>`，因此最好将其存储到 bash 变量中以便于使用。

```shell
VERSION=<THE-VERSION-YOU-RELEASE>
SOURCE_CODE_DIR=<YOUR-SOURCE-CODE-ROOT-DIR>  # the directory of your source code hold, the location of parent pom.xml instead of binary package

GH_USERNAME=<YOUR-GITHUB-USERNAME>
GH_REMOTE=<GITHUB-REMOTE>  # we use `upstream` or `origin` mostly base on your release environment

A_USERNAME=<YOUR-APACHE-USERNAME>
SVN_DIR=<PATH-TO-SVN-ROOT>  # to keep binary package checkout from SVN, the sub path end with `/dolphinscheduler/dev` and `/dolphinscheduler/release` will be create
```

> 注意：设置环境变量后，我们可以直接在你的 bash 中使用该变量，而无需更改任何内容。例如，我们可以直接使用命令 `git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git`
> 来克隆发布分支，他会自动将其中的 `"${VERSION}"` 转化成你设置的值 `<THE-VERSION-YOU-RELEASE>`。 但是您必须在一些非 bash 步骤中手动更改
> `<VERSION>` 为对应的版本号，例如发起投票中的内容。我们使用 `<VERSION>` 而不是 `"${VERSION}"` 来提示 release manager 他们必须手动更改这部分内容

### 更新文档和代码的版本

我们需要在 Maven 发布之前更新一些文档。 例如，要发布版本 `VERSION`，需要进行以下更新：

- 修改代码中的版本号:
  - `sql`:
    - `dolphinscheduler_mysql.sql`: `t_ds_version` 版本更新为 x.y.z
    - `dolphinscheduler_postgre.sql`: `t_ds_version` 版本更新为 x.y.z
    - `dolphinscheduler_h2.sql`: `t_ds_version` 版本更新为 x.y.z
    - `upgrade`: 是否新增 `x.y.z_schema` 文件夹，如果有一些升级的 DDL 或 DML，如果没有添加任何 DDL 或 DML 可以跳过这一步。
    - `soft_version`: 版本更新为 x.y.z
  - `deploy/docker/.env`: `HUB` 改为 `apache`，`TAG` 改为 `x.y.z`
  - `deploy/kubernetes/dolphinscheduler`:
    - `Chart.yaml`: `appVersion` 和 `version` 版本更新为 x.y.z
    - `values.yaml`: `image.tag` 版本更新为 x.y.z
- 修改文档（docs 模块）中的版本号:
  - 将 `docs` 文件夹下文件的占位符 `<version>` (除了 pom.xml 相关的) 修改成 `x.y.z`
  - 新增历史版本
    - `docs/docs/en/history-versions.md` 和 `docs/docs/zh/history-versions.md`: 增加新的历史版本为 `x.y.z`
  - 修改文档 sidebar
    - `docs/configs/docsdev.js`: 将里面的 `/dev/` 修改成 `/x.y.z/`，**不要**修改文件名称，website 仓库的 shell 脚本会对他进行修改

> 注意：`VERSION` 是一个占位字符串，与我们在 `VERSION=<THE-VERSION-YOU-RELEASE>` 中设置的版本相同。

### Maven 发布

#### Maven 发布检查

在准备分支的基础上创建发布分支。

```shell
cd "${SOURCE_CODE_DIR}"
git checkout -b "${VERSION}"-release "${VERSION}"-prepare
git push "${GH_REMOTE}" "${VERSION}"-release
```

> 注意：如果你在没有源代码的远程主机上发布，你应该先运行 `git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git`
> 首先要克隆源代码。 然后确保设置`GH_REMOTE="origin"` 以使所有命令正常工作。

```shell
# 运行发版校验
mvn release:prepare -Prelease -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dmaven.javadoc.skip=true -Dspotless.check.skip=true" -DautoVersionSubmodules=true -DdryRun=true -Dusername="${GH_USERNAME}"
```

- `-Prelease`: 选择 release 的 profile，这个 profile 会打包所有源码、jar 文件以及可执行二进制包。
- `-DautoVersionSubmodules=true`: 作用是发布过程中版本号只需要输入一次，不必为每个子模块都输入一次。
- `-DdryRun=true`: 演练，即不产生版本号提交，不生成新的 tag。

#### 准备发布

首先清理发布预校验本地信息。

```shell
mvn release:clean
```

然后准备执行发布。

```shell
mvn release:prepare -Prelease -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dmaven.javadoc.skip=true  -Dspotless.check.skip=true" -DautoVersionSubmodules=true -DpushChanges=false -Dusername="${GH_USERNAME}"
```

和上一步演练的命令基本相同，去掉了 `-DdryRun=true` 参数。

- `-DpushChanges=false`:不要将修改后的版本号和 tag 自动提交至 GitHub。

> 注意：如果你遇到来自 git 的类似 **Please tell me who you are.** 错误信息。您可以通过命令 `git config --global user.email "you@example.com"`
> 和 `git config --global user.name "Your Name"` 来配置你的用户名和邮箱如果你遇到一些错误。

将本地文件检查无误后，提交至 github。

```shell
git push -u "${GH_REMOTE}" "${VERSION}"-release
git push "${GH_REMOTE}" --tags
```

<!-- markdown-link-check-disable -->

> 注意 1：因为 Github 不再支持在 HTTPS 协议中使用原生密码在，所以在这一步你应该使用 github token 作为密码。你可以通过 https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
> 了解更多如果创建 token 的信息。
>
> 注意 2：命令完成后，会自动创建 `release.properties` 文件和 `*.Backup` 文件，它们在下面的命令中是需要的，不要删除它们

<!-- markdown-link-check-enable -->

#### 部署发布

```shell
mvn release:perform -Prelease -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dmaven.javadoc.skip=true -Dspotless.check.skip=true" -DautoVersionSubmodules=true -Dusername="${GH_USERNAME}"
```

执行完该命令后，待发布版本会自动上传到 Apache 的临时筹备仓库(staging repository)。你可以通过访问 [apache staging repositories](https://repository.apache.org/#stagingRepositories)
, 然后使用 Apache 的 LDAP 账户登录后，就会看到上传的版本，`Repository` 列的内容即为 `${STAGING.REPOSITORY}`。
点击 `Close` 来告诉 Nexus 这个构建已经完成，只有这样该版本才是可用的。如果电子签名等出现问题，`Close` 会失败，可以通过 `Activity` 查看失败信息。

### SVN

#### 检出 dolphinscheduler 发布目录

我们还需要将 Dolphinscheduler 开发版本目录检出到本地，并且

```shell
SVN_DIR_DEV="${SVN_DIR}/dolphinscheduler/dev"
SVN_DIR_RELEASE="${SVN_DIR}/dolphinscheduler/release"
# 可选，只有当路径不存在时
mkdir -p "${SVN_DIR_DEV}"

# When you first time checkout from this path
svn --username="${A_USERNAME}" co https://dist.apache.org/repos/dist/dev/dolphinscheduler "${SVN_DIR_DEV}"
# Or update when the svn directory exists, and you already checkout
svn --username="${A_USERNAME}" update "${SVN_DIR_DEV}"
```

> 注意：第一次结帐时可能需要几分钟才能同步到镜像，因为它会下载所有文件

#### 将 gpg KEYS 文件拷贝至发布目录（可选）

只有你第一次使用该 KEY 发版时才需要，如果之前已经发过版且 KEY 没有变化则不需要。你需要切换到一个新的目录，因为这步骤需要 checkout 并修改 release
库中的 KEYS 文件

```shell
# Optional, only if the SVN root path not exists.
mkdir -p "${SVN_DIR_RELEASE}"

cd "${SVN_DIR_RELEASE}"
svn --username="${A_USERNAME}" co https://dist.apache.org/repos/dist/release/dolphinscheduler
# Change the placeholder <YOUR-GPG-KEY-ID> to your id
gpg -a --export <YOUR-GPG-KEY-ID> >> KEYS
svn add *
svn --username="${A_USERNAME}" commit -m "new key <YOUR-GPG-KEY-ID> add"
```

> 注意：这个步骤需要一定的时间去 checkout 特别是在你第一次 checkout 的时候，因为这个库比较大，且这个操作需要 checkout 出全部文件

#### 将待发布的内容添加至 SVN 目录

按版本号创建文件夹，将源码包、二进制包、可执行二进制包移动到 SVN 工作目录。

```shell
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
```

> 注意：当你找不到你的 `asc` 文件时，你必须手动创建 gpg 签名，命令
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-bin.tar.gz` 和
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-src.tar.gz` 将创建它们

将源码包和二进制包添加至 SVN 工作目录。

### 检查发布文件内容

#### 检查源码包的文件内容

解压缩`apache-dolphinscheduler-<VERSION>-src.tar.gz`，进行如下检查:

- 检查源码包是否包含由于包含不必要文件，致使 tarball 过于庞大
- 存在`LICENSE`和`NOTICE`文件
- 只存在文本文件，不存在二进制文件
- 所有文件的开头都有 ASF 许可证
- 能够正确编译，单元测试可以通过 (mvn install)
- 版本内容与 GitHub 上 tag 的内容相符 (diff -r a verify_dir tag_dir)
- 检查是否有多余文件或文件夹，例如空文件夹等

#### 检查二进制包的文件内容

解压缩`apache-dolphinscheduler-<VERSION>-bin.tar.gz`进行如下检查:

- 存在`LICENSE`和`NOTICE`文件
- 所有文本文件开头都有 ASF 许可证
- 检查第三方依赖许可证：
  - 第三方依赖的许可证兼容
  - 所有第三方依赖的许可证都在`LICENSE`文件中声明
  - 依赖许可证的完整版全部在`license`目录
  - 如果依赖的是 Apache 许可证并且存在`NOTICE`文件，那么这些`NOTICE`文件也需要加入到版本的`NOTICE`文件中

## 发起投票

### 更新版本说明

在 GitHub 中通过 [创建新的 release note](https://github.com/apache/dolphinscheduler/releases/new) 创建一个 release note。 这要在
投票邮件开始之前完成，因为我们需要在邮件中使用 release note。你可以在 `tools/release` 目录中运行 `python release.py changelog` 自动创建
changelog.

> 备注： 如果你更加倾向于手动创建 changelog，你可以通过命令 `git log --pretty="- %s" <PREVIOUS-RELEASE-SHA>..<CURRENT-RELEASE-SHA> > changelog.md`
> 生成 changelog（部分可以不太准确，需要人为过滤一遍），然后将他们分类并粘贴到 GitHub 的 release note 中

### 投票阶段

DolphinScheduler 社区投票，发起投票邮件到`dev@dolphinscheduler.apache.org`。PMC 需要先按照文档检查版本的正确性，然后再进行投票。 经过
至少 72 小时并统计到至少 3 个`+1 并且没有-1 PMC member`票后，即可进入下一阶段。

宣布投票结果,发起投票结果邮件到`dev@dolphinscheduler.apache.org`。

### 模板

### VOTE 模板

标题：

```txt
[VOTE] Release Apache DolphinScheduler <VERSION>
```

正文：

```txt
Hello DolphinScheduler Community,

This is a call for vote to release Apache DolphinScheduler version <VERSION>

Release notes: https://github.com/apache/dolphinscheduler/releases/tag/<VERSION>

The release candidates: https://dist.apache.org/repos/dist/dev/dolphinscheduler/<VERSION>/

Maven 2 staging repository: https://repository.apache.org/content/repositories/<STAGING.REPOSITORY>/org/apache/dolphinscheduler/

Git tag for the release: https://github.com/apache/dolphinscheduler/tree/<VERSION>

Release Commit ID: https://github.com/apache/dolphinscheduler/commit/<SHA-VALUE>

Keys to verify the Release Candidate: https://downloads.apache.org/dolphinscheduler/KEYS

Look at here for how to verify this release candidate: https://dolphinscheduler.apache.org/zh-cn/docs/3.1.2/contribute/release/release

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
```

#### RESULT 模版

Title：

```txt
[RESULT][VOTE] Release Apache DolphinScheduler <VERSION>
```

```txt
The vote to release Apache DolphinScheduler <VERSION> has passed.Here is the vote result,

4 PMC member +1 votes:

xxx
xxx
xxx
xxx

1 community +1 vote:
xxx

Thanks everyone for taking time to check this release and help us.
```

## Announce

### Move Packages to Release

```shell
# move to release directory
svn mv -m "release ${VERSION}" https://dist.apache.org/repos/dist/dev/dolphinscheduler/"${VERSION}" https://dist.apache.org/repos/dist/release/dolphinscheduler/

# remove old release directory
svn delete -m "remove old release" https://dist.apache.org/repos/dist/release/dolphinscheduler/<PREVIOUS-RELEASE-VERSION>

# Remove prepare branch
cd "${SOURCE_CODE_DIR}"
git push --delete "${GH_REMOTE}" "${VERSION}-prepare"
```

在 [apache staging repositories](https://repository.apache.org/#stagingRepositories) 仓库找到 DolphinScheduler 并点击`Release`

### 更新文档

官网应该在您发送通知邮件之前完成更新，本节将告诉您如何更改网站。假设发版的版本是 `<VERSION>`，需要进行以下更新（注意，当修改 pull requests 被 merge 后就会生效）:

- **apache/dolphinscheduler-website** 仓库：
  - `config/download.json`: 增加 `<VERSION>` 版本发布包的下载
  - `scripts/conf.sh`: 在变量 `DEV_RELEASE_DOCS_VERSIONS` 中增加版本为 `<VERSION>` 的新键值对
- **apache/dolphinscheduler** 仓库 (dev 分支)：
  - `docs/configs/site.js`:
    - `docsLatest`: 更新为 `<VERSION>`
    - `docs0`: 两处 `en-us/zh-cn` 的 `text` 更新为 `latest(<VERSION>)`
  - `docs/configs/index.md.jsx`: 增加 `'<VERSION>': docsxyzConfig,` 以及新的 `import`
  - `docs/docs/en/history-versions.md` 和 `docs/docs/zh/history-versions.md`: 增加新的发版版本 `<VERSION>` 的链接
  - `.github/ISSUE_TEMPLATE/bug-report.yml`: DolphinScheduler 在 GitHub bug report 的 issue 中有版本选择，当有新的版本发版后，需要更新
    [bug-report](https://github.com/apache/dolphinscheduler/blob/dev/.github/ISSUE_TEMPLATE/bug-report.yml) 中的 **Version** 部分。

### 发布 Docker Image

我们有一个 [工作流](../../../../.github/workflows/publish-docker.yaml) 来自动发布 Docker 镜像，
以及一个 [工作流](../../../../.github/workflows/publish-helm-chart.yaml) 来自动发布 Helm Chart 到 Docker Hub。
当你将发版从 "pre-release" 改为 "release" 后，这两个工作流就会被触发。你需要做的就是观察上述的工作流，
当它们完成后，你可以在本地拉取 Docker 镜像并验证它们是否按预期工作。

### 发送公告邮件通知社区

当完成了上述的发版流程后，需要发送一封公告邮件给社区。你需要将邮件发送到 `dev@dolphinscheduler.apache.org` 并抄送到 `announce@apache.org`。

通知邮件模板如下：

标题：

```txt
[ANNOUNCE] Release Apache DolphinScheduler <VERSION>
```

正文：

```txt
Hi all,

We are glad to announce the release of Apache DolphinScheduler <VERSION>. Once again I would like to express my thanks to your help.

Dolphin Scheduler is a distributed and easy-to-extend visual workflow scheduler system,
dedicated to solving the complex task dependencies in data processing, making the scheduler system out of the box for data processing.


Download Links: https://dolphinscheduler.apache.org/zh-cn/download

Release Notes: https://github.com/apache/dolphinscheduler/releases/tag/<VERSION>

Website: https://dolphinscheduler.apache.org/

DolphinScheduler Resources:
- Issue: https://github.com/apache/dolphinscheduler/issues/
- Mailing list: dev@dolphinscheduler.apache.org
- Documents: https://dolphinscheduler.apache.org/zh-cn/docs/<VERSION>/about/introduction
```

## News

一切就绪后，应该写一篇文章发布到社区，它应该包括：

- 版本、功能添加、错误修复或两者的主要目的是什么
- 主要新功能及使用方法，最好有图片或 gif
- 主要错误修复和与之前版本不同的地方，最好有图片或 gif
- 自上一版本以来的所有贡献者

## 获取全部的贡献者

当您想要发布新版本的新闻或公告时，您可能需要当前版本的所有贡献者，您可以在 `tools/release` 中使用命令 `python release.py contributor` 自动生成贡献者 Github id。
