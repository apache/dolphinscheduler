/// *
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements. See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
// package org.apache.dolphinscheduler.plugin.datasource.api.utils;
//
// import static org.apache.dolphinscheduler.spi.utils.Constants.DATASOURCE_ENCRYPTION_ENABLE;
//
// import org.apache.dolphinscheduler.spi.utils.Constants;
// import org.apache.dolphinscheduler.spi.utils.PropertyUtils;
//
// import org.apache.hadoop.conf.Configuration;
// import org.apache.hadoop.security.UserGroupInformation;
//
// import org.junit.Assert;
// import org.junit.Test;
// import org.junit.runner.RunWith;
// import org.mockito.junit.MockitoJUnitRunner;
// import org.mockito.stubbing.Answer;
//
// TODO: fix powermock answer in this class
// @RunWith(MockitoJUnitRunner.class)
// public class CommonUtilsTest {
//
// @Test
// public void testGetKerberosStartupState() {
// Mockito.mockStatic(CommonUtils.class);
// Mockito.when(CommonUtils.getKerberosStartupState()).thenAnswer((Answer<Boolean>) invocation -> false);
// boolean kerberosStartupState = CommonUtils.getKerberosStartupState();
// Assert.assertFalse(kerberosStartupState);
//
// Mockito.mockStatic(PropertyUtils.class);
// Mockito.when(PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE)).thenAnswer((Answer<String>)
/// invocation -> "HDFS");
// Mockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, true))
// .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);
// kerberosStartupState = CommonUtils.getKerberosStartupState();
// Assert.assertFalse(kerberosStartupState);
// }
//
// @Test
// public void testLoadKerberosConf() {
// try {
// Mockito.mockStatic(PropertyUtils.class);
// Mockito.when(PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE)).thenAnswer((Answer<String>)
/// invocation -> "HDFS");
// Mockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
// .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);
// Mockito.when(PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH))
// .thenAnswer((Answer<String>) invocation -> "/opt/krb5.conf");
// Mockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME))
// .thenAnswer((Answer<String>) invocation -> "hdfs-mycluster@ESZ.COM");
// Mockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH))
// .thenAnswer((Answer<String>) invocation -> "/opt/hdfs.headless.keytab");
//
// Mockito.mockStatic(UserGroupInformation.class);
// Configuration configuration = Mockito.mock(Configuration.class);
// Mockito.whenNew(Configuration.class).withNoArguments().thenReturn(configuration);
//
// boolean result = CommonUtils.loadKerberosConf(configuration);
// Assert.assertTrue(result);
//
// CommonUtils.loadKerberosConf(null, null, null);
//
// } catch (Exception e) {
// Assert.fail("load Kerberos Conf failed" + e.getMessage());
// }
// }
//
// @Test
// public void encodePassword() {
// Mockito.mockStatic(PropertyUtils.class);
// Mockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
// .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);
//
// Assert.assertEquals("", PasswordUtils.encodePassword(""));
// Assert.assertEquals("bnVsbE1USXpORFUy", PasswordUtils.encodePassword("123456"));
// Assert.assertEquals("bnVsbElWRkJXbGhUVjBBPQ==", PasswordUtils.encodePassword("!QAZXSW@"));
// Assert.assertEquals("bnVsbE5XUm1aMlZ5S0VBPQ==", PasswordUtils.encodePassword("5dfger(@"));
//
// Mockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
// .thenAnswer((Answer<Boolean>) invocation -> Boolean.FALSE);
//
// Assert.assertEquals("", PasswordUtils.encodePassword(""));
// Assert.assertEquals("123456", PasswordUtils.encodePassword("123456"));
// Assert.assertEquals("!QAZXSW@", PasswordUtils.encodePassword("!QAZXSW@"));
// Assert.assertEquals("5dfger(@", PasswordUtils.encodePassword("5dfger(@"));
//
// }
//
// @Test
// public void decodePassword() {
// Mockito.mockStatic(PropertyUtils.class);
// Mockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
// .thenAnswer((Answer<Boolean>) invocation -> Boolean.TRUE);
//
// PropertyUtils.setValue(DATASOURCE_ENCRYPTION_ENABLE, "true");
//
// Mockito.mockStatic(PasswordUtils.class);
// Mockito.when(PasswordUtils.decodePassword("bnVsbE1USXpORFUy")).thenReturn("123456");
// Mockito.when(PasswordUtils.decodePassword("bnVsbElWRkJXbGhUVjBBPQ==")).thenReturn("!QAZXSW@");
// Mockito.when(PasswordUtils.decodePassword("bnVsbE5XUm1aMlZ5S0VBPQ==")).thenReturn("5dfger(@");
//
// Assert.assertEquals(null, PasswordUtils.decodePassword(""));
// Assert.assertEquals("123456", PasswordUtils.decodePassword("bnVsbE1USXpORFUy"));
// Assert.assertEquals("!QAZXSW@", PasswordUtils.decodePassword("bnVsbElWRkJXbGhUVjBBPQ=="));
// Assert.assertEquals("5dfger(@", PasswordUtils.decodePassword("bnVsbE5XUm1aMlZ5S0VBPQ=="));
//
// Mockito.when(PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
// .thenAnswer((Answer<Boolean>) invocation -> Boolean.FALSE);
//
// Mockito.when(PasswordUtils.decodePassword("123456")).thenReturn("123456");
// Mockito.when(PasswordUtils.decodePassword("!QAZXSW@")).thenReturn("!QAZXSW@");
// Mockito.when(PasswordUtils.decodePassword("5dfger(@")).thenReturn("5dfger(@");
//
// Assert.assertEquals(null, PasswordUtils.decodePassword(""));
// Assert.assertEquals("123456", PasswordUtils.decodePassword("123456"));
// Assert.assertEquals("!QAZXSW@", PasswordUtils.decodePassword("!QAZXSW@"));
// Assert.assertEquals("5dfger(@", PasswordUtils.decodePassword("5dfger(@"));
// }
//
// }
