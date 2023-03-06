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

## 使用 HiveServer2 HA Zookeeper


注意：如果没有开启 kerberos,请保证参数 `hadoop.security.authentication.startup.state` 值为 `false`,
参数 `java.security.krb5.conf.path` 值为空. 开启了 **kerberos**，则需要在 `common.properties` 配置以下参数

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

## 是否原生支持

是，数据源不需要任务附加操作即可使用。
