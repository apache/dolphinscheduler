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

根据你部署方式停止 dolphinscheduler 的所有服务。

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

### 血缘升级

执行脚本：`sh ./tools/bin/migrate-lineage.sh`。

执行结果：

- 原血缘数据迁移至新血缘表 `t_ds_workflow_task_lineage`。
- 此脚本仅执行 upsert 操作，不执行删除操作，如果需要删除，您可以手动删除。

### 服务升级

#### 修改配置内容

- 伪集群部署请参照[伪集群部署(Pseudo-Cluster)](../installation/pseudo-cluster.md)中的 `修改相关配置`
- 集群部署请参照[集群部署(Cluster)](../installation/cluster.md)中的 `修改相关配置`

## 注意事项

#### 升级版本限制

- 在 3.3.X 以及之后的版本，我们仅支持从 3.0.0 开始进行升级，低于此版本的请下载历史版本升级至 3.0.0。
- 在 3.3.X 以及之后的版本，二进制包不再默认提供插件依赖，因此第一次使用时，需要自行下载安装。具体请参考请参照[伪集群部署(Pseudo-Cluster)](../installation/pseudo-cluster.md)

#### 升级后的注意事项

在历史版本中可能告警插件会有一些脏数据，升级后请参考一下 SQL 手动清理。

```sql
delete from t_ds_alertgroup where group_name = 'global alert group' and description = 'global alert group';
```

