# Databend

![Databend 数据源](../../../../img/new_ui/dev/datasource/Databend.png)

## 数据源参数

|  **数据源**  |            **描述**             |
|-----------|-------------------------------|
| 数据源       | 选择 DATABEND。                  |
| 数据源名称     | 输入数据源的名称。                     |
| 描述        | 输入数据源的描述。                     |
| IP/主机名    | 输入 DATABEND 服务的 IP 地址。        |
| 端口        | 输入 DATABEND 服务的端口。            |
| 用户名       | 设置 DATABEND 连接的用户名。           |
| 密码        | 设置 DATABEND 连接的密码。            |
| 数据库名称     | 输入 DATABEND 连接的数据库名称。         |
| jdbc 连接参数 | DATABEND 连接的参数设置，以 JSON 格式表示。 |

``jdbc`` 连接参数可参考 [databend-jdbc](https://github.com/databendcloud/databend-jdbc)

## 是否原生支持

是，数据源不需要任务附加操作即可使用。
