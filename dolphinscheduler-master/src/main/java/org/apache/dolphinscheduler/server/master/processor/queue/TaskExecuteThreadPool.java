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

import org.apache.dolphinscheduler.common.enums.TaskEventType;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.cache.StreamTaskInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.TaskEventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
@Slf4j
public class TaskExecuteThreadPool extends ThreadPoolTaskExecutor {

    private final ConcurrentHashMap<String, TaskExecuteRunnable> multiThreadFilterMap = new ConcurrentHashMap<>();

    @Autowired
    private MasterConfig masterConfig;

    @Autowired
    private ProcessInstanceExecCacheManager processInstanceExecCacheManager;

    @Autowired
    private List<TaskEventHandler> taskEventHandlerList;

    @Autowired
    private StreamTaskInstanceExecCacheManager streamTaskInstanceExecCacheManager;

    private Map<TaskEventType, TaskEventHandler> taskEventHandlerMap = new HashMap<>();

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
        taskEventHandlerList.forEach(
                taskEventHandler -> taskEventHandlerMap.put(taskEventHandler.getHandleEventType(), taskEventHandler));
    }

    public void submitTaskEvent(TaskEvent taskEvent) {
        // stream task event handle
        if (taskEvent.getProcessInstanceId() == 0
                && streamTaskInstanceExecCacheManager.contains(taskEvent.getTaskInstanceId())) {
            streamTaskInstanceExecCacheManager.getByTaskInstanceId(taskEvent.getTaskInstanceId())
                    .addTaskEvent(taskEvent);
            return;
        }
        if (!processInstanceExecCacheManager.contains(taskEvent.getProcessInstanceId())) {
            log.warn("Cannot find workflowExecuteThread from cacheManager, event: {}", taskEvent);
            return;
        }
        TaskExecuteRunnable taskExecuteRunnable = taskExecuteThreadMap.computeIfAbsent(taskEvent.getProcessInstanceId(),
                (processInstanceId) -> new TaskExecuteRunnable(processInstanceId, taskEventHandlerMap));
        taskExecuteRunnable.addEvent(taskEvent);
    }

    public void eventHandler() {
        for (TaskExecuteRunnable taskExecuteThread : taskExecuteThreadMap.values()) {
            executeEvent(taskExecuteThread);
        }
    }

    public void executeEvent(TaskExecuteRunnable taskExecuteThread) {
        if (taskExecuteThread.isEmpty()) {
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
                log.error("[WorkflowInstance-{}] persist event failed", processInstanceId, ex);
                if (!processInstanceExecCacheManager.contains(processInstanceId)) {
                    taskExecuteThreadMap.remove(processInstanceId);
                    log.info(
                            "[WorkflowInstance-{}] Cannot find processInstance from cacheManager, remove process instance from threadMap",
                            processInstanceId);
                }
                multiThreadFilterMap.remove(taskExecuteThread.getKey());
            }

            @Override
            public void onSuccess(Object result) {
                Integer processInstanceId = taskExecuteThread.getProcessInstanceId();
                log.info("[WorkflowInstance-{}] persist events succeeded", processInstanceId);
                if (!processInstanceExecCacheManager.contains(processInstanceId)) {
                    taskExecuteThreadMap.remove(processInstanceId);
                    log.info(
                            "[WorkflowInstance-{}] Cannot find processInstance from cacheManager, remove process instance from threadMap",
                            processInstanceId);
                }
                multiThreadFilterMap.remove(taskExecuteThread.getKey());
            }
        });
    }
}
