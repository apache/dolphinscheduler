package org.apache.dolphinscheduler.api.security.plugins.ldap;

import org.apache.dolphinscheduler.api.security.AbstractLoginCredentials;

import lombok.Data;

@Data
public class LdapLoginCredentials extends AbstractLoginCredentials {

    public String userId;

    public String password;

}
