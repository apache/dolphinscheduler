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

import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import lombok.experimental.UtilityClass;

import com.google.common.base.Strings;

@UtilityClass
public class WorkflowInstanceUtils {

    public static String logWorkflowInstanceInDetails(ProcessInstance workflowInstance) {
        StringBuilder logBuilder = new StringBuilder();
        // set the length for '*'
        int horizontalLineLength = 80;
        // Append the title and the centered "Workflow Instance Detail"
        int titleLength = 40;
        int leftSpaces = (horizontalLineLength - titleLength) / 2;
        String centeredTitle = String.format("%" + leftSpaces + "s%s", "", "Workflow Instance Detail");
        logBuilder.append("\n").append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append(centeredTitle).append("\n")
                .append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append("Workflow Name:             ").append(workflowInstance.getProcessDefinition().getName())
                .append("\n")
                .append("Workflow Instance Name:    ").append(workflowInstance.getName()).append("\n")
                .append("Command Type:              ").append(workflowInstance.getCommandType()).append("\n")
                .append("State:                     ").append(workflowInstance.getState().getDesc()).append("\n")
                .append("Host:                      ").append(workflowInstance.getHost()).append("\n")
                .append("Is Sub Process:            ").append(workflowInstance.getIsSubProcess().getDescp())
                .append("\n")
                .append("Run Times:                 ").append(workflowInstance.getRunTimes()).append("\n")
                .append("Max Try Times:             ").append(workflowInstance.getMaxTryTimes()).append("\n")
                .append("Schedule Time:             ").append(workflowInstance.getScheduleTime()).append("\n")
                .append("Dry Run:                   ").append(workflowInstance.getDryRun()).append("\n")
                .append("Tenant:                    ").append(workflowInstance.getTenantCode()).append("\n")
                .append("Restart Time:              ").append(workflowInstance.getRestartTime()).append("\n")
                .append("Work Group:                ").append(workflowInstance.getWorkerGroup()).append("\n")
                .append("Start Time:                ").append(workflowInstance.getStartTime()).append("\n")
                .append("End Time:                  ").append(workflowInstance.getEndTime()).append("\n");
        return logBuilder.toString();
    }

    public String logTaskInstanceInDetail(TaskInstance taskInstance) {
        StringBuilder logBuilder = new StringBuilder();
        // set the length for '*'
        int horizontalLineLength = 80;
        // Append the title and the centered "Task Instance Detail"
        int titleLength = 40;
        int leftSpaces = (horizontalLineLength - titleLength) / 2;
        String centeredTitle = String.format("%" + leftSpaces + "s%s", "", "Task Instance Detail");
        logBuilder.append("\n").append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append(centeredTitle).append("\n")
                .append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append("Task Name:              ").append(taskInstance.getName()).append("\n")
                .append("Workflow Instance Name: ").append(taskInstance.getProcessInstance().getName()).append("\n")
                .append("Task Execute Type:      ").append(taskInstance.getTaskExecuteType().getDesc()).append("\n")
                .append("Execute State:          ").append(taskInstance.getState().getDesc()).append("\n")
                .append("Host:                   ").append(taskInstance.getHost()).append("\n")
                .append("Task Type:              ").append(taskInstance.getTaskType()).append("\n")
                .append("Priority:               ").append(taskInstance.getTaskInstancePriority().getDescp())
                .append("\n")
                .append("Tenant:                 ").append(taskInstance.getProcessInstance().getTenantCode())
                .append("\n")
                .append("First Submit Time:      ").append(taskInstance.getFirstSubmitTime()).append("\n")
                .append("Submit Time:            ").append(taskInstance.getSubmitTime()).append("\n")
                .append("Start Time:             ").append(taskInstance.getStartTime()).append("\n")
                .append("End Time:               ").append(taskInstance.getEndTime()).append("\n");
        return logBuilder.toString();
    }
}
