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
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

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
public class ProcessAlertManagerTest {

    private static final Logger logger = LoggerFactory.getLogger(ProcessAlertManagerTest.class);

    @InjectMocks
    ProcessAlertManager processAlertManager = new ProcessAlertManager();

    @Mock
    private AlertDao alertDao;

    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarningWorkerToleranceFaultTest() {
        // process instance
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setName("test");

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("test-task-1");
        taskInstance.setHost("127.0.0.1");
        taskInstance.setRetryTimes(3);
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);

        processAlertManager.sendAlertWorkerToleranceFault(processInstance, taskInstanceList);
    }

    /**
     * send worker alert fault tolerance
     */
    @Test
    public void sendWarnningOfProcessInstanceTest() {
        // process instance
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setWarningType(WarningType.SUCCESS);
        processInstance.setState(WorkflowExecutionStatus.SUCCESS);
        processInstance.setCommandType(CommandType.COMPLEMENT_DATA);
        processInstance.setWarningGroupId(1);

        ProjectUser projectUser = new ProjectUser();
        TaskInstance taskInstance = new TaskInstance();
        List<TaskInstance> taskInstanceList = new ArrayList<>();
        taskInstanceList.add(taskInstance);

        processAlertManager.sendAlertProcessInstance(processInstance, taskInstanceList, projectUser);
    }

    /**
     * send blocking alert
     */
    @Test
    public void sendBlockingAlertTest() {
        // process instance
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setId(1);
        processInstance.setName("test-process-01");
        processInstance.setCommandType(CommandType.START_PROCESS);
        processInstance.setState(WorkflowExecutionStatus.RUNNING_EXECUTION);
        processInstance.setRunTimes(0);
        processInstance.setStartTime(new Date());
        processInstance.setEndTime(new Date());
        processInstance.setHost("127.0.0.1");
        processInstance.setWarningGroupId(1);

        ProjectUser projectUser = new ProjectUser();

        processAlertManager.sendProcessBlockingAlert(processInstance, projectUser);
    }
}
