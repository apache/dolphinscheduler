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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;

/**
 * process service test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessServiceTest {

    @Mock
    private ProcessService processService;

    @Before
    public void init(){
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(0L);
        processDefinition.setExecutionType(ProcessExecutionTypeEnum.SERIAL_WAIT);

        PowerMockito.when(processService.theLatestVersionOfProcessDefinition(0L))
                .thenReturn(processDefinition);
    }

    @Test
    public void testTheLatestVersionOfProcessDefinition(){
        ProcessDefinition processDefinition = processService.theLatestVersionOfProcessDefinition(0L);
        Assert.assertTrue(processDefinition.getExecutionType().typeIsSerial());
    }
}
