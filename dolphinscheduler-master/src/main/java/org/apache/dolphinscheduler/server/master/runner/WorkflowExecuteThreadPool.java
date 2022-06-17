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

import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.google.common.base.Strings;

/**
 * Used to execute {@link WorkflowExecuteRunnable}, when
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
        this.setThreadNamePrefix("Workflow-Execute-Thread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    /**
     * submit state event
     */
    public void submitStateEvent(StateEvent stateEvent) {
        WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(stateEvent.getProcessInstanceId());
        if (workflowExecuteThread == null) {
            logger.warn("workflowExecuteThread is null, stateEvent:{}", stateEvent);
            return;
        }
        workflowExecuteThread.addStateEvent(stateEvent);
    }

    /**
     * Start the given workflow.
     */
    public void startWorkflow(WorkflowExecuteRunnable workflowExecuteThread) {
        ProcessInstanceMetrics.incProcessInstanceSubmit();
        submit(workflowExecuteThread);
    }

    /**
     * Handle the events belong to the given workflow.
     */
    public void executeEvent(final WorkflowExecuteRunnable workflowExecuteThread) {
        if (!workflowExecuteThread.isStart() || workflowExecuteThread.eventSize() == 0) {
            return;
        }
        if (multiThreadFilterMap.containsKey(workflowExecuteThread.getKey())) {
            logger.warn("The workflow:{} has been executed by another thread", workflowExecuteThread.getKey());
            return;
        }
        multiThreadFilterMap.put(workflowExecuteThread.getKey(), workflowExecuteThread);
        int processInstanceId = workflowExecuteThread.getProcessInstance().getId();
        ListenableFuture<?> future = this.submitListenable(workflowExecuteThread::handleEvents);
        future.addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                logger.error("handle events {} failed", processInstanceId, ex);
                multiThreadFilterMap.remove(workflowExecuteThread.getKey());
            }

            @Override
            public void onSuccess(Object result) {
                try {
                    if (workflowExecuteThread.workFlowFinish()) {
                        stateWheelExecuteThread.removeProcess4TimeoutCheck(workflowExecuteThread.getProcessInstance());
                        processInstanceExecCacheManager.removeByProcessInstanceId(processInstanceId);
                        notifyProcessChanged(workflowExecuteThread.getProcessInstance());
                        logger.info("process instance {} finished.", processInstanceId);
                    }
                } catch (Exception e) {
                    logger.error("handle events {} success, but notify changed error", processInstanceId, e);
                } finally {
                    // make sure the process has been removed from multiThreadFilterMap
                    multiThreadFilterMap.remove(workflowExecuteThread.getKey());
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
            if (processInstance.getHost().equalsIgnoreCase(address)) {
                this.notifyMyself(processInstance, taskInstance);
            } else {
                this.notifyProcess(finishProcessInstance, processInstance, taskInstance);
            }
        }
    }

    /**
     * notify myself
     */
    private void notifyMyself(ProcessInstance processInstance, TaskInstance taskInstance) {
        logger.info("notify process {} task {} state change", processInstance.getId(), taskInstance.getId());
        if (!processInstanceExecCacheManager.contains(processInstance.getId())) {
            return;
        }
        StateEvent stateEvent = new StateEvent();
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(processInstance.getId());
        stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
        this.submitStateEvent(stateEvent);
    }

    /**
     * notify process's master
     */
    private void notifyProcess(ProcessInstance finishProcessInstance, ProcessInstance processInstance, TaskInstance taskInstance) {
        String host = processInstance.getHost();
        if (Strings.isNullOrEmpty(host)) {
            logger.error("process {} host is empty, cannot notify task {} now", processInstance.getId(), taskInstance.getId());
            return;
        }
        String address = host.split(":")[0];
        int port = Integer.parseInt(host.split(":")[1]);
        StateEventChangeCommand stateEventChangeCommand = new StateEventChangeCommand(
                finishProcessInstance.getId(), 0, finishProcessInstance.getState(), processInstance.getId(), taskInstance.getId()
        );
        stateEventCallbackService.sendResult(address, port, stateEventChangeCommand.convert2Command());
    }
}
