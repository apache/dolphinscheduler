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

import org.apache.dolphinscheduler.server.master.cache.StreamTaskInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.StreamTaskExecuteRunnable;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import lombok.NonNull;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

/**
 * cache of process instance id and WorkflowExecuteThread
 */
@Component
public class StreamTaskInstanceExecCacheManagerImpl implements StreamTaskInstanceExecCacheManager {

    private final ConcurrentHashMap<Integer, StreamTaskExecuteRunnable> streamTaskInstanceExecMaps =
            new ConcurrentHashMap<>();

    @PostConstruct
    public void registerMetrics() {
        TaskMetrics.registerTaskPrepared(streamTaskInstanceExecMaps::size);
    }

    @Override
    public StreamTaskExecuteRunnable getByTaskInstanceId(int taskInstanceId) {
        return streamTaskInstanceExecMaps.get(taskInstanceId);
    }

    @Override
    public boolean contains(int taskInstanceId) {
        return streamTaskInstanceExecMaps.containsKey(taskInstanceId);
    }

    @Override
    public void removeByTaskInstanceId(int taskInstanceId) {
        streamTaskInstanceExecMaps.remove(taskInstanceId);
    }

    @Override
    public void cache(int taskInstanceId, @NonNull StreamTaskExecuteRunnable streamTaskExecuteRunnable) {
        streamTaskInstanceExecMaps.put(taskInstanceId, streamTaskExecuteRunnable);
    }

    @Override
    public Collection<StreamTaskExecuteRunnable> getAll() {
        return ImmutableList.copyOf(streamTaskInstanceExecMaps.values());
    }
}
