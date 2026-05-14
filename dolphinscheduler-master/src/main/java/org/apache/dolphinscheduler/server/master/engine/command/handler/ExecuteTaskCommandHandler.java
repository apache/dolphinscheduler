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
import static org.apache.dolphinscheduler.common.constants.CommandKeyConstants.CMD_PARAM_START_NODES;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.dao.repository.WorkflowInstanceDao;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowGraphTopologyLogicalVisitor;
import org.apache.dolphinscheduler.server.master.engine.task.execution.TaskExecution;
import org.apache.dolphinscheduler.server.master.engine.task.execution.TaskExecutionBuilder;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext.WorkflowExecuteContextBuilder;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.base.Splitter;

/**
 * This handler is used to handle {@link CommandType#EXECUTE_TASK}.
 * <p> It will rerun the given start task and all its downstream tasks in the same workflow instance.
 */
@Component
public class ExecuteTaskCommandHandler extends AbstractCommandHandler {

    @Autowired
    private WorkflowInstanceDao workflowInstanceDao;

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private MasterConfig masterConfig;

    @Override
    protected void assembleWorkflowInstance(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final Command command = workflowExecuteContextBuilder.getCommand();
        final int workflowInstanceId = command.getWorkflowInstanceId();
        final WorkflowInstance workflowInstance = workflowInstanceDao.queryOptionalById(workflowInstanceId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find WorkflowInstance:" + workflowInstanceId));
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION, command.getCommandType().name());
        workflowInstance.setCommandType(command.getCommandType());
        if (command.getTaskDependType() != null) {
            workflowInstance.setTaskDependType(command.getTaskDependType());
        }
        workflowInstance.setHost(masterConfig.getMasterAddress());
        workflowInstanceDao.updateById(workflowInstance);
        workflowExecuteContextBuilder.setWorkflowInstance(workflowInstance);
    }

    @Override
    protected void assembleWorkflowExecutionGraph(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final IWorkflowGraph workflowGraph = workflowExecuteContextBuilder.getWorkflowGraph();
        final List<String> startNodes = parseStartNodesFromCommand(workflowExecuteContextBuilder, workflowGraph);
        final Map<String, TaskInstance> taskInstanceMap = getValidTaskInstance(workflowExecuteContextBuilder
                .getWorkflowInstance())
                        .stream()
                        .collect(Collectors.toMap(TaskInstance::getName, Function.identity()));

        // Mark the selected task and all its downstream task instances as invalid, then trigger them again.
        final Set<String> taskNamesNeedRerun = new HashSet<>();
        final WorkflowGraphTopologyLogicalVisitor markInvalidTaskVisitor =
                WorkflowGraphTopologyLogicalVisitor.builder()
                        .onWorkflowGraph(workflowGraph)
                        .taskDependType(workflowExecuteContextBuilder.getWorkflowInstance().getTaskDependType())
                        .fromTask(startNodes)
                        .doVisitFunction((task, successors) -> taskNamesNeedRerun.add(task))
                        .build();
        markInvalidTaskVisitor.visit();

        final List<TaskInstance> taskInstancesNeedRerun = taskNamesNeedRerun.stream()
                .map(taskInstanceMap::remove)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(taskInstancesNeedRerun)) {
            taskInstanceDao.markTaskInstanceInvalid(taskInstancesNeedRerun);
        }

        final WorkflowExecutionGraph workflowExecutionGraph = new WorkflowExecutionGraph();
        final BiConsumer<String, Set<String>> taskExecutionCreator = (task, successors) -> {
            final TaskExecutionBuilder taskExecutionBuilder =
                    TaskExecutionBuilder
                            .builder()
                            .workflowExecutionGraph(workflowExecutionGraph)
                            .workflowDefinition(workflowExecuteContextBuilder.getWorkflowDefinition())
                            .project(workflowExecuteContextBuilder.getProject())
                            .workflowInstance(workflowExecuteContextBuilder.getWorkflowInstance())
                            .taskDefinition(workflowGraph.getTaskNodeByName(task))
                            .taskInstance(taskInstanceMap.get(task))
                            .workflowEventBus(workflowExecuteContextBuilder.getWorkflowEventBus())
                            .applicationContext(applicationContext)
                            .build();
            workflowExecutionGraph.addNode(new TaskExecution(taskExecutionBuilder));
            workflowExecutionGraph.addEdge(task, successors);
        };

        final WorkflowGraphTopologyLogicalVisitor workflowGraphTopologyLogicalVisitor =
                WorkflowGraphTopologyLogicalVisitor.builder()
                        .taskDependType(workflowExecuteContextBuilder.getWorkflowInstance().getTaskDependType())
                        .onWorkflowGraph(workflowGraph)
                        .fromTask(startNodes)
                        .doVisitFunction(taskExecutionCreator)
                        .build();
        workflowGraphTopologyLogicalVisitor.visit();
        workflowExecutionGraph.removeUnReachableEdge();
        workflowExecuteContextBuilder.setWorkflowExecutionGraph(workflowExecutionGraph);
    }

    private List<String> parseStartNodesFromCommand(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder,
                                                    final IWorkflowGraph workflowGraph) {
        final Command command = workflowExecuteContextBuilder.getCommand();
        final String startNodes = JSONUtils.getNodeString(command.getCommandParam(), CMD_PARAM_START_NODES);
        checkArgument(StringUtils.isNotBlank(startNodes),
                "Invalid command param, the start nodes is empty: " + command.getCommandParam());
        final List<Long> startNodeCodes = Splitter.on(',')
                .trimResults()
                .omitEmptyStrings()
                .splitToStream(startNodes)
                .map(Long::parseLong)
                .collect(Collectors.toList());
        checkArgument(CollectionUtils.isNotEmpty(startNodeCodes),
                "Invalid command param, cannot parse start nodes from command param: " + command.getCommandParam());
        return startNodeCodes
                .stream()
                .map(workflowGraph::getTaskNodeByCode)
                .map(TaskDefinition::getName)
                .collect(Collectors.toList());
    }

    @Override
    public CommandType commandType() {
        return CommandType.EXECUTE_TASK;
    }
}
