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

import org.apache.dolphinscheduler.server.master.runner.message.LogicTaskInstanceExecutionEventSenderManager;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MasterTaskExecutorThreadPoolManager {

    @Autowired
    private MasterSyncTaskExecutorThreadPool masterSyncTaskExecutorThreadPool;

    @Autowired
    private MasterAsyncTaskExecutorThreadPool masterAsyncTaskExecutorThreadPool;

    @Autowired
    private LogicTaskInstanceExecutionEventSenderManager logicTaskInstanceExecutionEventSenderManager;

    public boolean submitMasterTaskExecutor(final MasterTaskExecutor masterTaskExecutor) {
        MasterTaskExecutorHolder.putMasterTaskExecuteRunnable(masterTaskExecutor);
        sendDispatchedEvent(masterTaskExecutor);
        if (masterTaskExecutor instanceof SyncMasterTaskExecutor) {
            return masterSyncTaskExecutorThreadPool
                    .submitMasterTaskExecutor((SyncMasterTaskExecutor) masterTaskExecutor);
        }
        if (masterTaskExecutor instanceof AsyncMasterTaskExecutor) {
            return masterAsyncTaskExecutorThreadPool
                    .submitMasterTaskExecutor((AsyncMasterTaskExecutor) masterTaskExecutor);
        }
        throw new IllegalArgumentException("Unknown type of MasterTaskExecutor: " + masterTaskExecutor);
    }

    public boolean removeMasterTaskExecutor(final MasterTaskExecutor masterTaskExecutor) {
        if (masterTaskExecutor instanceof SyncMasterTaskExecutor) {
            return masterSyncTaskExecutorThreadPool
                    .removeMasterTaskExecutor((SyncMasterTaskExecutor) masterTaskExecutor);
        }
        if (masterTaskExecutor instanceof AsyncMasterTaskExecutor) {
            return masterAsyncTaskExecutorThreadPool
                    .removeMasterTaskExecutor((AsyncMasterTaskExecutor) masterTaskExecutor);
        }
        throw new IllegalArgumentException("Unknown type of MasterTaskExecutor: " + masterTaskExecutor);
    }

    private void sendDispatchedEvent(final MasterTaskExecutor masterTaskExecutor) {
        logicTaskInstanceExecutionEventSenderManager.dispatchEventSender()
                .sendMessage(masterTaskExecutor.getTaskExecutionContext());
    }

}
