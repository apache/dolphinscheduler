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

package org.apache.dolphinscheduler.server.master.runner.task.subworkflow;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.ProcessInstanceMap;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceDao;
import org.apache.dolphinscheduler.dao.repository.ProcessInstanceMapDao;
import org.apache.dolphinscheduler.extract.base.client.SingletonJdkDynamicRpcClientProxyFactory;
import org.apache.dolphinscheduler.extract.master.ITaskInstanceExecutionEventListener;
import org.apache.dolphinscheduler.extract.master.transportor.WorkflowInstanceStateChangeEvent;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SubProcessParameters;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.execute.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.server.master.runner.task.BaseAsyncLogicTask;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
public class SubWorkflowLogicTask extends BaseAsyncLogicTask<SubProcessParameters> {

    public static final String TASK_TYPE = "SUB_PROCESS";
    private final ProcessInstanceExecCacheManager processInstanceExecCacheManager;
    private final ProcessInstanceDao processInstanceDao;
    private final ProcessInstanceMapDao processInstanceMapDao;

    public SubWorkflowLogicTask(TaskExecutionContext taskExecutionContext,
                                ProcessInstanceExecCacheManager processInstanceExecCacheManager,
                                ProcessInstanceDao processInstanceDao,
                                ProcessInstanceMapDao processInstanceMapDao) {
        super(taskExecutionContext,
                JSONUtils.parseObject(taskExecutionContext.getTaskParams(), new TypeReference<SubProcessParameters>() {
                }));
        this.processInstanceExecCacheManager = processInstanceExecCacheManager;
        this.processInstanceDao = processInstanceDao;
        this.processInstanceMapDao = processInstanceMapDao;
    }

    @Override
    public AsyncTaskExecuteFunction getAsyncTaskExecuteFunction() throws MasterTaskExecuteException {
        // todo: create sub workflow instance here?
        return new SubWorkflowAsyncTaskExecuteFunction(taskExecutionContext, processInstanceDao);
    }

    @Override
    public void pause() throws MasterTaskExecuteException {
        WorkflowExecuteRunnable workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        if (workflowExecuteRunnable == null) {
            log.warn("Cannot find WorkflowExecuteRunnable");
            return;
        }
        ProcessInstance subProcessInstance =
                processInstanceDao.querySubProcessInstanceByParentId(taskExecutionContext.getProcessInstanceId(),
                        taskExecutionContext.getTaskInstanceId());
        if (subProcessInstance == null) {
            log.info("SubWorkflow instance is null");
            return;
        }
        TaskInstance taskInstance =
                workflowExecuteRunnable.getTaskInstance(taskExecutionContext.getTaskInstanceId()).orElse(null);
        if (taskInstance == null) {
            // we don't need to do this check, the task instance shouldn't be null
            log.info("TaskInstance is null");
            return;
        }
        if (taskInstance.getState().isFinished()) {
            log.info("The task instance is finished, no need to pause");
            return;
        }
        subProcessInstance.setStateWithDesc(WorkflowExecutionStatus.READY_PAUSE, "ready pause sub workflow");
        processInstanceDao.updateById(subProcessInstance);
        try {
            sendToSubProcess(taskExecutionContext, subProcessInstance);
            log.info("Success send pause request to SubWorkflow's master: {}", subProcessInstance.getHost());
        } catch (Exception e) {
            throw new MasterTaskExecuteException(String.format("Send pause request to SubWorkflow's master: %s failed",
                    subProcessInstance.getHost()), e);
        }
    }

    @Override
    public void kill() {
        WorkflowExecuteRunnable workflowExecuteRunnable =
                processInstanceExecCacheManager.getByProcessInstanceId(taskExecutionContext.getProcessInstanceId());
        if (workflowExecuteRunnable == null) {
            log.warn("Cannot find WorkflowExecuteRunnable");
            return;
        }
        ProcessInstance subProcessInstance =
                processInstanceDao.querySubProcessInstanceByParentId(taskExecutionContext.getProcessInstanceId(),
                        taskExecutionContext.getTaskInstanceId());
        if (subProcessInstance == null) {
            log.info("SubWorkflow instance is null");
            return;
        }
        TaskInstance taskInstance =
                workflowExecuteRunnable.getTaskInstance(taskExecutionContext.getTaskInstanceId()).orElse(null);
        if (taskInstance == null) {
            // we don't need to do this check, the task instance shouldn't be null
            log.info("TaskInstance is null");
            return;
        }
        if (subProcessInstance.getState().isFinished()) {
            log.info("The subProcessInstance is finished, no need to pause");
            return;
        }
        subProcessInstance.setStateWithDesc(WorkflowExecutionStatus.READY_STOP, "ready stop by kill task");
        processInstanceDao.updateById(subProcessInstance);
        try {
            sendToSubProcess(taskExecutionContext, subProcessInstance);
            log.info("Success send kill request to SubWorkflow's master: {}", subProcessInstance.getHost());
        } catch (Exception e) {
            log.error("Send kill request to SubWorkflow's master: {} failed", subProcessInstance.getHost(), e);
        }
    }

    @Override
    public List<Property> getVarPool() {
        List<Property> taskInstanceProps = getTaskParameters().getVarPool();
        ProcessInstanceMap processInstanceMap = processInstanceMapDao.queryWorkProcessMapByParent(
                taskExecutionContext.getProcessInstanceId(), taskExecutionContext.getTaskInstanceId());
        ProcessInstance childProcessInstance = processInstanceDao.queryById(processInstanceMap.getProcessInstanceId());
        if (!StringUtils.isBlank(childProcessInstance.getVarPool())) {
            Map<String, String> currentInstanceMap =
                    taskInstanceProps.stream().collect(Collectors.toMap(Property::getProp, Property::getValue));
            List<Property> childProps = JSONUtils.toList(childProcessInstance.getVarPool(), Property.class);
            Map<String, String> childProcessMap =
                    childProps.stream().filter(property -> property.getDirect() == Direct.OUT)
                            .collect(Collectors.toMap(Property::getProp, Property::getValue));
            List<Property> allProps = new ArrayList<>();
            allProps.addAll(childProps.stream().filter(property -> property.getDirect() == Direct.OUT)
                    .collect(Collectors.toList()));
            // overwrite existing parameter value of type out
            taskInstanceProps.forEach(property -> {
                if (property.getDirect() == Direct.OUT &&
                        StringUtils.isNotBlank(childProcessMap.get(property.getProp()))) {
                    property.setValue(childProcessMap.get(property.getProp()));
                }
                allProps.add(property);
            });

            // add parameters that do not exist
            childProps.forEach(property -> {
                if (!currentInstanceMap.containsKey(property.getProp())) {
                    allProps.add(property);
                }
            });
            return allProps;
        }

        return taskInstanceProps;
    }

    private void sendToSubProcess(TaskExecutionContext taskExecutionContext,
                                  ProcessInstance subProcessInstance) {
        final ITaskInstanceExecutionEventListener iTaskInstanceExecutionEventListener =
                SingletonJdkDynamicRpcClientProxyFactory
                        .getProxyClient(subProcessInstance.getHost(), ITaskInstanceExecutionEventListener.class);
        final WorkflowInstanceStateChangeEvent workflowInstanceStateChangeEvent = new WorkflowInstanceStateChangeEvent(
                taskExecutionContext.getProcessInstanceId(),
                taskExecutionContext.getTaskInstanceId(),
                subProcessInstance.getState(),
                subProcessInstance.getId(),
                0);
        iTaskInstanceExecutionEventListener.onWorkflowInstanceInstanceStateChange(workflowInstanceStateChangeEvent);
    }
}
