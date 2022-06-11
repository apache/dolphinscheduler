# Introduction

This module is the mysql registry plugin module, this plugin will use mysql as the registry center.

# How to use

If you want to set the registry center as mysql, you need to do the below two steps:

1. Initialize the mysql table

You can directly execute the sql script `src/main/resources/mysql_registry_init.sql`.

2. Open the config

You need to set the registry type to mysql in master/worker/api's appplication.yml

```yaml
registry:
  type: mysql
```

After do this two steps, you can start your DolphinScheduler cluster, your cluster will use mysql as registry centery to
store server metadata.