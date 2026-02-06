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

package org.apache.dolphinscheduler.server.master.engine.graph;

import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.Command;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.extract.master.command.ICommandParam;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskInstanceFactories;
import org.apache.dolphinscheduler.server.master.runner.IWorkflowExecuteContext;

import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Factory for creating WorkflowExecutionGraph based on command type.
 * <p>
 * This factory encapsulates all graph creation logic, ensuring command handlers
 * are not concerned with how instances are initialized.
 */
@Slf4j
@Component
public class WorkflowExecutionGraphFactory {

    @Autowired
    private TaskInstanceDao taskInstanceDao;

    @Autowired
    private TaskInstanceFactories taskInstanceFactories;

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Create a WorkflowExecutionGraph based on the command type in the context.
     *
     * @param context the workflow execute context
     * @return the WorkflowExecutionGraph, or null if not applicable
     */
    public IWorkflowExecutionGraph createWorkflowExecutionGraph(final IWorkflowExecuteContext context) {
        final Command command = context.getCommand();
        if (command == null) {
            log.warn("Command is null, cannot create workflow execution graph");
            return null;
        }

        final CommandType commandType = command.getCommandType();
        switch (commandType) {
            case START_PROCESS:
                return createForStartProcess(context);
            case REPEAT_RUNNING:
                return createForRepeatRunning(context);
            case START_FAILURE_TASK_PROCESS:
                return createForRecoverFailure(context);
            case RECOVER_TOLERANCE_FAULT_PROCESS:
                return createForFailover(context);
            case RECOVER_SERIAL_WAIT:
                return null; // No execution graph needed
            default:
                log.warn("Unsupported command type for graph creation: {}", commandType);
                return createForStartProcess(context);
        }
    }

    /**
     * Create graph for START_PROCESS command - creates new task instances.
     */
    private IWorkflowExecutionGraph createForStartProcess(final IWorkflowExecuteContext context) {
        final IWorkflowGraph workflowGraph = context.getWorkflowGraph();
        final WorkflowInstance workflowInstance = context.getWorkflowInstance();
        final List<String> startNodes = parseStartNodes(context);

        final WorkflowExecutionGraph workflowExecutionGraph = new WorkflowExecutionGraph();
        final BiConsumer<String, Set<String>> taskExecutionRunnableCreator = (task, successors) -> {
            final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder =
                    TaskExecutionRunnableBuilder
                            .builder()
                            .workflowExecutionGraph(workflowExecutionGraph)
                            .workflowDefinition(context.getWorkflowDefinition())
                            .project(context.getProject())
                            .workflowInstance(workflowInstance)
                            .taskDefinition(workflowGraph.getTaskNodeByName(task))
                            .workflowEventBus(context.getWorkflowEventBus())
                            .applicationContext(applicationContext)
                            .build();
            workflowExecutionGraph.addNode(new TaskExecutionRunnable(taskExecutionRunnableBuilder));
            workflowExecutionGraph.addEdge(task, successors);
        };

        final WorkflowGraphTopologyLogicalVisitor workflowGraphTopologyLogicalVisitor =
                WorkflowGraphTopologyLogicalVisitor.builder()
                        .taskDependType(workflowInstance.getTaskDependType())
                        .onWorkflowGraph(workflowGraph)
                        .fromTask(startNodes)
                        .doVisitFunction(taskExecutionRunnableCreator)
                        .build();
        workflowGraphTopologyLogicalVisitor.visit();
        workflowExecutionGraph.removeUnReachableEdge();

        return workflowExecutionGraph;
    }

    /**
     * Create graph for REPEAT_RUNNING command - invalidates old task instances and creates new ones.
     */
    private IWorkflowExecutionGraph createForRepeatRunning(final IWorkflowExecuteContext context) {
        final WorkflowInstance workflowInstance = context.getWorkflowInstance();

        // Mark all existing task instances as invalid before creating the new graph
        final List<TaskInstance> taskInstances = getValidTaskInstances(workflowInstance);
        taskInstanceDao.markTaskInstanceInvalid(taskInstances);

        // Create new graph (same logic as START_PROCESS)
        return createForStartProcess(context);
    }

    /**
     * Create graph for START_FAILURE_TASK_PROCESS command - recovers from failed tasks.
     */
    private IWorkflowExecutionGraph createForRecoverFailure(final IWorkflowExecuteContext context) {
        final IWorkflowGraph workflowGraph = context.getWorkflowGraph();
        final WorkflowInstance workflowInstance = context.getWorkflowInstance();
        final List<String> startNodes = parseStartNodes(context);

        final Map<String, TaskInstance> taskInstanceMap = dealWithHistoryTaskInstances(context)
                .stream()
                .collect(Collectors.toMap(TaskInstance::getName, Function.identity()));

        final WorkflowExecutionGraph workflowExecutionGraph = new WorkflowExecutionGraph();

        final BiConsumer<String, Set<String>> taskExecutionRunnableCreator = (task, successors) -> {
            final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder =
                    TaskExecutionRunnableBuilder
                            .builder()
                            .workflowExecutionGraph(workflowExecutionGraph)
                            .workflowDefinition(context.getWorkflowDefinition())
                            .project(context.getProject())
                            .workflowInstance(workflowInstance)
                            .taskDefinition(workflowGraph.getTaskNodeByName(task))
                            .taskInstance(taskInstanceMap.get(task))
                            .workflowEventBus(context.getWorkflowEventBus())
                            .applicationContext(applicationContext)
                            .build();
            workflowExecutionGraph.addNode(new TaskExecutionRunnable(taskExecutionRunnableBuilder));
            workflowExecutionGraph.addEdge(task, successors);
        };

        final WorkflowGraphTopologyLogicalVisitor workflowGraphTopologyLogicalVisitor =
                WorkflowGraphTopologyLogicalVisitor.builder()
                        .taskDependType(workflowInstance.getTaskDependType())
                        .onWorkflowGraph(workflowGraph)
                        .fromTask(startNodes)
                        .doVisitFunction(taskExecutionRunnableCreator)
                        .build();
        workflowGraphTopologyLogicalVisitor.visit();
        workflowExecutionGraph.removeUnReachableEdge();

        return workflowExecutionGraph;
    }

    /**
     * Create graph for RECOVER_TOLERANCE_FAULT_PROCESS command - recovers from failover.
     */
    private IWorkflowExecutionGraph createForFailover(final IWorkflowExecuteContext context) {
        final IWorkflowGraph workflowGraph = context.getWorkflowGraph();
        final WorkflowInstance workflowInstance = context.getWorkflowInstance();
        final List<String> startNodes = parseStartNodes(context);

        final Map<String, TaskInstance> taskInstanceMap =
                getValidTaskInstances(workflowInstance)
                        .stream()
                        .collect(Collectors.toMap(TaskInstance::getName, Function.identity()));

        final WorkflowExecutionGraph workflowExecutionGraph = new WorkflowExecutionGraph();

        final BiConsumer<String, Set<String>> taskExecutionRunnableCreator = (task, successors) -> {
            final TaskExecutionRunnableBuilder taskExecutionRunnableBuilder =
                    TaskExecutionRunnableBuilder
                            .builder()
                            .workflowExecutionGraph(workflowExecutionGraph)
                            .workflowDefinition(context.getWorkflowDefinition())
                            .project(context.getProject())
                            .workflowInstance(workflowInstance)
                            .taskDefinition(workflowGraph.getTaskNodeByName(task))
                            .taskInstance(taskInstanceMap.get(task))
                            .workflowEventBus(context.getWorkflowEventBus())
                            .applicationContext(applicationContext)
                            .build();
            workflowExecutionGraph.addNode(new TaskExecutionRunnable(taskExecutionRunnableBuilder));
            workflowExecutionGraph.addEdge(task, successors);
        };

        final WorkflowGraphTopologyLogicalVisitor workflowGraphTopologyLogicalVisitor =
                WorkflowGraphTopologyLogicalVisitor.builder()
                        .taskDependType(workflowInstance.getTaskDependType())
                        .onWorkflowGraph(workflowGraph)
                        .fromTask(startNodes)
                        .doVisitFunction(taskExecutionRunnableCreator)
                        .build();
        workflowGraphTopologyLogicalVisitor.visit();
        workflowExecutionGraph.removeUnReachableEdge();

        return workflowExecutionGraph;
    }

    /**
     * Parse start nodes from the workflow instance command param.
     * Converts task codes to task names.
     */
    private List<String> parseStartNodes(final IWorkflowExecuteContext context) {
        final WorkflowInstance workflowInstance = context.getWorkflowInstance();
        final ICommandParam commandParam =
                JSONUtils.parseObject(workflowInstance.getCommandParam(), ICommandParam.class);
        if (commandParam == null || CollectionUtils.isEmpty(commandParam.getStartNodes())) {
            return Collections.emptyList();
        }
        final IWorkflowGraph workflowGraph = context.getWorkflowGraph();
        return commandParam.getStartNodes()
                .stream()
                .map(workflowGraph::getTaskNodeByCode)
                .map(TaskDefinition::getName)
                .collect(Collectors.toList());
    }

    /**
     * Get valid (non-invalid) task instances for a workflow instance.
     */
    private List<TaskInstance> getValidTaskInstances(final WorkflowInstance workflowInstance) {
        return taskInstanceDao.queryValidTaskListByWorkflowInstanceId(
                workflowInstance.getId());
    }

    /**
     * Deal with history task instances for failure recovery.
     * Mark failure/killed tasks and their children as invalid.
     */
    private List<TaskInstance> dealWithHistoryTaskInstances(final IWorkflowExecuteContext context) {
        final WorkflowInstance workflowInstance = context.getWorkflowInstance();
        final Map<String, TaskInstance> taskInstanceMap = getValidTaskInstances(workflowInstance)
                .stream()
                .collect(Collectors.toMap(TaskInstance::getName, Function.identity()));

        final IWorkflowGraph workflowGraph = context.getWorkflowGraph();
        final List<String> startNodes = parseStartNodes(context);

        final Set<String> needRecoverTasks = new HashSet<>();
        final Set<String> markInvalidTasks = new HashSet<>();
        final BiConsumer<String, Set<String>> historyTaskInstanceMarker = (task, successors) -> {
            if (markInvalidTasks.contains(task)) {
                if (taskInstanceMap.containsKey(task)) {
                    taskInstanceDao.markTaskInstanceInvalid(Lists.newArrayList(taskInstanceMap.get(task)));
                    taskInstanceMap.remove(task);
                }
                markInvalidTasks.addAll(successors);
                return;
            }

            final TaskInstance taskInstance = taskInstanceMap.get(task);
            if (taskInstance == null) {
                return;
            }

            if (isTaskNeedRecreate(taskInstance) || isTaskCanRecover(taskInstance)) {
                needRecoverTasks.add(task);
                markInvalidTasks.addAll(successors);
            }
        };

        final WorkflowGraphTopologyLogicalVisitor workflowGraphTopologyLogicalVisitor =
                WorkflowGraphTopologyLogicalVisitor.builder()
                        .onWorkflowGraph(workflowGraph)
                        .taskDependType(workflowInstance.getTaskDependType())
                        .fromTask(startNodes)
                        .doVisitFunction(historyTaskInstanceMarker)
                        .build();
        workflowGraphTopologyLogicalVisitor.visit();

        for (String task : needRecoverTasks) {
            final TaskInstance taskInstance = taskInstanceMap.get(task);
            if (isTaskCanRecover(taskInstance)) {
                recoverTaskInstance(taskInstance);
            } else if (isTaskNeedRecreate(taskInstance)) {
                taskInstanceDao.markTaskInstanceInvalid(Lists.newArrayList(taskInstance));
                taskInstanceMap.remove(task);
            }
        }

        return Lists.newArrayList(taskInstanceMap.values());
    }

    private boolean isTaskNeedRecreate(final TaskInstance taskInstance) {
        final TaskExecutionStatus state = taskInstance.getState();
        return state == TaskExecutionStatus.PAUSE
                || state == TaskExecutionStatus.KILL
                || state == TaskExecutionStatus.FAILURE
                || state == TaskExecutionStatus.NEED_FAULT_TOLERANCE
                || state == TaskExecutionStatus.DISPATCH
                || state == TaskExecutionStatus.RUNNING_EXECUTION;
    }

    private boolean isTaskCanRecover(final TaskInstance taskInstance) {
        final TaskExecutionStatus state = taskInstance.getState();
        return state == TaskExecutionStatus.PAUSE || state == TaskExecutionStatus.KILL;
    }

    private void recoverTaskInstance(final TaskInstance taskInstance) {
        // The factory handles: setting old instance flag to NO, updating it, and inserting the new instance
        taskInstanceFactories.failedRecoverTaskInstanceFactory()
                .builder()
                .withTaskInstance(taskInstance)
                .build();
    }
}
