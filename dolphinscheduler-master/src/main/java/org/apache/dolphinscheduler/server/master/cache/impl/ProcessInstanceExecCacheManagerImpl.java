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

import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 * cache of process instance id and WorkflowExecuteThread
 */
@Component
public class ProcessInstanceExecCacheManagerImpl implements ProcessInstanceExecCacheManager {

    private final ConcurrentHashMap<Integer, WorkflowExecuteThread> processInstanceExecMaps = new ConcurrentHashMap<>();

    @Override
    public WorkflowExecuteThread getByProcessInstanceId(int processInstanceId) {
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
    public void cache(int processInstanceId, WorkflowExecuteThread workflowExecuteThread) {
        if (workflowExecuteThread == null) {
            return;
        }
        processInstanceExecMaps.put(processInstanceId, workflowExecuteThread);
    }

    @Override
    public Collection<WorkflowExecuteThread> getAll() {
        return ImmutableList.copyOf(processInstanceExecMaps.values());
    }
}