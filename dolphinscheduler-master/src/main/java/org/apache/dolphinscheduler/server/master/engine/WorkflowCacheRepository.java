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

package org.apache.dolphinscheduler.server.master.engine;

import org.apache.dolphinscheduler.server.master.engine.workflow.execution.IWorkflowExecution;
import org.apache.dolphinscheduler.server.master.metrics.WorkflowInstanceMetrics;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.NonNull;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class WorkflowCacheRepository implements IWorkflowRepository {

    private final Map<Integer, IWorkflowExecution> workflowExecutionMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void registerMetrics() {
        WorkflowInstanceMetrics.registerWorkflowInstanceRunningGauge(workflowExecutionMap::size);
    }

    @Override
    public IWorkflowExecution get(final int workflowInstanceId) {
        return workflowExecutionMap.get(workflowInstanceId);
    }

    @Override
    public boolean contains(final int workflowInstanceId) {
        return workflowExecutionMap.containsKey(workflowInstanceId);
    }

    @Override
    public void remove(final int workflowInstanceId) {
        workflowExecutionMap.remove(workflowInstanceId);
    }

    @Override
    public void put(@NonNull final IWorkflowExecution workflowExecution) {
        final Integer workflowInstanceId = workflowExecution.getId();
        workflowExecutionMap.put(workflowInstanceId, workflowExecution);
    }

    @Override
    public Collection<IWorkflowExecution> getAll() {
        return ImmutableList.copyOf(workflowExecutionMap.values());
    }

}
