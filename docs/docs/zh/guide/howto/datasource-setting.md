# 数据源配置

## Standalone 切换元数据库

我们这里以 MySQL 为例来说明如何配置外部数据库：

* 首先，参照 [数据源配置](datasource-setting.md) `伪分布式/分布式安装初始化数据库` 创建并初始化数据库
* 在你的命令行或者修改 bin/env/dolphinscheduler_env.sh 设定下列环境变量，将 `{user}` 和 `{password}` 改为你数据库的用户名和密码

```shell
export DATABASE=mysql
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
```

* 将mysql-connector-java驱动加到`./standalone-server/libs/standalone-server/`目录下, 下载方法见 [数据源配置](datasource-setting.md) `伪分布式/分布式安装初始化数据库` 一栏
* 启动standalone-server，此时你已经连接上mysql，重启或者停止standalone-server并不会清空您数据库里的数据

## 伪分布式/分布式安装初始化数据库

DolphinScheduler 元数据存储在关系型数据库中，目前支持 PostgreSQL 和 MySQL，如果使用 MySQL 则需要手动下载 [mysql-connector-java 驱动][mysql] (8.0.16) 并移动到 DolphinScheduler 的每个模块的 libs 目录下
其中包括 `api-server/libs/` 和 `alert-server/libs` 和 `master-server/libs` 和 `worker-server/libs`。下面以 MySQL 为例，说明如何初始化数据库

对于mysql 5.6 / 5.7：

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# 修改 {user} 和 {password} 为你希望的用户名和密码
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';

mysql> flush privileges;
```

对于mysql 8：

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# 修改 {user} 和 {password} 为你希望的用户名和密码
mysql> CREATE USER '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%';
mysql> CREATE USER '{user}'@'localhost' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost';
mysql> FLUSH PRIVILEGES;
```

然后修改`./bin/env/dolphinscheduler_env.sh`，将username和password改成你在上一步中设置的用户名{user}和密码{password}

```shell
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
```  

完成上述步骤后，您已经为 DolphinScheduler 创建一个新数据库，现在你可以通过快速的 Shell 脚本来初始化数据库

```shell
bash tools/bin/upgrade-schema.sh
```

## 数据源中心

数据源中心支持MySQL、POSTGRESQL、HIVE/IMPALA、SPARK、CLICKHOUSE、ORACLE、SQLSERVER等数据源。

- 点击"数据源中心->创建数据源"，根据需求创建不同类型的数据源
- 点击"测试连接"，测试数据源是否可以连接成功（只有当数据源通过连接性测试后才能保存数据源）。

### 使用不兼容 Apache LICENSE V2 许可的数据库

数据源中心里，DolphinScheduler 对部分数据源有原生的支持，但是部分数据源需要用户下载对应的 JDBC 驱动包并放置到正确的位置才能正常使用。
这对用户会增加用户的使用成本，但是我们不得不这么做，因为这部分数据源的 JDBC 驱动和 Apache LICENSE V2 不兼容，所以我们无法在
DolphinScheduler 分发的二进制包中包含他们。这部分数据源主要包括 MySQL，Oracle，SQL Server 等，幸运的是我们为这部分数据源的支持给出了解决方案。

#### 样例

我们以 MySQL 为例，如果你想要使用 MySQL 数据源，你需要先在 [mysql maven 仓库](https://repo1.maven.org/maven2/mysql/mysql-connector-java)
中下载对应版本的 JDBC 驱动，将其移入 `api-server/libs` 以及 `worker-server/libs` 文件夹中，最后重启 `api-server` 和 `worker-server`
服务，即可使用 MySQL 数据源。如果你使用容器启动 DolphinScheduler，同样也是将 JDBC 驱动挂载放到以上两个服务的对应路径下后，重启驱动即可。

> 注意：如果你只是想要在数据源中心使用 MySQL，则对 MySQL JDBC 驱动的版本没有要求，如果你想要将 MySQL 作为 DolphinScheduler 的元数据库，
> 则仅支持 [8.0.16 及以上](https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar)的版本。

[mysql]: https://downloads.MySQL.com/archives/c-j/
