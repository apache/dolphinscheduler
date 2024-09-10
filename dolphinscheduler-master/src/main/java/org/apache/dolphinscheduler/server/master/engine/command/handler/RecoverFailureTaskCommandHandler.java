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
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.master.engine.TaskGroupCoordinator;
import org.apache.dolphinscheduler.server.master.engine.graph.IWorkflowGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowExecutionGraph;
import org.apache.dolphinscheduler.server.master.engine.graph.WorkflowGraphTopologyLogicalVisitor;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskExecutionRunnableBuilder;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.TaskInstanceFactories;
import org.apache.dolphinscheduler.server.master.runner.TaskExecutionContextFactory;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteContext.WorkflowExecuteContextBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * This handler used to handle {@link CommandType#START_FAILURE_TASK_PROCESS}.
 * <p> Will start the failure/pause/killed and other task instance which is behind success tasks instance but not been triggered.
 */
@Component
public class RecoverFailureTaskCommandHandler extends AbstractCommandHandler {

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

    @Autowired
    private TaskInstanceFactories taskInstanceFactories;

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
        workflowInstance.setVarPool(null);
        workflowInstance.setStateWithDesc(WorkflowExecutionStatus.RUNNING_EXECUTION, command.getCommandType().name());
        workflowInstance.setCommandType(command.getCommandType());
        workflowInstanceDao.updateById(workflowInstance);

        workflowExecuteContextBuilder.setWorkflowInstance(workflowInstance);
    }

    /**
     * Generate the workflow execution graph.
     * <p> Will clear the history failure/killed task.
     * <p> If the task's predecessors exist failure/killed, will also mark the task as failure/killed.
     */
    @Override
    protected void assembleWorkflowExecutionGraph(final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final Map<String, TaskInstance> taskInstanceMap = dealWithHistoryTaskInstances(workflowExecuteContextBuilder)
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

    /**
     * Return the valid task instance which should not be recovered.
     * <p> Will mark the failure/killed task instance as invalid.
     */
    private List<TaskInstance> dealWithHistoryTaskInstances(
                                                            final WorkflowExecuteContextBuilder workflowExecuteContextBuilder) {
        final WorkflowInstance workflowInstance = workflowExecuteContextBuilder.getWorkflowInstance();
        final Map<String, TaskInstance> taskInstanceMap = super.getValidTaskInstance(workflowInstance)
                .stream()
                .collect(Collectors.toMap(TaskInstance::getName, Function.identity()));

        final IWorkflowGraph workflowGraph = workflowExecuteContextBuilder.getWorkflowGraph();

        final Set<String> needRecoverTasks = new HashSet<>();
        final Set<String> markInvalidTasks = new HashSet<>();
        final BiConsumer<String, Set<String>> historyTaskInstanceMarker = (task, successors) -> {
            // If the parent is need recover
            // Then the task should mark as invalid, and it's child should be mark as invalidated.
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
                        .fromTask(parseStartNodesFromWorkflowInstance(workflowExecuteContextBuilder))
                        .doVisitFunction(historyTaskInstanceMarker)
                        .build();
        workflowGraphTopologyLogicalVisitor.visit();

        for (String task : needRecoverTasks) {
            final TaskInstance taskInstance = taskInstanceMap.get(task);
            if (isTaskCanRecover(taskInstance)) {
                taskInstanceMap.put(task, createRecoverTaskInstance(taskInstance));
                continue;
            }
            if (isTaskNeedRecreate(taskInstance)) {
                taskInstanceMap.put(task, createRecreatedTaskInstance(taskInstance));
            }
        }
        return new ArrayList<>(taskInstanceMap.values());
    }

    /**
     * Whether the task need to be recreated.
     * <p> If the task state is FAILURE and KILL, then will mark the task invalid and recreate the task.
     */
    private boolean isTaskNeedRecreate(final TaskInstance taskInstance) {
        if (taskInstance == null) {
            return false;
        }
        return taskInstance.getState() == TaskExecutionStatus.FAILURE
                || taskInstance.getState() == TaskExecutionStatus.KILL;
    }

    private TaskInstance createRecreatedTaskInstance(final TaskInstance taskInstance) {
        return taskInstanceFactories.failedRecoverTaskInstanceFactory()
                .builder()
                .withTaskInstance(taskInstance)
                .build();
    }

    private boolean isTaskCanRecover(final TaskInstance taskInstance) {
        if (taskInstance == null) {
            return false;
        }
        return taskInstance.getState() == TaskExecutionStatus.PAUSE;
    }

    private TaskInstance createRecoverTaskInstance(final TaskInstance taskInstance) {
        return taskInstanceFactories.pauseRecoverTaskInstanceFactory()
                .builder()
                .withTaskInstance(taskInstance)
                .build();
    }

    @Override
    public CommandType commandType() {
        return CommandType.START_FAILURE_TASK_PROCESS;
    }

}
