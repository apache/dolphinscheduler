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

package org.apache.dolphinscheduler.plugin.task.api.utils;


import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.spi.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

@PrepareForTest({PropertyUtils.class})
public class LogUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(LogUtilsTest.class);
    private static final String APP_ID_FILE = LogUtilsTest.class.getResource("/appId.txt")
            .getFile();

    @Test
    public void getAppIdsFromLogFile() {
        List<String> appIds = LogUtils.getAppIdsFromLogFile(APP_ID_FILE);
        Assert.assertEquals(Lists.newArrayList("application_1548381669007_1234"), appIds);
    }

    @Test
    public void cancelApplication() {
        List<String> appIds = new ArrayList<>();
        appIds.add("application_1585532379175_228491");
        appIds.add("application_1598885606600_3677");
        String tenantCode = "dev";
        String executePath = "/ds-exec/1/1/1";

        LogUtils.cancelApplication(appIds, logger, tenantCode, executePath);

        Assert.assertNotNull(appIds);
    }

    @Test
    public void testGetKerberosInitCommand() {
        Mockito.mockStatic(PropertyUtils.class);
        Mockito.when(PropertyUtils.getBoolean(TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
            .thenReturn(true);
        Mockito.when(PropertyUtils.getString(TaskConstants.JAVA_SECURITY_KRB5_CONF_PATH)).thenReturn("/etc/krb5.conf");
        Mockito.when(PropertyUtils.getString(TaskConstants.LOGIN_USER_KEY_TAB_PATH)).thenReturn("/etc/krb5.keytab");
        Mockito.when(PropertyUtils.getString(TaskConstants.LOGIN_USER_KEY_TAB_USERNAME)).thenReturn("test@DS.COM");
        System.out.println(PropertyUtils.getBoolean(TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false));
        Assert.assertNotEquals("", LogUtils.getKerberosInitCommand());
        Mockito.when(PropertyUtils.getBoolean(TaskConstants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
            .thenReturn(false);
        Assert.assertEquals("", LogUtils.getKerberosInitCommand());
    }
}
