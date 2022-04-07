SkyWalking Agent Deployment
=============================

The `dolphinscheduler-skywalking` module provides [SkyWalking](https://skywalking.apache.org/) monitor agent for the DolphinScheduler project.

This document describes how to enable SkyWalking version 8.4+ support with this module (recommend using SkyWalking 8.5.0).

## Installation

The following configuration is used to enable the SkyWalking agent.

### Through Environment Variable Configuration (for Docker Compose)

Modify SkyWalking environment variables in `docker/docker-swarm/config.env.sh`:

```
SKYWALKING_ENABLE=true
SW_AGENT_COLLECTOR_BACKEND_SERVICES=127.0.0.1:11800
SW_GRPC_LOG_SERVER_HOST=127.0.0.1
SW_GRPC_LOG_SERVER_PORT=11800
```

And run:

```shell
$ docker-compose up -d
```

### Through Environment Variable Configuration (for Docker)

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

### Through install_config.conf Configuration (for DolphinScheduler install.sh)

Add the following configurations to `${workDir}/conf/config/install_config.conf`.

```properties

# SkyWalking config
# note: enable SkyWalking tracking plugin
enableSkywalking="true"
# note: configure SkyWalking backend service address
skywalkingServers="your.skywalking-oap-server.com:11800"
# note: configure SkyWalking log reporter host
skywalkingLogReporterHost="your.skywalking-log-reporter.com"
# note: configure SkyWalking log reporter port
skywalkingLogReporterPort="11800"

```

## Usage

### Import Dashboard

#### Import DolphinScheduler Dashboard to SkyWalking Server

Copy the `${dolphinscheduler.home}/ext/skywalking-agent/dashboard/dolphinscheduler.yml` file into `${skywalking-oap-server.home}/config/ui-initialized-templates/` directory, and restart SkyWalking oap-server.

#### View DolphinScheduler Dashboard

If you have opened the SkyWalking dashboard with a browser before, you need to clear the browser cache.

![img1](/img/skywalking/import-dashboard-1.jpg)
