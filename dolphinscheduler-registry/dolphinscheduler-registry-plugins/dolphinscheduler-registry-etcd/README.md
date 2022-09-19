# Introduction

This module is the etcd registry plugin module, this plugin will use etcd as the registry center.

# How to use

If you want to set the registry center as etcd, you need to set the registry properties in master/worker/api's appplication.yml

```yaml
registry:
  type: etcd
  endpoints: "http://etcd0:2379, http://etcd1:2379, http://etcd2:2379"
  # The options below have default values
  namespace: dolphinscheduler
  connectionTimeout: 9s
  # The unit is milliseconds
  retryDelay: 60ms
  retryMaxDelay: 300ms
  retryMaxDuration: 1500ms
  # The following options are set according to personal needs
  user: ""
  password: ""
  authority: ""
  loadBalancerPolicy: ""
```

After do this config, you can start your DolphinScheduler cluster, your cluster will use etcd as registry center to
store server metadata.
