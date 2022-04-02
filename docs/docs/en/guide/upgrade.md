# DolphinScheduler Upgrade Documentation

## Back-Up Previous Version's Files and Database

## Stop All Services of DolphinScheduler

 `sh ./script/stop-all.sh`

## Download the Newest Version Installation Package

- [download](/en-us/download/download.html) the latest version of the installation packages.
- The following upgrade operations need to be performed in the new version's directory.

## Database Upgrade

- Modify the following properties in `conf/datasource.properties`.

- If using MySQL as the database to run DolphinScheduler, please comment out PostgreSQL related configurations, and add MYSQL connector jar into lib dir, here we download `mysql-connector-java-8.0.16.jar`, and then correctly configure database connection information. You can download MYSQL connector jar from [here](https://downloads.MySQL.com/archives/c-j/). Alternatively, if you use PostgreSQL as the database, you just need to comment out Mysql related configurations and correctly configure database connect information.

    ```properties
      # postgre
      #spring.datasource.driver-class-name=org.postgresql.Driver
      #spring.datasource.url=jdbc:postgresql://localhost:5432/dolphinscheduler
      # mysql
      spring.datasource.driver-class-name=com.mysql.jdbc.Driver
      spring.datasource.url=jdbc:mysql://xxx:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&allowMultiQueries=true
      spring.datasource.username=xxx
      spring.datasource.password=xxx
    ```

- Execute database upgrade script:

    `sh ./script/upgrade-dolphinscheduler.sh`

## Backend Service Upgrade

### Modify the Content in `conf/config/install_config.conf` File

- Standalone Deployment please refer to the [Standalone-Deployment](./installation/standalone.md).
- Cluster Deployment please refer to the [Cluster-Deployment](./installation/cluster.md).

#### Masters Need Attentions

Create worker group in 1.3.1 version has a different design: 

- Before version 1.3.1 worker group can be created through UI interface.
- Since version 1.3.1 worker group can be created by modifying the worker configuration. 

#### When Upgrade from Version Before 1.3.1 to 1.3.2, the Below Operations are What We Need to Do to Keep Worker Group Configuration Consist with Previous

1. Go to the backup database, search records in `t_ds_worker_group table`, mainly focus `id, name and IP` three columns.

| id | name | ip_list    |
| :---         |     :---:      |          ---: |
| 1   | service1     | 192.168.xx.10    |
| 2   | service2     | 192.168.xx.11,192.168.xx.12      |

2. Modify the worker configuration in `conf/config/install_config.conf` file.

Assume bellow are the machine worker service to be deployed:
| hostname | ip |
| :---  | :---:  |
| ds1   | 192.168.xx.10     |
| ds2   | 192.168.xx.11     |
| ds3   | 192.168.xx.12     |

To keep worker group config consistent with the previous version, we need to modify workers configuration as below:

```shell
#worker service is deployed on which machine, and also specify which worker group this worker belongs to. 
workers="ds1:service1,ds2:service2,ds3:service2"
```

#### The Worker Group has Been Enhanced in Version 1.3.2

Workers in 1.3.1 can't belong to more than one worker group, but in 1.3.2 it's supported. So in 1.3.1 it's not supported when `workers="ds1:service1,ds1:service2"`, and in 1.3.2 it's supported. 

### Execute Deploy Script

```shell
`sh install.sh`
```


