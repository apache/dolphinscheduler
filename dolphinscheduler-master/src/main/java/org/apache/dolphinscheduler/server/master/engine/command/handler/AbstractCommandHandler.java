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

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.dolphinscheduler.common.utils.JSONUtils.parseObject;

import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowDefinitionLogDao;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.server.master.engine.WorkflowEventBus;
import org.apache.dolphinscheduler.server.master.engine.command.ICommandHandler;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowGraphFactory;
import org.apache.dolphinscheduler.server.master.engine.workflow.listener.IWorkflowLifecycleListener;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.workflow.runnable.WorkflowExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext.WorkflowExecuteContextBuilder;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class AbstractCommandHandler implements ICommandHandler {

    @Autowired
    protected WorkflowDefinitionLogDao workflowDefinitionLogDao;

    @Autowired
    protected WorkflowGraphFactory workflowGraphFactory;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected TaskInstanceDao taskInstanceDao;

    @Autowired
    protected List<IWorkflowLifecycleListener> workflowLifecycleListeners;

    @Override
    public WorkflowExecutionRunnable handleCommand(final Command command) {
        final WorkflowExecuteContextBuilder workflowExecuteContextBuilder = WorkflowExecuteContext.builder()
                .withCommand(command);

        assembleWorkflowDefinition(workflowExecuteContextBuilder);
        assembleWorkflowGraph(workflowExecuteContextBuilder);
        assembleWorkflowInstance(workflowExecuteContextBuilder);
        assembleWorkflowInstanceLifecycleListeners(workflowExecuteContextBuilder);
        assembleWorkflowEventBus(workflowExecuteContextBuilder);
        assembleWorkflowExecutionGraph(workflowExecuteContextBuilder);

        final WorkflowExecutionRunnableBuilder workflowExecutionRunnableBuilder = WorkflowExecutionRunnableBuilder
                .builder()
                .workflowExecuteContextBuilder(workflowExecuteContextBuilder)
                .applicationContext(applicationContext)
                .build();
        return new WorkflowExecutionRunnable(workflowExecutionRunnableBuilder);
    }

    protected void assembleWorkflowEventBus(
                                            final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        workflowExecuteContextBuilder.setWorkflowEventBus(new WorkflowEventBus());
    }

    protected void assembleWorkflowInstanceLifecycleListeners(
                                                              final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        workflowExecuteContextBuilder.setWorkflowInstanceLifecycleListeners(workflowLifecycleListeners);
    }

    protected void assembleWorkflowDefinition(
                                              final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final Command command = workflowExecuteContextBuilder.getCommand();
        final long workflowDefinitionCode = command.getWorkflowDefinitionCode();
        final int workflowDefinitionVersion = command.getWorkflowDefinitionVersion();

        final WorkflowDefinition workflowDefinition = workflowDefinitionLogDao.queryByDefinitionCodeAndVersion(
                workflowDefinitionCode,
                workflowDefinitionVersion);
        checkArgument(workflowDefinition != null,
                "Cannot find the WorkflowDefinition: [" + workflowDefinitionCode + ":" + workflowDefinitionVersion
                        + "]");
        workflowExecuteContextBuilder.setWorkflowDefinition(workflowDefinition);

    }

    protected void assembleWorkflowGraph(
                                         final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final WorkflowDefinition workflowDefinition = workflowExecuteContextBuilder.getWorkflowDefinition();
        workflowExecuteContextBuilder.setWorkflowGraph(workflowGraphFactory.createWorkflowGraph(workflowDefinition));
    }

    protected abstract void assembleWorkflowInstance(
                                                     final WorkflowExecuteContextBuilder workflowExecuteContextBuilder);

    protected abstract void assembleWorkflowExecutionGraph(
                                                           final WorkflowExecuteContextBuilder workflowExecuteContextBuilder);

    protected List<String> parseStartNodesFromWorkflowInstance(
                                                               final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final WorkflowInstance workflowInstance = workflowExecuteContextBuilder.getWorkflowInstance();
        final ICommandParam commandParam = parseObject(workflowInstance.getCommandParam(), ICommandParam.class);
        checkArgument(commandParam != null, "Invalid command param : " + workflowInstance.getCommandParam());
        List<Long> startCodes = commandParam.getStartNodes();
        if (CollectionUtils.isEmpty(startCodes)) {
            return Collections.emptyList();
        }
        final IWorkflowGraph workflowGraph = workflowExecuteContextBuilder.getWorkflowGraph();
        return startCodes
                .stream()
                .map(workflowGraph::getTaskNodeByCode)
                .map(TaskDefinition::getName)
                .collect(Collectors.toList());

    }

    protected List<TaskInstance> getValidTaskInstance(final WorkflowInstance workflowInstance) {
        return taskInstanceDao.queryValidTaskListByWorkflowInstanceId(
                workflowInstance.getId(),
                workflowInstance.getTestFlag());
    }

}
