# 数据源配置

## Standalone 切换元数据库

我们这里以 MySQL 为例来说明如何配置外部数据库：

* 首先，参照 [通用配置](general-setting.md) `伪分布式/分布式安装初始化数据库` 创建并初始化数据库
* 在你的命令行或者修改 bin/env/dolphinscheduler_env.sh 设定下列环境变量，将 `{user}` 和 `{password}` 改为你数据库的用户名和密码

```shell
export DATABASE=mysql
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
```

* 将mysql-connector-java驱动加到`./standalone-server/libs/standalone-server/`目录下, 下载方法见 [通用配置](general-setting.md) `伪分布式/分布式安装初始化数据库` 一栏
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
sh tools/bin/upgrade-schema.sh
```

[mysql]: https://downloads.MySQL.com/archives/c-j/
