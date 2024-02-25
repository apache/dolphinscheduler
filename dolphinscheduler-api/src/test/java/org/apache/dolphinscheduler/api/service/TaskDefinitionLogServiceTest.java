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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.service.impl.TaskDefinitionLogServiceImpl;
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;
import org.apache.dolphinscheduler.dao.mapper.ProcessTaskRelationLogMapper;
import org.apache.dolphinscheduler.dao.repository.ProcessTaskRelationLogDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionLogDao;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskDefinitionLogServiceTest {

    @InjectMocks
    private TaskDefinitionLogServiceImpl taskDefinitionLogService;

    @Mock
    private ProcessTaskRelationLogDao processTaskRelationLogDao;

    @Mock
    private TaskDefinitionLogDao taskDefinitionLogDao;
    @Mock
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    private List<ProcessTaskRelationLog> getProcessTaskRelationList() {
        ProcessTaskRelationLog processTaskRelationLog1 = new ProcessTaskRelationLog();
        processTaskRelationLog1.setPreTaskCode(0L);
        processTaskRelationLog1.setPostTaskCode(1L);
        processTaskRelationLog1.setPostTaskVersion(1);

        ProcessTaskRelationLog processTaskRelationLog2 = new ProcessTaskRelationLog();
        processTaskRelationLog2.setPreTaskCode(0L);
        processTaskRelationLog2.setPostTaskCode(1L);
        processTaskRelationLog2.setPostTaskVersion(2);

        return Arrays.asList(
                processTaskRelationLog1,
                processTaskRelationLog2);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteTaskByWorkflowDefinitionCode() {
        when(processTaskRelationLogDao.queryByWorkflowDefinitionCode(1L)).thenReturn(Collections.emptyList());
        assertDoesNotThrow(() -> taskDefinitionLogService.deleteTaskByWorkflowDefinitionCode(1L));

        when(processTaskRelationLogDao.queryByWorkflowDefinitionCode(2L)).thenReturn(getProcessTaskRelationList());
        assertDoesNotThrow(() -> taskDefinitionLogService.deleteTaskByWorkflowDefinitionCode(2L));
    }
}
