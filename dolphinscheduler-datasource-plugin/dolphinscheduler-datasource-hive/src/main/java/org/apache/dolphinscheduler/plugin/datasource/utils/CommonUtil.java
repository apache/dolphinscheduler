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

package org.apache.dolphinscheduler.plugin.datasource.utils;

import static org.apache.dolphinscheduler.spi.utils.Constants.JAVA_SECURITY_KRB5_CONF;

import org.apache.dolphinscheduler.spi.datasource.JdbcConnectionParam;
import org.apache.dolphinscheduler.spi.enums.ResUploadType;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
import org.apache.dolphinscheduler.spi.utils.StringUtils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.Objects;

public class CommonUtil {

    private CommonUtil() {
    }

    public static boolean getKerberosStartupState() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        return resUploadType == ResUploadType.HDFS && kerberosStartupState;
    }

    public static synchronized UserGroupInformation createUGI(Configuration configuration, JdbcConnectionParam connectionParam)
            throws IOException {
        if (getKerberosStartupState()) {
            Objects.requireNonNull(connectionParam.getKerberosKeytab());
            if (StringUtils.isNotBlank(connectionParam.getKerberosKrb5Conf())) {
                System.setProperty(JAVA_SECURITY_KRB5_CONF, connectionParam.getKerberosKrb5Conf());
            }
            return loginKerberos(configuration, connectionParam.getKerberosPrincipal(), connectionParam.getKerberosKeytab());
        }
        return UserGroupInformation.createRemoteUser(connectionParam.getUser());
    }

    public static synchronized UserGroupInformation loginKerberos(final Configuration config, final String principal, final String keyTab)
            throws IOException {
        config.set(Constants.HADOOP_SECURITY_AUTHENTICATION, Constants.KERBEROS);
        UserGroupInformation.setConfiguration(config);
        UserGroupInformation.loginUserFromKeytab(principal.trim(), keyTab.trim());
        return UserGroupInformation.getCurrentUser();
    }

}
