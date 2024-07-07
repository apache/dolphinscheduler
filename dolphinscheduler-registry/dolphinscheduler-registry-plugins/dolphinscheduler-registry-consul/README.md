# Introduction

This module is the consul registry plugin module, this plugin will use consul as the registry center.

# How to use

If you want to set the registry center as consul,you need to set the registry properties in master/worker/api's application.yml

```yaml
registry:
  type: consul
  url: http://localhost:8500
  namespace: dolphinscheduler
  session-timeout: 30s
  session-refresh-time: 3s
  user-name: 
  password:
  acl-token:
```

After do this config, you can start your DolphinScheduler cluster, your cluster will use consul as registry center to
store server metadata.
