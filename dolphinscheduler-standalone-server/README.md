# Introduction

This module is used to start the dolphinscheduler in standalone mode. Currently Zookeeper and JDBC plugin are enabled as registries, depending on the properties configuration.

# How to use

1. Select the type of registry you want. The default is Zookeeper

-  If you have chosen Zookeeper as the registry, you can refer to the dollinscheduler-registry-plugins dollinscheduler-registry-Zookeeper readme.md documentation for configuration. (note: the default is of type Zookeeper, and a mock Zookeeper server is created, without the need to actually install ZK)
-  If you choose JDBC as the Registry, you can refer to the readme.md documentation for configuration




##