# 通用配置

## 语言

DolphinScheduler 支持两种内置语言，包括 `English` 和 `Chinese` 。您可以点击顶部控制栏名为 `English` 或 `Chinese` 的按钮切换语言。
当您将语言从一种切换为另一种时，您所有 DolphinScheduler 的页面语言页面将发生变化。

## 主题

DolphinScheduler 支持两种类型的内置主题，包括 `Dark` 和 `Light`。当您想改变主题时，只需单击顶部控制栏在 [语言](#语言) 左侧名为 `Dark`(or `Light`)
的按钮即可。

## 时区

DolphinScheduler 支持时区设置。

服务时区

使用脚本 `bin/dolphinshceduler_daemon.sh`启动服务， 服务的默认时区为UTC， 可以在`bin/env/dolphinscheduler_env.sh`中进行修改， 如`export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-Asia/Shanghai}`。<br>
IDEA 启动服务默认时区为本地时区，可以加jvm参数如`-Duser.timezone=UTC`来修改时区。 时区选择详见[List of tz database time zones](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)

用户时区

用户的默认时区基于您运行 DolphinScheduler 服务的时区。如果你想要切换时区，可以点击 [语言](#语言) 按钮右侧的时区按钮，
然后点击 `请选择时区` 进行时区选择。当切换完成后，所有与时间相关的组件都将更改。

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
