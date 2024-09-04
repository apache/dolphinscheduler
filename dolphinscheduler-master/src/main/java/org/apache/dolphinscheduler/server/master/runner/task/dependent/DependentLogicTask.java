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

package org.apache.dolphinscheduler.server.master.runner.task.dependent;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.DependentParameters;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.server.master.runner.task.BaseAsyncLogicTask;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class DependentLogicTask extends BaseAsyncLogicTask<DependentParameters> {

    public static final String TASK_TYPE = "DEPENDENT";

    private final ProjectDao projectDao;
    private final WorkflowDefinitionDao workflowDefinitionDao;
    private final TaskDefinitionDao taskDefinitionDao;
    private final TaskInstanceDao taskInstanceDao;
    private final WorkflowInstanceDao workflowInstanceDao;

    private final IWorkflowExecutionRunnable workflowExecutionRunnable;

    private DependentAsyncTaskExecuteFunction dependentAsyncTaskExecuteFunction;

    public DependentLogicTask(TaskExecutionContext taskExecutionContext,
                              ProjectDao projectDao,
                              WorkflowDefinitionDao workflowDefinitionDao,
                              TaskDefinitionDao taskDefinitionDao,
                              TaskInstanceDao taskInstanceDao,
                              WorkflowInstanceDao workflowInstanceDao,
                              IWorkflowExecutionRunnable workflowExecutionRunnable) {
        super(taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<DependentParameters>() {
                }));
        this.projectDao = projectDao;
        this.workflowDefinitionDao = workflowDefinitionDao;
        this.taskDefinitionDao = taskDefinitionDao;
        this.taskInstanceDao = taskInstanceDao;
        this.workflowInstanceDao = workflowInstanceDao;
        this.workflowExecutionRunnable = workflowExecutionRunnable;

    }

    @Override
    public AsyncTaskExecuteFunction getAsyncTaskExecuteFunction() {
        dependentAsyncTaskExecuteFunction = new DependentAsyncTaskExecuteFunction(taskExecutionContext,
                taskParameters,
                projectDao,
                workflowDefinitionDao,
                taskDefinitionDao,
                taskInstanceDao,
                workflowInstanceDao);
        return dependentAsyncTaskExecuteFunction;
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        // todo: support pause
    }

    @Override
    public void kill() throws MasterTaskExecuteException {
        // todo: support kill
    }

}
