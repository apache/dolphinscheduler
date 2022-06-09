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

package org.apache.dolphinscheduler.server.master.processor.queue;

import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.utils.DataQualityResultOperator;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class TaskExecuteThreadPool extends ThreadPoolTaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecuteThreadPool.class);

    private final ConcurrentHashMap<String, TaskExecuteRunnable> multiThreadFilterMap = new ConcurrentHashMap<>();

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    /**
     * data quality result operator
     */
    @Autowired
    private DataQualityResultOperator dataQualityResultOperator;


    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    /**
     * task event thread map
     */
    private final ConcurrentHashMap<Integer, TaskExecuteRunnable> taskExecuteThreadMap = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        this.setDaemon(true);
        this.setThreadNamePrefix("Task-Execute-Thread-");
        this.setMaxPoolSize(masterConfig.getExecThreads());
        this.setCorePoolSize(masterConfig.getExecThreads());
    }

    public void submitTaskEvent(TaskEvent taskEvent) {
        if (!processInstanceExecCacheManager.contains(taskEvent.getProcessInstanceId())) {
            logger.warn("Cannot find workflowExecuteThread from cacheManager, event: {}", taskEvent);
            return;
        }
        if (!taskExecuteThreadMap.containsKey(taskEvent.getProcessInstanceId())) {
            TaskExecuteRunnable taskExecuteThread = new TaskExecuteRunnable(
                    taskEvent.getProcessInstanceId(),
                    processService, workflowExecuteThreadPool,
                    processInstanceExecCacheManager,
                    dataQualityResultOperator);
            taskExecuteThreadMap.put(taskEvent.getProcessInstanceId(), taskExecuteThread);
        }
        TaskExecuteRunnable taskExecuteRunnable= taskExecuteThreadMap.get(taskEvent.getProcessInstanceId());
        if (taskExecuteRunnable != null) {
            taskExecuteRunnable.addEvent(taskEvent);
        }
    }

    public void eventHandler() {
        for (TaskExecuteRunnable taskExecuteThread : taskExecuteThreadMap.values()) {
            executeEvent(taskExecuteThread);
        }
    }

    public void executeEvent(TaskExecuteRunnable taskExecuteThread) {
        if (taskExecuteThread.eventSize() == 0) {
            return;
        }
        if (multiThreadFilterMap.containsKey(taskExecuteThread.getKey())) {
            return;
        }
        multiThreadFilterMap.put(taskExecuteThread.getKey(), taskExecuteThread);
        ListenableFuture future = this.submitListenable(taskExecuteThread::run);
        future.addCallback(new ListenableFutureCallback() {
            @Override
            public void onFailure(Throwable ex) {
                Integer processInstanceId = taskExecuteThread.getProcessInstanceId();
                logger.error("persist event failed processInstanceId: {}", processInstanceId, ex);
                if (!processInstanceExecCacheManager.contains(processInstanceId)) {
                    taskExecuteThreadMap.remove(processInstanceId);
                    logger.info("Cannot find processInstance from cacheManager, remove process instance from threadMap: {}",
                            processInstanceId);
                }
                multiThreadFilterMap.remove(taskExecuteThread.getKey());
            }

            @Override
            public void onSuccess(Object result) {
                Integer processInstanceId = taskExecuteThread.getProcessInstanceId();
                logger.info("persist events succeeded, processInstanceId: {}", processInstanceId);
                if (!processInstanceExecCacheManager.contains(processInstanceId)) {
                    taskExecuteThreadMap.remove(processInstanceId);
                    logger.info("Cannot find processInstance from cacheManager, remove process instance from threadMap: {}",
                            processInstanceId);
                }
                multiThreadFilterMap.remove(taskExecuteThread.getKey());
            }
        });
    }
}
