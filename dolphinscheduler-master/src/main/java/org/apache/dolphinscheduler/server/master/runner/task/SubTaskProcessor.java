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

package org.apache.dolphinscheduler.server.master.runner.task;

import static org.apache.dolphinscheduler.common.Constants.LOCAL_PARAMS;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.TASK_TYPE_SUB_PROCESS;

import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskTimeoutStrategy;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.remote.command.WorkflowStateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.utils.LogUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.auto.service.AutoService;

/**
 * subtask processor
 */
@AutoService(ITaskProcessor.class)
public class SubTaskProcessor extends BaseTaskProcessor {

    private ProcessInstance subProcessInstance = null;

    /**
     * run lock
     */
    private final Lock runLock = new ReentrantLock();

    private StateEventCallbackService stateEventCallbackService =
            SpringApplicationContext.getBean(StateEventCallbackService.class);

    @Override
    public boolean submitTask() {
        this.taskInstance =
                processService.submitTaskWithRetry(processInstance, taskInstance, maxRetryTimes, commitInterval);

        if (this.taskInstance == null) {
            return false;
        }
        this.setTaskExecutionLogger();
        taskInstance.setLogPath(LogUtils.getTaskLogPath(taskInstance.getFirstSubmitTime(),
                processInstance.getProcessDefinitionCode(),
                processInstance.getProcessDefinitionVersion(),
                taskInstance.getProcessInstanceId(),
                taskInstance.getId()));

        return true;
    }

    @Override
    public boolean runTask() {
        try {
            this.runLock.lock();
            if (setSubWorkFlow()) {
                updateTaskState();
            }
        } catch (Exception e) {
            logger.error("work flow {} sub task {} exceptions",
                    this.processInstance.getId(),
                    this.taskInstance.getId(),
                    e);
        } finally {
            this.runLock.unlock();
        }
        return true;
    }

    @Override
    protected boolean resubmitTask() {
        return true;
    }

    @Override
    protected boolean dispatchTask() {
        return true;
    }

    @Override
    protected boolean taskTimeout() {
        TaskTimeoutStrategy taskTimeoutStrategy = taskInstance.getTaskDefine().getTimeoutNotifyStrategy();
        if (TaskTimeoutStrategy.FAILED != taskTimeoutStrategy
                && TaskTimeoutStrategy.WARNFAILED != taskTimeoutStrategy) {
            return true;
        }
        logger.info("sub process task {} timeout, strategy {} ",
                taskInstance.getId(), taskTimeoutStrategy.getDescp());
        killTask();
        return true;
    }

    private void updateTaskState() {
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        logger.info("work flow {} task {}, sub work flow: {} state: {}",
                this.processInstance.getId(),
                this.taskInstance.getId(),
                subProcessInstance.getId(),
                subProcessInstance.getState());
        if (subProcessInstance != null && subProcessInstance.getState().isFinished()) {
            // todo: check the status and transform
            taskInstance.setState(TaskExecutionStatus.of(subProcessInstance.getState().getCode()));
            taskInstance.setEndTime(new Date());
            dealFinish();
            processService.saveTaskInstance(taskInstance);
        }
    }

    /**
     * get the params from subProcessInstance to this subProcessTask
     */
    private void dealFinish() {
        String thisTaskInstanceVarPool = taskInstance.getVarPool();
        if (StringUtils.isNotEmpty(thisTaskInstanceVarPool)) {
            String subProcessInstanceVarPool = subProcessInstance.getVarPool();
            if (StringUtils.isNotEmpty(subProcessInstanceVarPool)) {
                List<Property> varPoolProperties = JSONUtils.toList(thisTaskInstanceVarPool, Property.class);
                Map<String, Object> taskParams =
                        JSONUtils.parseObject(taskInstance.getTaskParams(), new TypeReference<Map<String, Object>>() {
                        });
                Object localParams = taskParams.get(LOCAL_PARAMS);
                if (localParams != null) {
                    List<Property> properties = JSONUtils.toList(JSONUtils.toJsonString(localParams), Property.class);
                    Map<String, String> subProcessParam =
                            JSONUtils.toList(subProcessInstanceVarPool, Property.class).stream()
                                    .collect(Collectors.toMap(Property::getProp, Property::getValue));
                    List<Property> outProperties =
                            properties.stream().filter(r -> Direct.OUT == r.getDirect()).collect(Collectors.toList());
                    for (Property info : outProperties) {
                        info.setValue(subProcessParam.get(info.getProp()));
                        varPoolProperties.add(info);
                    }
                    taskInstance.setVarPool(JSONUtils.toJsonString(varPoolProperties));
                    // deal with localParam for show in the page
                    processService.changeOutParam(taskInstance);
                }
            }
        }
    }

    @Override
    protected boolean pauseTask() {
        pauseSubWorkFlow();
        return true;
    }

    private boolean pauseSubWorkFlow() {
        ProcessInstance subProcessInstance =
                processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if (subProcessInstance == null || taskInstance.getState().isFinished()) {
            return false;
        }
        subProcessInstance.setStateWithDesc(WorkflowExecutionStatus.READY_PAUSE, "ready pause sub workflow");
        processInstanceDao.updateProcessInstance(subProcessInstance);
        sendToSubProcess();
        return true;
    }

    private boolean setSubWorkFlow() {
        logger.info("set work flow {} task {} running",
                this.processInstance.getId(),
                this.taskInstance.getId());
        if (this.subProcessInstance != null) {
            return true;
        }
        subProcessInstance = processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if (subProcessInstance == null || taskInstance.getState().isFinished()) {
            return false;
        }
        taskInstance.setHost(NetUtils.getAddr(masterConfig.getListenPort()));
        taskInstance.setState(TaskExecutionStatus.RUNNING_EXECUTION);
        taskInstance.setStartTime(new Date());
        processService.updateTaskInstance(taskInstance);
        logger.info("set sub work flow {} task {} state: {}",
                processInstance.getId(),
                taskInstance.getId(),
                taskInstance.getState());
        return true;

    }

    @Override
    protected boolean killTask() {
        ProcessInstance subProcessInstance =
                processService.findSubProcessInstance(processInstance.getId(), taskInstance.getId());
        if (subProcessInstance == null || taskInstance.getState().isFinished()) {
            return false;
        }
        subProcessInstance.setStateWithDesc(WorkflowExecutionStatus.READY_STOP, "ready stop by kill task");
        processInstanceDao.updateProcessInstance(subProcessInstance);
        sendToSubProcess();
        return true;
    }

    private void sendToSubProcess() {
        WorkflowStateEventChangeCommand workflowStateEventChangeCommand = new WorkflowStateEventChangeCommand(
                processInstance.getId(), taskInstance.getId(), subProcessInstance.getState(),
                subProcessInstance.getId(), 0);
        Host host = new Host(subProcessInstance.getHost());
        this.stateEventCallbackService.sendResult(host, workflowStateEventChangeCommand.convert2Command());
    }

    @Override
    public String getType() {
        return TASK_TYPE_SUB_PROCESS;
    }

}
