package org.apache.dolphinscheduler.common.datasource;

public class BaseHdfsConnectionParam extends BaseConnectionParam {
    protected String principal;
    protected String javaSecurityKrb5Conf;
    protected String loginUserKeytabUsername;
    protected String loginUserKeytabPath;

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getJavaSecurityKrb5Conf() {
        return javaSecurityKrb5Conf;
    }

    public void setJavaSecurityKrb5Conf(String javaSecurityKrb5Conf) {
        this.javaSecurityKrb5Conf = javaSecurityKrb5Conf;
    }

    public String getLoginUserKeytabUsername() {
        return loginUserKeytabUsername;
    }

    public void setLoginUserKeytabUsername(String loginUserKeytabUsername) {
        this.loginUserKeytabUsername = loginUserKeytabUsername;
    }

    public String getLoginUserKeytabPath() {
        return loginUserKeytabPath;
    }

    public void setLoginUserKeytabPath(String loginUserKeytabPath) {
        this.loginUserKeytabPath = loginUserKeytabPath;
    }
}
