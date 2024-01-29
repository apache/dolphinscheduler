Please refer to the contribution document [How to contribute](docs/docs/en/contribute/join/contribute.md)

## How to Build

```bash
./mvnw clean install -Prelease
```

### Build with different Zookeeper versions

The default Zookeeper Server version supported is 3.8.0.
```bash
# Default Zookeeper 3.8.0
./mvnw clean install -Prelease
# Support to Zookeeper 3.4.6+
./mvnw clean install -Prelease -Dzk-3.4
```

Artifact:

```
dolphinscheduler-dist/target/apache-dolphinscheduler-${latest.release.version}-bin.tar.gz: Binary package of DolphinScheduler
dolphinscheduler-dist/target/apache-dolphinscheduler-${latest.release.version}-src.tar.gz: Source code package of DolphinScheduler
```