# 集群部署(Cluster)

集群部署目的是在多台机器部署 DolphinScheduler 服务，用于运行大量任务情况。

如果你是新手，想要体验 DolphinScheduler 的功能，推荐使用[Standalone](standalone.md)方式体检。如果你想体验更完整的功能，或者更大的任务量，推荐使用[伪集群部署](pseudo-cluster.md)。如果你是在生产中使用，推荐使用[集群部署](cluster.md)或者[kubernetes](kubernetes.md)

## 部署步骤

集群部署(Cluster)使用的脚本和配置文件与[伪集群部署](pseudo-cluster.md)中的配置一样，所以所需要的步骤也与伪集群部署大致一样。区别就是伪集群部署针对的是一台机器，而集群部署(Cluster)需要针对多台机器，且两者“修改相关配置”步骤区别较大

### 开启SSL（可选）

在集群部署中，您可以启用SSL以实现安全的内部通信。DolphinScheduler集群可以配置为使用安全通信，并对集群中的节点进行内部身份验证。

开启SLL认证，你有两件事要做。 首先你需要生成`cert.crt`和`private.pem`文件。

步骤 1：生成私钥（private.pem）

打开终端并运行以下命令生成私钥：

```bash
openssl genpkey -algorithm RSA -out private.pem -pkeyopt rsa_keygen_bits:2048
```

此命令会生成一个 2048 位的 RSA 私钥，并将其保存为 private.pem 文件。

步骤 2：生成证书签署请求（CSR）

在生成证书之前，您需要生成一个证书签署请求（CSR）。运行以下命令：

```bash
openssl req -new -key private.pem -out request.csr
```

此命令会提示您输入一些信息，例如国家、州/省、组织名等。您输入的信息将会嵌入到生成的证书中。

步骤 3：生成自签名证书（cert.crt）

使用 CSR 来生成自签名证书。运行以下命令：

```bash
openssl x509 -req -days 365 -in request.csr -signkey private.pem -out cert.crt
```

此命令会生成一个有效期为 365 天的自签名证书，并将其保存为 cert.crt 文件。

然后修改`dolphinscheduler-master`、`dolphinscheduler-worker`、`dolphinscheduler-api`、`dolphinscheduler-alert-server`模块中的`application.yaml`文件。

```yaml
rpc:
  ssl:
    enabled: true
    cert-file-path: /path/cert.crt
    key-file-path: /path/private.pem
```

您需要将`enabled`改为`true`，同时将配置`cert-file-path`和`key-file-path`的文件路劲。

### 前置准备工作 && 准备 DolphinScheduler 启动环境

需要将安装包分发至每台集群的每台服务器上，并且需要在每台机器中进行配置执行[伪集群部署](pseudo-cluster.md)中的所有执行项

> **_注意:_** 请确保每台机器的配置文件都是一致的，否则会导致集群无法正常工作
> **_注意:_** 每个服务都是无状态且互相独立的，所以可以在每台机器上部署多个服务，但是需要注意端口冲突问题
> **_注意_**: DS默认使用本地模式的目录 /tmp/dolphinscheduler 作为资源中心, 如果需要修改资源中心目录, 请修改配置文件 conf/common.properties 中 resource 的相关配置项

### 修改相关配置

这个是与[伪集群部署](pseudo-cluster.md)差异较大的一步，请使用 scp 等方式将配置文件分发到各台机器上，然后修改配置文件

## 启动 DolphinScheduler && 登录 DolphinScheduler && 启停服务

[与伪集群部署](pseudo-cluster.md)保持一致
