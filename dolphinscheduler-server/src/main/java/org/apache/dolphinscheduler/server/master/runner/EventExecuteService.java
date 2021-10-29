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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.thread.Stopper;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.remote.command.StateEventChangeCommand;
import org.apache.dolphinscheduler.remote.processor.StateEventCallbackService;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@Service
public class EventExecuteService extends Thread {

    private static final Logger logger = LoggerFactory.getLogger(EventExecuteService.class);


    /**
     * dolphinscheduler database interface
     */
    @Autowired
    private ProcessService processService;

    @Autowired
    private MasterConfig masterConfig;

    private ExecutorService eventExecService;

    /**
     *
     */
    private StateEventCallbackService stateEventCallbackService;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    private ConcurrentHashMap<String, WorkflowExecuteThread> eventHandlerMap = new ConcurrentHashMap();
    ListeningExecutorService listeningExecutorService;

    public void init() {

        eventExecService = ThreadUtils.newDaemonFixedThreadExecutor("MasterEventExecution", masterConfig.getMasterExecThreads());

        listeningExecutorService = MoreExecutors.listeningDecorator(eventExecService);
        this.stateEventCallbackService = SpringApplicationContext.getBean(StateEventCallbackService.class);

    }

    @Override
    public synchronized void start() {
        super.setName("EventServiceStarted");
        super.start();
    }

    public void close() {
        eventExecService.shutdown();
        logger.info("event service stopped...");
    }

    @Override
    public void run() {
        logger.info("Event service started");
        while (Stopper.isRunning()) {
            try {
                eventHandler();

                TimeUnit.MILLISECONDS.sleep(Constants.SLEEP_TIME_MILLIS);

            } catch (Exception e) {
                logger.error("Event service thread error", e);
            }
        }
    }

    private void eventHandler() {
        for (WorkflowExecuteThread workflowExecuteThread : this.processInstanceExecCacheManager.getAll()) {
            if (workflowExecuteThread.eventSize() == 0
                    || StringUtils.isEmpty(workflowExecuteThread.getKey())
                    || eventHandlerMap.containsKey(workflowExecuteThread.getKey())) {
                continue;
            }
            int processInstanceId = workflowExecuteThread.getProcessInstance().getId();
            logger.info("handle process instance : {} , events count:{}",
                    processInstanceId,
                    workflowExecuteThread.eventSize());
            logger.info("already exists handler process size:{}", this.eventHandlerMap.size());
            eventHandlerMap.put(workflowExecuteThread.getKey(), workflowExecuteThread);
            ListenableFuture future = this.listeningExecutorService.submit(workflowExecuteThread);
            FutureCallback futureCallback = new FutureCallback() {
                @Override
                public void onSuccess(Object o) {
                    if (workflowExecuteThread.workFlowFinish()) {
                        processInstanceExecCacheManager.removeByProcessInstanceId(processInstanceId);
                        notifyProcessChanged();
                        logger.info("process instance {} finished.", processInstanceId);
                    }
                    if (workflowExecuteThread.getProcessInstance().getId() != processInstanceId) {
                        processInstanceExecCacheManager.removeByProcessInstanceId(processInstanceId);
                        processInstanceExecCacheManager.cache(workflowExecuteThread.getProcessInstance().getId(), workflowExecuteThread);

                    }
                    eventHandlerMap.remove(workflowExecuteThread.getKey());
                }

                private void notifyProcessChanged() {
                    Map<ProcessInstance, TaskInstance> fatherMaps
                            = processService.notifyProcessList(processInstanceId, 0);

                    for (ProcessInstance processInstance : fatherMaps.keySet()) {
                        String address = NetUtils.getAddr(masterConfig.getListenPort());
                        if (processInstance.getHost().equalsIgnoreCase(address)) {
                            notifyMyself(processInstance, fatherMaps.get(processInstance));
                        } else {
                            notifyProcess(processInstance, fatherMaps.get(processInstance));
                        }
                    }
                }

                private void notifyMyself(ProcessInstance processInstance, TaskInstance taskInstance) {
                    logger.info("notify process {} task {} state change", processInstance.getId(), taskInstance.getId());
                    if (!processInstanceExecCacheManager.contains(processInstance.getId())) {
                        return;
                    }
                    WorkflowExecuteThread workflowExecuteThreadNotify = processInstanceExecCacheManager.getByProcessInstanceId(processInstance.getId());
                    StateEvent stateEvent = new StateEvent();
                    stateEvent.setTaskInstanceId(taskInstance.getId());
                    stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
                    stateEvent.setProcessInstanceId(processInstance.getId());
                    stateEvent.setExecutionStatus(ExecutionStatus.RUNNING_EXECUTION);
                    workflowExecuteThreadNotify.addStateEvent(stateEvent);
                }

                private void notifyProcess(ProcessInstance processInstance, TaskInstance taskInstance) {
                    String host = processInstance.getHost();
                    if (StringUtils.isEmpty(host)) {
                        logger.info("process {} host is empty, cannot notify task {} now.",
                                processInstance.getId(), taskInstance.getId());
                        return;
                    }
                    String address = host.split(":")[0];
                    int port = Integer.parseInt(host.split(":")[1]);
                    logger.info("notify process {} task {} state change, host:{}",
                            processInstance.getId(), taskInstance.getId(), host);
                    StateEventChangeCommand stateEventChangeCommand = new StateEventChangeCommand(
                            processInstanceId, 0, workflowExecuteThread.getProcessInstance().getState(), processInstance.getId(), taskInstance.getId()
                    );

                    stateEventCallbackService.sendResult(address, port, stateEventChangeCommand.convert2Command());
                }

                @Override
                public void onFailure(Throwable throwable) {
                }
            };
            Futures.addCallback(future, futureCallback, this.listeningExecutorService);
        }
    }
}
