# KYUUBI

## Use HiveServer2

![kyuubi](../../../../img/new_ui/dev/datasource/kyuubi.png)

## Datasource Parameters

|       **Datasource**       |                      **Description**                      |
|----------------------------|-----------------------------------------------------------|
| Datasource                 | Select KYUUBI.                                            |
| Datasource name            | Enter the name of the DataSource.                         |
| Description                | Enter a description of the DataSource.                    |
| IP/Host Name               | Enter the KYUUBI service IP.                              |
| Port                       | Enter the KYUUBI service port.                            |
| Username                   | Set the username for KYUUBI connection.                   |
| Password                   | Set the password for KYUUBI connection.                   |
| Database name              | Enter the database name of the KYUUBI connection.         |
| Jdbc connection parameters | Parameter settings for KYUUBI connection, in JSON format. |

## Use HiveServer2 HA ZooKeeper

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
