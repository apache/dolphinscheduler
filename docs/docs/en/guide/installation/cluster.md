# Cluster Deployment

Cluster deployment is to deploy the DolphinScheduler on multiple machines for running massive tasks in production.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Deployment Steps

Cluster deployment uses the same scripts and configuration files as [pseudo-cluster deployment](pseudo-cluster.md), so the preparation and deployment steps are the same as pseudo-cluster deployment. The difference is that pseudo-cluster deployment is for one machine, while cluster deployment (Cluster) is for multiple machines. And steps of "Modify Configuration" are quite different between pseudo-cluster deployment and cluster deployment.

### Prerequisites and DolphinScheduler Startup Environment Preparations

Configure all the configurations refer to [pseudo-cluster deployment](pseudo-cluster.md) on every machine, except sections `Prerequisites`, `Start ZooKeeper` and `Initialize the Database` of the `DolphinScheduler Startup Environment`.

### Modify Configuration

This step differs quite a lot from [pseudo-cluster deployment](pseudo-cluster.md), please use `scp` or other methods to distribute the configuration files to each machine, then modify the configuration files.

## Start and Login DolphinScheduler

Same as [pseudo-cluster](pseudo-cluster.md)

## Start and Stop Server

Same as [pseudo-cluster](pseudo-cluster.md)
