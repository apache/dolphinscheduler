# 发版指南

## 检查环境

为确保您可以成功完成 DolphinScheduler 的发布，您应该检查您的环境并确保满足所有条件，如果缺少任何条件，您应该安装它们并确保它们正常工作。

```shell
# 需要 JDK 1.8 及以上的版本
java -version
# 需要 Maven 
mvn -version
# 需要 Python 3.6 及以上的版本，并且需要 `python` 关键字能在命令行中运行，且版本符合条件。
python --version
```

## GPG设置

### 安装GPG

在[GnuPG官网](https://www.gnupg.org/download/index.html)下载安装包。
GnuPG的1.x版本和2.x版本的命令有细微差别，下列说明以`GnuPG-2.1.23`版本为例。

安装完成后，执行以下命令查看版本号。

```shell
gpg --version
```

### 创建key

安装完成后，执行以下命令创建key。

`GnuPG-2.x`可使用：

```shell
gpg --full-gen-key
```

`GnuPG-1.x`可使用：

```shell
gpg --gen-key
```

根据提示完成key，**注意：请使用Apache mail 和 对应的密码生成GPG的Key。**

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

需要使用自己的用户登录服务器，而不是root切到自己的账户

### 查看生成的key

```shell
gpg --list-keys
```

执行结果：

```shell
pub   4096R/85E11560 2019-11-15
uid                  ${用户名} (${注释}) <{邮件地址}>
sub   4096R/A63BC462 2019-11-15
```

其中85E11560为公钥ID。

### 将公钥同步到服务器

命令如下：

```shell
gpg --keyserver hkp://pool.sks-keyservers.net --send-key 85E11560
```

`pool.sks-keyservers.net`为随意挑选的[公钥服务器](https://sks-keyservers.net/status/)，每个服务器之间是自动同步的，选任意一个即可。

注意：如果同步到公钥服务器，可以在服务器上查到新建的公钥
http://keyserver.ubuntu.com:11371/pks/lookup?search=${用户名}&fingerprint=on&op=index
备用公钥服务器 gpg --keyserver hkp://keyserver.ubuntu.com --send-key ${公钥ID}

## 发布Apache Maven中央仓库

### 设置 `settings-security.xml` 和 `settings.xml` 文件

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

### 配置环境变量

我们将多次使用发布版本 `VERSION`，github名称 `GH_USERNAME`，以及 Apache 用户名 `<YOUR-APACHE-USERNAME>`，因此最好将其存储到bash变量中以便于使用。

```shell
VERSION=<THE-VERSION-YOU-RELEASE>
GH_USERNAME=<YOUR-GITHUB-USERNAME>
A_USERNAME=<YOUR-APACHE-USERNAME>
```

> 注意：设置环境变量后，我们可以直接在你的 bash 中使用该变量，而无需更改任何内容。例如，我们可以直接使用命令 `git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git`
> 来克隆发布分支，他会自动将其中的 `"${VERSION}"` 转化成你设置的值 `<THE-VERSION-YOU-RELEASE>`。 但是您必须在一些非 bash 步骤中手动更改
> `<VERSION>` 为对应的版本号，例如发起投票中的内容。我们使用 `<VERSION>` 而不是 `"${VERSION}"` 来提示 release manager 他们必须手动更改这部分内容

### 创建发布分支

在本节中，我们从 github 下载源代码并创建新分支以发布

```shell
git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git
cd ~/dolphinscheduler/
git pull
git checkout -b ${RELEASE.VERSION}-release
git push origin ${RELEASE.VERSION}-release
```

### 发布预校验

```shell
# 保证 python profile 的 gpg 可以正常运行
export GPG_TTY=$(tty)

# 运行发版校验
mvn release:prepare -Prelease,python -Darguments="-Dmaven.test.skip=true -Dcheckstyle.skip=true -Dmaven.javadoc.skip=true" -DautoVersionSubmodules=true -DdryRun=true -Dusername="${GH_USERNAME}"
```

* `-Prelease,python`: 选择release和python的profile，这个profile会打包所有源码、jar文件以及可执行二进制包，以及Python的二进制包。
* `-DautoVersionSubmodules=true`: 作用是发布过程中版本号只需要输入一次，不必为每个子模块都输入一次。
* `-DdryRun=true`: 演练，即不产生版本号提交，不生成新的tag。

### 准备发布

首先清理发布预校验本地信息。

```shell
mvn release:clean
```

然后准备执行发布。

```shell
mvn release:prepare -Prelease,python -Darguments="-Dmaven.test.skip=true -Dcheckstyle.skip=true -Dmaven.javadoc.skip=true" -DautoVersionSubmodules=true -DpushChanges=false -Dusername="${GH_USERNAME}"
```

和上一步演练的命令基本相同，去掉了 `-DdryRun=true` 参数。

* `-DpushChanges=false`:不要将修改后的版本号和tag自动提交至GitHub。

> 注意：如果你遇到来自 git 的类似 **Please tell me who you are.** 错误信息。您可以通过命令 `git config --global user.email "you@example.com"`
> 和 `git config --global user.name "Your Name"` 来配置你的用户名和邮箱如果你遇到一些错误。

将本地文件检查无误后，提交至github。

```shell
git push -u origin "${VERSION}"-release
git push origin --tags
```

<!-- markdown-link-check-disable -->

> 注意1：因为 Github 不再支持在 HTTPS 协议中使用原生密码在，所以在这一步你应该使用 github token 作为密码。你可以通过 https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token
> 了解更多如果创建 token 的信息。
>
> 注意2：命令完成后，会自动创建 `release.properties` 文件和 `*.Backup` 文件，它们在下面的命令中是需要的，不要删除它们

<!-- markdown-link-check-enable -->

### 部署发布

```shell
mvn release:perform -Prelease,python -Darguments="-Dmaven.test.skip=true -Dcheckstyle.skip=true -Dmaven.javadoc.skip=true" -DautoVersionSubmodules=true -Dusername="${GH_USERNAME}"
```

执行完该命令后，待发布版本会自动上传到Apache的临时筹备仓库(staging repository)。你可以通过访问 [apache staging repositories](https://repository.apache.org/#stagingRepositories)
, 然后使用Apache的LDAP账户登录后，就会看到上传的版本，`Repository` 列的内容即为 `${STAGING.REPOSITORY}`。
点击 `Close` 来告诉Nexus这个构建已经完成，只有这样该版本才是可用的。如果电子签名等出现问题，`Close` 会失败，可以通过 `Activity` 查看失败信息。

## 发布Apache SVN仓库

### 检出dolphinscheduler发布目录

如无本地工作目录，则先创建本地工作目录。

```shell
mkdir -p ~/ds_svn/dev/
cd ~/ds_svn/dev/
```

创建完毕后，从Apache SVN检出dolphinscheduler发布目录。

```shell
svn --username="${A_USERNAME}" co https://dist.apache.org/repos/dist/dev/dolphinscheduler
cd ~/ds_svn/dev/dolphinscheduler
```

### 将 gpg KEYS 文件拷贝至发布目录（可选）

只有你第一次使用该 KEY 发版时才需要，如果之前已经发过版且 KEY 没有变化则不需要。你需要切换到一个新的目录，因为这步骤需要 checkout 并修改 release
库中的 KEYS 文件

```shell
mkdir -p ~/ds_svn/release/
cd ~/ds_svn/release/
svn --username="${A_USERNAME}" co https://dist.apache.org/repos/dist/release/dolphinscheduler
gpg -a --export <YOUR-GPG-KEY-ID> >> KEYS
svn add *
svn --username="${A_USERNAME}" commit -m "new key <YOUR-GPG-KEY-ID> add"
```

> 注意：这个步骤需要一定的时间去 checkout 特别是在你第一次 checkout 的时候，因为这个库比较大，且这个操作需要 checkout 出全部文件

### 将待发布的内容添加至SVN目录

创建版本号目录。

```shell
mkdir -p ~/ds_svn/dev/dolphinscheduler/"${VERSION}"
mkdir -p ~/ds_svn/dev/dolphinscheduler/"${VERSION}"/python
cd ~/ds_svn/dev/dolphinscheduler/"${VERSION}"
```

将源码包和二进制包添加至SVN工作目录。

```shell
# 主程序源码包和二进制包
cp -f ~/dolphinscheduler/dolphinscheduler-dist/target/*.tar.gz ~/ds_svn/dev/dolphinscheduler/"${VERSION}"
cp -f ~/dolphinscheduler/dolphinscheduler-dist/target/*.tar.gz.asc ~/ds_svn/dev/dolphinscheduler/"${VERSION}"

# Python API 源码和二进制包
cp -f ~/dolphinscheduler/dolphinscheduler-dist/target/python/* ~/ds_svn/dev/dolphinscheduler/"${VERSION}"/python
```

### 生成文件签名

```shell
shasum -a 512 apache-dolphinscheduler-"${VERSION}"-src.tar.gz >> apache-dolphinscheduler-"${VERSION}"-src.tar.gz.sha512
shasum -b -a 512 apache-dolphinscheduler-"${VERSION}"-bin.tar.gz >> apache-dolphinscheduler-"${VERSION}"-bin.tar.gz.sha512
cd python
shasum -a 512 apache-dolphinscheduler-python-"${VERSION}".tar.gz >> apache-dolphinscheduler-python-"${VERSION}".tar.gz.sha512
shasum -b -a 512 apache_dolphinscheduler-python-"${VERSION}"-py3-none-any.whl >> apache_dolphinscheduler-python-"${VERSION}"-py3-none-any.whl.sha512
cd ../
```

### 提交Apache SVN

```shell
cd ~/ds_svn/dev/dolphinscheduler
svn add *
svn --username="${A_USERNAME}" commit -m "release ${VERSION}"
```

## 检查发布结果

### 检查sha512哈希

```shell
shasum -c apache-dolphinscheduler-"${VERSION}"-src.tar.gz.sha512
shasum -c apache-dolphinscheduler-"${VERSION}"-bin.tar.gz.sha512
cd python
shasum -c apache-dolphinscheduler-python-"${VERSION}".tar.gz.sha512
shasum -c apache_dolphinscheduler-python-"${VERSION}"-py3-none-any.whl.sha512
cd ../
```

### 检查gpg签名

首先导入发布人公钥。从svn仓库导入KEYS到本地环境。（发布版本的人不需要再导入，帮助做验证的人需要导入，用户名填发版人的即可）

```shell
curl https://dist.apache.org/repos/dist/release/dolphinscheduler/KEYS >> KEYS
gpg --import KEYS
gpg --edit-key "${A_USERNAME}"
  > trust

Please decide how far you trust this user to correctly verify other users' keys
(by looking at passports, checking fingerprints from different sources, etc.)

  1 = I don't know or won't say
  2 = I do NOT trust
  3 = I trust marginally
  4 = I trust fully
  5 = I trust ultimately
  m = back to the main menu

Your decision? 5

  > save
```

然后进行gpg签名检查。

```shell
gpg --verify apache-dolphinscheduler-"${VERSION}"-src.tar.gz.asc
gpg --verify apache-dolphinscheduler-"${VERSION}"-bin.tar.gz.asc
cd python
gpg --verify apache-dolphinscheduler-python-"${VERSION}".tar.gz.asc
gpg --verify apache_dolphinscheduler-python-"${VERSION}"-py3-none-any.whl.asc
cd ../
```

> 注意：当你找不到你的 `asc` 文件时，你必须手动创建 gpg 签名，命令
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-bin.tar.gz` 和
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-src.tar.gz` 将创建它们

### 检查发布文件内容

#### 检查源码包的文件内容

解压缩`apache-dolphinscheduler-<VERSION>-src.tar.gz`以及Python文件夹下的`apache-dolphinscheduler-python-<VERSION>.tar.gz`，进行如下检查:

- 检查源码包是否包含由于包含不必要文件，致使tarball过于庞大
- 存在`LICENSE`和`NOTICE`文件
- 只存在文本文件，不存在二进制文件
- 所有文件的开头都有ASF许可证
- 能够正确编译，单元测试可以通过 (mvn install)
- 版本内容与GitHub上tag的内容相符 (diff -r a verify_dir tag_dir)
- 检查是否有多余文件或文件夹，例如空文件夹等

#### 检查二进制包的文件内容

解压缩`apache-dolphinscheduler-<VERSION>-src.tar.gz`和`apache-dolphinscheduler-python-<VERSION>-bin.tar.gz`
进行如下检查:

- 存在`LICENSE`和`NOTICE`文件
- 所有文本文件开头都有ASF许可证
- 检查第三方依赖许可证：
  - 第三方依赖的许可证兼容
  - 所有第三方依赖的许可证都在`LICENSE`文件中声明
  - 依赖许可证的完整版全部在`license`目录
  - 如果依赖的是Apache许可证并且存在`NOTICE`文件，那么这些`NOTICE`文件也需要加入到版本的`NOTICE`文件中

## 发起投票

### 更新版本说明

在 GitHub 中通过 [创建新的 release note](https://github.com/apache/dolphinscheduler/releases/new) 创建一个 release note。 这要在
投票邮件开始之前完成，因为我们需要在邮件中使用 release note。你可以在 `tools/release` 目录中运行 `python release.py changelog` 自动创建
changelog.

> 备注： 如果你更加倾向于手动创建 changelog，你可以通过命令 `git log --pretty="- %s" <PREVIOUS-RELEASE-SHA>..<CURRENT-RELEASE-SHA> > changelog.md`
> 生成 changelog（部分可以不太准确，需要人为过滤一遍），然后将他们分类并粘贴到 GitHub 的 release note 中

### 投票阶段

1. DolphinScheduler社区投票，发起投票邮件到`dev@dolphinscheduler.apache.org`。PMC需要先按照文档检查版本的正确性，然后再进行投票。
   经过至少72小时并统计到至少3个`+1 并且没有-1 PMC member`票后，即可进入下一阶段。

2. 宣布投票结果,发起投票结果邮件到`dev@dolphinscheduler.apache.org`。

### 投票模板

1. DolphinScheduler社区投票模板

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

Look at here for how to verify this release candidate: https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/contribute/release/release.html

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

2.宣布投票结果模板

正文：

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

## 完成发布

### 将源码和二进制包从svn的dev目录移动到release目录

```shell
svn mv https://dist.apache.org/repos/dist/dev/dolphinscheduler/"${VERSION}" https://dist.apache.org/repos/dist/release/dolphinscheduler/
```

### 更新文档

官网应该在您发送通知邮件之前完成更新，本节将告诉您如何更改网站。假设发版的版本是 `<VERSION>`，需要进行以下更新（注意，当修改pull requests 被 merge 后就会生效）:

- **apache/dolphinscheduler-website** 仓库：
  - `download/en-us/download.md` 和 `download/zh-cn/download.md`: 增加 `<VERSION>` 版本发布包的下载
  - `scripts/conf.sh`: 在变量 `DEV_RELEASE_DOCS_VERSIONS` 中增加版本为 `<VERSION>` 的新键值对
- **apache/dolphinscheduler** 仓库 (dev 分支)：
  - `docs/configs/site.js`:
    - `docsLatest`: 更新为 `<VERSION>`
    - `docs0`: 两处 `en-us/zh-cn` 的 `text` 更新为 `latest(<VERSION>)`
  - `docs/configs/index.md.jsx`: 增加 `'<VERSION>': docsxyzConfig,`
  - `docs/docs/en/history-versions.md` 和 `docs/docs/zh/history-versions.md`: 增加新的发版版本 `<VERSION>` 的链接
  - `.github/ISSUE_TEMPLATE/bug-report.yml`: DolphinScheduler 在 GitHub issue 中有版本选择的部分，当有新版本发版后，需要更新这部分的内容。目前与版本关联的是
    [bug-report](https://github.com/apache/dolphinscheduler/blob/dev/.github/ISSUE_TEMPLATE/bug-report.yml)，发版的时候需要
    向其中的 **Version** 部分增加内容。

### 在 [apache staging repositories](https://repository.apache.org/#stagingRepositories) 仓库找到 DolphinScheduler 并点击`Release`

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


Download Links: https://dolphinscheduler.apache.org/en-us/download/download.html

Release Notes: https://github.com/apache/dolphinscheduler/releases/tag/<VERSION>

Website: https://dolphinscheduler.apache.org/

DolphinScheduler Resources:
- Issue: https://github.com/apache/dolphinscheduler/issues/
- Mailing list: dev@dolphinscheduler.apache.org
- Documents: https://dolphinscheduler.apache.org/zh-cn/docs/<VERSION>/user_doc/about/introduction.html
```

