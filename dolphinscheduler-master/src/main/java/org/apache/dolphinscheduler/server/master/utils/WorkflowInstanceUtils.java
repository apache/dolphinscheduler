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

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;

import java.util.List;
import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;

import com.google.common.base.Strings;

@UtilityClass
public class WorkflowInstanceUtils {

    public static String logWorkflowInstanceInDetails(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        final IWorkflowExecuteContext workflowExecuteContext = workflowExecutionRunnable.getWorkflowExecuteContext();
        final IWorkflowExecutionGraph workflowExecutionGraph = workflowExecuteContext.getWorkflowExecutionGraph();
        final IWorkflowGraph workflowGraph = workflowExecuteContext.getWorkflowGraph();
        final WorkflowInstance workflowInstance = workflowExecuteContext.getWorkflowInstance();
        final WorkflowEventBus workflowEventBus = workflowExecuteContext.getWorkflowEventBus();

        final List<String> startNodes = workflowExecutionGraph
                .getStartNodes()
                .stream()
                .map(ITaskExecutionRunnable::getName)
                .collect(Collectors.toList());

        final StringBuilder logBuilder = new StringBuilder();
        // set the length for '*'
        final int horizontalLineLength = 80;
        // Append the title and the centered "Workflow Instance Detail"
        final int titleLength = 40;
        final int leftSpaces = (horizontalLineLength - titleLength) / 2;
        final String centeredTitle = String.format("%" + leftSpaces + "s%s", "", "Workflow Instance Detail");
        logBuilder.append("\n").append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append(centeredTitle).append("\n")
                .append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append("Workflow Instance Name:  ").append(workflowInstance.getName()).append("\n")
                .append("Command Type:            ").append(workflowInstance.getCommandType()).append("\n")
                .append("State:                   ").append(workflowInstance.getState().name()).append("\n")
                .append("StartNodes:              ").append(startNodes).append("\n")
                .append("TotalTasks:              ")
                .append(workflowExecutionRunnable.getWorkflowExecuteContext().getWorkflowExecutionGraph()
                        .getAllTaskExecutionRunnable().size())
                .append("\n")
                .append("Host:                    ").append(workflowInstance.getHost()).append("\n")
                .append("Is SubWorkflow:          ").append(workflowInstance.getIsSubWorkflow().name()).append("\n")
                .append("Run Times:               ").append(workflowInstance.getRunTimes()).append("\n")
                .append("Tenant:                  ").append(workflowInstance.getTenantCode()).append("\n")
                .append("Work Group:              ").append(workflowInstance.getWorkerGroup()).append("\n")
                .append("EventBusSummary:         ").append(workflowEventBus.getWorkflowEventBusSummary()).append("\n")
                .append("Schedule Time:           ").append(workflowInstance.getScheduleTime()).append("\n")
                .append("Start Time:              ").append(workflowInstance.getStartTime()).append("\n")
                .append("Restart Time:            ").append(workflowInstance.getRestartTime()).append("\n")
                .append("End Time:                ").append(workflowInstance.getEndTime());
        return logBuilder.toString();
    }

    public String logTaskInstanceInDetail(TaskInstance taskInstance) {
        final StringBuilder logBuilder = new StringBuilder();
        // set the length for '*'
        final int horizontalLineLength = 80;
        // Append the title and the centered "Task Instance Detail"
        final int titleLength = 40;
        final int leftSpaces = (horizontalLineLength - titleLength) / 2;
        final String centeredTitle = String.format("%" + leftSpaces + "s%s", "", "Task Instance Detail");
        logBuilder.append("\n").append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append(centeredTitle).append("\n")
                .append(Strings.repeat("*", horizontalLineLength)).append("\n")
                .append("Task Name:              ").append(taskInstance.getName()).append("\n")
                .append("Workflow Instance Name: ").append(taskInstance.getWorkflowInstance().getName()).append("\n")
                .append("Task Execute Type:      ").append(taskInstance.getTaskExecuteType().getDesc()).append("\n")
                .append("Execute State:          ").append(taskInstance.getState().getDesc()).append("\n")
                .append("Host:                   ").append(taskInstance.getHost()).append("\n")
                .append("Task Type:              ").append(taskInstance.getTaskType()).append("\n")
                .append("Priority:               ").append(taskInstance.getTaskInstancePriority().getDescp())
                .append("\n")
                .append("Tenant:                 ").append(taskInstance.getWorkflowInstance().getTenantCode())
                .append("\n")
                .append("First Submit Time:      ").append(taskInstance.getFirstSubmitTime()).append("\n")
                .append("Submit Time:            ").append(taskInstance.getSubmitTime()).append("\n")
                .append("Start Time:             ").append(taskInstance.getStartTime()).append("\n")
                .append("End Time:               ").append(taskInstance.getEndTime()).append("\n");
        return logBuilder.toString();
    }
}
