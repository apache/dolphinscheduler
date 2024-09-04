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
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.WorkflowFailoverCommandParam;
import org.apache.dolphinscheduler.server.master.engine.TaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowGraphTopologyLogicalVisitor;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionContextFactory;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext.WorkflowExecuteContextBuilder;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * This handler used to handle {@link CommandType#RECOVER_TOLERANCE_FAULT_PROCESS}.
 * <p> Will do failover of the workflow instance and recover it to the origin state.
 */
@Component
public class WorkflowFailoverCommandHandler extends AbstractCommandHandler {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskExecutionContextFactory taskExecutionContextFactory;

    @Autowired
    private TaskGroupCoordinator taskGroupCoordinator;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Generate the recover workflow instance.
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
    protected void assembleWorkflowInstance(
                                            final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final Command command = workflowExecuteContextBuilder.getCommand();
        final int workflowInstanceId = command.getWorkflowInstanceId();
        final WorkflowInstance workflowInstance = workflowInstanceDao.queryOptionalById(workflowInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find WorkflowInstance:" + workflowInstanceId));
        final WorkflowFailoverCommandParam workflowFailoverCommandParam = JSONUtils.parseObject(
                command.getCommandParam(),
                WorkflowFailoverCommandParam.class);
        if (workflowFailoverCommandParam == null) {
            throw new IllegalArgumentException(
                    "The WorkflowFailoverCommandParam: " + command.getCommandParam() + " is invalid");
        }
        workflowInstance.setState(workflowFailoverCommandParam.getWorkflowExecutionStatus());
        workflowInstanceDao.updateById(workflowInstance);

        workflowExecuteContextBuilder.setWorkflowInstance(workflowInstance);
    }

    /**
     * Generate the workflow execution graph.
     * <p> Will rebuild the WorkflowExecutionGraph from the exist task instance.
     */
    @Override
    protected void assembleWorkflowExecutionGraph(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final Map<String, TaskInstance> taskInstanceMap =
                getValidTaskInstance(workflowExecuteContextBuilder.getWorkflowInstance())
                        .stream()
                        .collect(Collectors.toMap(TaskInstance::getName, Function.identity()));

        final IWorkflowGraph workflowGraph = workflowExecuteContextBuilder.getWorkflowGraph();
        final WorkflowExecutionGraph workflowExecutionGraph = new WorkflowExecutionGraph();

        final BiConsumer<String, Set<String>> taskExecutionRunnableCreator = (task, successors) -> {
            final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder =
                    TaskExecutionRunnableBuilder
                            .builder()
                            .workflowExecutionGraph(workflowExecutionGraph)
                            .workflowDefinition(workflowExecuteContextBuilder.getWorkflowDefinition())
                            .workflowInstance(workflowExecuteContextBuilder.getWorkflowInstance())
                            .taskDefinition(workflowGraph.getTaskNodeByName(task))
                            .taskInstance(taskInstanceMap.get(task))
                            .workflowEventBus(workflowExecuteContextBuilder.getWorkflowEventBus())
                            .applicationContext(applicationContext)
                            .build();
            workflowExecutionGraph.addNode(new TaskExecutionRunnable(taskExecutionRunnableBuilder));
            workflowExecutionGraph.addEdge(task, successors);
        };

        final WorkflowGraphTopologyLogicalVisitor workflowGraphTopologyLogicalVisitor =
                WorkflowGraphTopologyLogicalVisitor.builder()
                        .taskDependType(workflowExecuteContextBuilder.getWorkflowInstance().getTaskDependType())
                        .onWorkflowGraph(workflowGraph)
                        .fromTask(parseStartNodesFromWorkflowInstance(workflowExecuteContextBuilder))
                        .doVisitFunction(taskExecutionRunnableCreator)
                        .build();
        workflowGraphTopologyLogicalVisitor.visit();

        workflowExecuteContextBuilder.setWorkflowExecutionGraph(workflowExecutionGraph);
    }

    @Override
    public CommandType commandType() {
        return CommandType.RECOVER_TOLERANCE_FAULT_PROCESS;
    }

}
