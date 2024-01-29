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

package org.apache.dolphinscheduler.plugin.datasource.hive.security;

import static org.apache.dolphinscheduler.common.constants.Constants.JAVA_SECURITY_KRB5_CONF;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.ResUploadType;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UserGroupInformationFactory {

    private static final Map<String, Integer> currentLoginTimesMap = new HashMap<>();

    private static final Map<String, UserGroupInformation> userGroupInformationMap = new HashMap<>();

    private static final ScheduledExecutorService kerberosRenewalService =
            ThreadUtils.newSingleDaemonScheduledExecutorService("Hive-Kerberos-Renewal-Thread-");

    static {
        kerberosRenewalService.scheduleWithFixedDelay(() -> {
            if (userGroupInformationMap.isEmpty()) {
                return;
            }
            userGroupInformationMap.forEach((key, ugi) -> {
                try {
                    if (ugi.isFromKeytab()) {
                        ugi.checkTGTAndReloginFromKeytab();
                    }
                    log.info("Relogin from keytab success, user: {}", key);
                } catch (Exception e) {
                    log.error("Relogin from keytab failed, user: {}", key, e);
                }
            });
        }, 0, 5, TimeUnit.MINUTES);
    }

    public synchronized static UserGroupInformation login(String userName) {
        UserGroupInformation userGroupInformation = userGroupInformationMap.get(userName);
        if (userGroupInformation == null) {
            if (!openKerberos()) {
                userGroupInformation = createRemoteUser(userName);
            } else {
                userGroupInformation = createKerberosUser();
            }
            userGroupInformationMap.put(userName, userGroupInformation);
        }
        currentLoginTimesMap.compute(userName, (k, v) -> v == null ? 1 : v + 1);
        return userGroupInformation;
    }

    public synchronized static void logout(String userName) {
        Integer currentLoginTimes = currentLoginTimesMap.get(userName);
        if (currentLoginTimes == null) {
            return;
        }
        if (currentLoginTimes <= 1) {
            currentLoginTimesMap.remove(userName);
            userGroupInformationMap.remove(userName);
        } else {
            currentLoginTimesMap.put(userName, currentLoginTimes - 1);
        }
    }

    private static UserGroupInformation createRemoteUser(String userName) {
        return UserGroupInformation.createRemoteUser(userName);
    }

    private static UserGroupInformation createKerberosUser() {
        String krb5File = PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH);
        String keytab = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH);
        String principal = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME);
        if (StringUtils.isNotBlank(krb5File)) {
            System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5File);
        }

        Configuration hadoopConf = new Configuration();
        hadoopConf.setBoolean("ipc.client.fallback-to-simple-auth-allowed", true);
        hadoopConf.set(Constants.HADOOP_SECURITY_AUTHENTICATION, Constants.KERBEROS);

        try {
            UserGroupInformation.setConfiguration(hadoopConf);
            UserGroupInformation userGroupInformation =
                    UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal.trim(), keytab.trim());
            UserGroupInformation.setLoginUser(userGroupInformation);
            return userGroupInformation;
        } catch (IOException e) {
            throw new RuntimeException("createUserGroupInformation fail. ", e);
        }
    }

    public static boolean openKerberos() {
        String resUploadStartupType = PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE);
        ResUploadType resUploadType = ResUploadType.valueOf(resUploadStartupType);
        Boolean kerberosStartupState =
                PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false);
        return resUploadType == ResUploadType.HDFS && kerberosStartupState;
    }

}
