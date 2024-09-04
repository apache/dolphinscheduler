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

import org.apache.dolphinscheduler.dao.repository.ProjectDao;
import org.apache.dolphinscheduler.dao.repository.TaskDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.task.DependentLogicTaskChannelFactory;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ILogicTaskPluginFactory;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskInitializeException;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DependentLogicTaskPluginFactory implements ILogicTaskPluginFactory<DependentLogicTask> {

    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private WorkflowDefinitionDao workflowDefinitionDao;
    @Autowired
    private TaskDefinitionDao taskDefinitionDao;
    @Autowired
    private TaskInstanceDao taskInstanceDao;
    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private IWorkflowRepository IWorkflowRepository;

    @Override
    public DependentLogicTask createLogicTask(final ITaskExecutor taskExecutor) throws LogicTaskInitializeException {
        final TaskExecutionContext taskExecutionContext = taskExecutor.getTaskExecutionContext();
        final int workflowInstanceId = taskExecutionContext.getWorkflowInstanceId();
        final IWorkflowExecutionRunnable workflowExecutionRunnable = IWorkflowRepository.get(workflowInstanceId);
        if (workflowExecutionRunnable == null) {
            throw new LogicTaskInitializeException("Cannot find the WorkflowExecuteRunnable: " + workflowInstanceId);
        }
        return new DependentLogicTask(
                taskExecutionContext,
                projectDao,
                workflowDefinitionDao,
                taskDefinitionDao,
                taskInstanceDao,
                workflowInstanceDao,
                workflowExecutionRunnable);
    }

    @Override
    public String getTaskType() {
        return DependentLogicTaskChannelFactory.NAME;
    }
}
