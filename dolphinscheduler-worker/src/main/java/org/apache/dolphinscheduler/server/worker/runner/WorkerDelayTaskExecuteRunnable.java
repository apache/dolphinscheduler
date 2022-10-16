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

import lombok.NonNull;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.storage.StorageOperate;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;

import javax.annotation.Nullable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public abstract class WorkerDelayTaskExecuteRunnable extends WorkerTaskExecuteRunnable implements Delayed {

    protected WorkerDelayTaskExecuteRunnable(@NonNull TaskExecutionContext taskExecutionContext,
                                             @NonNull WorkerConfig workerConfig,
                                             @NonNull String masterAddress,
                                             @NonNull WorkerMessageSender workerMessageSender,
                                             @NonNull AlertClientService alertClientService,
                                             @NonNull TaskPluginManager taskPluginManager,
                                             @Nullable StorageOperate storageOperate) {
        super(taskExecutionContext, workerConfig, masterAddress, workerMessageSender, alertClientService, taskPluginManager, storageOperate);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        TaskExecutionContext taskExecutionContext = getTaskExecutionContext();
        return unit.convert(
                DateUtils.getRemainTime(
                        DateUtils.timeStampToDate(taskExecutionContext.getFirstSubmitTime()),
                    taskExecutionContext.getDelayTime() * 60L), TimeUnit.SECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        if (o == null) {
            return 1;
        }
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

}
