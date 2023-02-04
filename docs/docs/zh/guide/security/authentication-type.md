# 认证方式

* 目前我们支持两种认证方式，Apache DolphinScheduler自身账号密码登录和LDAP。

## 修改认证方式

> dolphinscheduler-api/src/main/resources/application.yaml

```yaml
security:
  authentication:
    # Authentication types (supported types: PASSWORD,LDAP)
    type: PASSWORD
    # IF you set type `LDAP`, below config will be effective
    ldap:
      # ldap server config
      urls: ldap://ldap.forumsys.com:389/
      base-dn: dc=example,dc=com
      username: cn=read-only-admin,dc=example,dc=com
      password: password
      user:
        # admin userId when you use LDAP login
        admin: read-only-admin
        identity-attribute: uid
        email-attribute: mail
        # action when ldap user is not exist (supported types: CREATE,DENY)
        not-exist-action: CREATE
```

具体字段解释详见：[Api-server相关配置](../../architecture/configuration.md)

## LDAP测试

我们提供了一个单元测试类，可以在不启动项目的情况下测试DolphinScheduler与LDAP的集成。

> dolphinscheduler-api/src/test/java/org/apache/dolphinscheduler/api/security/impl/ldap/LdapServiceTest.java

使用步骤如下：
- 修改`TestPropertySource`配置参数为你的LDAP信息;
- 修改`ldapLogin`方法中的userId和userPwd为你的账号密码;
- 修改`ldapLogin`方法中的expected email为正常登陆的返回值;
- 执行`ldapLogin`方法，判断LDAP登陆结果是否为预期;
