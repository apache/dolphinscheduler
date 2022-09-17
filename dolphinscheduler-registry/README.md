## Function

DS use registry to do the below three things:

1. Store the metadata of master/worker so that it can get notify when nodes up and down.
2. Store the metadata of worker to do load balance.
3. Acquire a global lock when do failover.

So for DS, the registry need to notify the server when the server subscribe data have added/deleted/updated, support a way to create/release a global lock,
delete the server's metadata when server down.

## How to use

At present, we have implements three registry: Zookeeper(Default),Etcd,Mysql. If you
want to use them, you should config it at resource/application.yaml. The configuration details
can be viewed in the README of plugin under Module dolphinscheduler-registry-plugins

## Module

### dolphinscheduler-registry-all

This module is used for exporting the implemention of registry.
If you want to add new registry,you should add the dependency in the pom.xml

### dolphinscheduler-registry-api

This module contains the relevant interfaces involved in the use of the registry.
The following are several important interfaces
1. Registry Interface: If you want to implement your own registry, you just need to implement this interface
2. ConnectionListener Interface: This interface is responsible for the connection status between the client and the registry,
The connection state can be viewed in ConnectionState.java
3. SubscribeListener Interface: This interface is responsible for monitoring the state changes of child nodes under the specified prefix.
Event content can be viewed in event.java

### dolphinscheduler-registry-plugins

This module contains all registry implementations in DS
