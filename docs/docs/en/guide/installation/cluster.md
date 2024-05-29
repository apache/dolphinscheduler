# Cluster Deployment

Cluster deployment is to deploy the DolphinScheduler on multiple machines for running massive tasks in production.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Deployment Steps

Cluster deployment uses the same scripts and configuration files as [pseudo-cluster deployment](pseudo-cluster.md), so the preparation and deployment steps are the same as pseudo-cluster deployment. The difference is that pseudo-cluster deployment is for one machine, while cluster deployment (Cluster) is for multiple machines. And steps of "Modify Configuration" are quite different between pseudo-cluster deployment and cluster deployment.

### Prerequisites and DolphinScheduler Startup Environment Preparations

Configure all the configurations refer to [pseudo-cluster deployment](pseudo-cluster.md) on every machine, except sections `Prerequisites`, `Start ZooKeeper` and `Initialize the Database` of the `DolphinScheduler Startup Environment`.

### Modify Configuration

This step differs quite a lot from [pseudo-cluster deployment](pseudo-cluster.md), because the deployment script transfers the required resources for installation to each deployment machine by using `scp`. So we only need to modify the configuration of the machine that runs `install.sh` script and configurations will dispatch to cluster by `scp`. The configuration file is under the path `bin/env/install_env.sh`, here we only need to modify section **INSTALL MACHINE**, **DolphinScheduler ENV, Database, Registry Server** and keep other sections the same as [pseudo-cluster deployment](pseudo-cluster.md), the following describes the parameters that must be modified:

```shell
# ---------------------------------------------------------
# INSTALL MACHINE
# ---------------------------------------------------------
# Using IP or machine hostname for the server going to deploy master, worker, API server, the IP of the server
# If you using a hostname, make sure machines could connect each other by hostname
# As below, the hostname of the machine deploying DolphinScheduler is ds1, ds2, ds3, ds4, ds5, where ds1, ds2 install the master server, ds3, ds4, and ds5 installs worker server, the alert server is installed in ds4, and the API server is installed in ds5
ips="ds1,ds2,ds3,ds4,ds5"
masters="ds1,ds2"
workers="ds3:default,ds4:default,ds5:default"
alertServer="ds4"
apiServers="ds5"
```

## Start and Login DolphinScheduler

Same as [pseudo-cluster](pseudo-cluster.md)

## Start and Stop Server

Same as [pseudo-cluster](pseudo-cluster.md)
