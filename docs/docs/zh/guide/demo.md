# DolphinScheduler 初始化工作流 demo

## 准备工作

### 备份上一版本文件和数据库

为了防止操作错误导致数据丢失，建议初始化工作流 demo 服务之前备份数据，备份方法请结合你数据库的情况来定

### 下载新版本的安装包

在[下载](https://dolphinscheduler.apache.org/zh-cn/download)页面下载最新版本的二进制安装包，并将二进制包放到与当前 dolphinscheduler 服务不一样的路径中，以下服务启动操作都需要在新版本的目录进行。

## 服务启动步骤

### 开启 dolphinscheduler 服务

根据你部署方式开启 dolphinscheduler 的所有服务，如果你是通过 [集群部署](installation/cluster.md) 来部署你的 dolphinscheduler 的话，可以通过 `sh ./script/start-all.sh` 开启全部服务。

### 数据库配置

初始化工作流 demo 服务需要使用 MySQL 或 PostgreSQL 等其他数据库作为其元数据存储数据，因此必须更改一些配置。
请参考[数据源配置](howto/datasource-setting.md) `Standalone 切换元数据库`创建并初始化数据库 ，然后运行 demo 服务启动脚本。

### 租户配置

#### 修改 `dolphinscheduler-tools/resources/application.yaml` 配置内容

```
demo:
  tenant-code: default
  domain-name: localhost
  api-server-port: 5173
```

其中 tenant-code 是默认租户 default ，用户可以根据自己操作系统用户名修改，从而代替手动创建租户操作。api-server-port 是 dolphinscheduler 服务的端口号

然后执行初始化工作流 demo 服务的启动脚本：`sh ./tools/bin/create-demo-processes.sh` 来启动服务。

创建 demo 可以参考[快速上手](start/quick-start.md)
