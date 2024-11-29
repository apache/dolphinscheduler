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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.dependent;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.AbstractLogicTask;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ITaskParameterDeserializer;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class DependentLogicTask extends AbstractLogicTask<DependentParameters> {

    private final TaskExecutionContext taskExecutionContext;

    private final DependentTaskTracker dependentTaskTracker;

    public DependentLogicTask(TaskExecutionContext taskExecutionContext,
                              ProjectDao projectDao,
                              WorkflowDefinitionDao workflowDefinitionDao,
                              TaskDefinitionDao taskDefinitionDao,
                              TaskInstanceDao taskInstanceDao,
                              WorkflowInstanceDao workflowInstanceDao,
                              IWorkflowExecutionRunnable workflowExecutionRunnable) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
        this.dependentTaskTracker = new DependentTaskTracker(
                taskExecutionContext,
                taskParameters,
                projectDao,
                workflowDefinitionDao,
                taskDefinitionDao,
                taskInstanceDao,
                workflowInstanceDao);
        onTaskRunning();
    }

    @Override
    public void start() throws MasterTaskExecuteException {
        log.info("Dependent task: {} started", taskExecutionContext.getTaskName());
    }

    @Override
    public TaskExecutionStatus getTaskExecutionState() {
        if (isRunning()) {
            taskExecutionStatus = dependentTaskTracker.getDependentTaskStatus();
            return taskExecutionStatus;
        }
        return taskExecutionStatus;
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        onTaskPaused();
        log.info("Pause task : {} success", taskExecutionContext.getTaskName());
    }

    @Override
    public void kill() throws MasterTaskExecuteException {
        onTaskKilled();
        log.info("Kill task : {} success", taskExecutionContext.getTaskName());
    }

    @Override
    public ITaskParameterDeserializer<DependentParameters> getTaskParameterDeserializer() {
        return taskParamsJson -> JSONUtils.parseObject(taskParamsJson, new TypeReference<DependentParameters>() {
        });
    }

}
