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

package org.apache.dolphinscheduler.server.master.runner;

import com.google.common.base.Strings;
import lombok.NonNull;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.remote.command.WorkflowStateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.utils.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used to execute {@link WorkflowExecuteRunnable}.
 */
@Component
public class WorkflowExecuteThreadPool extends ThreadPoolTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(WorkflowExecuteThreadPool.class);

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessService processService;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private StateEventCallbackService stateEventCallbackService;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    /**
     * multi-thread filter, avoid handling workflow at the same time
     */
    private ConcurrentHashMap<String, WorkflowExecuteRunnable> multiThreadFilterMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("WorkflowExecuteThread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    /**
     * submit state event
     */
    public void submitStateEvent(StateEvent stateEvent) {
        WorkflowExecuteRunnable workflowExecuteThread =
                processInstanceExecCacheManager.getByProcessInstanceId(stateEvent.getProcessInstanceId());
        if (workflowExecuteThread == null) {
            logger.warn("Submit state event error, cannot from workflowExecuteThread from cache manager, stateEvent:{}",
                    stateEvent);
            return;
        }
        workflowExecuteThread.addStateEvent(stateEvent);
        logger.info("Submit state event success, stateEvent: {}", stateEvent);
    }

    /**
     * Handle the events belong to the given workflow.
     */
    public void executeEvent(final WorkflowExecuteRunnable workflowExecuteThread) {
        if (!workflowExecuteThread.isStart() || workflowExecuteThread.eventSize() == 0) {
            return;
        }
        if (multiThreadFilterMap.containsKey(workflowExecuteThread.getKey())) {
            logger.warn("The workflow has been executed by another thread");
            return;
        }
        multiThreadFilterMap.put(workflowExecuteThread.getKey(), workflowExecuteThread);
        int processInstanceId = workflowExecuteThread.getProcessInstance().getId();
        ListenableFuture<?> future = this.submitListenable(workflowExecuteThread::handleEvents);
        future.addCallback(new ListenableFutureCallback() {

            @Override
            public void onFailure(Throwable ex) {
                LoggerUtils.setWorkflowInstanceIdMDC(processInstanceId);
                try {
                    logger.error("Workflow instance events handle failed", ex);
                    multiThreadFilterMap.remove(workflowExecuteThread.getKey());
                } finally {
                    LoggerUtils.removeWorkflowInstanceIdMDC();
                }
            }

            @Override
            public void onSuccess(Object result) {
                try {
                    LoggerUtils.setWorkflowInstanceIdMDC(workflowExecuteThread.getProcessInstance().getId());
                    if (workflowExecuteThread.workFlowFinish()) {
                        stateWheelExecuteThread
                                .removeProcess4TimeoutCheck(workflowExecuteThread.getProcessInstance().getId());
                        processInstanceExecCacheManager.removeByProcessInstanceId(processInstanceId);
                        notifyProcessChanged(workflowExecuteThread.getProcessInstance());
                        logger.info("Workflow instance is finished.");
                    }
                } catch (Exception e) {
                    logger.error("Workflow instance is finished, but notify changed error", e);
                } finally {
                    // make sure the process has been removed from multiThreadFilterMap
                    multiThreadFilterMap.remove(workflowExecuteThread.getKey());
                    LoggerUtils.removeWorkflowInstanceIdMDC();
                }
            }
        });
    }

    /**
     * notify process change
     */
    private void notifyProcessChanged(ProcessInstance finishProcessInstance) {
        if (Flag.NO == finishProcessInstance.getIsSubProcess()) {
            return;
        }
        Map<ProcessInstance, TaskInstance> fatherMaps = processService.notifyProcessList(finishProcessInstance.getId());
        for (Map.Entry<ProcessInstance, TaskInstance> entry : fatherMaps.entrySet()) {
            ProcessInstance processInstance = entry.getKey();
            TaskInstance taskInstance = entry.getValue();
            String address = NetUtils.getAddr(masterConfig.getListenPort());
            try {
                LoggerUtils.setWorkflowAndTaskInstanceIDMDC(processInstance.getId(), taskInstance.getId());
                if (processInstance.getHost().equalsIgnoreCase(address)) {
                    logger.info("Process host is local master, will notify it");
                    this.notifyMyself(processInstance, taskInstance);
                } else {
                    logger.info("Process host is remote master, will notify it");
                    this.notifyProcess(finishProcessInstance, processInstance, taskInstance);
                }
            } finally {
                LoggerUtils.removeWorkflowAndTaskInstanceIdMDC();
            }
        }
    }

    /**
     * notify myself
     */
    private void notifyMyself(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        if (!processInstanceExecCacheManager.contains(processInstance.getId())) {
            logger.warn("The execute cache manager doesn't contains this workflow instance");
            return;
        }
        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(processInstance.getId())
                .taskInstanceId(taskInstance.getId())
                .type(StateEventType.TASK_STATE_CHANGE)
                .status(TaskExecutionStatus.RUNNING_EXECUTION)
                .build();
        this.submitStateEvent(stateEvent);
    }

    /**
     * notify process's master
     */
    private void notifyProcess(ProcessInstance finishProcessInstance, ProcessInstance processInstance,
                               TaskInstance taskInstance) {
        String processInstanceHost = processInstance.getHost();
        if (Strings.isNullOrEmpty(processInstanceHost)) {
            logger.error("Process {} host is empty, cannot notify task {} now, taskId: {}", processInstance.getName(),
                    taskInstance.getName(), taskInstance.getId());
            return;
        }
        WorkflowStateEventChangeCommand workflowStateEventChangeCommand = new WorkflowStateEventChangeCommand(
                finishProcessInstance.getId(), 0, finishProcessInstance.getState(), processInstance.getId(),
                taskInstance.getId());
        Host host = new Host(processInstanceHost);
        stateEventCallbackService.sendResult(host, workflowStateEventChangeCommand.convert2Command());
    }
}
