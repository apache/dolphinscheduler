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
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.DATA_QUALITY_JAR_DIR;
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

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ClassPathResource;

/**
 * common utils
 */
@Slf4j
public class CommonUtils {

    private CommonUtils() {
        throw new UnsupportedOperationException("Construct CommonUtils");
    }

    private static String DEFAULT_DATA_QUALITY_JAR_PATH = null;

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

    public static String getDataQualityJarPath() {
        log.info("Trying to get data quality jar in path");
        String dqJarDir = PropertyUtils.getString(DATA_QUALITY_JAR_DIR);

        if (StringUtils.isNotEmpty(dqJarDir)) {
            log.info(
                    "Configuration data-quality.jar.dir is not empty, will try to get data quality jar from directory {}",
                    dqJarDir);
            getDataQualityJarPathFromPath(dqJarDir).ifPresent(jarName -> DEFAULT_DATA_QUALITY_JAR_PATH = jarName);
        }

        if (StringUtils.isEmpty(DEFAULT_DATA_QUALITY_JAR_PATH)) {
            log.info("data quality jar path is empty, will try to auto discover it from build-in rules.");
            getDefaultDataQualityJarPath();
        }

        if (StringUtils.isEmpty(DEFAULT_DATA_QUALITY_JAR_PATH)) {
            log.error(
                    "Can not find data quality jar in both configuration and auto discover, please check your configuration or report a bug.");
            throw new RuntimeException("data quality jar path is empty");
        }

        return DEFAULT_DATA_QUALITY_JAR_PATH;
    }

    private static String getDefaultDataQualityJarPath() {
        if (StringUtils.isNotEmpty(DEFAULT_DATA_QUALITY_JAR_PATH)) {
            return DEFAULT_DATA_QUALITY_JAR_PATH;
        }
        try {
            // not standalone mode
            String currentAbsolutePath = new ClassPathResource("./").getFile().getAbsolutePath();
            String currentLibPath = currentAbsolutePath + "/../libs";
            getDataQualityJarPathFromPath(currentLibPath).ifPresent(jarName -> DEFAULT_DATA_QUALITY_JAR_PATH = jarName);

            // standalone mode
            if (StringUtils.isEmpty(DEFAULT_DATA_QUALITY_JAR_PATH)) {
                log.info(
                        "Can not get data quality jar from path {}, maybe service running in standalone mode, will try to find another path",
                        currentLibPath);
                currentLibPath = currentAbsolutePath + "/../../worker-server/libs";
                getDataQualityJarPathFromPath(currentLibPath)
                        .ifPresent(jarName -> DEFAULT_DATA_QUALITY_JAR_PATH = jarName);
            }
        } catch (IOException e) {
            throw new RuntimeException("get default data quality jar path error", e);
        }
        log.info("get default data quality jar name: {}", DEFAULT_DATA_QUALITY_JAR_PATH);
        return DEFAULT_DATA_QUALITY_JAR_PATH;
    }

    private static Optional<String> getDataQualityJarPathFromPath(String path) {
        log.info("Try to get data quality jar from path {}", path);
        File[] jars = new File(path).listFiles();
        if (jars == null) {
            log.warn("No any files find given path {}", path);
            return Optional.empty();
        }
        for (File jar : jars) {
            if (jar.getName().startsWith("dolphinscheduler-data-quality")) {
                return Optional.of(jar.getAbsolutePath());
            }
        }
        log.warn("No data quality related jar found from path {}", path);
        return Optional.empty();
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
