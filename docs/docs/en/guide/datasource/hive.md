# HIVE

## Use HiveServer2

![hive](/img/new_ui/dev/datasource/hive.png)

- Datasource: select `HIVE`
- Datasource name: enter the name of the DataSource
- Description: enter a description of the DataSource
- IP/Host Name: enter the HIVE service IP
- Port: enter the HIVE service port
- Username: set the username for HIVE connection
- Password: set the password for HIVE connection
- Database name: enter the database name of the HIVE connection
- Jdbc connection parameters: parameter settings for HIVE connection, in JSON format

> NOTICE: If you wish to execute multiple HIVE SQL in the same session, you could set `support.hive.oneSession = true` in `common.properties`. 
> It is helpful when you try to set env variables before running HIVE SQL. Default value of `support.hive.oneSession` is `false` and multi-SQLs run in different sessions.

## Use HiveServer2 HA ZooKeeper

![hive-server2](/img/new_ui/dev/datasource/hiveserver2.png)

NOTICE: If Kerberos is disabled, ensure the parameter `hadoop.security.authentication.startup.state` is false, and parameter `java.security.krb5.conf.path` value sets null. 
If **Kerberos** is enabled, needs to set the following parameters  in `common.properties`: 

```conf
# whether to startup kerberos
hadoop.security.authentication.startup.state=true

# java.security.krb5.conf path
java.security.krb5.conf.path=/opt/krb5.conf

# login user from keytab username
login.user.keytab.username=hdfs-mycluster@ESZ.COM

# login user from keytab path
login.user.keytab.path=/opt/hdfs.headless.keytab
```

## Native Supported

Yes, could use this datasource by default. 
