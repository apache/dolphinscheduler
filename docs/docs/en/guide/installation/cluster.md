# Cluster Deployment

Cluster deployment is to deploy the DolphinScheduler on multiple machines for running massive tasks in production.

If you are a new hand and want to experience DolphinScheduler functions, we recommend you install follow [Standalone deployment](standalone.md). If you want to experience more complete functions and schedule massive tasks, we recommend you install follow [pseudo-cluster deployment](pseudo-cluster.md). If you want to deploy DolphinScheduler in production, we recommend you follow [cluster deployment](cluster.md) or [Kubernetes deployment](kubernetes.md).

## Deployment Steps

Cluster deployment uses the same scripts and configuration files as [pseudo-cluster deployment](pseudo-cluster.md), so the preparation and deployment steps are the same as pseudo-cluster deployment. The difference is that pseudo-cluster deployment is for one machine, while cluster deployment (Cluster) is for multiple machines. And steps of "Modify Configuration" are quite different between pseudo-cluster deployment and cluster deployment.

## Enable SSL (optional)

In cluster deployment, you can enable SSL for secure internal communication. The DolphinScheduler cluster can be configured to use secured communication with internal authentication of the nodes in the cluster.
To enable SLL authentication, you have two things to do. Firstly, you need to generate `cert.crt` and `private.pem` files.

Step 1: Generate private key (private.pem)

Open the terminal and run the following command to generate a private key:

```bash
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
```

This command will generate a 2048 bit RSA private key and save it as a private.pem file.

Step 2: Generate Certificate Signing Request (CSR)

Before generating a certificate, you need to generate a Certificate Signing Request (CSR). Run the following command:

```bash
openssl req -new -key private.pem -out request.csr
```

This command will prompt you to enter some information, such as country, state/province, organization name, etc. The information you input will be embedded into the generated certificate.

Step 3: Generate a self signed certificate (cert.crt)

Use CSR to generate self signed certificates. Run the following command:

```bash
openssl x509 -req -days 365 -in request.csr -signkey private.pem -out cert.crt
```

This command will generate a self signed certificate with a validity period of 365 days and save it as a cert.crt file.

Then modify the `application.yaml` file in the `dolphinscheduler-master`, `dolphinscheduler-worker`, `dolphinscheduler-api` and `dolphinscheduler-alert-server` modules.

```yaml
rpc:
  ssl:
    enabled: true
    cert-file-path: /path/cert.crt
    key-file-path: /path/private.pem
```

You need to change `enabled` to `true` and configure the file routing for `cert-file-path` and `key-file-path`.

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
