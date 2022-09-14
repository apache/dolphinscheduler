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

package org.apache.dolphinscheduler.plugin.task.flink.executor;

import org.apache.dolphinscheduler.plugin.task.flink.entity.ParamsInfo;
import org.apache.dolphinscheduler.plugin.task.flink.factory.YarnClusterDescriptorFactory;
import org.apache.dolphinscheduler.spi.utils.Constants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.util.StringUtils;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.conf.YarnConfiguration;
import org.apache.kerby.kerberos.kerb.keytab.Keytab;
import org.apache.kerby.kerberos.kerb.type.base.PrincipalName;

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * executor function in kerberos env
 *
 */
public class KerberosSecurityContext {

    private static final Logger logger = LoggerFactory.getLogger(ClusterClient.class);

    private static Cache<String, UserGroupInformation> ugiCache =
            CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();

    public static <T> T runSecured(ParamsInfo jobParamsInfo,
                                   final Supplier<T> supplier) throws IOException, InterruptedException {
        logger.info("KerberosSecurityContext jobParamsInfo:{}", jobParamsInfo.toString());

        String krb5Path = PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH);
        String principal = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME);
        String keytabPath = PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH);
        String hadoopConfDir = jobParamsInfo.getHadoopConfDir();

        if (StringUtils.isNullOrWhitespaceOnly(principal)) {
            principal = extractPrincipalFromKeytab(keytabPath);
        }

        String cacheKey = hadoopConfDir + "_" + principal;
        UserGroupInformation cachedUgi = ugiCache.getIfPresent(cacheKey);
        if (Objects.nonNull(cachedUgi)) {
            return cachedUgi.doAs((PrivilegedExceptionAction<T>) supplier::get);
        }

        if (!StringUtils.isNullOrWhitespaceOnly(krb5Path)) {
            System.setProperty("java.security.krb5.conf", krb5Path);
        }

        YarnConfiguration yarnConf =
                YarnClusterDescriptorFactory.INSTANCE.parseYarnConfFromConfDir(
                        jobParamsInfo.getHadoopConfDir());

        // print auth_to_local
        String auth_to_local = yarnConf.get("hadoop.security.auth_to_local");
        logger.debug("auth_to_local is : {}", auth_to_local);
        // security context init
        UserGroupInformation.setConfiguration(yarnConf);
        UserGroupInformation ugi =
                UserGroupInformation.loginUserFromKeytabAndReturnUGI(principal, keytabPath);

        logger.info(
                "userGroupInformation current user = {} ugi user  = {} ",
                UserGroupInformation.getCurrentUser(),
                ugi.getUserName());

        if (jobParamsInfo.isCacheUgi()) {
            // Cache UGI requires a thread to correspond to a principal
            ugiCache.put(cacheKey, ugi);
        }
        return ugi.doAs((PrivilegedExceptionAction<T>) supplier::get);
    }

    private static String extractPrincipalFromKeytab(String keytabPath) throws IOException {
        Keytab keytab = Keytab.loadKeytab(new File(keytabPath));
        List<PrincipalName> principals = keytab.getPrincipals();
        principals.forEach(principalName -> logger.info("principalName:{}", principalName));
        return principals.get(0).getName();
    }
}
