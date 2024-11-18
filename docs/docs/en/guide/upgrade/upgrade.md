# DolphinScheduler Upgrade

## Prepare

### Check Incompatible Change

You should check [incompatible change](./incompatible.md) before you upgrade, because some incompatible change may break your current function.

### Backup Previous Version's Files and Database

To prevent data loss by some miss-operation, it is recommended to back up data before upgrading. The backup way according to your environment.

### Download the Latest Version Installation Package

Download the latest binary distribute package from [download](https://dolphinscheduler.apache.org/en-us/download) and then put it in the different
directory where current service running. And all below command is running in this directory.

## Upgrade

### Stop All Services of DolphinScheduler

Stop all services of dolphinscheduler according to your deployment method..

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

### Migrate Resource

After refactoring resource center in version 3.2.0, original resources become unmanaged. You can assign a target tenant and execute one-time migration script. All resources will be migrated to directory `.migrate` of target tenant.

#### Example

Assign an existed target tenant `abc`, the base resource path is `/dolphinscheduler/abc/`.

Execute script: `sh ./tools/bin/migrate-resource.sh abc`.

Execution result:

- The original file resource `a/b.sh` migrates to `/dolphinscheduler/abc/resources/.migrate/a/b.sh`.
- The original UDF resource `x/y.jar` migrates to `/dolphinscheduler/abc/udf/.migrate/x/y.jar`.
- Update UDF function's bound resource info.

### Upgrade Lineage

Execute script: `sh ./tools/bin/migrate-lineage.sh`.

Execution result:

- Migrate lineage data to new table `t_ds_workflow_task_lineage`.
- This script only performs upsert operations, not deletes. You can delete it manually if you need to.

### Upgrade Service

- If you deploy with Pseudo-Cluster deployment, change it according to [Pseudo-Cluster](../installation/pseudo-cluster.md) section "Modify Configuration".
- If you deploy with Cluster deployment, change it according to [Cluster](../installation/cluster.md) section "Modify Configuration".

## Notice

#### Upgrade version restriction

- After version 3.3.X and later, we only support upgrading from 3.0.0. For versions lower than this, please download the historical version and upgrade to 3.0.0.
- After version 3.3.X and later, binary packages no longer provide plugins dependencies by default, so when you use them for the first time, you need to download and install them yourself. For more information, please refer to [Pseudo-Cluster](../installation/pseudo-cluster.md).

#### Precautions after the upgrade

The alert plugin may have some dirty data. After the upgrade, clear it manually by referring to SQL.

```sql
delete from t_ds_alertgroup where group_name = 'global alert group' and description = 'global alert group';
```

