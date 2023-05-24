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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class MasterTaskExecuteRunnableHolder {

    private static final Map<Integer, MasterTaskExecuteRunnable> SUBMITTED_MASTER_TASK_MAP = new ConcurrentHashMap<>();

    public void putMasterTaskExecuteRunnable(MasterTaskExecuteRunnable masterTaskExecuteRunnable) {
        SUBMITTED_MASTER_TASK_MAP.put(masterTaskExecuteRunnable.getTaskExecutionContext().getTaskInstanceId(),
                masterTaskExecuteRunnable);
    }

    public MasterTaskExecuteRunnable getMasterTaskExecuteRunnable(Integer taskInstanceId) {
        return SUBMITTED_MASTER_TASK_MAP.get(taskInstanceId);
    }

    public void removeMasterTaskExecuteRunnable(Integer taskInstanceId) {
        SUBMITTED_MASTER_TASK_MAP.remove(taskInstanceId);
    }

}
