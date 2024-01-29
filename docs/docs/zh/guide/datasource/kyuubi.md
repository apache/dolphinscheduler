# KYUUBI数据源

## 使用HiveServer2

![kyuubi](../../../../img/new_ui/dev/datasource/kyuubi.png)

- 数据源：选择 KYUUBI
- 数据源名称：输入数据源的名称
- 描述：输入数据源的描述
- IP 主机名：输入连接 KYUUBI 的 IP
- 端口：输入连接 KYUUBI 的端口
- 用户名：设置连接 KYUUBI 的用户名
- 密码：设置连接 KYUUBI 的密码
- 数据库名：输入连接 KYUUBI 的数据库名称
- Jdbc 连接参数：用于 KYUUBI 连接的参数设置，以 JSON 形式填写

```Kerberos 验证
如需Kerberos验证，请直接配置相应参数在jdbc连接参数中

clientKeytab: 用户客户端验证的keytab文件路径

clientPrincipal: 用户客户端验证的Kerberos principal

serverPrincipal: 在服务端通过kyuubi.kinit.principal配置的Kerberos principal.
```

## 是否原生支持

是，数据源不需要任务附加操作即可使用。
