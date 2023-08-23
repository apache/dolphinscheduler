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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.thread.BaseDaemonThread;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.extract.master.transportor.ITaskInstanceExecutionEvent;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalTaskInstanceDispatchQueueLooper extends BaseDaemonThread {

    @Autowired
    private GlobalTaskInstanceDispatchQueue globalTaskInstanceDispatchQueue;

    @Autowired
    private WorkerConfig workerConfig;

    @Autowired
    private WorkerMessageSender workerMessageSender;

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Autowired
    private WorkerManagerThread workerManager;

    @Autowired(required = false)
    private StorageOperate storageOperate;

    @Autowired
    private WorkerRegistryClient workerRegistryClient;

    protected GlobalTaskInstanceDispatchQueueLooper() {
        super("GlobalTaskDispatchQueueLooper");
    }

    public synchronized void start() {
        log.info("GlobalTaskDispatchQueueLooper starting");
        super.start();
        log.info("GlobalTaskDispatchQueueLooper started");
    }

    public void run() {
        while (true) {
            try {
                TaskExecutionContext taskExecutionContext = globalTaskInstanceDispatchQueue.take();
                LogUtils.setTaskInstanceLogFullPathMDC(taskExecutionContext.getLogPath());
                LogUtils.setTaskInstanceIdMDC(taskExecutionContext.getTaskInstanceId());
                // delay task process
                long remainTime =
                        DateUtils.getRemainTime(DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                                taskExecutionContext.getDelayTime() * 60L);
                if (remainTime > 0) {
                    log.info("Current taskInstance is choose delay execution, delay time: {}s", remainTime);
                    taskExecutionContext.setCurrentExecutionStatus(TaskExecutionStatus.DELAY_EXECUTION);
                    // todo: use delay running event
                    workerMessageSender.sendMessage(taskExecutionContext,
                            ITaskInstanceExecutionEvent.TaskInstanceExecutionEventType.FINISH);
                }
                WorkerDelayTaskExecuteRunnable workerTaskExecuteRunnable = WorkerTaskExecuteRunnableFactoryBuilder
                        .createWorkerDelayTaskExecuteRunnableFactory(
                                taskExecutionContext,
                                workerConfig,
                                workerMessageSender,
                                taskPluginManager,
                                storageOperate,
                                workerRegistryClient)
                        .createWorkerTaskExecuteRunnable();
                if (workerManager.offer(workerTaskExecuteRunnable)) {
                    log.info("Success submit WorkerDelayTaskExecuteRunnable to WorkerManagerThread's waiting queue");
                }
            } catch (InterruptedException e) {
                log.error("GlobalTaskDispatchQueueLooper interrupted");
                break;
            } catch (Exception ex) {
                log.error("GlobalTaskDispatchQueueLooper error", ex);
            } finally {
                LogUtils.removeTaskInstanceIdMDC();
                LogUtils.removeTaskInstanceLogFullPathMDC();
            }
        }
    }

}
