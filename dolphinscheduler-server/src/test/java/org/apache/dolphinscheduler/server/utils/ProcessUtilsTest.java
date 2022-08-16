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

package org.apache.dolphinscheduler.server.utils;

import static org.powermock.api.mockito.PowerMockito.when;
import org.apache.commons.lang3.SystemUtils;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.utils.HadoopUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;

import java.util.ArrayList;
import java.util.List;

import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({System.class, OSUtils.class, HadoopUtils.class, PropertyUtils.class, SystemUtils.class})
public class ProcessUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void getPidsStr() throws Exception {
        int processId = 1;
        PowerMockito.mockStatic(OSUtils.class);
        Whitebox.setInternalState(SystemUtils.class, "IS_OS_MAC", true);
        when(OSUtils.exeCmd(String.format("%s -p %d", Constants.PSTREE, processId))).thenReturn(null);
        String pidListMac = ProcessUtils.getPidsStr(processId);
        Assert.assertEquals("", pidListMac);
    }

    @Test
    public void testGetKerberosInitCommand() {
        PowerMockito.mockStatic(PropertyUtils.class);
        PowerMockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                .thenReturn(true);
        PowerMockito.when(PropertyUtils.getString(Constants.JAVA_SECURITY_KRB5_CONF_PATH)).thenReturn("/etc/krb5.conf");
        PowerMockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_PATH)).thenReturn("/etc/krb5.keytab");
        PowerMockito.when(PropertyUtils.getString(Constants.LOGIN_USER_KEY_TAB_USERNAME)).thenReturn("test@DS.COM");
        Assert.assertNotEquals("", ProcessUtils.getKerberosInitCommand());
        PowerMockito.when(PropertyUtils.getBoolean(Constants.HADOOP_SECURITY_AUTHENTICATION_STARTUP_STATE, false))
                .thenReturn(false);
        Assert.assertEquals("", ProcessUtils.getKerberosInitCommand());
    }

    @Test
    public void testCancelApplication() {
        List<String> appIds = new ArrayList<>();
        appIds.add("application_1585532379175_228491");
        appIds.add("application_1598885606600_3677");
        String tenantCode = "dev";
        String executePath = "/ds-exec/1/1/1";
        TaskExecutionStatus running = TaskExecutionStatus.RUNNING_EXECUTION;

        PowerMockito.mockStatic(HadoopUtils.class);
        HadoopUtils hadoop = HadoopUtils.getInstance();

        try {
            PowerMockito.whenNew(HadoopUtils.class).withAnyArguments().thenReturn(hadoop);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            when(hadoop.getApplicationStatus("application_1585532379175_228491")).thenReturn(running);
            when(hadoop.getApplicationStatus("application_1598885606600_3677")).thenReturn(running);
        } catch (Exception e) {
            e.printStackTrace();
            ProcessUtils.cancelApplication(appIds, logger, tenantCode, executePath);
        }

        Assert.assertNotNull(appIds);
    }
}
