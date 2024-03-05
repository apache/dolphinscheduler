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

package org.apache.dolphinscheduler.server.master.dag;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkflowExecuteRunnableRepository {

    private final Map<Integer, IWorkflowExecutionRunnable> workflowExecutionRunnableMap = new ConcurrentHashMap<>();

    public void storeWorkflowExecutionRunnable(IWorkflowExecutionRunnable workflowExecutionRunnable) {
        workflowExecutionRunnableMap.put(
                workflowExecutionRunnable.getWorkflowExecutionContext().getWorkflowInstanceId(),
                workflowExecutionRunnable);
    }

    public IWorkflowExecutionRunnable getWorkflowExecutionRunnableById(Integer workflowInstanceId) {
        return workflowExecutionRunnableMap.get(workflowInstanceId);
    }

    public Collection<IWorkflowExecutionRunnable> getActiveWorkflowExecutionRunnable() {
        return workflowExecutionRunnableMap.values();
    }

    public void removeWorkflowExecutionRunnable(Integer workflowInstanceId) {
        workflowExecutionRunnableMap.remove(workflowInstanceId);
    }

}
