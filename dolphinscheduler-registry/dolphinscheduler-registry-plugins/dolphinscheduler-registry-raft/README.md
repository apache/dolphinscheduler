# Introduction

This module is the RAFT consensus algorithm registry plugin module, this plugin will use raft cluster as the registry center.

# How to use

If you want to set the registry center as raft, 

you need to set the registry properties in master/worker/api's appplication.yml,

please remember change the server-port in appplication.yml.

In PRO environment, worker and api add `/learner` suffix in `server-address-list`


```yaml
registry:
  type: raft
  cluster-name: dolphinscheduler
  server-address-list: 127.0.0.1:8181,127.0.0.1:8182/learner,127.0.0.1:8183/learner
  log-storage-dir: raft-data/
  db-storage-dir: raft-db/
  election-timeout: 1000ms
  snapshot-interval: 1800s
  listener-check-interval: 2s
  connection-expire-factor: 2
  server-address: 127.0.0.1
  server-port: 8181
```

Then you can start your DolphinScheduler cluster, your cluster will use raft cluster as registry center to
store server metadata.