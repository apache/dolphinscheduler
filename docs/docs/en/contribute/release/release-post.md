# Release Post

We still have some publish task to do after we send the announcement mail, currently we have to publish Docker images to
Docker Hub and also publish pydolphinscheduler to PyPI.

## Publish Docker Image

we already have the exists CI to publish the latest Docker image to GitHub container register with [config](https://github.com/apache/dolphinscheduler/blob/d80cf21456265c9d84e642bdb4db4067c7577fc6/.github/workflows/publish-docker.yaml#L55-L63).
We could reuse the main command the CI run and publish our Docker images to Docker Hub by single command.

```bash
# Please change the <VERSION> place hold to the version you release
./mvnw -B clean deploy \
    -Dmaven.test.skip \
    -Dmaven.javadoc.skip \
    -Dmaven.checkstyle.skip \
    -Dmaven.deploy.skip \
    -Ddocker.tag=<VERSION> \
    -Ddocker.hub=apache \
    -Pdocker,release
```

## Publish pydolphinscheduler to PyPI

Python API need to release to PyPI for easier download and use, you can see more detail in [Python API release](https://github.com/apache/dolphinscheduler/blob/dev/dolphinscheduler-python/pydolphinscheduler/RELEASE.md#to-pypi)
to finish PyPI release.

## Get All Contributors

You might need all contributors in current release when you want to publish the release news or announcement, you could
use command `python release.py contributor` in directory `tools/release` to auto generate contributor GitHub id.
