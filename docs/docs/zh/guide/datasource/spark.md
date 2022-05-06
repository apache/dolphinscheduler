# Spark数据源

![sparksql](/img/new_ui/dev/datasource/sparksql.png)

- 数据源：选择 Spark
- 数据源名称：输入数据源的名称
- 描述：输入数据源的描述
- IP/主机名：输入连接Spark的IP
- 端口：输入连接Spark的端口
- 用户名：设置连接Spark的用户名
- 密码：设置连接Spark的密码
- 数据库名：输入连接Spark的数据库名称
- Jdbc连接参数：用于Spark连接的参数设置，以JSON形式填写

注意：如果开启了**kerberos**，则需要填写 **Principal**

<p align="center">
    <img src="/img/sparksql_kerberos.png" width="80%" />
  </p>

## 是否原生支持

是，数据源不需要任务附加操作即可使用。
