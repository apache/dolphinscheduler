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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * configuration test
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(value = { PropertyUtils.class, UserGroupInformation.class})
public class CommonUtilsTest {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtilsTest.class);
    @Test
    public void getSystemEnvPath() {
        logger.info(CommonUtils.getSystemEnvPath());
        Assert.assertTrue(true);
    }

    @Test
    public void isDevelopMode() {
        logger.info("develop mode: {}",CommonUtils.isDevelopMode());
        Assert.assertTrue(true);
    }

    @Test
    public void getKerberosStartupState() {
        boolean kerberosStartupState = CommonUtils.getKerberosStartupState();
        logger.info("kerberos startup state: {}",kerberosStartupState);
        Assert.assertFalse(kerberosStartupState);
        PowerMockito.mockStatic(PropertyUtils.class);
        PowerMockito.when(PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE)).thenReturn("HDFS");
        PowerMockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false)).thenReturn(Boolean.TRUE);
        kerberosStartupState = CommonUtils.getKerberosStartupState();
        logger.info("kerberos startup state: {}",kerberosStartupState);
        Assert.assertTrue(kerberosStartupState);

    }

    @Test
    public void loadKerberosConf() {
        try {
            PowerMockito.mockStatic(PropertyUtils.class);
            PowerMockito.when(PropertyUtils.getUpperCaseString(Constants.RESOURCE_STORAGE_TYPE)).thenReturn("HDFS");
            PowerMockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false)).thenReturn(Boolean.TRUE);
            PowerMockito.when(PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH)).thenReturn("/opt/krb5.conf");
            PowerMockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME)).thenReturn("hdfs-mycluster@ESZ.COM");
            PowerMockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH)).thenReturn("/opt/hdfs.headless.keytab");

            PowerMockito.mockStatic(UserGroupInformation.class);
            boolean result = CommonUtils.loadKerberosConf(new Configuration());
            Assert.assertTrue(result);

            CommonUtils.loadKerberosConf(null, null, null);

        } catch (Exception e) {
            Assert.fail("load Kerberos Conf failed");
        }
    }

    @Test
    public void getHdfsDataBasePath() {
        logger.info(HadoopUtils.getHdfsDataBasePath());
        Assert.assertTrue(true);
    }

    @Test
    public void getDownloadFilename() {
        logger.info(FileUtils.getDownloadFilename("a.txt"));
        Assert.assertTrue(true);
    }

    @Test
    public void getUploadFilename() {
        logger.info(FileUtils.getUploadFilename("1234", "a.txt"));
        Assert.assertTrue(true);
    }

    @Test
    public void getHdfsDir() {
        logger.info(HadoopUtils.getHdfsResDir("1234"));
        Assert.assertTrue(true);
    }

    @Test
    public void test() {
        InetAddress ip;
        try {
            ip = InetAddress.getLocalHost();
            logger.info(ip.getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(true);
    }

    @Test
    public void encodePassword() {

        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE,"true");

        Assert.assertEquals("",CommonUtils.encodePassword(""));
        Assert.assertEquals("IUAjJCVeJipNVEl6TkRVMg==",CommonUtils.encodePassword("123456"));
        Assert.assertEquals("IUAjJCVeJipJVkZCV2xoVFYwQT0=",CommonUtils.encodePassword("!QAZXSW@"));
        Assert.assertEquals("IUAjJCVeJipOV1JtWjJWeUtFQT0=",CommonUtils.encodePassword("5dfger(@"));

        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE,"false");

        Assert.assertEquals("",CommonUtils.encodePassword(""));
        Assert.assertEquals("123456",CommonUtils.encodePassword("123456"));
        Assert.assertEquals("!QAZXSW@",CommonUtils.encodePassword("!QAZXSW@"));
        Assert.assertEquals("5dfger(@",CommonUtils.encodePassword("5dfger(@"));

    }

    @Test
    public void decodePassword() {

        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "true");

        Assert.assertEquals("", CommonUtils.decodePassword(""));
        Assert.assertEquals("123456", CommonUtils.decodePassword("IUAjJCVeJipNVEl6TkRVMg=="));
        Assert.assertEquals("!QAZXSW@", CommonUtils.decodePassword("IUAjJCVeJipJVkZCV2xoVFYwQT0="));
        Assert.assertEquals("5dfger(@", CommonUtils.decodePassword("IUAjJCVeJipOV1JtWjJWeUtFQT0="));

        PropertyUtils.setValue(Constants.DATASOURCE_ENCRYPTION_ENABLE, "false");

        Assert.assertEquals("", CommonUtils.decodePassword(""));
        Assert.assertEquals("123456", CommonUtils.decodePassword("123456"));
        Assert.assertEquals("!QAZXSW@", CommonUtils.decodePassword("!QAZXSW@"));
        Assert.assertEquals("5dfger(@", CommonUtils.decodePassword("5dfger(@"));
    }

}