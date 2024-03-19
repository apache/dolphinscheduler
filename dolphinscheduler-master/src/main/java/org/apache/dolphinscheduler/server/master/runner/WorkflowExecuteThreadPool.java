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

import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.StateEvent;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;
import org.apache.dolphinscheduler.server.master.workflow.WorkflowExecutionRunnable;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * Used to execute {@link WorkflowExecutionRunnable}.
 */
@Component
@Slf4j
public class WorkflowExecuteThreadPool extends ThreadPoolTaskExecutor {

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private IWorkflowExecuteRunnableRepository IWorkflowExecuteRunnableRepository;

    @Autowired
    private StateWheelExecuteThread stateWheelExecuteThread;

    /**
     * multi-thread filter, avoid handling workflow at the same time
     */
    private ConcurrentHashMap<Integer, WorkflowExecutionRunnable> multiThreadFilterMap =
            new ConcurrentHashMap<>();

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
        IWorkflowExecutionRunnable workflowExecuteThread =
                IWorkflowExecuteRunnableRepository.getByProcessInstanceId(stateEvent.getProcessInstanceId());
        if (workflowExecuteThread == null) {
            log.warn("Submit state event error, cannot from workflowExecuteThread from cache manager, stateEvent:{}",
                    stateEvent);
            return;
        }
        // workflowExecuteThread.addStateEvent(stateEvent);
        log.info("Submit state event success, stateEvent: {}", stateEvent);
    }

}
