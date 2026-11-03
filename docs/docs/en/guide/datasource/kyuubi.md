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

```Kerberos Authentication
just simply configure them in the connection parameters if kerberos authentication is required.

clientKeytab: path of Kerberos keytab file for client authentication

clientPrincipal: Kerberos principal for client authentication

serverPrincipal: Kerberos principal configured by kyuubi.kinit.principal at the server side.
```

## Custom JDBC URL

Select **JDBC URL** in the Kyuubi connection configuration to enter a complete URL instead of host, port, database and JDBC parameter fields. Existing datasources continue to use **Standard configuration** and require no migration.

```text
jdbc:kyuubi://kyuubi-server:10009/default;socketTimeout=600000?kyuubi.engine.share.level=USER;kyuubi.engine.share.level.subdomain=example
```

Client properties follow `;`; session properties use a leading `?` or `#`. The Kyuubi driver parses the URL. Supported catalog, discovery, HTTP and authentication options depend on the installed driver and server versions; this feature does not upgrade either component.

Enter the username and password in their existing fields. Do not include `user` or `password` properties in the URL. In URL mode, the host, port, database and other JDBC parameter fields are not used. Test the connection before saving. Switching back to standard configuration requires valid standard connection fields.

The create, update and connection-test APIs accept the optional string `rawJdbcUrl`. It is persisted in the existing connection-parameters JSON; the runtime `jdbcUrl` is derived from it. A nonblank `rawJdbcUrl` takes precedence over legacy connection fields, and `other` is not appended or passed as driver properties. Missing or blank `rawJdbcUrl` retains the previous behavior. No database schema change is needed.

Deploy the updated plugin to the API server and workers before using custom URLs. Datasources of type **HIVE** are unchanged, even when their endpoint is a Kyuubi server.

## Native Supported

- No, read section example in [pseudo-cluster](../installation/pseudo-cluster.md) `Download Plugins Dependencies` section to activate this datasource.

