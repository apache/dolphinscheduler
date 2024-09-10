# Cluster Deployment

Cluster deployment is to deploy the DolphinScheduler on multiple machines for running massive tasks in production.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Deployment Steps

Cluster deployment uses the same scripts and configuration files as [pseudo-cluster deployment](pseudo-cluster.md), so the preparation and deployment steps are the same as pseudo-cluster deployment. The difference is that pseudo-cluster deployment is for one machine, while cluster deployment (Cluster) is for multiple machines. And steps of "Modify Configuration" are quite different between pseudo-cluster deployment and cluster deployment.

### Prerequisites and DolphinScheduler Startup Environment Preparations

Distribute the installation package to each server of each cluster and perform all the steps in [pseudo-cluster deployment](pseudo-cluster.md) on each machine.

> **_NOTICE:_** Make sure that the configuration files on each machine are consistent, otherwise the cluster will not work properly.
> **_NOTICE:_** Each service is stateless and independent of each other, so you can deploy multiple services on each machine, but you need to pay attention to port conflicts.
> **_NOTICE:_** DS uses the /tmp/dolphinscheduler directory as the resource center by default. If you need to change the directory of the resource center, change the resource items in the conf/common.properties file

### Modify Configuration

This step differs quite a lot from [pseudo-cluster deployment](pseudo-cluster.md), please use `scp` or other methods to distribute the configuration files to each machine, then modify the configuration files.

## Start and Login DolphinScheduler

Same as [pseudo-cluster](pseudo-cluster.md)

## Start and Stop Server

Same as [pseudo-cluster](pseudo-cluster.md)
