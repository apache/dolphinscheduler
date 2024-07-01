# StarRocks数据源

![starrocks](../../../../img/new_ui/dev/datasource/starrocks.png)

- 数据源：选择 STARROCKS
- 数据源名称：输入数据源的名称
- 描述：输入数据源的描述
- IP 主机名：输入连接 STARROCKS 的 IP
- 端口：输入连接 STARROCKS 的端口
- 用户名：设置连接 STARROCKS 的用户名
- 密码：设置连接 STARROCKS 的密码
- 数据库名：输入连接 STARROCKS 的数据库名称
- Jdbc 连接参数：用于 STARROCKS 连接的参数设置，以 JSON 形式填写

## 是否原生支持

否，StarRocks使用Mysql JDBC Driver, 使用前需请参考 [数据源配置](../howto/datasource-setting.md) 中的 "数据源中心" 章节激活Mysql JDBC Driver。
