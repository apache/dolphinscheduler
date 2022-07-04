# Datasource Setting

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
