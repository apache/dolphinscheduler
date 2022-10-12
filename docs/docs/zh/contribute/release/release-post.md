# 发版后续

发送公告邮件后，我们还有一些发布任务要做，目前我们必须将 Docker 镜像发布到 Docker Hub 和 并且需要将 pydolphinscheduler 发布到 PyPI。

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

## 发布 pydolphinscheduler 到 PyPI

需要将 Python API 发布到 PyPI，请参考 [Python API release](https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-python/pydolphinscheduler/RELEASE.md#to-pypi)
完成 PyPI 的发版

## 获取全部的贡献者

当您想要发布新版本的新闻或公告时，您可能需要当前版本的所有贡献者，您可以在 `tools/release` 中使用命令 `python release.py contributor` 自动生成贡献者 Github id。
