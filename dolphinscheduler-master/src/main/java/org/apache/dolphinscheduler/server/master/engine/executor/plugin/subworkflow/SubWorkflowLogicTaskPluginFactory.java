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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin.subworkflow;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.task.SubWorkflowLogicTaskChannelFactory;
import org.apache.dolphinscheduler.server.master.engine.IWorkflowRepository;
import org.apache.dolphinscheduler.server.master.engine.executor.plugin.ILogicTaskPluginFactory;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.task.executor.ITaskExecutor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SubWorkflowLogicTaskPluginFactory implements ILogicTaskPluginFactory<SubWorkflowLogicTask> {

    private final ApplicationContext applicationContext;

    private final IWorkflowRepository workflowRepository;

    public SubWorkflowLogicTaskPluginFactory(final ApplicationContext applicationContext,
                                             final IWorkflowRepository workflowRepository) {
        this.applicationContext = applicationContext;
        this.workflowRepository = workflowRepository;
    }

    @Override
    public SubWorkflowLogicTask createLogicTask(final ITaskExecutor taskExecutor) {
        final TaskExecutionContext taskExecutionContext = taskExecutor.getTaskExecutionContext();
        final int workflowInstanceId = taskExecutionContext.getWorkflowInstanceId();
        final IWorkflowExecutionRunnable workflowExecutionRunnable = workflowRepository.get(workflowInstanceId);
        return new SubWorkflowLogicTask(
                taskExecutionContext,
                workflowExecutionRunnable,
                taskExecutor,
                applicationContext);
    }

    @Override
    public String getTaskType() {
        return SubWorkflowLogicTaskChannelFactory.NAME;
    }
}
