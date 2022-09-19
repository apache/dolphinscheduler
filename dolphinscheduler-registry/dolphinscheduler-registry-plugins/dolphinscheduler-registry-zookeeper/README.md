# Introduction

This module is the zookeeper registry plugin module, this plugin will use zookeeper as the registry center.

# How to use

If you want to set the registry center as zookeeper,you need to set the registry properties in master/worker/api's appplication.yml

```yaml
registry:
  type: zookeeper
  zookeeper:
    namespace: dolphinscheduler
    connect-string: localhost:2181
    retry-policy:
      base-sleep-time: 60ms
      max-sleep: 300ms
      max-retries: 5
    session-timeout: 30s
    connection-timeout: 9s
    block-until-connected: 600ms
    # The following options are set according to personal needs    
    digest: ~
```

After do this config, you can start your DolphinScheduler cluster, your cluster will use zookeeper as registry center to
store server metadata.
