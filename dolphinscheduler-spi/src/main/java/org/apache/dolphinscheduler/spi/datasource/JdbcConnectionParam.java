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

package org.apache.dolphinscheduler.spi.datasource;

import org.apache.dolphinscheduler.spi.enums.DbType;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import java.text.MessageFormat;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * JDBC connection parameters
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JdbcConnectionParam {

    private DbType dbType;

    private String jdbcUrl;

    private String user;

    private String password;

    private Integer version;

    private String driverClassName;

    private String driverLocation;

    private String validationQuery;

    private String kerberosPrincipal;

    private String kerberosKeytab;

    private String kerberosKrb5Conf;

    private Map<String, String> props;

    public String getDatasourceUniqueId() {
        String name = StringUtils.isBlank(getKerberosPrincipal()) ? getUser() : getKerberosPrincipal();
        return MessageFormat.format("{0}@{1}@{2}", dbType.getDescp(), name, jdbcUrl);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDriverLocation() {
        return driverLocation;
    }

    public void setDriverLocation(String driverLocation) {
        this.driverLocation = driverLocation;
    }

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
    }

    public String getKerberosPrincipal() {
        return kerberosPrincipal;
    }

    public void setKerberosPrincipal(String kerberosPrincipal) {
        this.kerberosPrincipal = kerberosPrincipal;
    }

    public String getKerberosKeytab() {
        return kerberosKeytab;
    }

    public void setKerberosKeytab(String kerberosKeytab) {
        this.kerberosKeytab = kerberosKeytab;
    }

    public String getKerberosKrb5Conf() {
        return kerberosKrb5Conf;
    }

    public void setKerberosKrb5Conf(String kerberosKrb5Conf) {
        this.kerberosKrb5Conf = kerberosKrb5Conf;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    @Override
    public String toString() {
        return "JdbcConnectionParam{"
                + "dbType=" + dbType
                + ", jdbcUrl='" + jdbcUrl + '\''
                + ", user='" + user + '\''
                + ", password='" + password + '\''
                + ", version='" + version + '\''
                + ", driverClassName='" + driverClassName + '\''
                + ", driverLocation='" + driverLocation + '\''
                + ", validationQuery='" + validationQuery + '\''
                + ", kerberosPrincipal='" + kerberosPrincipal + '\''
                + ", kerberosKeytab='" + kerberosKeytab + '\''
                + ", kerberosKrb5Conf='" + kerberosKrb5Conf + '\''
                + ", props=" + props
                + '}';
    }

}
