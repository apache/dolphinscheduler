# DORIS

![doris](../../../../img/new_ui/dev/datasource/doris.png)

## Datasource Parameters

|       **Datasource**       |                                    **Description**                                    |
|----------------------------|---------------------------------------------------------------------------------------|
| Datasource                 | Select DORIS.                                                                         |
| Datasource name            | Enter the name of the DataSource.                                                     |
| Description                | Enter a description of the DataSource.                                                |
| IP/Host Name               | Enter the DORIS service IP.(If there are multiple IPs, please separate them with `,`) |
| Port                       | Enter the DORIS service port.                                                         |
| Username                   | Set the username for DORIS connection.                                                |
| Password                   | Set the password for DORIS connection.                                                |
| Database name              | Enter the database name of the DORIS connection.                                      |
| Jdbc connection parameters | Parameter settings for DORIS connection, in JSON format.                              |

## Native Supported

- No, read section example in [pseudo-cluster](../installation/pseudo-cluster.md) `Download Plugins Dependencies` section to activate this datasource.
- Driver download link [mysql-connector-j-8.0.33](https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar)

