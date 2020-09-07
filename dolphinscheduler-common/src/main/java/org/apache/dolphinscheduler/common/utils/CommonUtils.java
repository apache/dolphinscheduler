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

package org.apache.dolphinscheduler.common.utils;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * common utils
 */
public class CommonUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    private static final Base64 BASE64 = new Base64();

    private CommonUtils() {
        throw new UnsupportedOperationException("Construct CommonUtils");
    }

    /**
     * @return get the path of system environment variables
     */
    public static String getSystemEnvPath() {
        String envPath = PropertyUtils.getString(Constants.DOLPHINSCHEDULER_ENV_PATH);
        if (StringUtils.isEmpty(envPath)) {
            URL envDefaultPath = CommonUtils.class.getClassLoader().getResource(Constants.ENV_PATH);

            if (envDefaultPath != null) {
                envPath = envDefaultPath.getPath();
                logger.debug("env path :{}", envPath);
            } else {
                envPath = "/etc/profile";
            }
        }

        return envPath;
    }

    /**
     * @return is develop mode
     */
    public static boolean isDevelopMode() {
        return PropertyUtils.getBoolean(Constants.DEVELOPMENT_STATE, true);
    }

    /**
     * if upload resource is HDFS and kerberos startup is true , else false
     *
     * @return true if upload resource is HDFS and kerberos startup
     */
    public static boolean getKerberosStartupState() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState = PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        return resUploadType == ResUploadType.HDFS && kerberosStartupState;
    }

    /**
     * load kerberos configuration
     *
     * @throws Exception errors
     */
    public static void loadKerberosConf() throws Exception {
        if (CommonUtils.getKerberosStartupState()) {
            System.setProperty(Constants.JAVA_SECURITY_KRB5_CONF, PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH));
            Configuration configuration = new Configuration();
            configuration.set(Constants.HADOOP_SECURITY_AUTHENTICATION, Constants.KERBEROS);
            UserGroupInformation.setConfiguration(configuration);
            UserGroupInformation.loginUserFromKeytab(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME),
                    PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH));
        }
    }

    /**
     * encode password
     */
    public static String encodePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return StringUtils.EMPTY;
        }
        //if encryption is not turned on, return directly
        boolean encryptionEnable = PropertyUtils.getBoolean(Constants.DATASOURCE_ENCRYPTION_ENABLE, false);
        if (!encryptionEnable) {
            return password;
        }

        // Using Base64 + salt to process password
        String salt = PropertyUtils.getString(Constants.DATASOURCE_ENCRYPTION_SALT, Constants.DATASOURCE_ENCRYPTION_SALT_DEFAULT);
        String passwordWithSalt = salt + new String(BASE64.encode(password.getBytes(StandardCharsets.UTF_8)));
        return new String(BASE64.encode(passwordWithSalt.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * decode password
     */
    public static String decodePassword(String password) {
        if (StringUtils.isEmpty(password)) {
            return StringUtils.EMPTY;
        }

        //if encryption is not turned on, return directly
        boolean encryptionEnable = PropertyUtils.getBoolean(Constants.DATASOURCE_ENCRYPTION_ENABLE, false);
        if (!encryptionEnable) {
            return password;
        }

        // Using Base64 + salt to process password
        String salt = PropertyUtils.getString(Constants.DATASOURCE_ENCRYPTION_SALT, Constants.DATASOURCE_ENCRYPTION_SALT_DEFAULT);
        String passwordWithSalt = new String(BASE64.decode(password), StandardCharsets.UTF_8);
        if (!passwordWithSalt.startsWith(salt)) {
            logger.warn("There is a password and salt mismatch: {} ", password);
            return password;
        }
        return new String(BASE64.decode(passwordWithSalt.substring(salt.length())), StandardCharsets.UTF_8);
    }

}
