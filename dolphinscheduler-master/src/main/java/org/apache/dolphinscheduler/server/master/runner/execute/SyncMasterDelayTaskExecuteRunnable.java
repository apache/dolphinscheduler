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

package org.apache.dolphinscheduler.server.master.runner.execute;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.log.TaskInstanceLogHeader;
import org.apache.dolphinscheduler.server.master.exception.MasterTaskExecuteException;
import org.apache.dolphinscheduler.server.master.runner.message.LogicTaskInstanceExecutionEventSenderManager;
import org.apache.dolphinscheduler.server.master.runner.task.ISyncLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.LogicTaskPluginFactoryBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SyncMasterDelayTaskExecuteRunnable extends MasterDelayTaskExecuteRunnable {

    public SyncMasterDelayTaskExecuteRunnable(TaskExecutionContext taskExecutionContext,
                                              LogicTaskPluginFactoryBuilder logicTaskPluginFactoryBuilder,
                                              LogicTaskInstanceExecutionEventSenderManager logicTaskInstanceExecutionEventSenderManager) {
        super(taskExecutionContext, logicTaskPluginFactoryBuilder, logicTaskInstanceExecutionEventSenderManager);
    }

    @Override
    protected void executeTask() throws MasterTaskExecuteException {
        if (logicTask == null) {
            throw new MasterTaskExecuteException("The task plugin instance is null");
        }
        ISyncLogicTask iSyncLogicTask = (ISyncLogicTask) logicTask;
        iSyncLogicTask.handle();
    }

    protected void afterExecute() throws MasterTaskExecuteException {
        TaskInstanceLogHeader.printFinalizeTaskHeader();
        // todo: move this to constructor to avoid check every time
        if (logicTask == null) {
            throw new MasterTaskExecuteException("The current task instance is null");
        }
        sendTaskResult();
        log.info(
                "Execute task finished, will send the task execute result to master, the current task execute result is {}",
                taskExecutionContext.getCurrentExecutionStatus().name());
        closeLogAppender();
        MasterTaskExecutionContextHolder.removeTaskExecutionContext(taskExecutionContext.getTaskInstanceId());
        MasterTaskExecuteRunnableHolder.removeMasterTaskExecuteRunnable(taskExecutionContext.getTaskInstanceId());
    }

}
