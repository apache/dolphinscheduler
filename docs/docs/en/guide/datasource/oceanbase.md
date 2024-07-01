# OceanBase

![oceanbase](../../../../img/new_ui/dev/datasource/oceanbase.png)

## Datasource Parameters

|       **Datasource**       |                       **Description**                        |
|----------------------------|--------------------------------------------------------------|
| Datasource                 | Select OCEANBASE.                                            |
| Datasource name            | Enter the name of the DataSource.                            |
| Description                | Enter a description of the DataSource.                       |
| IP/Host name               | Enter the OceanBase service IP.                              |
| Port                       | Enter the OceanBase service port.                            |
| Username                   | Set the username for OceanBase connection.                   |
| Password                   | Set the password for OceanBase connection.                   |
| Database name              | Enter the database name of the OceanBase connection.         |
| Compatible mode            | Set the compatible mode of the OceanBase connection.         |
| Jdbc connection parameters | Parameter settings for OceanBase connection, in JSON format. |

## Native Supported

No, you need to import the OceanBase jdbc driver [oceanbase-client](https://mvnrepository.com/artifact/com.oceanbase/oceanbase-client) first, refer to the section example in [datasource-setting](../howto/datasource-setting.md) `DataSource Center` section.

The compatible mode of the datasource can be 'mysql' or 'oracle', if you only use OceanBase with 'mysql' mode, you can also treat OceanBase as MySQL and manage the datasource referring to [mysql datasource](mysql.md)
