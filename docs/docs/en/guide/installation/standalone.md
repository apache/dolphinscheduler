# Standalone

Standalone only for quick experience for DolphinScheduler.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

> **_Note:_** Standalone only recommends the usage of fewer than 20 workflows, because it uses H2 Database, ZooKeeper Testing Server, too many tasks may cause instability.

## Preparation

* JDK：download [JDK][jdk] (1.8+), and configure `JAVA_HOME` to and `PATH` variable. You can skip this step if it already exists in your environment.
* Binary package: download the DolphinScheduler binary package at [download page](https://dolphinscheduler.apache.org/en-us/download/download.html).

## Start DolphinScheduler Standalone Server

### Extract and Start DolphinScheduler

There is a standalone startup script in the binary compressed package, which can be quickly started after extraction. Switch to a user with sudo permission and run the script:

```shell
# Extract and start Standalone Server
tar -xvzf apache-dolphinscheduler-*-bin.tar.gz
cd apache-dolphinscheduler-*-bin
sh ./bin/dolphinscheduler-daemon.sh start standalone-server
```

### Login DolphinScheduler

Access address `http://localhost:12345/dolphinscheduler` and login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**

### Start or Stop Server

The script `./bin/dolphinscheduler-daemon.sh`can be used not only quickly start standalone, but also to stop the service operation. The following are all the commands:

```shell
# Start Standalone Server
sh ./bin/dolphinscheduler-daemon.sh start standalone-server
# Stop Standalone Server
sh ./bin/dolphinscheduler-daemon.sh stop standalone-server
```

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html
