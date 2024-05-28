# Standalone

Standalone only for quick experience for DolphinScheduler.

If you are a newbie and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md).
If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md).
If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

> **_Note:_** Standalone only recommends the usage of fewer than 20 workflows, because it uses in-memory H2 Database in default, ZooKeeper Testing Server, too many tasks may cause instability.
> When Standalone stops or restarts, in-memory H2 database will clear up. To use Standalone with external databases like mysql or postgresql, please see [`Database Configuration`](#database-configuration).

## Preparation

- JDK：download [JDK][jdk] (1.8+), install and configure environment variable `JAVA_HOME` and append `bin` dir (included in `JAVA_HOME`) to `PATH` variable. You can skip this step if it already exists in your environment.
- Binary package: download the DolphinScheduler binary package at [download page](https://dolphinscheduler.apache.org/en-us/download/<version>).  <!-- markdown-link-check-disable-line -->

## Start DolphinScheduler Standalone Server

### Extract and Start DolphinScheduler

There is a standalone startup script in the binary compressed package, which can be quickly started after extraction. Switch to a user with sudo permission and run the script:

```shell
# Extract and start Standalone Server
tar -xvzf apache-dolphinscheduler-*-bin.tar.gz
chmod -R 755 apache-dolphinscheduler-*-bin
cd apache-dolphinscheduler-*-bin
bash ./bin/dolphinscheduler-daemon.sh start standalone-server
```

### Login DolphinScheduler

Access address `http://localhost:12345/dolphinscheduler/ui` and login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**

![login](../../../../img/new_ui/dev/quick-start/login.png)

### Start or Stop Server

The script `./bin/dolphinscheduler-daemon.sh` can be used not only quickly start standalone, but also to stop the service operation. The following are all the commands:

```shell
# Start Standalone Server
bash ./bin/dolphinscheduler-daemon.sh start standalone-server
# Stop Standalone Server
bash ./bin/dolphinscheduler-daemon.sh stop standalone-server
# Check Standalone Server status
bash ./bin/dolphinscheduler-daemon.sh status standalone-server
```

> Note: Python gateway service is started along with the api-server, and if you do not want to start Python gateway
> service please disabled it by changing the yaml config `python-gateway.enabled : false` in api-server's configuration
> path `api-server/conf/application.yaml`

[jdk]: https://www.oracle.com/technetwork/java/javase/downloads/index.html

## Database Configuration

Standalone server use H2 database as its metadata store, it is easy and users do not need to start database before they set up server.
But if user want to store metabase in other database like MySQL or PostgreSQL, they have to change some configuration. Follow the instructions in [datasource-setting](../howto/datasource-setting.md) `Standalone Switching Metadata Database Configuration` section to create and initialize database
