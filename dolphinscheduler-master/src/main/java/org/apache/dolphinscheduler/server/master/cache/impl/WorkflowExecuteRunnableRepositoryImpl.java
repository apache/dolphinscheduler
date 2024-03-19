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

package org.apache.dolphinscheduler.server.master.cache.impl;

import org.apache.dolphinscheduler.server.master.cache.IWorkflowExecuteRunnableRepository;
import org.apache.dolphinscheduler.server.master.metrics.ProcessInstanceMetrics;
import org.apache.dolphinscheduler.server.master.workflow.IWorkflowExecutionRunnable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.NonNull;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class WorkflowExecuteRunnableRepositoryImpl
        implements
            IWorkflowExecuteRunnableRepository<IWorkflowExecutionRunnable> {

    private final Map<Integer, IWorkflowExecutionRunnable> processInstanceExecMaps = new ConcurrentHashMap<>();

    public WorkflowExecuteRunnableRepositoryImpl() {
        ProcessInstanceMetrics.registerProcessInstanceRunningGauge(processInstanceExecMaps::size);
    }

    @Override
    public IWorkflowExecutionRunnable getByProcessInstanceId(int processInstanceId) {
        return processInstanceExecMaps.get(processInstanceId);
    }

    @Override
    public boolean contains(int processInstanceId) {
        return processInstanceExecMaps.containsKey(processInstanceId);
    }

    @Override
    public void removeByProcessInstanceId(int processInstanceId) {
        processInstanceExecMaps.remove(processInstanceId);
    }

    @Override
    public void cache(int processInstanceId, @NonNull IWorkflowExecutionRunnable workflowExecuteThread) {
        processInstanceExecMaps.put(processInstanceId, workflowExecuteThread);
    }

    @Override
    public Collection<IWorkflowExecutionRunnable> getAll() {
        return ImmutableList.copyOf(processInstanceExecMaps.values());
    }

    @Override
    public void clearCache() {
        processInstanceExecMaps.clear();
    }
}
