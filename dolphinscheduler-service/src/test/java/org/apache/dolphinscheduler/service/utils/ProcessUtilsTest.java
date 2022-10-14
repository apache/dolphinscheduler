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

package org.apache.dolphinscheduler.service.utils;

import static org.mockito.ArgumentMatchers.anyString;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.service.storage.impl.HadoopUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class ProcessUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getPidsStr() throws Exception {
        int processId = 1;

        try (MockedStatic<OSUtils> mockedStaticOSUtils = Mockito.mockStatic(OSUtils.class)) {
            mockedStaticOSUtils.when(() -> OSUtils.exeCmd(anyString())).thenReturn(null);
            String pidList = ProcessUtils.getPidsStr(processId);
            Assert.assertEquals("", pidList);
        }
    }

    @Test
    public void testGetKerberosInitCommand() {
        try (MockedStatic<PropertyUtils> mockedStaticPropertyUtils = Mockito.mockStatic(PropertyUtils.class)) {
            mockedStaticPropertyUtils
                    .when(() -> PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                    .thenReturn(true);
            mockedStaticPropertyUtils.when(() -> PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH))
                    .thenReturn("/etc/krb5.conf");
            mockedStaticPropertyUtils.when(() -> PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH))
                    .thenReturn("/etc/krb5.keytab");
            mockedStaticPropertyUtils.when(() -> PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME))
                    .thenReturn("test@DS.COM");
            Assert.assertNotEquals("", ProcessUtils.getKerberosInitCommand());
            mockedStaticPropertyUtils
                    .when(() -> PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                    .thenReturn(false);
            Assert.assertEquals("", ProcessUtils.getKerberosInitCommand());
        }
    }

    @Test
    public void testCancelApplication() {
        List<String> appIds = new ArrayList<>();
        appIds.add("application_1585532379175_228491");
        appIds.add("application_1598885606600_3677");
        String tenantCode = "dev";
        String executePath = "/ds-exec/1/1/1";
        TaskExecutionStatus running = TaskExecutionStatus.RUNNING_EXECUTION;

        try (MockedStatic<HadoopUtils> mockedStaticHadoopUtils = Mockito.mockStatic(HadoopUtils.class)) {
            HadoopUtils hadoop = HadoopUtils.getInstance();

            try {
                Mockito.when(hadoop.getApplicationStatus("application_1585532379175_228491")).thenReturn(running);
                Mockito.when(hadoop.getApplicationStatus("application_1598885606600_3677")).thenReturn(running);
            } catch (Exception e) {
                e.printStackTrace();
                ProcessUtils.cancelApplication(appIds, logger, tenantCode, executePath);
            }

            Assert.assertNotNull(appIds);
        }
    }
}
