# DolphinScheduler Upgrade

## Prepare

### Check Incompatible Change

You should check [incompatible change](./incompatible.md) before you upgrade, because some incompatible change may break your current function.

### Backup Previous Version's Files and Database

To prevent data loss by some miss-operation, it is recommended to back up data before upgrading. The backup way according to your environment.

### Download the Latest Version Installation Package

Download the latest binary distribute package from [download](/en-us/download/download.html) and then put it in the different
directory where current service running. And all below command is running in this directory.

## Upgrade

### Stop All Services of DolphinScheduler

Stop all services of dolphinscheduler according to your deployment method. If you deploy your dolphinscheduler according to [cluster deployment](../installation/cluster.md), you can stop all services by command `sh ./script/stop-all.sh`.

### Upgrade Database

Set the following environment variables ({user} and {password} are changed to your database username and password), and then run the upgrade script.

Using MySQL as an example, change the value if you use other databases. Please manually download the [mysql-connector-java driver jar](https://downloads.MySQL.com/archives/c-j/)
jar package and add it to the `./tools/libs` directory, then export the following environment variables

        ```shell
        export DATABASE=${DATABASE:-mysql}
        export SPRING_PROFILES_ACTIVE=${DATABASE}
        export SPRING_DATASOURCE_URL="jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8&useSSL=false"
        export SPRING_DATASOURCE_USERNAME={user}
        export SPRING_DATASOURCE_PASSWORD={password}
        ```

Execute database upgrade script: `sh ./tools/bin/upgrade-schema.sh`

### Upgrade Service

#### Change Configuration `bin/env/install_config.conf`

- If you deploy with Pseudo-Cluster deployment, change it according to [Pseudo-Cluster](../installation/pseudo-cluster.md) section "Modify Configuration".
- If you deploy with Cluster deployment, change it according to [Cluster](../installation/cluster.md) section "Modify Configuration".

And them run command `sh ./bin/start-all.sh` to start all services.

## Notice

### Differences of worker group (before or after version 1.3.1 of dolphinscheduler)

The architecture of worker group is different between version before version 1.3.1 until version 2.0.0

- Before version 1.3.1(include itself) worker group can be created through UI interface.
- Since version 1.3.1 and before version 2.0.0, worker group can be created by modifying the worker configuration.

#### How Can I Do When I Upgrade from 1.3.1 to version before 2.0.0

* Check the backup database, search records in table `t_ds_worker_group` table and mainly focus on three columns: `id, name and IP`.

| id |   name   |                     ip_list |
|:---|:--------:|----------------------------:|
| 1  | service1 |               192.168.xx.10 |
| 2  | service2 | 192.168.xx.11,192.168.xx.12 |

* Modify worker related configuration in `bin/env/install_config.conf`.

Assume bellow are the machine worker service to be deployed:

| hostname |      ip       |
|:---------|:-------------:|
| ds1      | 192.168.xx.10 |
| ds2      | 192.168.xx.11 |
| ds3      | 192.168.xx.12 |

To keep worker group config consistent with the previous version, we need to modify workers configuration as below:

```shell
#worker service is deployed on which machine, and also specify which worker group this worker belongs to.
workers="ds1:service1,ds2:service2,ds3:service2"
```

#### The Worker Group has Been Enhanced in Version 1.3.2

Workers in 1.3.1 can only belong to one worker group, but after version 1.3.2 and before version 2.0.0 worker support more than one worker group.

```sh
workers="ds1:service1,ds1:service2"
```

#### Recovery UI Create Worker Group after Version 2.0.0

After version 2.0.0, include itself, we are recovery function create worker group from web UI.
