SkyWalking Agent 部署
=============================

dolphinscheduler-skywalking 模块为 Dolphinscheduler 项目提供了 [Skywalking](https://skywalking.apache.org/) 监控代理。

本文档介绍了如何通过此模块接入 SkyWalking 8.4+ (推荐使用8.5.0)。

# 安装

以下配置用于启用 Skywalking agent。

### 通过配置环境变量 (使用 Docker Compose 部署时)

修改 `docker/docker-swarm/config.env.sh` 文件中的 SKYWALKING 环境变量:

```
SKYWALKING_ENABLE=true
SW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800
SW_GRPC_LOG_SERVER_HOST=127.0.0.1
SW_GRPC_LOG_SERVER_PORT=11800
```

并且运行

```shell
$ docker-compose up -d
```

### 通过配置环境变量 (使用 Docker 部署时)

```shell
$ docker run -d --name dolphinscheduler \
-e DATABASE_HOST="192.168.x.x" -e DATABASE_PORT="5432" -e DATABASE_DATABASE="dolphinscheduler" \
-e DATABASE_USERNAME="test" -e DATABASE_PASSWORD="test" \
-e ZOOKEEPER_QUORUM="192.168.x.x:2181" \
-e SKYWALKING_ENABLE="true" \
-e SW_AGENT_COLLECTOR_BACKEND_SERVICES="your.skywalking-oap-server.com:11800" \
-e SW_GRPC_LOG_SERVER_HOST="your.skywalking-log-reporter.com" \
-e SW_GRPC_LOG_SERVER_PORT="11800" \
-p 12345:12345 \
apache/dolphinscheduler:1.3.8 all
```

### 通过配置 install_config.conf (使用 DolphinScheduler install.sh 部署时)

添加以下配置到 `${workDir}/conf/config/install_config.conf`.

```properties

# skywalking config
# note: enable skywalking tracking plugin
enableSkywalking="true"
# note: configure skywalking backend service address
skywalkingServers="your.skywalking-oap-server.com:11800"
# note: configure skywalking log reporter host
skywalkingLogReporterHost="your.skywalking-log-reporter.com"
# note: configure skywalking log reporter port
skywalkingLogReporterPort="11800"

```

# 使用

### 导入图表

#### 导入图表到 Skywalking server

复制 `${dolphinscheduler.home}/ext/skywalking-agent/dashboard/dolphinscheduler.yml` 文件到 `${skywalking-oap-server.home}/config/ui-initialized-templates/` 目录下，并重启 Skywalking oap-server。

#### 查看 dolphinscheduler 图表

如果之前已经使用浏览器打开过 Skywalking，则需要清空浏览器缓存。

![img1](/img/skywalking/import-dashboard-1.jpg)
