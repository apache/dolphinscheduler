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

package org.apache.dolphinscheduler.server.master.event;

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.processor.queue.TaskEvent;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskCacheEventHandlerTest {

    @InjectMocks
    private TaskCacheEventHandler taskCacheEventHandler;

    @Mock
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Mock
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    @Mock
    private DataQualityResultOperator dataQualityResultOperator;

    @Mock
    private ProcessService processService;

    @Mock
    private TaskInstanceDao taskInstanceDao;

    @Test
    void testHandleTaskEvent() {
        TaskEvent taskEvent = Mockito.mock(TaskEvent.class);
        int processInstanceId = 1;
        int taskInstanceId = 2;
        int cacheTaskInstanceId = 3;
        int cacheProcessInstanceId = 4;

        Mockito.when(taskEvent.getTaskInstanceId()).thenReturn(taskInstanceId);
        Mockito.when(taskEvent.getProcessInstanceId()).thenReturn(processInstanceId);
        Mockito.when(taskEvent.getCacheTaskInstanceId()).thenReturn(cacheTaskInstanceId);

        TaskInstance cacheTaskInstance = new TaskInstance();
        cacheTaskInstance.setId(cacheTaskInstanceId);
        cacheTaskInstance.setProcessInstanceId(cacheProcessInstanceId);
        cacheTaskInstance.setTaskParams(JSONUtils.toJsonString(new HashMap<>()));

        Mockito.when(taskInstanceDao.queryById(cacheTaskInstanceId)).thenReturn(cacheTaskInstance);

        WorkflowExecuteRunnable workflowExecuteRunnable = Mockito.mock(WorkflowExecuteRunnable.class);
        Mockito.when(processInstanceExecCacheManager.getByProcessInstanceId(processInstanceId))
                .thenReturn(workflowExecuteRunnable);
        Optional<TaskInstance> taskInstanceOptional = Mockito.mock(Optional.class);
        Mockito.when(workflowExecuteRunnable.getTaskInstance(taskInstanceId)).thenReturn(taskInstanceOptional);
        Mockito.when(taskInstanceOptional.isPresent()).thenReturn(true);

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskParams(JSONUtils.toJsonString(new HashMap<>()));
        taskInstance.setId(taskInstanceId);
        taskInstance.setProcessInstanceId(processInstanceId);
        taskInstance.setProcessInstanceName("test");
        ProcessInstance processInstance = new ProcessInstance();
        taskInstance.setProcessInstance(processInstance);
        ProcessDefinition processDefinition = new ProcessDefinition();
        taskInstance.setProcessDefine(processDefinition);
        taskInstance.setSubmitTime(new Date());

        Mockito.when(taskInstanceOptional.get()).thenReturn(taskInstance);

        taskCacheEventHandler.handleTaskEvent(taskEvent);

        Assertions.assertEquals(Flag.YES, taskInstance.getFlag());
        Assertions.assertEquals(taskInstanceId, taskInstance.getId());
        Assertions.assertEquals(processInstanceId, taskInstance.getProcessInstanceId());
    }

}
