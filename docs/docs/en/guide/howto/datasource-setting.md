# Datasource Setting

## Standalone Switching Metadata Database Configuration

We here use MySQL as an example to illustrate how to configure an external database:

* First of all, follow the instructions in [datasource-setting](datasource-setting.md) `Pseudo-Cluster/Cluster Initialize the Database` section to create and initialize database
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
bash tools/bin/upgrade-schema.sh
```

## DataSource Center

DataSource supports MySQL, PostgreSQL, Hive/Impala, Spark, ClickHouse, Oracle, SQL Server and other DataSources.

- Click bottom `Data Source Center -> Create Data Source` to create a new datasource.
- Click `Test Connection` to test whether the DataSource can connect successfully (datasource can be saved only if it passes the connection test).

### Using datasource incompatible to Apache LICENSE V2 LICENSE

Some of datasource are native supported to DolphinScheduler while others need users download JDBC driver package manually,
because those JDBC driver incompatible to Apache LICENSE V2 LICENSE. For this reason we have to release DolphinScheduler's
distribute package without those packages, even if this will make more complicated for users. Datasource such as MySQL,
Oracle, SQL Server as the examples, but we have the solution to solve this

### Example

For example, if you want to use MySQL datasource, you need to download the correct JDBC driver from [mysql maven repository](https://repo1.maven.org/maven2/mysql/mysql-connector-java),
and move it into directory `api-server/libs` and `worker-server/libs`. After that, you could activate MySQL datasource by
restarting `api-server` and `worker-server`. Mount to container volume in the same path and restart it if you use container
like Docker.

> Note: If you only want to use MySQL in the datasource center, there is no requirement for the version of MySQL JDBC driver.
> But if you want to use MySQL as the metabase of DolphinScheduler, it only supports [8.0.16 and above](https:/ /repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.16/mysql-connector-java-8.0.16.jar) version.

[mysql]: https://downloads.MySQL.com/archives/c-j/
