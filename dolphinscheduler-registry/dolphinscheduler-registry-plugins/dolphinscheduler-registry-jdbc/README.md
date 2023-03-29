# Introduction

This module is the mysql registry plugin module, this plugin will use mysql as the registry center.

# How to use

## Use Mysql as registry center
If you want to set mysql as the registry center, you need to do the below two steps:

1. Initialize the mysql table

You can directly execute the sql script `src/main/resources/mysql_registry_init.sql`.

2. Open the config

You need to set the registry properties in master/worker/api's appplication.yml

```yaml
registry:
  type: jdbc
  term-refresh-interval: 2s
  term-expire-times: 3
  hikari-config:
    jdbc-url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler
    username: root
    password: root
    maximum-pool-size: 5
    connection-timeout: 9000
    idle-timeout: 600000
```

After do this two steps, you can start your DolphinScheduler cluster, your cluster will use mysql as registry center to
store server metadata.

NOTE: You need to add `mysql-connector-java.jar` into DS classpath, since this plugin will not bundle this driver in distribution.
You can get the detail about <a href="https://dolphinscheduler.apache.org/en-us/docs/3.1.2/guide/installation/pseudo-cluster">Initialize the Database</a>

## Use Postgresql as registry center
If you want to set Postgresql as the registry center, you need to do the below two steps:

1. Initialize the PostgreSQL table

You can directly execute the sql script `src/main/resources/postgresql_registry_init.sql`.

2. Open the config

You need to set the registry properties in master/worker/api's appplication.yml

```yaml
registry:
  type: jdbc
  term-refresh-interval: 2s
  term-expire-times: 3
  hikari-config:
    jdbc-url: jdbc:postgresql://localhost:5432/dolphinscheduler
    username: root
    password: root
    maximum-pool-size: 5
    connection-timeout: 9000
    idle-timeout: 600000
```

After do this two steps, you can start your DolphinScheduler cluster, your cluster will use postgresql as registry center to
store server metadata.
