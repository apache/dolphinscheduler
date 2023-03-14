# Kubernetes non-Helm deployments

In some scenarios, your production environment may not support using or disabling Helm to deploy Kubernetes applications. In this case, you need to use the original configuration YAML file to deploy the DolphinScheduler service to the Kubernetes cluster.

This document will introduce how to use Helm in the development environment to generate a YAML resource file set, and then publish it to production for deployment.

## Prerequisites

- [Helm](https://helm.sh/) 3.1.0+ (Only In Development Environment)
- [Kubernetes](https://kubernetes.io/) 1.12+
- PV provisioner support in the underlying infrastructure

## Install DolphinScheduler

Download the official website docker image

```
docker pull apache/dolphinscheduler-master:<version>
docker pull apache/dolphinscheduler-worker:<version>
docker pull apache/dolphinscheduler-tools:<version>
docker pull apache/dolphinscheduler-api:<version>
docker pull apache/dolphinscheduler-alert-server:<version>
```

Based on the official website image modified to intranet Harbor image

If your build environment cannot be connected to the external network, you need to prepare an apt sources.list file that is accessible from the internal network in advance. The example is as follows:

```shell
deb http://mirrors.tencent.com/ubuntu-ports/ focal main restricted
deb http://mirrors.tencent.com/ubuntu-ports/ focal-updates main restricted
deb http://mirrors.tencent.com/ubuntu-ports/ focal universe
deb http://mirrors.tencent.com/ubuntu-ports/ focal-updates universe
deb http://mirrors.tencent.com/ubuntu-ports/ focal multiverse
deb http://mirrors.tencent.com/ubuntu-ports/ focal-updates multiverse
deb http://mirrors.tencent.com/ubuntu-ports/ focal-backports main restricted universe multiverse
```

Then use the following Dockerfile template to rebuild, and after the new image is built, push it to your company's Harbor warehouse

```Dockerfile
FROM apache/dolphinscheduler-<module>:<version>
# Enable non-interactive commands
ENV DEBIAN_FRONTEND noninteractive
ENV DEBCONF_NOWARNINGS yes
# replace apt source
COPY sources.list /etc/apt/
# Use mysql driver, note that if it is a dolphinscheduler-tools mirror, you need to use the /opt/dolphinscheduler/tools/libs directory
COPY mysql-connector-java-8.0.16.jar /opt/dolphinscheduler/libs
#  Some basic toolkits are recommended for installation, which is convenient for debugging and troubleshooting when problems occur
RUN apt-get update && apt-get install -y sudo telnet iputils-ping curl dnsutils iproute2 vim traceroute procps nload
```

Please download the source code package `apache-dolphinscheduler-<version>-src.tar.gz` ，Quickly generate deployment files with local Helm download address: [download address](https://dolphinscheduler.apache.org/zh-cn/download)

After downloading and decompressing, enter the directory of deploy/kubernetes/dolphinscheduler

1, Modify Chart.yaml

Modify Chart.yaml to remove the postgresql, mysql, and zookeeper components that Chart depends on. It is recommended to use external and separately installed mysql and zookeeper components

2，Configure Values.yaml

- Modify the mirror address to the intranet address, including master, worker, api, alert, tools, busybox (auxiliary mirror to detect whether the db service is ready)
- Configure the account and password for accessing the database
- Configure access to mysql, host_ip of zookeeper, do not use localhost
- enable ingress
- If there is no HDFS environment, change resource.storage.type to NONE to avoid failure to load Hadoop dependencies
- Other JVM memory, probe, Pod copy, monitoring heartbeat interval, rolling strategy, can be configured according to the actual situation

3，Generate deployment YAML set

```shell
helm template --namespace dolphinscheduler  dolphinscheduler . --output-dir prod_yamls
```

After the above command is executed, the YAML configuration of all resource objects of dolphinscheduler will be generated in the prod_yamls directory

Note: In the generated YAML deployment set, if there is a problem with the apiVersion version of the resource object, use the following command to verify it again (executed in the upper-level directory of values.yaml):

```shell
helm install --dry-run --debug dolphinscheduler  ./dolphinscheduler
```

4，Submit the generated YAML directory to the Git code warehouse, and then check out and deploy it in the production environment

```shell
kubectl apply -f prod_yamls
```

## Access DolphinScheduler UI

Subsequent steps are the same as using Helm, please refer to the Deployment chapter [kubernetes](./kubernetes.md) Access DolphinScheduler UI

