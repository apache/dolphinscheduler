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

package org.apache.dolphinscheduler.plugin.datasource.api.utils;

import static org.apache.dolphinscheduler.common.constants.Constants.RESOURCE_STORAGE_TYPE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DATA_QUALITY_JAR_NAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.HADOOP_SECURITY_AUTHENTICATION;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JAVA_SECURITY_KRB5_CONF;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.JAVA_SECURITY_KRB5_CONF_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.KERBEROS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOGIN_USER_KEY_TAB_PATH;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.LOGIN_USER_KEY_TAB_USERNAME;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.RESOURCE_UPLOAD_PATH;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;

/**
 * common utils
 */
public class CommonUtils {

    private CommonUtils() {
        throw new UnsupportedOperationException("Construct CommonUtils");
    }

    private static final boolean IS_DEVELOP_MODE = PropertyUtils.getBoolean(Constants.DEVELOPMENT_STATE, true);

    /**
     * @return is develop mode
     */
    public static boolean isDevelopMode() {
        return IS_DEVELOP_MODE;
    }

    /**
     * if upload resource is HDFS and kerberos startup is true , else false
     *
     * @return true if upload resource is HDFS and kerberos startup
     */
    public static boolean getKerberosStartupState() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        return resUploadType == ResUploadType.HDFS && kerberosStartupState;
    }

    /**
     * load kerberos configuration
     *
     * @param configuration
     * @return load kerberos config return true
     * @throws IOException errors
     */
    public static boolean loadKerberosConf(Configuration configuration) throws IOException {
        return loadKerberosConf(PropertyUtils.getString(JAVA_SECURITY_KRB5_CONF_PATH),
                PropertyUtils.getString(LOGIN_USER_KEY_TAB_USERNAME),
                PropertyUtils.getString(LOGIN_USER_KEY_TAB_PATH), configuration);
    }

    /**
     * load kerberos configuration
     *
     * @param javaSecurityKrb5Conf    javaSecurityKrb5Conf
     * @param loginUserKeytabUsername loginUserKeytabUsername
     * @param loginUserKeytabPath     loginUserKeytabPath
     * @throws IOException errors
     */
    public static synchronized void loadKerberosConf(String javaSecurityKrb5Conf,
                                                     String loginUserKeytabUsername,
                                                     String loginUserKeytabPath) throws IOException {
        Configuration configuration = new Configuration();
        configuration.setClassLoader(configuration.getClass().getClassLoader());
        loadKerberosConf(javaSecurityKrb5Conf, loginUserKeytabUsername, loginUserKeytabPath, configuration);
    }

    /**
     * load kerberos configuration
     *
     * @param javaSecurityKrb5Conf javaSecurityKrb5Conf
     * @param loginUserKeytabUsername loginUserKeytabUsername
     * @param loginUserKeytabPath loginUserKeytabPath
     * @param configuration configuration
     * @return load kerberos config return true
     * @throws IOException errors
     */
    public static boolean loadKerberosConf(String javaSecurityKrb5Conf, String loginUserKeytabUsername,
                                           String loginUserKeytabPath, Configuration configuration) throws IOException {
        if (CommonUtils.getKerberosStartupState()) {
            System.setProperty(JAVA_SECURITY_KRB5_CONF, StringUtils.defaultIfBlank(javaSecurityKrb5Conf,
                    PropertyUtils.getString(JAVA_SECURITY_KRB5_CONF_PATH)));
            configuration.set(HADOOP_SECURITY_AUTHENTICATION, KERBEROS);
            UserGroupInformation.setConfiguration(configuration);
            UserGroupInformation.loginUserFromKeytab(
                    StringUtils.defaultIfBlank(loginUserKeytabUsername,
                            PropertyUtils.getString(LOGIN_USER_KEY_TAB_USERNAME)),
                    StringUtils.defaultIfBlank(loginUserKeytabPath, PropertyUtils.getString(LOGIN_USER_KEY_TAB_PATH)));
            return true;
        }
        return false;
    }

    public static String getDataQualityJarName() {
        String dqsJarName = PropertyUtils.getString(DATA_QUALITY_JAR_NAME);

        if (StringUtils.isEmpty(dqsJarName)) {
            return "dolphinscheduler-data-quality.jar";
        }

        return dqsJarName;
    }

    /**
     * hdfs udf dir
     *
     * @param tenantCode tenant code
     * @return get udf dir on hdfs
     */
    public static String getHdfsUdfDir(String tenantCode) {
        return String.format("%s/udfs", getHdfsTenantDir(tenantCode));
    }

    /**
     * @param tenantCode tenant code
     * @return file directory of tenants on hdfs
     */
    public static String getHdfsTenantDir(String tenantCode) {
        return String.format("%s/%s", getHdfsDataBasePath(), tenantCode);
    }

    /**
     * get data hdfs path
     *
     * @return data hdfs path
     */
    public static String getHdfsDataBasePath() {
        String resourceUploadPath = PropertyUtils.getString(RESOURCE_UPLOAD_PATH, "/dolphinscheduler");
        if ("/".equals(resourceUploadPath)) {
            // if basepath is configured to /, the generated url may be //default/resources (with extra leading /)
            return "";
        } else {
            return resourceUploadPath;
        }
    }
}
