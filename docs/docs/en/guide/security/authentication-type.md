# Authentication Type

* So far we support two authentication types, Apache DolphinScheduler password and LDAP.

## Change Authentication Type

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

For detailed explanation of specific fields, please see: [Api-server related configuration](../../architecture/configuration.md)

## LDAP Test

We offer you a unit-test class while you can test the integration of DolphinScheduler with LDAP without running the service.

> dolphinscheduler-api/src/test/java/org/apache/dolphinscheduler/api/security/impl/ldap/LdapServiceTest.java

You can follow guide below：
- Change`TestPropertySource`configuration to your LDAP information.
- Change userId && userPwd to your information in the `ldapLogin` method.
- Change the expected email to the return value you expect in the `ldapLogin` method.
- Run`ldapLogin`method and determine whether the LDAP login result is expected.
