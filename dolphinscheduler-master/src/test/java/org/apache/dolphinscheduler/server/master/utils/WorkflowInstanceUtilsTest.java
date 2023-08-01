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

package org.apache.dolphinscheduler.server.master.utils;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.common.enums.TaskExecuteType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinition;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;

import java.sql.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class WorkflowInstanceUtilsTest {

    @Test
    public void testLogWorkflowInstanceInDetails() {
        ProcessDefinition processDefinition = new ProcessDefinition();
        processDefinition.setName("test_workflow");

        ProcessInstance workflowInstance = new ProcessInstance();
        workflowInstance.setProcessDefinition(processDefinition);
        workflowInstance.setName("test_workflow_20230801");
        workflowInstance.setCommandType(CommandType.REPEAT_RUNNING);
        workflowInstance.setState(WorkflowExecutionStatus.SUCCESS);
        workflowInstance.setHost("127.0.0.1");
        workflowInstance.setIsSubProcess(Flag.NO);
        workflowInstance.setRunTimes(1);
        workflowInstance.setMaxTryTimes(0);
        workflowInstance.setScheduleTime(Date.valueOf("2023-08-01"));
        workflowInstance.setDryRun(0);
        workflowInstance.setTenantCode("default");
        workflowInstance.setRestartTime(Date.valueOf("2023-08-01"));
        workflowInstance.setWorkerGroup("default");
        workflowInstance.setStartTime(Date.valueOf("2023-08-01"));
        workflowInstance.setEndTime(Date.valueOf("2023-08-01"));
        Assertions.assertEquals("\n"
                + "********************************************************************************\n"
                + "                    Workflow Instance Detail\n"
                + "********************************************************************************\n"
                + "Workflow Name:             test_workflow\n"
                + "Workflow Instance Name:    test_workflow_20230801\n"
                + "Command Type:              REPEAT_RUNNING\n"
                + "State:                     success\n"
                + "Host:                      127.0.0.1\n"
                + "Is Sub Process:            no\n"
                + "Run Times:                 1\n"
                + "Max Try Times:             0\n"
                + "Schedule Time:             2023-08-01\n"
                + "Dry Run:                   0\n"
                + "Tenant:                    default\n"
                + "Restart Time:              2023-08-01\n"
                + "Work Group:                default\n"
                + "Start Time:                2023-08-01\n"
                + "End Time:                  2023-08-01\n",
                WorkflowInstanceUtils.logWorkflowInstanceInDetails(workflowInstance));
    }

    @Test
    public void testLogTaskInstanceInDetails() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setName("test_process");
        processInstance.setTenantCode("default");

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setName("test_task");
        taskInstance.setProcessInstance(processInstance);
        taskInstance.setState(TaskExecutionStatus.SUCCESS);
        taskInstance.setTaskExecuteType(TaskExecuteType.BATCH);
        taskInstance.setHost("127.0.0.1");
        taskInstance.setTaskType("SHELL");
        taskInstance.setTaskInstancePriority(Priority.MEDIUM);
        taskInstance.setFirstSubmitTime(Date.valueOf("2023-08-01"));
        taskInstance.setSubmitTime(Date.valueOf("2023-08-01"));
        taskInstance.setStartTime(Date.valueOf("2023-08-01"));
        taskInstance.setEndTime(Date.valueOf("2023-08-01"));
        Assertions.assertEquals("\n"
                + "********************************************************************************\n"
                + "                    Task Instance Detail\n"
                + "********************************************************************************\n"
                + "Task Name:              test_task\n"
                + "Workflow Instance Name: test_process\n"
                + "Task Execute Type:      batch\n"
                + "Execute State:          success\n"
                + "Host:                   127.0.0.1\n"
                + "Task Type:              SHELL\n"
                + "Priority:               medium\n"
                + "Tenant:                 default\n"
                + "First Submit Time:      2023-08-01\n"
                + "Submit Time:            2023-08-01\n"
                + "Start Time:             2023-08-01\n"
                + "End Time:               2023-08-01\n", WorkflowInstanceUtils.logTaskInstanceInDetail(taskInstance));
    }
}
