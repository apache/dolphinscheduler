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

import static org.apache.dolphinscheduler.common.constants.DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor(
    value = {"org.apache.dolphinscheduler.spi.utils.PropertyUtils", "org.apache.hadoop.security.UserGroupInformation"}
)
@PrepareForTest(value = {PropertyUtils.class, UserGroupInformation.class, CommonUtils.class, PasswordUtils.class})
@PowerMockIgnore({"jdk.xml.*", "org.apache.hadoop.security.*"})
public class CommonUtilsTest {

    @Test
    public void testGetKerberosStartupState() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.getKerberosStartupState()).thenAnswer((Answer<Boolean>) invocation -> false);
        boolean kerberosStartupState = CommonUtils.getKerberosStartupState();
        Assert.assertFalse(kerberosStartupState);

        PowerMockito.mockStatic(PropertyUtils.class);
        PowerMockito.when(PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE)).thenAnswer((Answer<String>) invocation -> "HDFS");
        PowerMockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, true))
            .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);
        kerberosStartupState = CommonUtils.getKerberosStartupState();
        Assert.assertFalse(kerberosStartupState);
    }

    @Test
    public void testLoadKerberosConf() {
        try {
            PowerMockito.mockStatic(PropertyUtils.class);
            PowerMockito.when(PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE)).thenAnswer((Answer<String>) invocation -> "HDFS");
            PowerMockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);
            PowerMockito.when(PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH))
                .thenAnswer((Answer<String>) invocation -> "/opt/krb5.conf");
            PowerMockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME))
                .thenAnswer((Answer<String>) invocation -> "hdfs-mycluster@ESZ.COM");
            PowerMockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH))
                .thenAnswer((Answer<String>) invocation -> "/opt/hdfs.headless.keytab");

            PowerMockito.mockStatic(UserGroupInformation.class);
            Configuration configuration = PowerMockito.mock(Configuration.class);
            PowerMockito.whenNew(Configuration.class).withNoArguments().thenReturn(configuration);

            boolean result = CommonUtils.loadKerberosConf(configuration);
            Assert.assertTrue(result);

            CommonUtils.loadKerberosConf(null, null, null);

        } catch (Exception e) {
            Assert.fail("load Kerberos Conf failed" + e.getMessage());
        }
    }

    @Test
    public void encodePassword() {
        PowerMockito.mockStatic(PropertyUtils.class);
        PowerMockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
            .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);

        Assert.assertEquals("", PasswordUtils.encodePassword(""));
        Assert.assertEquals("bnVsbE1USXpORFUy", PasswordUtils.encodePassword("123456"));
        Assert.assertEquals("bnVsbElWRkJXbGhUVjBBPQ==", PasswordUtils.encodePassword("!QAZXSW@"));
        Assert.assertEquals("bnVsbE5XUm1aMlZ5S0VBPQ==", PasswordUtils.encodePassword("5dfger(@"));

        PowerMockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
            .thenAnswer((Answer<Boolean>) invocation -> Boolean.FALSE);

        Assert.assertEquals("", PasswordUtils.encodePassword(""));
        Assert.assertEquals("123456", PasswordUtils.encodePassword("123456"));
        Assert.assertEquals("!QAZXSW@", PasswordUtils.encodePassword("!QAZXSW@"));
        Assert.assertEquals("5dfger(@", PasswordUtils.encodePassword("5dfger(@"));

    }

    @Test
    public void decodePassword() {
        PowerMockito.mockStatic(PropertyUtils.class);
        PowerMockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
            .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);

        PropertyUtils.setValue(DATASOURCE_ENCRYPTION_ENABLE, "true");

        PowerMockito.mockStatic(PasswordUtils.class);
        PowerMockito.when(PasswordUtils.decodePassword("bnVsbE1USXpORFUy")).thenReturn("123456");
        PowerMockito.when(PasswordUtils.decodePassword("bnVsbElWRkJXbGhUVjBBPQ==")).thenReturn("!QAZXSW@");
        PowerMockito.when(PasswordUtils.decodePassword("bnVsbE5XUm1aMlZ5S0VBPQ==")).thenReturn("5dfger(@");

        Assert.assertEquals(null, PasswordUtils.decodePassword(""));
        Assert.assertEquals("123456", PasswordUtils.decodePassword("bnVsbE1USXpORFUy"));
        Assert.assertEquals("!QAZXSW@", PasswordUtils.decodePassword("bnVsbElWRkJXbGhUVjBBPQ=="));
        Assert.assertEquals("5dfger(@", PasswordUtils.decodePassword("bnVsbE5XUm1aMlZ5S0VBPQ=="));

        PowerMockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
            .thenAnswer((Answer<Boolean>) invocation -> Boolean.FALSE);

        PowerMockito.when(PasswordUtils.decodePassword("123456")).thenReturn("123456");
        PowerMockito.when(PasswordUtils.decodePassword("!QAZXSW@")).thenReturn("!QAZXSW@");
        PowerMockito.when(PasswordUtils.decodePassword("5dfger(@")).thenReturn("5dfger(@");

        Assert.assertEquals(null, PasswordUtils.decodePassword(""));
        Assert.assertEquals("123456", PasswordUtils.decodePassword("123456"));
        Assert.assertEquals("!QAZXSW@", PasswordUtils.decodePassword("!QAZXSW@"));
        Assert.assertEquals("5dfger(@", PasswordUtils.decodePassword("5dfger(@"));
    }

}
