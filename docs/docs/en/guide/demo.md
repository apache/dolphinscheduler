# DolphinScheduler Initialize The Workflow Demo

## Prepare

### Backup Previous Version's Files and Database

To prevent data loss by some miss-operation, it is recommended to back up data before initializing the workflow demo. The backup way according to your environment.

### Download the Latest Version Installation Package

Download the latest binary distribute package from [download](https://dolphinscheduler.apache.org/en-us/download) and then put it in the different
directory where current service running. And all below command is running in this directory.

## Start

### Start Services of DolphinScheduler

Start all services of dolphinscheduler according to your deployment method. If you deploy your dolphinscheduler according to [cluster deployment](installation/cluster.md), you can start all services by command `sh ./script/start-all.sh`.

### Database Configuration

Initializing the workflow demo needs to store metabase in other database like MySQL or PostgreSQL, they have to change some configuration. Follow the instructions in [datasource-setting](howto/datasource-setting.md) `Standalone Switching Metadata Database Configuration` section to create and initialize database.

### Tenant Configuration

#### Change `dolphinscheduler-tools/resources/application.yaml` Placement Details

```
demo:
  tenant-code: default
  domain-name: localhost
  api-server-port: 5173
```

Mentioned above, tenant-code is the default tenant, users can modify the user name according to their operating system, this replaces the manual tenant creation action, api-server-port is the port number of the service.

Then execute the startup script that initializes the workflow demo service: `sh ./tools/bin/create-demo-processes.sh` to start the service.

To create a demo, you can refer to [Quick Start](start/quick-start.md)
