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

import org.apache.dolphinscheduler.server.master.runner.task.dependent.DependentLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.dynamic.DynamicLogicTask;
import org.apache.dolphinscheduler.server.master.runner.task.subworkflow.SubWorkflowLogicTask;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

@Component
public class MasterTaskExecuteRunnableFactoryBuilder {

    @Autowired
    private AsyncMasterDelayTaskExecuteRunnableFactory asyncMasterDelayTaskExecuteRunnableFactory;

    @Autowired
    private SyncMasterDelayTaskExecuteRunnableFactory syncMasterDelayTaskExecuteRunnableFactory;

    private static final Set<String> ASYNC_TASK_TYPE = Sets.newHashSet(
            DependentLogicTask.TASK_TYPE,
            SubWorkflowLogicTask.TASK_TYPE,
            DynamicLogicTask.TASK_TYPE);

    public MasterDelayTaskExecuteRunnableFactory<? extends MasterDelayTaskExecuteRunnable> createWorkerDelayTaskExecuteRunnableFactory(String taskType) {
        if (ASYNC_TASK_TYPE.contains(taskType)) {
            return asyncMasterDelayTaskExecuteRunnableFactory;
        }
        return syncMasterDelayTaskExecuteRunnableFactory;
    }
}
