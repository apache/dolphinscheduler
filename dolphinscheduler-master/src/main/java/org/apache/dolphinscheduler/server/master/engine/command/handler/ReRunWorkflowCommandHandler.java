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

package org.apache.dolphinscheduler.server.master.engine.command.handler;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext.WorkflowExecuteContextBuilder;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This handler used to handle {@link CommandType#REPEAT_RUNNING} which will rerun the workflow instance.
 */
@Component
public class ReRunWorkflowCommandHandler extends RunWorkflowCommandHandler {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MasterConfig masterConfig;

    /**
     * Generate the repeat running workflow instance.
     * <p> Will use the origin workflow instance, but will update the following fields. Need to note we cannot not
     * update the command params here, since this will make the origin command params lost.
     * <ul>
     *     <li>state</li>
     *     <li>command type</li>
     *     <li>start time</li>
     *     <li>restart time</li>
     *     <li>end time</li>
     *     <li>run times</li>
     * </ul>
     */
    @Override
    protected void assembleWorkflowInstance(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final Command command = workflowExecuteContextBuilder.getCommand();
        final int workflowInstanceId = command.getWorkflowInstanceId();
        final WorkflowInstance workflowInstance = workflowInstanceDao.queryOptionalById(workflowInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find WorkflowInstance:" + workflowInstanceId));
        workflowInstance.setVarPool(null);
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION, command.getCommandType().name());
        workflowInstance.setCommandType(command.getCommandType());
        workflowInstance.setRestartTime(new Date());
        workflowInstance.setHost(masterConfig.getMasterAddress());
        workflowInstance.setEndTime(null);
        workflowInstance.setRunTimes(workflowInstance.getRunTimes() + 1);
        workflowInstanceDao.updateById(workflowInstance);

        workflowExecuteContextBuilder.setWorkflowInstance(workflowInstance);
    }

    /**
     * Generate the workflow execution graph.
     * <p> Will clear the history task instance and assembly the start tasks into the WorkflowExecutionGraph.
     */
    @Override
    protected void assembleWorkflowExecutionGraph(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        markAllTaskInstanceInvalid(workflowExecuteContextBuilder);
        super.assembleWorkflowExecutionGraph(workflowExecuteContextBuilder);
    }

    private void markAllTaskInstanceInvalid(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final WorkflowInstance workflowInstance = workflowExecuteContextBuilder.getWorkflowInstance();
        final List<TaskInstance> taskInstances = getValidTaskInstance(workflowInstance);
        taskInstanceDao.markTaskInstanceInvalid(taskInstances);
    }

    @Override
    public CommandType commandType() {
        return CommandType.REPEAT_RUNNING;
    }
}
