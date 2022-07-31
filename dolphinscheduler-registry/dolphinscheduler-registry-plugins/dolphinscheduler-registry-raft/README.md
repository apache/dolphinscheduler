# Introduction

This module is the RAFT consensus algorithm registry plugin module, this plugin will use raft cluster as the registry center.

# How to use

If you want to set the registry center as raft, 

you need to set the registry properties in master/worker/api's appplication.yml,

worker and api address add `/learner` suffix in `server-address-list`, indicates that they are not participate in the leader election.

`please remember change the server-port in appplication.yml`.

NOTE: In production environment, in order to achieve high availability, the master must be an odd number e.g 3 or 5.

```yaml
registry:
  type: raft
  cluster-name: dolphinscheduler
  server-address-list: 127.0.0.1:8181,127.0.0.1:8182/learner,127.0.0.1:8183/learner
  log-storage-dir: raft-data/
  election-timeout: 1000ms
  listener-check-interval: 2s
  distributed-lock-timeout: 3s
  server-address: 127.0.0.1
  server-port: 8183
  module: api
  rpc-core-threads: 8
  rpc-timeout-millis: 5000ms
```

Then you can start your DolphinScheduler cluster, your cluster will use raft cluster as registry center to
store server metadata.