# Release Guide

## Preparation

This section is pre-release work, most of it is one-off, meaning **it is only needed for your first release**. If you have
released before, then skip this section to the next.

### Check Your Environment

To make sure you could successfully complete the release for DolphinScheduler, you should check your environment and make sure
all conditions are met, if any or them are missing, you should install them and make sure them work.

```shell
# JDK 1.8 above is requests
java -version
# Maven requests
mvn -version
```

### GPG Settings

#### Install GPG

Download installation package on [official GnuPG website](https://www.gnupg.org/download/index.html).
The command of GnuPG 1.x version can differ a little from that of 2.x version.
The following instructions take `GnuPG-2.1.23` version for example.

After the installation, execute the following command to check the version number.

```shell
gpg --version
```

#### Create Key

After the installation, execute the following command to create key.

This command indicates `GnuPG-2.x` can be used:

```shell
gpg --full-gen-key
```

This command indicates `GnuPG-1.x` can be used:

```shell
gpg --gen-key
```

Finish the key creation according to instructions, **Notice: Please use Apache mails and its password for key creation.**

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

Real name: ${Input username}
Email address: ${Input email}
Comment: ${Input comment}
You selected this USER-ID:
   "${Inputed username} (${Inputed comment}) <${Inputed email}>"

Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit? O
You need a Passphrase to protect your secret key. # Input your Apache mail passwords
```

#### Check Generated Key

```shell
gpg --list-keys
```

Execution Result:

```shell
pub   4096R/85E11560 2019-11-15
uid                  ${Username} (${Comment}) <{Email}>
sub   4096R/A63BC462 2019-11-15
```

Among them, 85E11560 is public key ID.

#### Upload the Public Key to Key Server

The command is as follow:

```shell
gpg --keyserver hkp://pool.sks-keyservers.net --send-key 85E11560
```

`pool.sks-keyservers.net` is randomly chosen from [public key server](https://sks-keyservers.net/status/).
Each server will automatically synchronize with one another, so it would be okay to choose any one, a backup keys servers
is `gpg --keyserver hkp://keyserver.ubuntu.com --send-key <YOUR_KEY_ID>`

### Configure Apache Maven Central Repository

#### Set `settings-security.xml` and `settings.xml`

In this section, we add Apache server maven configuration to prepare the release, we have to add `settings-security.xml` according
to [here](http://maven.apache.org/guides/mini/guide-encryption.html) firstly and then change your `~/.m2/settings.xml` like below

```xml
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
```

## Releasing

### Check release-docs

Compared with the last release, the `release-docs` of the current release needs to be updated to the latest, if there are dependencies and versions changes

- `dolphinscheduler-dist/release-docs/LICENSE`
- `dolphinscheduler-dist/release-docs/NOTICE`
- `dolphinscheduler-dist/release-docs/licenses`

### Set Release in Environment

We will use the release version, your github name and your Apache username below several times, so it is better to store
it to bash variable for easier use.

```shell
VERSION=<THE-VERSION-YOU-RELEASE>
SOURCE_CODE_DIR=<YOUR-SOURCE-CODE-ROOT-DIR>  # the directory of your source code hold, the location of parent pom.xml instead of binary package

GH_USERNAME=<YOUR-GITHUB-USERNAME>
GH_REMOTE=<GITHUB-REMOTE>  # we use `upstream` or `origin` mostly base on your release environment

A_USERNAME=<YOUR-APACHE-USERNAME>
SVN_DIR=<PATH-TO-SVN-ROOT>  # to keep binary package checkout from SVN, the sub path end with `/dolphinscheduler/dev` and `/dolphinscheduler/release` will be create
```

> Note: We can use the variable directly in you bash after we set environment, without changing anything. For example, we
> can use command `git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git` to clone the release branch
> and it can be successful by converting the `"${VERSION}"` to `<THE-VERSION-YOU-RELEASE>`. But you have to change `<VERSION>` manually in
> some of not bash step like [vote mail](#vote-procedure), we are using `<VERSION>` instead of `"${VERSION}"` to notice release
> manager they have to change by hand.

### Update Documentation or Code Version

We need to update some documentation before the Maven release. For example, to release version `VERSION`, the following updates are required:

- Version in the code:
  - `sql`:
    - `dolphinscheduler_mysql.sql`: `t_ds_version` needs to be updated to `VERSION`
    - `dolphinscheduler_postgre.sql`: `t_ds_version` needs to be updated to `VERSION`
    - `dolphinscheduler_h2.sql`: `t_ds_version` needs to be updated to `VERSION`
    - `upgrade`: whether to add `VERSION_schema` if there are some upgrade DDL or DML, can skip this step if no any DDL or DML added.
    - `soft_version`: need to be updated to `VERSION`
  - `deploy/docker/.env`: `HUB` change to `apache`，`TAG` change to `VERSION`
  - `deploy/kubernetes/dolphinscheduler`:
    - `Chart.yaml`: `appVersion` and `version` needs to be updated to x.y.z
    - `values.yaml`: `image.tag` needs to be updated to x.y.z
- Version in the docs:
  - Change the placeholder `<version>`(except `pom`) to the `x.y.z` in directory `docs`
  - Add new history version
    - `docs/docs/en/history-versions.md` and `docs/docs/zh/history-versions.md`: Add the new version and link for `x.y.z`
  - `docs/configs/docsdev.js`: change `/dev/` to `/x.y.z/`, **DO NOT** change this filename, is will be auto change by website tools.

> Note: `VERSION` is a place hold string, is same as the version we set in `VERSION=<THE-VERSION-YOU-RELEASE>`.

### Maven Release

#### Maven Release Check

Create release branch base on prepare branch.

```shell
cd "${SOURCE_CODE_DIR}"
git checkout -b "${VERSION}"-release "${VERSION}"-prepare
git push "${GH_REMOTE}" "${VERSION}"-release
```

> Note: If you release in remote host without source code, you should run `git clone -b "${VERSION}"-prepare https://github.com/apache/dolphinscheduler.git`
> first to clone the source code. And then make sure you set `GH_REMOTE="origin"` to make all command work fine.

```shell
mvn release:prepare -Prelease -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dmaven.javadoc.skip=true -Dspotless.check.skip=true" -DautoVersionSubmodules=true -DdryRun=true -Dusername="${GH_USERNAME}"
```

- `-Prelease`: choose release profile, which will pack all the source codes, jar files and executable binary packages.
- `-DautoVersionSubmodules=true`: it can make the version number is inputted only once and not for each sub-module.
- `-DdryRun=true`: dry run which means not to generate or submit new version number and new tag.

#### Maven Release Check Prepare

First, clean local pre-release check information.

```shell
mvn release:clean
```

Then, prepare to execute the release.

```shell
mvn release:prepare -Prelease -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dmaven.javadoc.skip=true -Dspotless.check.skip=true" -DautoVersionSubmodules=true -DpushChanges=false -Dusername="${GH_USERNAME}"
```

It is basically the same as the previous rehearsal command, but deleting `-DdryRun=true` parameter.

- `-DpushChanges=false`: do not submit the edited version number and tag to GitHub automatically.

> Note: You have to config your git `user.name` and `user.password` by command `git config --global user.email "you@example.com"`
> and `git config --global user.name "Your Name"` if you meet some mistake like **Please tell me who you are.**
> from git.

After making sure there is no mistake in local files, submit them to GitHub.

```shell
git push -u "${GH_REMOTE}" "${VERSION}"-release
git push "${GH_REMOTE}" --tags
```

<!-- markdown-link-check-disable -->

> Note1: In this step, you should use github token for password because native password no longer supported, you can see
> https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/creating-a-personal-access-token for more
> detail about how to create token about it.
>
> Note2: After the command done, it will auto-created `release.properties` file and `*.Backup` files, their will be need
> in the following command and DO NOT DELETE THEM

<!-- markdown-link-check-enable -->

#### Maven Release Deploy

```shell
mvn release:perform -Prelease -Darguments="-Dmaven.test.skip=true -Dspotless.skip=true -Dmaven.javadoc.skip=true -Dspotless.check.skip=true" -DautoVersionSubmodules=true -Dusername="${GH_USERNAME}"
```

After that command is executed, the version to be released will be uploaded to Apache staging repository automatically.
Go to [apache staging repositories](https://repository.apache.org/#stagingRepositories) and login by Apache LDAP. then you can see the uploaded version, the content of `Repository` column is the `${STAGING.REPOSITORY}`.
Click `Close` to tell Nexus that the construction is finished, because only in this way, this version can be usable.
If there is any problem in gpg signature, `Close` will fail, but you can see the failure information through `Activity`.

### SVN

#### Checkout Dolphinscheduler Release Directory

We need too checkout Dolphinscheduler dev release directory to local, and

```shell
SVN_DIR_DEV="${SVN_DIR}/dolphinscheduler/dev"
SVN_DIR_RELEASE="${SVN_DIR}/dolphinscheduler/release"
# Optional, only if the SVN root path not exists.
mkdir -p "${SVN_DIR_DEV}"

# When you first time checkout from this path
svn --username="${A_USERNAME}" co https://dist.apache.org/repos/dist/dev/dolphinscheduler "${SVN_DIR_DEV}"
# Or update when the svn directory exists, and you already checkout
svn --username="${A_USERNAME}" update "${SVN_DIR_DEV}"
```

> NOTE: it may take a few minutes to sync to the mirror in your first time checkout, because it will download all the files

#### Export New GPG Key to KEYS(Optional)

Only if the first time you release with this gpg KEY, including it is you first release, or you change your KEY. You should
change working directory to another one because this step need checkout and change KEYS in release directory.

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

#### Add Release Content to SVN

Create folder by version number, and move the source code packages, binary packages and executable binary packages to SVN working directory.

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

> Note: You have to create gpg signature manually when you can not find your `asc` file, the command
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-bin.tar.gz` and
> `gpg --armor --detach-sign --digest-algo=SHA512 apache-dolphinscheduler-"${VERSION}"-src.tar.gz` will create them

### Check Released Files

#### Check source package

Decompress `apache-dolphinscheduler-<VERSION>-src.tar.gz` then check the following items:

- Check whether source tarball is oversized for including nonessential files
- `LICENSE` and `NOTICE` files exist
- Correct year in `NOTICE` file
- There is only text files but no binary files
- All source files have ASF headers
- Codes can be compiled and pass the unit tests (mvn install)
- The contents of the release match with what's tagged in version control (diff -r a verify_dir tag_dir)
- Check if there is any extra files or folders, empty folders for example

#### Check binary packages

Decompress `apache-dolphinscheduler-<VERSION>-bin.tar.gz` to check the following items:

- `LICENSE` and `NOTICE` files exist
- Correct year in `NOTICE` file
- Check the third party dependency license:
  - The software have a compatible license
  - All software licenses mentioned in `LICENSE`
  - All the third party dependency licenses are under `licenses` folder
  - If it depends on Apache license and has a `NOTICE` file, that `NOTICE` file need to be added to `NOTICE` file of the release

## VOTE

### Update Release Notes

You should create a release note in GitHub by [new release note](https://github.com/apache/dolphinscheduler/releases/new).
It should be done before vote mail because we need the release note in the mail. You could use command
`python release.py changelog` in directory `tools/release` to creat the changelog.

> NOTE: Or if you prefer to create manually, you can use command `git log --pretty="- %s" <PREVIOUS-RELEASE-SHA>..<CURRENT-RELEASE-SHA> > changelog.md`
> (some log maybe not correct, you should filter them by yourself) and classify them and paste them to GitHub release note page

### Vote procedure

DolphinScheduler community vote: send the vote e-mail to `dev@dolphinscheduler.apache.org`. PMC needs to check the
rightness of the version according to the document before they vote. After at least 72 hours and with at least 3
`+1 and no -1 PMC member` votes, it can come to the next stage of the vote.

Announce the vote result: send the result vote e-mail to `dev@dolphinscheduler.apache.org`。

### Templates

#### Vote Template

Title：

```txt
[VOTE] Release Apache DolphinScheduler <VERSION>
```

Body：

```txt
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
```

#### Result Template

Title：

```txt
[RESULT][VOTE] Release Apache DolphinScheduler <VERSION>
```

Body：

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

### Handle Release Tarball and Remove Release Branch

```shell
# move to release directory
svn mv -m "release ${VERSION}" https://dist.apache.org/repos/dist/dev/dolphinscheduler/"${VERSION}" https://dist.apache.org/repos/dist/release/dolphinscheduler/

# remove old release directory
svn delete -m "remove old release" https://dist.apache.org/repos/dist/release/dolphinscheduler/<PREVIOUS-RELEASE-VERSION>

# Remove prepare branch
cd "${SOURCE_CODE_DIR}"
git push --delete "${GH_REMOTE}" "${VERSION}-prepare"
```

and then find DolphinScheduler in [apache staging repositories](https://repository.apache.org/#stagingRepositories) and click `Release`

### Update Document

Website should be present before you send the announce mail this section will tell you how to change the website. For example,
the release version is `<VERSION>`, the following updates are required(note it will take effect immediately when the PR is merged):

- Repository **apache/dolphinscheduler-website**:
  - `config/download.json`: add the download of the `<VERSION>` release package
  - `scripts/conf.sh`: Add new release version `<VERSION>` key-value pair to variable `DEV_RELEASE_DOCS_VERSIONS`
- Repository **apache/dolphinscheduler** (dev branch):
  - `docs/configs/site.js`:
    - `docsLatest`: update to `<VERSION>`
    - `docs0`: The `text` of two places of `en-us/zh-cn` needs to be updated to `latest(<VERSION>)`
  - `docs/configs/index.md.jsx`: Add `<VERSION>: docsxyzConfig` and add new `import` for the new `docsxyzConfig`
  - `docs/docs/en/history-versions.md` and `docs/docs/zh/history-versions.md`: Add new `<VERSION>` release docs.
  - `.github/ISSUE_TEMPLATE/bug-report.yml`: DolphinScheduler's GitHub [bug-report](https://github.com/apache/dolphinscheduler/blob/dev/.github/ISSUE_TEMPLATE/bug-report.yml)
    issue template have **Version** selection bottom. So after released we should add the new `<VERSION>` to
    bug-report.yml

### Publish Docker Image and Helm Chart

We have a [workflow](../../../../.github/workflows/publish-docker.yaml) to automatically publish Docker images
and a [workflow](../../../../.github/workflows/publish-helm-chart.yaml) to automatically publish Helm Chart to Docker Hub,
after you change the release from "pre-release" to "release", the workflow will be triggered. All you need to do
is to observe the aforementioned workflows, and after they are completed, you can pull the Docker images locally and
verify that they work as expected.

### Send Announcement E-mail Community

You should send announcement E-mail after release process finished. The E-mail should send to `dev@dolphinscheduler.apache.org`
and cc to `announce@apache.org`.

Announcement e-mail template as below：

Title：

```txt
[ANNOUNCE] Release Apache DolphinScheduler <VERSION>
```

Body：

```txt
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
```

## News

After all set, an article should be written and publish it to the community, it should include:

- What is the major purpose of the version, feature add, bugfix, or both
- Major new features and how to use it, better with picture or gif
- Major bugfix and make what different from previous version, better with picture or gif
- All contributors since previous release

### Get All Contributors

You might need all contributors in current release when you want to publish the release news or announcement, you could
use command `python release.py contributor` in directory `tools/release` to auto generate contributor GitHub id.
