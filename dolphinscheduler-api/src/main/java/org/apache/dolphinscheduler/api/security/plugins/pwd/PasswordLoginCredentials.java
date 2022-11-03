package org.apache.dolphinscheduler.api.security.plugins.pwd;

import org.apache.dolphinscheduler.api.security.AbstractLoginCredentials;

import lombok.Data;

@Data
public class PasswordLoginCredentials extends AbstractLoginCredentials {

    public String userId;

    public String password;

}
