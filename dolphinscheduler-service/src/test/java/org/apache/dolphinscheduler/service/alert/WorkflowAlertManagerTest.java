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

package org.apache.dolphinscheduler.service.alert;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowDefinitionLogMapper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ProcessAlertManager Test
 */
@ExtendWith(MockitoExtension.class)
public class WorkflowAlertManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowAlertManagerTest.class);

    @InjectMocks
    WorkflowAlertManager workflowAlertManager = new WorkflowAlertManager();

    @Mock
    private AlertDao alertDao;

    @Mock
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    @Mock
    private UserMapper userMapper;

    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarningWorkerToleranceFaultTest() {
        // process instance
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setName("test");

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("test-task-1");
        taskInstance.setHost("127.0.0.1");
        taskInstance.setRetryTimes(3);
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);

        workflowAlertManager.sendAlertWorkerToleranceFault(workflowInstance, taskInstanceList);
    }

    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarnningOfProcessInstanceTest() {
        // process instance
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setWarningType(WarningType.SUCCESS);
        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);
        workflowInstance.setCommandType(CommandType.COMPLEMENT_DATA);
        workflowInstance.setWarningGroupId(1);
        workflowInstance.setWorkflowDefinitionCode(1L);
        workflowInstance.setWorkflowDefinitionVersion(1);

        ProjectUser projectUser = new ProjectUser();
        TaskInstance taskInstance = new TaskInstance();
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);

        workflowAlertManager.sendAlertWorkflowInstance(workflowInstance, taskInstanceList, projectUser);
    }

    /**
     * send blocking alert
     */
    @Test
    public void sendBlockingAlertTest() {
        // process instance
        WorkflowInstance workflowInstance = new WorkflowInstance();
        workflowInstance.setId(1);
        workflowInstance.setName("test-process-01");
        workflowInstance.setCommandType(CommandType.START_PROCESS);
        workflowInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        workflowInstance.setRunTimes(0);
        workflowInstance.setStartTime(new Date());
        workflowInstance.setEndTime(new Date());
        workflowInstance.setHost("127.0.0.1");
        workflowInstance.setWarningGroupId(1);
        workflowInstance.setWorkflowDefinitionCode(1L);
        workflowInstance.setWorkflowDefinitionVersion(1);

        ProjectUser projectUser = new ProjectUser();

        workflowAlertManager.sendWorkflowBlockingAlert(workflowInstance, projectUser);
    }
}
