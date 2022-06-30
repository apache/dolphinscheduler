# General Setting

## Language

DolphinScheduler supports two types of built-in language which include `English` and `Chinese`. You could click the button
on the top control bar named `English` and `Chinese` and change it to another one when you want to switch the language.
The entire DolphinScheduler page language will shift when you switch the language selection.

## Theme

DolphinScheduler supports two types of built-in theme which include `Dark` and `Light`. When you want to change the theme
of DolphinScheduler, all you have to do is click the button named `Dark`(or `Light`) on the top control bar and on the left
of to [language](#language) control button.

## Time Zone

DolphinScheduler support time zone setting. 

Server Time Zone

The default time zone is UTC when using `bin/dolphinshceduler_daemon.sh` to start the server, you could update `SPRING_JACKSON_TIME_ZONE` in `bin/env/dolphinscheduler_env.sh`, such as `export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-Asia/Shanghai}`.<br>
If you start server in IDEA, the default time zone is your local time zone, you could add the JVM parameter to update server time zone, such as `-Duser.timezone=UTC`. Time zone list refer to [List of tz database time zones](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)

User Time zone

The user's default time zone is based on the time zone which you run the DolphinScheduler service.You could
click the button on the right of the [language](#language) button and then click `Choose timeZone` to choose the time zone
you want to switch. All time related components will adjust their time zone according to the time zone setting you select.


## Standalone Switching Metadata Database Configuration

We here use MySQL as an example to illustrate how to configure an external database:

* First of all, follow the instructions in [general-setting](general-setting.md) `Pseudo-Cluster/Cluster Initialize the Database` section to create and initialize database
* Set the following environment variables in your terminal or modify the `bin/env/dolphinscheduler_env.sh` with your database username and password for `{user}` and `{password}`:

```shell
export DATABASE=mysql
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
```

* Add mysql-connector-java driver to `./standalone-server/libs/standalone-server/`, see [general-setting](general-setting.md) `Pseudo-Cluster/Cluster Initialize the Database` section about where to download
* Start standalone-server, now you are using mysql as database and it will not clear up your data when you stop or restart standalone-server.

## Pseudo-Cluster/Cluster Initialize the Database

DolphinScheduler metadata is stored in the relational database. Currently, supports PostgreSQL and MySQL. If you use MySQL, you need to manually download [mysql-connector-java driver][mysql] (8.0.16) and move it to the libs directory of DolphinScheduler
which is `api-server/libs/` and `alert-server/libs` and `master-server/libs` and `worker-server/libs`. Let's take MySQL as an example for how to initialize the database:

For mysql 5.6 / 5.7

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# Replace {user} and {password} with your username and password
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';

mysql> flush privileges;
```

For mysql 8:

```shell
mysql -uroot -p

mysql> CREATE DATABASE dolphinscheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;

# Replace {user} and {password} with your username and password
mysql> CREATE USER '{user}'@'%' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'%';
mysql> CREATE USER '{user}'@'localhost' IDENTIFIED BY '{password}';
mysql> GRANT ALL PRIVILEGES ON dolphinscheduler.* TO '{user}'@'localhost';
mysql> FLUSH PRIVILEGES;
``` 

Then, modify `./bin/env/dolphinscheduler_env.sh` to use mysql, change {user} and {password} to what you set in the previous step.

```shell
export DATABASE=${DATABASE:-mysql}
export SPRING_PROFILES_ACTIVE=${DATABASE}
export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
export SPRING_DATASOURCE_USERNAME={user}
export SPRING_DATASOURCE_PASSWORD={password}
```

After the above steps done you would create a new database for DolphinScheduler, then run the Shell script to init database:

```shell
sh tools/bin/upgrade-schema.sh
```

[mysql]: https://downloads.MySQL.com/archives/c-j/
