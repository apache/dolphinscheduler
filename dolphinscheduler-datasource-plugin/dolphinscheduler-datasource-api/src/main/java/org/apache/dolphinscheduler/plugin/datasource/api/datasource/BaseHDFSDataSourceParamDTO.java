/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.plugin.datasource.api.datasource;

public abstract class BaseHDFSDataSourceParamDTO extends BaseDataSourceParamDTO {

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

    public String getJavaSecurityKrb5Conf() {
        return javaSecurityKrb5Conf;
    }

    public void setJavaSecurityKrb5Conf(String javaSecurityKrb5Conf) {
        this.javaSecurityKrb5Conf = javaSecurityKrb5Conf;
    }
}
