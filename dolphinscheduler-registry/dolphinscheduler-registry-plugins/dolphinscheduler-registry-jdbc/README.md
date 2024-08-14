# Introduction

This module is the jdbc registry plugin module, this plugin will use jdbc as the registry center. Will use the database
configuration same as DolphinScheduler in api'yaml default.

# How to use

1. Initialize the database table

- If you use Mysql you can directly execute the sql script `src/main/resources/mysql_registry_init.sql`.

- If you use Postgresql you can directly execute the sql script `src/main/resources/postgresql_registry_init.sql`.

2. Change the config

You need to set the registry properties in master/worker/api's application.yml

```yaml
registry:
  type: jdbc
```

After do this two steps, you can start your DolphinScheduler cluster, your cluster will use mysql as registry center to
store server metadata.

NOTE: You need to add `mysql-connector-java.jar` into DS classpath if you use mysql database, since this plugin will not
bundle this driver in distribution.
You can get the detail
about <a href="https://dolphinscheduler.apache.org/en-us/docs/3.1.2/guide/installation/pseudo-cluster">Initialize the
Database</a>.

## Optional configuration

```yaml
registry:
  type: jdbc
  # Used to schedule refresh the heartbeat.
  heartbeat-refresh-interval: 3s
  # Once the client's heartbeat is not refresh in this time, the server will consider the client is offline.
  session-timeout: 60s
  # The hikari configuration, default will use the same datasource pool as DolphinScheduler.
  hikari-config:
    jdbc-url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler
    username: root
    password: root
    maximum-pool-size: 5
    connection-timeout: 9000
    idle-timeout: 600000
```

## Use different database configuration for jdbc registry center

You need to set the registry properties in master/worker/api's application.yml

### Use Mysql as registry center

```yaml
registry:
  type: jdbc
  heartbeat-refresh-interval: 3s
  session-timeout: 60s
  hikari-config:
    jdbc-url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler
    username: root
    password: root
    maximum-pool-size: 5
    connection-timeout: 9000
    idle-timeout: 600000
```

### Use Postgresql as registry center

```yaml
registry:
  type: jdbc
  heartbeat-refresh-interval: 3s
  session-timeout: 60s
  hikari-config:
    jdbc-url: jdbc:postgresql://localhost:5432/dolphinscheduler
    username: root
    password: root
    maximum-pool-size: 5
    connection-timeout: 9000
    idle-timeout: 600000
```

