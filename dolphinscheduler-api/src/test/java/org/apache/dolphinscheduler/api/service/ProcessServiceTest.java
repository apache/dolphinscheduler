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
import org.apache.dolphinscheduler.dao.mapper.ProcessDefinitionMapper;
import org.apache.dolphinscheduler.service.process.ProcessServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * process service test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class ProcessServiceTest {

    @InjectMocks
    private ProcessServiceImpl processService;

    @Mock
    private ProcessDefinitionMapper processDefineMapper;

    @Test
    public void testTheLatestVersionOfProcessDefinition(){
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setCode(1L);
        processDefinition.setExecutionType(ProcessExecutionTypeEnum.SERIAL_WAIT);

        Mockito.when(processDefineMapper.queryByCode(1L)).thenReturn(processDefinition);
        ProcessDefinition definition = processService.theLatestVersionOfProcessDefinition(1L);
        Assert.assertTrue(definition.getExecutionType().typeIsSerial());
    }
}
