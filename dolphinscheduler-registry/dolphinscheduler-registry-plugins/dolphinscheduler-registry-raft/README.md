# Introduction

This module is the RAFT consensus algorithm registry plugin module, this plugin will use raft cluster as the registry center.
The Raft registration plugin consists of two parts: 
the server and the client. The Master module of DolphinScheduler will form a Raft server cluster
, while the Worker modules and API modules will interact with the Raft server using the Raft client.

# How to use

If you want to set the registry center as raft, 

you need to set the registry properties in master/worker/api's appplication.yml,


`please remember change the server-port in appplication.yml`.

NOTE: In production environment, in order to achieve high availability, the master must be an odd number e.g 3 or 5.

master's properties example
```yaml
registry:
  type: raft
  cluster-name: dolphinscheduler
  server-address-list: 127.0.0.1:8181,127.0.0.1:8182,127.0.0.1:8183
  server-address: 127.0.0.1
  server-port: 8181
  log-storage-dir: raft-data/
  listener-check-interval: 3s
  distributed-lock-timeout: 3s
  distributedLockRetryInterval: 3s
  module: master
```
worker's appplication.yml example
```yaml
registry:
  type: raft
  cluster-name: dolphinscheduler
  server-address-list: 127.0.0.1:8181,127.0.0.1:8182,127.0.0.1:8183
  listener-check-interval: 3s
  distributed-lock-timeout: 3s
  distributedLockRetryInterval: 3s
  module: worker
```
api's appplication.yml example
```yaml
registry:
  type: raft
  cluster-name: dolphinscheduler
  listener-check-interval: 3s
  distributed-lock-timeout: 3s
  distributedLockRetryInterval: 3s
  module: api
```

Then you can start your DolphinScheduler cluster, your cluster will use raft cluster as registry center to
store server metadata.