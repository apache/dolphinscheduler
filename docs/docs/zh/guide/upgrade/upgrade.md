# DolphinScheduler 升级

## 准备工作

### 检查不向前兼容的更改

在升级之前，您应该检查 [incompatible change](./incompatible.md)，因为一些不兼容的更改可能会破坏您当前的功能。

### 备份上一版本文件和数据库

为了防止操作错误导致数据丢失，建议升级之前备份数据，备份方法请结合你数据库的情况来定

### 下载新版本的安装包

在[下载](https://dolphinscheduler.apache.org/zh-cn/download)页面下载最新版本的二进制安装包，并将二进制包放到与当前 dolphinscheduler 服务不一样的路径中，以下升级操作都需要在新版本的目录进行。

## 升级步骤

### 停止 dolphinscheduler 所有服务

根据你部署方式停止 dolphinscheduler 的所有服务，如果你是通过 [集群部署](../installation/cluster.md) 来部署你的 dolphinscheduler 的话，可以通过 `sh ./script/stop-all.sh` 停止全部服务。

### 数据库升级

设置相关环境变量（{user}和{password}改成你数据库的用户名和密码），然后运行升级脚本。

下面以 MySQL 为例，别的数据库仅需要修改成对应的配置即可。请先手动下载 [mysql-connector-java 驱动 jar](https://downloads.MySQL.com/archives/c-j/)
jar 包 并添加到 `./tools/libs` 目录下，设置以下环境变量

        ```shell
        export DATABASE=${DATABASE:-mysql}
        export SPRING_PROFILES_ACTIVE=${DATABASE}
        export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
        export SPRING_DATASOURCE_USERNAME={user}
        export SPRING_DATASOURCE_PASSWORD={password}
        ```

执行数据库升级脚本：`sh ./tools/bin/upgrade-schema.sh`

### 资源迁移

3.2.0 版本资源中心重构，原资源中心内的资源将不受管理，您可以指定迁移到的目标租户，然后运行一次性资源迁移脚本，所有资源会迁移到目标租户的 .migrate 目录下。

#### 示例：

指定已存在目标租户 `abc`，其资源根目录为 `/dolphinscheduler/abc/`。

执行脚本：`sh ./tools/bin/migrate-resource.sh abc`。

执行结果：

- 原文件资源 `a/b.sh` 迁移至 `/dolphinscheduler/abc/resources/.migrate/a/b.sh`。
- 原 UDF 资源 `x/y.jar` 迁移至 `/dolphinscheduler/abc/udf/.migrate/x/y.jar`。
- 更新 UDF 函数绑定资源信息。

### 服务升级

#### 修改 `bin/env/install_env.sh` 配置内容

- 伪集群部署请参照[伪集群部署(Pseudo-Cluster)](../installation/pseudo-cluster.md)中的 `修改相关配置`
- 集群部署请参照[集群部署(Cluster)](../installation/cluster.md)中的 `修改相关配置`

然后运行命令 `sh ./bin/start-all.sh` 重启全部服务。

## 注意事项

### worker 分组的区别（以 1.3.1 版本为界）

创建 worker 分组在 1.3.1 版本之前，与 1.3.1 之后到 2.0.0 之间的版本有不同的设计：

- worker 分组在 1.3.1 版本之前是通过 UI 界面创建
- worker 分组在 1.3.1 到 2.0.0 之前的版本是修改 worker 配置指定

#### 面对这种区别我应该怎么升级

1.3.1 之前的版本升级 1.3.2 时如何设置 worker 分组与之前一致

- 查询已备份的数据库，查看 `t_ds_worker_group` 表记录，重点看下 id、name 和 ip_list 三个字段

| id |   name   |                     ip_list |
|:---|:--------:|----------------------------:|
| 1  | service1 |               192.168.xx.10 |
| 2  | service2 | 192.168.xx.11,192.168.xx.12 |

- 修改 `bin/env/install_env.sh` 中的 workers 参数

假设以下为要部署的 worker 主机名和 ip 的对应关系
| 主机名 | ip |
| :--- | :---: |
| ds1 | 192.168.xx.10 |
| ds2 | 192.168.xx.11 |
| ds3 | 192.168.xx.12 |

那么为了保持与之前版本 worker 分组一致，则需要把 workers 参数改为如下

```sh
# worker服务部署在哪台机器上,并指定此worker属于哪一个worker组
workers="ds1:service1,ds2:service2,ds3:service2"
```

#### 1.3.2 及以后的版本对 worker 分组功能进行增强

1.3.1 以及之前的版本 worker 不能同时属于多个 worker 分组，1.3.2 及之后，2.0.0 之前的版本是可以支持的，所以可以使用如下配置对一台 worker 配置多个分组

```sh
workers="ds1:service1,ds1:service2"
```

#### 在 2.0.0 版本之后恢复 UI 创建 worker group

在 2.0.0 以及之后的版本，我们恢复了在 UI 创建 worker group 的功能。
