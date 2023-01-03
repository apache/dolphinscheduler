# 发版后续

发送公告邮件后，我们还有一些发布任务要做，目前我们必须将 Docker 镜像发布到 Docker Hub。

## 发布 Docker 镜像

我们已经有 CI 发布最新的 Docker 镜像到 GitHub container register [点击查看详情](https://github.com/apache/dolphinscheduler/blob/d80cf21456265c9d84e642bdb4db4067c7577fc6/.github/workflows/publish-docker.yaml#L55-L63)。
我们可以稍微修改 CI 的主要命令实现单个命令发布 Docker 镜像发布到 Docker Hub。

```bash
# 请将 <VERSION> 修改成你要发版的版本
./mvnw -B clean deploy \
    -Dmaven.test.skip \
    -Dmaven.javadoc.skip \
    -Dmaven.checkstyle.skip \
    -Dmaven.deploy.skip \
    -Ddocker.tag=<VERSION> \
    -Ddocker.hub=apache \
    -Pdocker,release
```

## 获取全部的贡献者

当您想要发布新版本的新闻或公告时，您可能需要当前版本的所有贡献者，您可以使用 git 命令 `git log --pretty="%an" <PREVIOUS-RELEASE-SHA>..<CURRENT-RELEASE-SHA> | sort | uniq`
（将对应的版本改成两个版本的 tag 值）自动生成 git 作者姓名。
